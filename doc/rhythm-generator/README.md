# Rhythmus-Generator

Der Rhythmus-Generator erzeugt und bewertet alle binären Rhythmen eines
4/4-Takts im 16tel-Raster und speichert sie in PostgreSQL. „Huffman“ bezeichnet
hier das projektinterne Informationsmaß; der Generator baut keinen klassischen
Huffman-Baum und komprimiert keine Audiodaten.

Diese Dokumentation beschreibt den aktuellen Stand. Insbesondere sind
Informationsprofile pro Beat, eine Suche nach Profilformen und eine
reproduzierbare Zufallsauswahl noch nicht implementiert.

## Ablauf

Der öffentliche Einstieg ist:

```bash
syrincs init
syrincs calculate rhythms
```

`init` legt die Tabelle `huffmanRhythms` an oder ergänzt ein älteres Schema.
`calculate rhythms` führt anschließend diese Pipeline aus:

```text
16-Bit-Maske
    │
    ▼
Onset-String aus x und o
    │
    ▼
HuffmanRhythm: Information je Beat berechnen
    │
    ├── Summe der Beat-Information
    └── Populationsstandardabweichung
    │
    ▼
RhythmRepository
    │
    ▼
PostgreSQL: huffmanRhythms
```

Die CLI delegiert über den `UseCaseInteractor` an
[`GenerateAndPersistRhythmUseCase`](../../src/main/java/syrincs/b_application/GenerateAndPersistRhythmUseCase.java).
Der Use Case kennt nur den Application-Port
[`RhythmRepository`](../../src/main/java/syrincs/b_application/ports/RhythmRepository.java).
Die technische Batch-Persistenz übernimmt
[`PostgresRhythmRepository`](../../src/main/java/syrincs/c_adapters/postgres/PostgresRhythmRepository.java).
Damit bleiben JDBC und PostgreSQL außerhalb der Domäne und der
Anwendungsschicht.

## Erzeugung des vollständigen Suchraums

Ein 4/4-Takt enthält bei vier Positionen pro Beat genau 16 Positionen. Jede
Position hat zwei mögliche Werte:

- `x`: An dieser Position beginnt ein Einsatz.
- `o`: An dieser Position beginnt kein neuer Einsatz.

Der vollständige Suchraum enthält deshalb:

```text
2^16 = 65.536 Rhythmen
```

`GenerateAndPersistRhythmUseCase` zählt die Ganzzahlen von `0` bis `65.535`
hoch und liest jede Zahl als 16-Bit-Maske. Das höchstwertige Bit wird zur
ersten Position im Takt:

| Maske | Onset-String |
| ---: | --- |
| `0` | `oooooooooooooooo` |
| `1` | `ooooooooooooooox` |
| `32768` | `xooooooooooooooo` |
| `65535` | `xxxxxxxxxxxxxxxx` |

Aus jeder Maske entsteht ein `HuffmanRhythm` mit 4/4-Takt und dem
Anwendungstempo aus `AppDefaults`, derzeit 120 BPM. Alle Objekte werden
zunächst im Speicher aufgebaut und danach gemeinsam an das Repository
übergeben.

Neben der vollständigen Erzeugung besitzt der Use Case einen für Tests und
gezielte Aufrufer geeigneten Overload mit `List<String>`. Diese Eingaben
verwenden `0` und `1`, müssen genau 16 Zeichen lang sein und werden nach `o`
beziehungsweise `x` übersetzt. `null`-Einträge und Einträge mit falscher Länge
werden derzeit übersprungen; andere Zeichen führen zu einer
`IllegalArgumentException`. Dieser Overload ist nicht als eigener CLI-Befehl
veröffentlicht.

## Rhythmusmodell und Eingabeformat

[`Rhythm`](../../src/main/java/syrincs/a_domain/rhythm/Rhythm.java) normalisiert
den Onset-String:

- Whitespace wird entfernt.
- Großbuchstaben werden in Kleinbuchstaben umgewandelt.
- Erlaubt sind ausschließlich `x` und `o`.
- Die Länge muss positiv und ein Vielfaches von 16 sein.
- Der String wird in Gruppen zu je vier Positionen, also Beats, zerlegt.

Das Onset-Format ist nicht mit RDL-0 zu verwechseln. RDL verwendet `x` für
einen Hit und `-` für eine Pause; der Generator verwendet ausschließlich
`x`/`o`.

Obwohl `Rhythm` mehrere vollständige Takte akzeptiert, erzeugt
`GenerateAndPersistRhythmUseCase` ausschließlich genau einen 16 Positionen
langen Takt.

## Projektinternes Informationsmaß

[`HuffmanRhythm`](../../src/main/java/syrincs/a_domain/rhythm/HuffmanRhythm.java)
bewertet jeden Beat mit einem kleinen Zustandsautomaten. Dessen Zustand
kombiniert:

- ob am Beat-Anfang gespielt wird (`Idle` oder `Playing`);
- die aktuelle zeitliche Unterteilung (`Quarter`, `Eighth` oder
  `Sixteenth`).

Beim Lesen der vier Positionen eines Beats erzeugt der Automat abstrakte
Codesymbole für:

| Code | Bedeutung im Modell |
| --- | --- |
| `00` | Unterteilung einmal zusammenführen |
| `01` | Unterteilung einmal aufspalten |
| `10` | Pause einsetzen |
| `11` | Note einsetzen |

Die Information eines Beats ist die Anzahl der erzeugten Codesymbole, nicht
die Anzahl seiner `x`-Zeichen. Der Spielzustand wird von einem Beat zum
nächsten weitergeführt. Daher kann dasselbe Viererpattern abhängig vom
vorherigen Beat unterschiedlich bewertet werden.

Beispiel:

```text
Onsets:             xooo xoxo xooo xoxo
Information/Beat:      1    1    0    1
Summe:                              3
Populationsstandardabweichung:      0,4330127018922193
```

`HuffmanRhythm` veröffentlicht aktuell nur:

- `getInformation()`: Summe der Informationswerte aller Beats;
- `getStandardDeviation()`: Populationsstandardabweichung der Beat-Werte.

### Berechnung und Bedeutung der Standardabweichung

Die Standardabweichung wird aus den **Informationswerten der einzelnen
Beats** gebildet. Für einen 4/4-Takt sind das vier Werte, für zwei 4/4-Takte
acht Werte. Sie wird nicht unmittelbar aus den 16 Onset-Zeichen, aus der
Anzahl der `x` oder aus der aufsummierten Gesamtinformation berechnet.

Seien `b₁, b₂, …, bₙ` die vom Zustandsautomaten berechneten
Beat-Informationswerte. Zuerst wird ihr arithmetischer Mittelwert bestimmt:

```text
Mittelwert = (b₁ + b₂ + ... + bₙ) / N
```

Danach berechnet `StandardDeviation.calc(...)` die quadratischen Abstände
jedes Beat-Werts von diesem Mittelwert. Die Varianz ist deren Mittelwert; die
Standardabweichung ist die Quadratwurzel daraus:

```text
Varianz             = ((b₁ - Mittelwert)² + ... + (bₙ - Mittelwert)²) / N
Standardabweichung  = √Varianz
```

Es handelt sich ausdrücklich um die **Populationsstandardabweichung**: Der
Divisor ist `N`, nicht `N - 1`. Alle Beats des vorliegenden Rhythmus gelten
als die vollständig zu beschreibende Population und nicht als Stichprobe
einer größeren Menge.

Für das obige Beispiel lautet die vollständige Rechnung:

```text
Beat-Werte:                  [1, 1, 0, 1]
N:                           4
Mittelwert:                  (1 + 1 + 0 + 1) / 4 = 0,75
Quadratische Abstände:       0,0625 + 0,0625 + 0,5625 + 0,0625
Varianz:                     0,75 / 4 = 0,1875
Standardabweichung:          √0,1875 = 0,4330127018922193
```

Der Wert beschreibt damit, wie stark die Informationsmenge zwischen den
Beats schwankt:

- `0` bedeutet, dass jeder Beat denselben Informationswert besitzt;
- ein größerer Wert bedeutet eine stärkere Streuung um den mittleren
  Beat-Informationswert;
- der Wert sagt nicht, an welcher Stelle ein niedriger oder hoher Beat-Wert
  liegt.

Beispielsweise haben die Profile `[0, 1, 1, 2]` und `[1, 2, 1, 0]` dieselbe
Summe, denselben Mittelwert und dieselbe Standardabweichung, obwohl ihre
zeitliche Entwicklung verschieden ist. Auch die Standardabweichung erhält
also keine Information über Reihenfolge, Steigung oder Peak-Position. Das
Tempo geht ebenfalls nicht in die Berechnung ein; maßgeblich sind nur die
Beat-Informationswerte, die der Automat aus dem Onset-String und seinem über
Beatgrenzen fortgeführten Spielzustand erzeugt.

Die geordnete Liste der Beat-Werte wird nur während der Konstruktion
berechnet und anschließend verworfen. Zwei Rhythmen können deshalb dieselben
gespeicherten Aggregate besitzen, obwohl sich ihre Information zeitlich
unterschiedlich entwickelt. Das ist der zentrale Ansatzpunkt für ein
späteres rhythmisches Informationsprofil.

Die kodifizierten Erwartungen für das Maß stehen in
[`HuffmanRhythmTest`](../../src/test/java/syrincs/a_domain/rhythm/HuffmanRhythmTest.java).
Änderungen am Zustandsautomaten oder an der Zustandsfortführung sind
fachliche Änderungen und müssen diese Beispiele bewusst mitbetrachten.

## Persistenz

Der PostgreSQL-Adapter speichert folgende Werte:

| Spalte | Herkunft |
| --- | --- |
| `rhythmstring` | normalisierter `x`/`o`-String |
| `numerator` | Zähler, beim Generator `4` |
| `denominator` | Nenner, beim Generator `4` |
| `info` | Summe der Beat-Information |
| `deviation` | Populationsstandardabweichung |

Die ID erzeugt PostgreSQL. Das Tempo wird nicht gespeichert. Beim späteren
Laden rekonstruiert der Adapter den `HuffmanRhythm` mit dem aktuellen
Standardtempo von 120 BPM und berechnet Information und Abweichung erneut aus
dem Onset-String.

`PostgresRhythmRepository.saveAll(...)` schreibt innerhalb einer Transaktion
in Batches von 1024 Datensätzen. Es gibt weder einen Unique Constraint für
den Onset-String noch eine Deduplizierung im Use Case. Jeder erneute Aufruf von
`calculate rhythms` hängt deshalb weitere 65.536 Zeilen an.

## Abgrenzung zu Suche und Playback

Der Generator endet mit der Persistenz. Die heutige Auswahl und Wiedergabe
ist ein nachgelagerter Ablauf:

```bash
syrincs play rhythm info 3 5 7
```

Für jeden angefragten Informationsgrad lädt
`PlayHuffmanRhythmsUseCase` Kandidaten mit
`deviation > AppDefaults.MIN_HUFFMAN_RHYTHM_DEVIATION`, derzeit `0.7`, und
wählt zufällig einen Kandidaten. Danach werden die Onsets auf Kick und Snare
verteilt und über MIDI abgespielt.

Aktuell gibt es dabei:

- keine Ausgabe der Kandidaten vor der Auswahl;
- keine Suche nach einem geordneten Beat-Informationsprofil;
- keine Filter nach Peak-Position, Onset-Dichte oder metrischer Gewichtung;
- keinen Seed für reproduzierbare Auswahl;
- keinen eigenständigen Suchbefehl ohne Playback.

Diese Punkte gehören nicht in die Erzeugung der 65.536 Grundrhythmen. Sie
lassen sich auf der erzeugten Datenbasis als eigenes Analyse- und
Suchverhalten ergänzen. Ein Informationsprofil würde allerdings zunächst
eine öffentliche, unveränderliche Darstellung der geordneten Beat-Werte in
der Domäne benötigen; anschließend müsste entschieden werden, ob es beim
Laden stets neu berechnet oder zusätzlich persistiert wird.

## Wichtige Tests

Die engsten Tests für den Generator sind:

```bash
mvn -Dtest='HuffmanRhythmTest,RhythmTest,GenerateAndPersistRhythmUseCaseTest' test
```

- `HuffmanRhythmTest` schützt Informationsmaß, Zustandsfortführung und
  Standardabweichung.
- `RhythmTest` schützt Normalisierung, Validierung und Mehrtaktverhalten.
- `GenerateAndPersistRhythmUseCaseTest` schützt die Abbildung von
  Binärstrings auf Onsets und den Repository-Aufruf.

Die Unit-Tests benötigen keine PostgreSQL-Instanz. Für den vollständigen
CLI-Ablauf mit Persistenz müssen PostgreSQL erreichbar und das Schema mit
`syrincs init` vorbereitet sein.

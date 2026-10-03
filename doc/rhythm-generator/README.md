# Rhythmusmodul und Rhythmus-Generator

Der Rhythmus-Generator erzeugt und bewertet alle binären Rhythmen eines
4/4-Takts im 16tel-Raster und speichert sie in PostgreSQL. „Huffman“ bezeichnet
hier das projektinterne Informationsmaß; der Generator baut keinen klassischen
Huffman-Baum und komprimiert keine Audiodaten.

Diese Dokumentation beschreibt den aktuellen Stand. Informationsprofile
pro Beat sind öffentlich abrufbar und mit `analyze rhythm --details` sichtbar.
Eine Suche nach Profilformen und eine reproduzierbare Zufallsauswahl sind
noch nicht implementiert.

## Funktionsübersicht des Rhythmusmoduls

Das Modul umfasst neben dem Generator auch Analyse, Dateieingabe, Auswahl,
Mapping und Wiedergabe. Huffman bewertet `x`/`o`-Onsets. RDL-0 beschreibt
bereits auf Stimmen verteilte `x`/`-`-Pattern und durchläuft keine
Huffman-Analyse.

| Funktion | Einstieg | Aktuelles Verhalten |
| --- | --- | --- |
| Onsets analysieren | `analyze rhythm "xooo xoxo xooo xoxo" [--details]` | Normalisierung, Gesamtinformation, Standardabweichung und Beat-Onset-Strings ausgeben; optional Beat-Informationswerte und Mittelwert, ohne DB oder Playback. |
| Schema vorbereiten | `init` | Rhythmustabelle anlegen oder älteres Schema ergänzen. |
| Rhythmen erzeugen | `calculate rhythms` | Alle 65.536 eintaktigen 4/4-Pattern bewerten und speichern. |
| DB-Rhythmen auswählen | `play rhythm info 3 5 7` | Je Grad zufällig einen Kandidaten auswählen und abspielen; Default `deviation > 0.7`, optional inklusive Min-/Max-Grenzen. |
| Kick/Snare zuordnen | Intern beim DB-Playback | Jeden Onset eines 16-Schritt-Takts anhand gewichteter Positionsregeln einer Stimme zuweisen. |
| Takte verbinden | Intern beim DB-Playback | Einzeln gemappte Takte in Anfragereihenfolge zu einem Pattern verbinden. |
| RDL lesen und validieren | `play rhythm --in data/beat.rdl` | Header, Voices und Pattern lesen; Kick/Snare, Längen und Werte prüfen. |
| MIDI abspielen | Beide `play rhythm`-Wege | Note-On/Off-Ereignisse mit Tempo, Raster, Gate und Nachlauf erzeugen und senden. |
| Ausgang wählen | `--device` bei beiden Playback-Wegen | MIDI-Ausgang auswählen; `devices` zeigt verfügbare Geräte. |

Bedienbeispiele und Formatdetails stehen im Haupt-README unter
[Huffman-Rhythmik](../../README.md#huffman-rhythmik),
[RDL-0](../../README.md#rdl-0) und [MIDI](../../README.md#midi).

## Eigenständige Analyse

[`AnalyseRhythmUseCase`](../../src/main/java/syrincs/b_application/AnalyseRhythmUseCase.java)
erstellt einen `HuffmanRhythm` mit 4/4 und 120 BPM. Die CLI zeigt dessen
normalisierte Onsets, Gesamtinformation, Standardabweichung und die nach
Beats gruppierten Onset-Strings. `Beats=[...]` enthält `x`/`o`-Strings,
keine numerischen Beat-Informationswerte.

`--details` ergänzt eine zweite Ausgabezeile, ohne die normale Analysezeile
oder die Bedeutung von `Beats` zu ändern:

```bash
syrincs analyze rhythm "xooo xoxo xooo xoxo" --details
```

```text
[ANALYZE] Rhythm=xoooxoxoxoooxoxo | Info=3 | Deviation=0.433013 | Beats=[xooo, xoxo, xooo, xoxo]
[DETAILS] BeatInformation=[1, 1, 0, 1] | Mean=0.750000
```

Mehrere vollständige 4/4-Takte können gemeinsam analysiert werden. Dabei
läuft der Playing-Zustand über Beat- und Taktgrenzen weiter; Information und
Standardabweichung beziehen sich auf die gesamte Eingabe. `BeatInformation`
enthält sämtliche Beats flach in Eingabereihenfolge, keine Taktgruppen oder
eigenständigen Taktwerte. Die Analyse speichert nichts und spielt nichts ab.

## Ablauf der Erzeugung

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
- Die Länge muss positiv und ein Vielfaches von `4 × Taktzähler` sein;
  im 4/4-Pfad von Analyse und Generator also ein Vielfaches von 16.
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

`HuffmanRhythm` berechnet und speichert die geordneten Beat-Werte einmal
bei der Konstruktion. Alle folgenden Aggregate stammen aus derselben Liste:

- `getBeatInformation()`: unveränderliche Liste der Informationswerte aller Beats;
- `getInformation()`: Summe der Informationswerte aller Beats;
- `getMeanBeatInformation()`: arithmetischer Mittelwert dieser Werte;
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

Zwei Rhythmen können dieselben Aggregate besitzen, obwohl sich ihre
Information zeitlich unterschiedlich entwickelt. Die unveränderliche
Beat-Liste erhält diese Reihenfolge und macht den Unterschied zugänglich.
Sie wird nicht zusätzlich in PostgreSQL gespeichert, sondern beim Laden
wie die bisherigen Aggregate aus den Onsets neu berechnet. Codesymbolfolgen
bleiben intern; Zustandsmaschine und Informationsmaß sind unverändert.

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

Suchprädikate beziehen sich wie bisher auf die gespeicherten Spalten `info`
und `deviation`, nicht auf die anschließend neu berechneten Aggregate.
Das Schema erlaubt eine unbekannte Deviation (`NULL`); mit einer gesetzten
Grenze ist eine solche Zeile kein Treffer. Ohne Deviation-Grenzen enthält
der neue Bereichs-Port keine Deviation-Bedingung. Bestehende Katalogdaten
werden weder bereinigt noch versioniert oder nachträglich abgeglichen.

`PostgresRhythmRepository.saveAll(...)` schreibt innerhalb einer Transaktion
in Batches von 1024 Datensätzen. Es gibt weder einen Unique Constraint für
den Onset-String noch eine Deduplizierung im Use Case. Jeder erneute Aufruf von
`calculate rhythms` hängt deshalb weitere 65.536 Zeilen an.

## Abgrenzung zu Suche und Playback

Der Generator endet mit der Persistenz. Die heutige Auswahl und Wiedergabe
ist ein nachgelagerter Ablauf:

```bash
syrincs play rhythm info 3 5 7
syrincs play rhythm info 3 5 --deviation-min 0.2 --deviation-max 0.8
```

Für jeden angefragten Informationsgrad lädt
der `UseCaseInteractor` ohne Deviation-Optionen Kandidaten mit
`deviation > AppDefaults.MIN_HUFFMAN_RHYTHM_DEVIATION`, derzeit `0.7`, und
wählt zufällig einen Kandidaten. Sobald mindestens eine Grenze gesetzt ist,
verwendet er stattdessen den frameworkfreien `DeviationRange`-Vertrag und
`RhythmRepository.getAllByInformationAndDeviationRange(...)`:

- Min allein: `deviation >= min`, kein Maximum;
- Max allein: `deviation <= max`, kein implizites Minimum;
- beide: beide Bedingungen inklusive.

Die bestehende Strict-Min-Methode bleibt unverändert. Beide Grenzen sind
optional, endlich und nichtnegativ; `min > max` ist ungültig. Die Validierung
erfolgt vor Repository-Aufrufen. Min 0 schließt auch Rhythmen mit Deviation 0
ein. Die SQL-Grenzen werden als Parameter gebunden. Nach der Auswahl werden
die Onsets wie bisher auf Kick und Snare verteilt und über MIDI abgespielt.

Grade ohne Kandidaten werden übersprungen. Gibt es für keinen angefragten
Grad Kandidaten, meldet die CLI einen Fehler mit Hinweisen auf `init` und
`calculate rhythms`. Der nachgelagerte `PlayHuffmanRhythmsUseCase` übernimmt
Mapping, Verkettung, Validierung und Übergabe an das Playback.

Aktuell gibt es dabei:

- keine Ausgabe der Kandidaten vor der Auswahl;
- keine Suche nach einem geordneten Beat-Informationsprofil;
- keine Filter nach Peak-Position, Onset-Dichte oder metrischer Gewichtung;
- keinen Seed für reproduzierbare Auswahl;
- keinen eigenständigen Suchbefehl ohne Playback.

Diese Punkte gehören nicht in die Erzeugung der 65.536 Grundrhythmen. Sie
lassen sich auf der erzeugten Datenbasis als eigenes Analyse- und
Suchverhalten ergänzen. Die öffentliche, unveränderliche Beat-Liste steht
dafür bereits bereit; beim Laden wird sie aus den Onsets neu berechnet.
Eine Suche nach diesen Werten ist noch nicht implementiert.

## Kick-/Snare-Mapping und Verkettung

Der
[`RhythmMapperFromOnsetStringToKickAndSnare`](../../src/main/java/syrincs/a_domain/rhythm/RhythmMapperFromOnsetStringToKickAndSnare.java)
akzeptiert genau einen 16-Schritt-Takt. Gewichtete Regeln bevorzugen Kick
auf Downbeats und Antizipationen sowie Snare auf Backbeats. Für jeden Onset
gewinnt der höhere Score; bei Gleichstand gewinnt an den nullbasierten
Positionen 4 und 12 die Snare, sonst die Kick.

Die Domänen-API liefert Masken, Positionslisten und Scores und kennt die
Styles `DEFAULT` und `FOUR_ON_FLOOR`. Das DB-Playback verwendet `DEFAULT`;
Stilwahl und Scores sind nicht über die CLI zugänglich. Standardstimmen sind
Kick 36 und Snare 38, jeweils Kanal 9, Velocity 90 und Gate 50 Prozent.

`PlayHuffmanRhythmsUseCase` mappt ausgewählte Takte einzeln und verbindet sie
zu einem gemeinsamen Playback. Er verlangt übereinstimmende Taktart, Tempo
und Raster. Die Mehrtaktfähigkeit der Analyse bedeutet nicht, dass der
Mapper einen mehrtaktigen Onset-String direkt verarbeiten kann.

## RDL-Eingabe, Validierung und MIDI-Wiedergabe

[`RhythmFileParser`](../../src/main/java/syrincs/c_adapters/RhythmFileParser.java)
liest `time`, `tempo`, `res-per-beat`, `bars`, `voice` und `pattern`.
Fehlende Headerwerte werden mit 4/4, 120 BPM, vier Schritten pro Beat und
einem Takt ergänzt. Ohne `--in` verwendet die CLI `data/beat.rdl`.
Syntax und Voice-Parameter erläutert das [RDL-Beispiel](../../README.md#rdl-0).

[`ValidatePatternsUseCase`](../../src/main/java/syrincs/b_application/ValidatePatternsUseCase.java)
verlangt Voice-Deklarationen für Kick und Snare und genau diese beiden
Pattern. Er prüft passende Patternlängen, positive Taktzähler, Raster- und
Taktanzahlen sowie Noten/Velocity `0..127` und Kanäle `0..15`.
Die Validierung wird auch auf gemappte Huffman-Pattern angewandt.

Beide Wege verwenden den `RhythmPlaybackPort` und den MIDI-Adapter.
[`SequenceBuilder`](../../src/main/java/syrincs/c_adapters/midi/SequenceBuilder.java)
erstellt eine Sequenz mit PPQ 480, setzt das Tempo und berechnet Note-Off aus
dem Gate-Anteil der Schrittlänge. Standardmäßig folgen zwei Sekunden
Nachlauf. Rhythmus-Playback besitzt keinen OSC-Pfad.

Die Geräteauflösung verwendet zuerst `--device`, danach
`SYRINCS_MIDI_DEVICE`, Roland Digital Piano/DP603 und schließlich den ersten
verfügbaren Ausgang; Details stehen unter [MIDI](../../README.md#midi).
Tempo und Voice-Parameter kommen bei RDL aus der Datei, beim DB-Playback aus
den genannten Defaults. Die Datenbank speichert kein Tempo.

## Wichtige Tests

Die engsten Tests für den Generator sind:

```bash
mvn -Dtest='HuffmanRhythmTest,RhythmTest,GenerateAndPersistRhythmUseCaseTest' test
```

- `HuffmanRhythmTest` schützt Informationsmaß, Zustandsfortführung und
  Standardabweichung sowie Beat-Profile, Mittelwert und Unveränderlichkeit.
- `RhythmTest` schützt Normalisierung, Validierung und Mehrtaktverhalten.
- `GenerateAndPersistRhythmUseCaseTest` schützt die Abbildung von
  Binärstrings auf Onsets und den Repository-Aufruf.

Die Unit-Tests benötigen keine PostgreSQL-Instanz. Für den vollständigen
CLI-Ablauf mit Persistenz müssen PostgreSQL erreichbar und das Schema mit
`syrincs init` vorbereitet sein.

Weitere relevante Tests sind `RootCmdRhythmCliTest` (Analyseausgabe,
RDL-Aufruf, Geräteübergabe und Verkettung), `RhythmE2ETest` sowie
`SequenceBuilderTest` (MIDI-Ereignisse und Zeitpunkte).

`DeviationRangeTest`, `UseCaseInteractorRhythmSelectionTest` und die CLI-Tests
prüfen tatsächliche Grad-/Grenzfilterung, Grenzgleichheit, Deviation 0,
Fehlereingaben und den unveränderten strikten Playback-Default.
`PostgresRhythmRepositoryTest` verifiziert die tatsächlich erzeugten
SQL-Prädikate, Bindungen, Rekonstruktion und Fehlerpfade mit einem JDBC-
Testdouble ohne Netzwerk; er ersetzt keinen realen PostgreSQL-Integrationstest.

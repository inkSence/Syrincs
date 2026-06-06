# Syrincs

Syrincs ist ein Java-basierter MIDI-Generator für Akkorde und Rhythmen. Das Projekt kann Akkorde nach Paul Hindemiths Akkordbestimmung analysieren, erzeugen, in PostgreSQL speichern und als MIDI ausgeben. Rhythmen werden entweder aus RDL-0-Dateien abgespielt oder als 16tel-Onset-Strings nach einer selbst entwickelten Huffman-Komplexität bewertet, gespeichert und aus der Datenbank wiedergegeben.

## Kernfunktionen

- Akkordanalyse nach Hindemith: Spalte A/B nach Tritonusfreiheit, Grundtonbestimmung, Gruppenzuordnung und Rahmenintervall.
- Akkordgenerator: erzeugt 3-, 4- und 5-stimmige Akkorde in einem MIDI-Notenbereich, filtert doppelte Pitch Classes und begrenzt die Lage auf maximal drei Oktaven.
- MIDI-Ausgabe: sendet einzelne Töne, Akkorde und Rhythmus-Sequenzen über `javax.sound.midi`.
- Rhythmus-Dateien: liest ein kleines RDL-0-Format mit Takt, Tempo, Stimmen und Patterns.
- Huffman-Rhythmik: bewertet 4/4-Onset-Strings im 16tel-Raster mit einem projektinternen Informationsmaß und Standardabweichung pro Beat.
- Persistenz: speichert Hindemith-Akkorde und Huffman-Rhythmen über JDBC in PostgreSQL.

## Technik

- Sprache: Java 21
- Build: Maven
- CLI: Picocli
- MIDI: JDK `javax.sound.midi`
- Datenbank: PostgreSQL JDBC
- Tests: JUnit 5

Die Maven-Koordinaten stehen in `pom.xml`:

```xml
<groupId>org</groupId>
<artifactId>syrincs</artifactId>
<version>0.1</version>
```

## Projektstruktur

```text
src/main/java/syrincs/
  a_domain/                Domänenmodell: Akkorde, Hindemith, Rhythmus, Statistik
  b_application/           Use Cases und Ports
  c_adapters/              CLI, MIDI-Adapter, PostgreSQL-Adapter, RDL-Parser
  d_frameworksAndDrivers/  Konfiguration
  Main.java                Composition Root

data/
  beat.rdl                 Beispielrhythmus

doc/
  Gruppen.md               Projektnotizen zu Hindemith-Gruppen
  *.pdf                    Hindemith-Referenzmaterial
  *.png                    Diagramme zur Huffman-Rhythmik
```

Das Projekt folgt grob einer Clean-Architecture-Aufteilung: Die Domäne hängt nicht an MIDI oder JDBC, die Anwendungsschicht spricht über Ports, und konkrete Adapter liegen außen.

## Build und Tests

```bash
mvn test
mvn package
```

Nach `mvn package` erzeugt der Appassembler ein Startskript:

```bash
target/app/bin/syrincs --help
```

Alternativ kann die CLI über Maven gestartet werden:

```bash
mvn exec:java -Dexec.args="--help"
```

## MIDI-Gerät

Beim Start lädt `DeviceService` aktuell ein Standardgerät mit dem Namens-Substring `Roland Digital Piano`. Dieser Wert steht in `MidiConfig.defaults()`.

Wenn auf dem System kein passendes MIDI-Ausgabegerät vorhanden ist, können CLI-Kommandos bereits beim Bootstrapping fehlschlagen. Für ein anderes Setup muss der Default in `src/main/java/syrincs/c_adapters/midi/MidiConfig.java` angepasst oder die Geräteauflösung erweitert werden.

MIDI-Kanäle werden intern nullbasiert verwendet: `channel=9` entspricht dem üblichen MIDI-Kanal 10 für General-MIDI-Drums.

## CLI

Geräte anzeigen:

```bash
mvn exec:java -Dexec.args="list"
```

Einzelnote spielen:

```bash
mvn exec:java -Dexec.args="play note note 60 vel 0.5 dur 500"
```

Akkord nach Hindemith analysieren:

```bash
mvn exec:java -Dexec.args="analyse chord 60 64 67"
```

Ausgabeformat:

```text
[ANALYZE] Notes=[60, 64, 67] | Column=A_TRITONE_FREE | Root=60 | Group=1 | Frame=7
```

Akkorde erzeugen und persistieren:

```bash
mvn exec:java -Dexec.args="calculate chords 48 84"
```

Akkorde aus der Datenbank spielen:

```bash
mvn exec:java -Dexec.args="play chords numNotes 3 4 groups 1 2 3 rootNote 60 range 24 duration 200 channel 0"
```

Die Picocli-Optionen für `play chords` sind im aktuellen Stand ohne führende Bindestriche definiert, zum Beispiel `numNotes`, `groups`, `rootNote`, `range`, `duration` und `channel`.

Rhythmusdatei abspielen:

```bash
mvn exec:java -Dexec.args="play rhythm --in data/beat.rdl"
```

Rhythmus nach Huffman-Komplexität analysieren:

```bash
mvn exec:java -Dexec.args='analyse rhythm "xooo xoxo xooo xoxo"'
```

Alle 4/4-Rhythmen im 16tel-Raster generieren und persistieren:

```bash
mvn exec:java -Dexec.args="calculate rhythms"
```

Das erzeugt `2^16` Patterns und speichert sie als Huffman-Rhythmen in PostgreSQL.

Zufällige gespeicherte Rhythmen nach Informationsgraden spielen:

```bash
mvn exec:java -Dexec.args="play rhythm db 3 5 7"
```

Pro angegebenem Informationsgrad wird ein Rhythmus aus der Datenbank geladen. Der aktuelle Code filtert dabei zusätzlich auf `deviation > 0.7`.

## Hindemith-Akkordbestimmung

Die Hindemith-Logik liegt in `src/main/java/syrincs/a_domain/hindemith`.

Wichtige Klassen:

- `Series1`: Hindemiths Reihe 1 als MIDI-Verwandtschaftsreihe eines Grundtons.
- `Series2`: Hindemiths Reihe 2 als Intervallordnung `[12, 7, 5, 4, 8, 3, 9, 2, 10, 1, 11, 6]`.
- `HindemithInterval`: bewertet Intervalle nach Reihe 2 und bestimmt einen bevorzugten Intervall-Grundton.
- `ChordAnalysis`: analysiert MIDI-Notenlisten.
- `ChordSpecificationRepository`: definiert die Gruppenspezifikationen.
- `ChordRules`: prüft Intervall-, Tritonus- und Grundton/Bass-Regeln.

`ChordAnalysis.analyze(...)` liefert:

- `column`: `A_TRITONE_FREE` oder `B_WITH_TRITONE`
- `rootNote`: bestimmter Grundton als MIDI-Note
- `group`: Hindemith-Gruppe nach aktueller Spezifikation
- `frameInterval`: Abstand zwischen tiefster und höchster Note
- `notes`: sortierte MIDI-Noten

Die aktuelle Implementierung definiert in `ChordSpecificationRepository` 18 Gruppenspezifikationen, die als Gruppen `1..18` zurückgegeben werden. Die Datei `doc/Gruppen.md` beschreibt eine ältere konzeptionelle Zwischenfassung mit Gruppen `1..14` und ist weiterhin als Herleitung nützlich.

Beispiele aus den Tests:

```text
[60, 64, 67]     -> Spalte A, Grundton 60, Gruppe 1
[60, 63, 68]     -> Spalte A, Grundton 68, Gruppe 2
[60, 64, 70]     -> Spalte B, Grundton 60, Gruppe 3
[60, 66, 69]     -> Spalte B, Grundton 66, Gruppe 18
```

## Huffman-Rhythmik

Die Huffman-Rhythmik liegt in `src/main/java/syrincs/a_domain/rhythm/HuffmanRhythm.java`.

Eingaben sind Onset-Strings mit `x` für Einsatz und `o` für kein neuer Einsatz. Whitespace wird ignoriert:

```text
xooo xoxo xooo xoxo
```

Im aktuellen Modell gilt:

- Standardtakt für Analyse: `4/4`
- Auflösung: vier Positionen pro Beat, also 16tel-Raster
- Gültige Zeichen: `x` und `o`
- Die Länge muss zur Taktlänge passen

`HuffmanRhythm` verarbeitet jeden Beat mit einer endlichen Zustandsmaschine. Der Zustand unterscheidet unter anderem, ob gerade gespielt wird und ob der Beat als Viertel, Achtel oder Sechzehntel behandelt wird. Dabei werden Codesymbole erzeugt:

```text
00  Trennung erniedrigen
01  Trennung erhöhen
10  Pause spielen
11  Note spielen
```

Der Informationswert ist im aktuellen Projektstand die Summe der erzeugten Codesymbole über alle Beats. Zusätzlich wird die Standardabweichung der Informationswerte pro Beat berechnet und als rhythmische Streuung gespeichert.

Beispiele aus den Tests:

```text
xooo xooo xooo xooo -> Information 1
xooo oooo oooo oooo -> Information 2
xoxo xoxo xoxo xoxo -> Information 5
xxxx xxxx xxxx xxxx -> Information 9
xxox xxox xxox xxox -> Information 17
```

Beim Abspielen gespeicherter Huffman-Rhythmen wird der reine Onset-String mit `RhythmMapperFromOnsetStringToKickAndSnare` auf Kick und Snare verteilt. Die Default-Heuristik bevorzugt Kick auf Downbeats, Snare auf Backbeats, Kick-Antizipationen und Snare-Ghosts.

## RDL-0 Rhythmusdateien

`RhythmFileParser` liest ein minimales RDL-0-Format. Beispiel aus `data/beat.rdl`:

```text
time: 4/4
tempo: 120
res-per-beat: 4
bars: 1

voice snare note=38 channel=9 vel=90 gate=50
voice kick  note=36 channel=9 vel=90 gate=50

pattern snare: | - x x x | - x - - | - - x x | x - - - |
pattern kick:  | x - - - | x - - - | x - - - | x x - - |
```

Parser-Regeln:

- Kommentare beginnen mit `#`.
- `time`, `tempo`, `res-per-beat` und `bars` sind Headerwerte.
- `voice <name> note=<0-127> channel=<0-15> vel=<0-127> gate=<0-100>` definiert eine Stimme.
- `pattern <name>:` enthält `x` für Hit und `-` für Pause.
- `|` und Whitespace werden in Pattern-Zeilen ignoriert.
- Für die Wiedergabe werden aktuell genau die Stimmen `kick` und `snare` validiert.

## Datenbank

Die Anwendung verwendet PostgreSQL. Im aktuellen Code sind die Verbindungswerte in `AppConfig.loadDbConfig(...)` fest gesetzt:

```text
jdbc:postgresql://localhost:5432/syrincsdb
user: syrincs
password: syrincs
```

In `AppConfig` sind CLI- und Environment-basierte Konfigurationspfade bereits vorbereitet, aber derzeit auskommentiert. Die `Main`-Klasse filtert `--db-url=`, `--db-user=` und `--db-pass=` zwar aus den Picocli-Argumenten, `AppConfig` nutzt aktuell aber die festen Werte.

Erwartete Tabellen:

```sql
CREATE TABLE public.hindemithChords (
    id BIGSERIAL PRIMARY KEY,
    notes INT4[] NOT NULL,
    numNotes INT NOT NULL,
    minNote INT NOT NULL,
    maxNote INT NOT NULL,
    rootNote INT NOT NULL,
    chordGroup INT NOT NULL
);

CREATE TABLE public.huffmanRhythms (
    id BIGSERIAL PRIMARY KEY,
    rhythmstring VARCHAR(100),
    numerator SMALLINT NOT NULL,
    denominator SMALLINT NOT NULL,
    info SMALLINT NOT NULL,
    deviation DOUBLE PRECISION
);
```

Es sind keine Migrationsskripte im Repository enthalten.

## Tests

Die Tests decken unter anderem ab:

- Hindemith-Reihe 1 und Reihe 2
- Akkordanalyse und Gruppenzuordnung
- Akkordgenerierung und Persistenz-Use-Cases mit Fakes
- MIDI-Senden über Fake-Ports
- RDL-Parsing, Patternvalidierung und Sequenzaufbau
- Huffman-Informationswerte für Beispielrhythmen
- Picocli-Parsing für `play chords`

Ausführen:

```bash
mvn test
```

## Aktueller Entwicklungsstand

Einige Stellen sind sichtbar noch in Arbeit:

- `doc/Gruppen.md` und `ChordSpecificationRepository` sind nicht vollständig synchron: Die Dokumentation beschreibt die Entwicklung bis 14 Gruppen, die aktuelle Implementierung klassifiziert bis Gruppe 18.
- Die Datenbankkonfiguration ist vorbereitet, aber derzeit hardcodiert.
- Die MIDI-Geräteauswahl ist auf das Standardgerät `Roland Digital Piano` ausgerichtet.
- `calculate rhythms` akzeptiert CLI-Optionen wie `--tempo`, nutzt im aktuellen Code aber die fest verdrahtete 4/4-Generierung aller 16tel-Rhythmen.

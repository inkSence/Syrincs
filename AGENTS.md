# AGENTS.md

Diese Hinweise gelten für das gesamte Repository.

## Einstieg

- Lies zuerst `README.md`. Dort stehen Zweck, CLI-Beispiele, Datenbanktabellen, RDL-Format sowie der aktuelle Stand der Hindemith- und Huffman-Rhythmuslogik.
- Prüfe zu Beginn `git status --short`. Der Arbeitsbaum kann absichtlich unfertig sein; vorhandene Änderungen nicht zurücksetzen und nicht ungefragt umformatieren.
- `target/` und IDE-Dateien sind Build-/Tooling-Artefakte. Dort nur arbeiten, wenn es ausdrücklich verlangt wird.

## Projektüberblick

Syrincs ist ein Java-21/Maven-Projekt für MIDI-Generierung:

- Akkorde werden nach Hindemith analysiert, erzeugt, in PostgreSQL persistiert und als MIDI gespielt.
- Rhythmen werden entweder aus RDL-0-Dateien gespielt oder als 16tel-Onset-Strings nach einer projektinternen Huffman-Komplexität bewertet.
- MIDI läuft über `javax.sound.midi`; die CLI läuft über Picocli.

Die Schichten sind grob Clean Architecture:

- `src/main/java/syrincs/a_domain/`: Domäne für Akkorde, Hindemith, Rhythmus, Statistik.
- `src/main/java/syrincs/b_application/`: Use Cases und Ports.
- `src/main/java/syrincs/c_adapters/`: CLI, MIDI, PostgreSQL, RDL-Parser.
- `src/main/java/syrincs/d_frameworksAndDrivers/`: Konfiguration.
- `src/main/java/syrincs/Main.java`: Composition Root.

Halte diese Richtung ein: Domäne nicht von JDBC, Picocli oder `javax.sound.midi` abhängig machen.

## Build und Tests

Standardbefehle:

```bash
mvn test
mvn package
mvn exec:java -Dexec.args="--help"
```

Bekannter Test-Hinweis:

- `mvn test` kann in Umgebungen ohne MIDI-Gerät `Roland Digital Piano` an `RhythmE2ETest.setupMidi` scheitern.
- Für die nicht umgebungsabhängigen Tests ist dieser Befehl sinnvoll:

```bash
mvn -Dtest='*,!RhythmE2ETest' test
```

Wenn Tests wegen fehlendem MIDI-Gerät, fehlender PostgreSQL-Instanz oder lokaler Audio/MIDI-Konfiguration scheitern, melde das klar als Umgebungsproblem. Nicht stillschweigend Defaults ändern, nur damit Tests lokal grün werden.

## Hindemith-Domäne

Wichtige Dateien:

- `a_domain/hindemith/Series1.java`
- `a_domain/hindemith/Series2.java`
- `a_domain/hindemith/HindemithInterval.java`
- `a_domain/hindemith/ChordAnalysis.java`
- `a_domain/hindemith/ChordSpecificationRepository.java`
- `a_domain/hindemith/ChordRules.java`
- `doc/Gruppen.md`

Aktueller Stand:

- `ChordAnalysis.analyze(...)` liefert Spalte A/B, Grundton, Gruppe, Rahmenintervall und sortierte Noten.
- `ChordSpecificationRepository` enthält derzeit 18 Gruppenspezifikationen und gibt Gruppen `1..18` zurück.
- `doc/Gruppen.md` beschreibt eine ältere konzeptionelle Zwischenfassung mit Gruppen `1..14`. Bei Arbeiten an Gruppenlogik immer Code, Tests und Doku gegeneinander prüfen.

Bei Änderungen an Akkordgruppen immer gezielte Tests in `src/test/java/syrincs/a_domain/hindemith/` und `src/test/java/syrincs/b_application/AnalyseChordByHindemithUseCaseTest.java` ergänzen oder aktualisieren.

## Rhythmus-Domäne

Wichtige Dateien:

- `a_domain/rhythm/Rhythm.java`
- `a_domain/rhythm/HuffmanRhythm.java`
- `a_domain/rhythm/RhythmMapperFromOnsetStringToKickAndSnare.java`
- `a_domain/statistics/StandardDeviation.java`
- `b_application/GenerateAndPersistRhythmUseCase.java`
- `b_application/PlayHuffmanRhythmsUseCase.java`
- `c_adapters/RhythmFileParser.java`

Konventionen:

- Huffman-Rhythmen nutzen `x` für Einsatz und `o` für kein neuer Einsatz; Whitespace wird ignoriert.
- Der aktuelle Analysepfad geht von 4/4 und 16tel-Raster aus.
- RDL-0-Pattern nutzen `x` für Hit und `-` für Pause; `|` und Whitespace werden ignoriert.
- Die Wiedergabe validiert aktuell genau die Stimmen `kick` und `snare`.
- `play rhythm db ...` lädt pro Informationsgrad zufällig einen Rhythmus und filtert aktuell zusätzlich mit `deviation > 0.7`.

Bei Arbeiten an der Huffman-Komplexität die erwarteten Informationswerte in `HuffmanRhythmTest` beachten. Einige Klassen geben aktuell Diagnoseausgaben auf `System.out` aus; nicht nebenbei entfernen, wenn die Aufgabe das nicht betrifft.

## CLI-Besonderheiten

CLI-Klasse: `src/main/java/syrincs/c_adapters/cli/RootCmd.java`.

Einige Picocli-Optionen sind bewusst oder historisch ohne führende Bindestriche definiert:

```bash
mvn exec:java -Dexec.args="play chords numNotes 3 4 groups 1 2 rootNote 60 range 24 duration 200 channel 0"
```

Diese Syntax nicht ungefragt auf GNU-Style `--numNotes` ändern; Tests erwarten die aktuelle Form.

Nützliche Beispiele:

```bash
mvn exec:java -Dexec.args="analyse chord 60 64 67"
mvn exec:java -Dexec.args='analyse rhythm "xooo xoxo xooo xoxo"'
mvn exec:java -Dexec.args="play rhythm --in data/beat.rdl"
mvn exec:java -Dexec.args="calculate chords 48 84"
```

## MIDI und Datenbank

MIDI:

- Default-Gerät steht in `c_adapters/midi/MidiConfig.java`: `Roland Digital Piano`.
- MIDI-Kanäle sind intern nullbasiert; `channel=9` ist General-MIDI-Kanal 10.
- Nicht jede Umgebung hat ein echtes MIDI-Out. Tests und CLI können deshalb lokal scheitern.

Datenbank:

- `AppConfig.loadDbConfig(...)` nutzt aktuell fest `jdbc:postgresql://localhost:5432/syrincsdb`, User `syrincs`, Passwort `syrincs`.
- CLI-/Environment-Konfiguration ist im Code vorbereitet, aber aktuell auskommentiert.
- Es gibt keine Migrationsskripte. Erwartete Tabellen stehen in `README.md`.

Bei DB- oder MIDI-Änderungen lieber Konfiguration testbar machen, statt Werte tiefer in Domäne oder Use Cases zu verschieben.

## Arbeitsweise

- Bevorzuge kleine, fokussierte Änderungen.
- Bei fachlichen Änderungen zuerst die bestehenden Tests lesen, weil dort viele Hindemith- und Rhythmusbeispiele kodifiziert sind.
- Verwende Maven für Verifikation. Wenn vollständige Tests wegen Umgebung nicht laufen, führe den sinnvoll eingeschränkten Testbefehl aus und dokumentiere die Einschränkung.
- Keine generierten Dateien aus `target/` einchecken oder als Quelle der Wahrheit verwenden.

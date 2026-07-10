# AGENTS.md

Diese Hinweise gelten für das gesamte Repository.

## Einstieg und Quellen der Wahrheit

- Lies zuerst `README.md`. Es beschreibt Zweck, aktuelle CLI, externe
  Abhängigkeiten, Datenbanktabellen, RDL-0 und den fachlichen Stand.
- Prüfe zu Beginn `git status --short`. Der Arbeitsbaum kann absichtlich
  unfertig sein; vorhandene Änderungen gehören dem Benutzer und dürfen nicht
  zurückgesetzt, überschrieben oder ungefragt umformatiert werden.
- Prüfe bei Verhaltensfragen immer Produktionscode und zugehörige Tests. Ältere
  Notizen unter `doc/` können vom aktuellen Stand abweichen.
- `target/`, IDE-Dateien und lokale Audio-/MIDI-Artefakte sind keine Quellen
  der Wahrheit und werden nicht eingecheckt oder manuell bearbeitet, sofern es
  nicht ausdrücklich verlangt wird.

## Projektüberblick

Syrincs ist eine Java-21-/Maven-CLI für algorithmische Harmonik und Rhythmik:

- Akkorde werden nach Hindemith analysiert, drei- bis fünfstimmig erzeugt, in
  PostgreSQL gespeichert und standardmäßig über SuperCollider/OSC gespielt.
- Rhythmen kommen entweder aus RDL-0-Dateien oder aus 4/4-Onset-Strings im
  16tel-Raster. Letztere werden mit einem projektinternen Huffman-Maß bewertet
  und können in PostgreSQL gespeichert werden.
- Noten und Akkorde verwenden standardmäßig OSC; RDL- und Huffman-Rhythmus-
  Playback läuft immer über `javax.sound.midi`.
- Die öffentliche CLI wird mit Picocli umgesetzt. `Main.java` verdrahtet die
  Use Cases mit PostgreSQL-, MIDI-, OSC- und Runtime-Adaptern.

Die Schichten folgen grob Clean Architecture:

- `src/main/java/syrincs/a_domain/`: Akkorde, Hindemith, Rhythmus, Statistik
  sowie noch nicht an die CLI angebundene Scale-/Counterpoint-Modelle.
- `src/main/java/syrincs/b_application/`: Use Cases, Ports und
  Anwendungsdefaults.
- `src/main/java/syrincs/c_adapters/`: CLI, RDL-Parser, MIDI, OSC, PostgreSQL
  und lokale Runtime.
- `src/main/java/syrincs/d_frameworksAndDrivers/`: Konfigurationsauflösung.
- `src/main/java/syrincs/Main.java`: Composition Root.
- `supercollider/`: OSC-Consumer, Presets, Szenen, Effekte und eigene
  Dokumentation.

Halte die Abhängigkeitsrichtung ein: Die Domäne darf nicht von Picocli, JDBC,
OSC oder `javax.sound.midi` abhängen. Externe Technik gehört in Adapter;
Anwendungslogik spricht über Ports.

## Build, Start und Tests

Standardbefehle:

```bash
mvn test
mvn exec:java -Dexec.args="--help"
mvn package -Dsyrincs.skipCompletionInstall=true
```

`mvn package` erzeugt `target/app/bin/syrincs`. Ohne
`-Dsyrincs.skipCompletionInstall=true` installiert der Build zusätzlich eine
Bash-Completion unter
`~/.local/share/bash-completion/completions/syrincs`; diesen Seiteneffekt bei
automatischer Verifikation vermeiden, sofern er nicht Teil der Aufgabe ist.

Ein schneller fachlicher Ausschnitt:

```bash
mvn -Dtest='HuffmanRhythmTest,RhythmTest,RootCmdChordsCliTest,LocalRuntimeTest' test
```

Umgebungsabhängige Tests:

- `SuperColliderOscOutputAdapterTest` benötigt einen lokalen UDP-Port und
  überspringt sich, wenn die Umgebung UDP verbietet.
- `SuperColliderManualSoundEngineTest` ist ein explizit aktivierbarer
  manueller Hörtest (`-DrunScAudioTest=true`).
- Echte MIDI-Sendetests werden ohne ein passendes Gerät (`Roland Digital
  Piano`/`DP603`) übersprungen.
- DB-gestützte CLI- und Runtime-Abläufe benötigen eine erreichbare lokale
  PostgreSQL-Instanz.

Fehlendes MIDI, UDP, SuperCollider oder PostgreSQL ist als Umgebungsgrenze zu
melden. Keine fachlichen Defaults ändern und keine Validierung lockern, nur um
lokal grüne Tests zu erzwingen.

## CLI-Konventionen

Die CLI steht in `src/main/java/syrincs/c_adapters/cli/RootCmd.java`. Die
öffentlichen Hauptbefehle sind `devices`, `init`, `start`, `status`, `play`,
`calculate` und `analyze`. Maßgeblich sind `RootCmd`, die CLI-Tests und die
Ausgabe von `syrincs --help`.

Korrekte Beispiele:

```bash
mvn exec:java -Dexec.args="analyze chord 60 64 67"
mvn exec:java -Dexec.args='analyze rhythm "xooo xoxo xooo xoxo"'
mvn exec:java -Dexec.args="play rhythm --in data/beat.rdl"
mvn exec:java -Dexec.args="play rhythm info 3 5 7"
mvn exec:java -Dexec.args="calculate chords 48 84"
```

Einige Optionen von `play note` und `play chords` sind historisch ohne
führende Bindestriche definiert:

```bash
mvn exec:java -Dexec.args="play chords numNotes 3 4 groups 1 2 rootNote 60 range 24 duration 200 channel 0"
```

Diese Syntax nicht beiläufig auf GNU-Style wie `--numNotes` umstellen; die
CLI-Tests erwarten die bestehenden Namen. `--output midi`, DB-Flags sowie die
Optionen der `play sc`-Unterbefehle verwenden dagegen führende Bindestriche.
Bei CLI-Änderungen Hilfe, Bash-Completion und Tests gemeinsam aktualisieren.

## Hindemith- und Akkorddomäne

Wichtige Dateien:

- `a_domain/hindemith/Series1.java`
- `a_domain/hindemith/Series2.java`
- `a_domain/hindemith/HindemithInterval.java`
- `a_domain/hindemith/ChordAnalysis.java`
- `a_domain/hindemith/ChordSpecification.java`
- `a_domain/hindemith/ChordSpecificationRepository.java`
- `a_domain/hindemith/ChordRules.java`
- `a_domain/chord/NoteCombinator.java`
- `doc/Gruppen.md`

Aktueller Stand:

- `ChordAnalysis.analyze(...)` sortiert die Noten und liefert Spalte A/B,
  Grundton, Gruppe und Rahmenintervall.
- `ChordSpecificationRepository` enthält 18 Gruppenspezifikationen und gibt
  öffentlich die Gruppen `1..18` zurück.
- `doc/Gruppen.md` beschreibt eine ältere Zwischenfassung mit Gruppen
  `1..14`. Bei Gruppenänderungen Code, Tests, README und diese Doku bewusst
  gegeneinander prüfen.
- Akkordgenerierung erzeugt Kombinationen mit drei, vier und fünf Noten,
  analysiert sie und persistiert die Ergebnisse. Wiederholte Berechnungen
  hängen weitere DB-Zeilen an.

Bei fachlichen Änderungen gezielte Tests unter
`src/test/java/syrincs/a_domain/hindemith/` und
`src/test/java/syrincs/b_application/AnalyseChordByHindemithUseCaseTest.java`
ergänzen oder aktualisieren. Erzeugungsänderungen zusätzlich mit
`GenerateChordsUseCaseTest` abdecken.

## Rhythmusdomäne und RDL-0

Wichtige Dateien:

- `a_domain/rhythm/Rhythm.java`
- `a_domain/rhythm/HuffmanRhythm.java`
- `a_domain/rhythm/RhythmMapperFromOnsetStringToKickAndSnare.java`
- `a_domain/statistics/StandardDeviation.java`
- `b_application/GenerateAndPersistRhythmUseCase.java`
- `b_application/PlayHuffmanRhythmsUseCase.java`
- `b_application/ValidatePatternsUseCase.java`
- `c_adapters/RhythmFileParser.java`
- `c_adapters/midi/SequenceBuilder.java`

Zwei Zeichenformate dürfen nicht vermischt werden:

- Huffman-Onsets: `x` = Einsatz, `o` = kein neuer Einsatz; Whitespace wird
  ignoriert. Der aktuelle Analysepfad verwendet 4/4 und vier Positionen pro
  Beat; die Länge ist ein positives Vielfaches von 16.
- RDL-0: `x` = Hit, `-` = Pause; `|` und Whitespace werden ignoriert. Ein
  vollständiges Beispiel liegt in `data/beat.rdl`.

Weitere aktuelle Regeln:

- `calculate rhythms` erzeugt alle `2^16` Onset-Pattern und persistiert sie;
  erneute Aufrufe ersetzen vorhandene Zeilen nicht.
- `play rhythm info ...` lädt für jeden Informationsgrad Kandidaten mit
  `deviation > AppDefaults.MIN_HUFFMAN_RHYTHM_DEVIATION` (`0.7`), wählt je
  Grad sofern vorhanden zufällig einen und fügt die Auswahl zu einem
  MIDI-Pattern zusammen. Nur wenn für keinen Grad ein Kandidat existiert,
  schlägt der Aufruf fehl.
- Die Onset-zu-Drum-Zuordnung verarbeitet genau einen 16-Schritt-Takt und
  verteilt Einsätze regelbasiert auf Kick und Snare.
- RDL- und generiertes Rhythmus-Playback validieren aktuell genau die Stimmen
  `kick` und `snare`, passende Patternlängen sowie MIDI-Bereiche.
- `SequenceBuilder` fügt standardmäßig zwei Sekunden Nachlaufzeit an, damit
  Synth- und Effektfahnen ausklingen können.

Bei Änderungen am Informationsmaß die kodifizierten Erwartungswerte in
`HuffmanRhythmTest` beibehalten oder fachlich begründet aktualisieren.
Domänenobjekte dürfen keine Diagnoseausgaben auf `System.out` oder
`System.err` schreiben.

## OSC, SuperCollider und MIDI

SuperCollider:

- `SuperColliderOscOutputAdapter` sendet standardmäßig an `127.0.0.1:57120`.
- `syrincs start sc` startet
  `scripts/start-supercollider-consumer.sh` im Vordergrund; Stoppen erfolgt mit
  `Ctrl+C`.
- `play sc` unterstützt neben Noten/Akkorden unter anderem Drums, FX,
  Automation, Szenen, Rollen und Demos. Details stehen in
  `supercollider/README.md` und im Consumer-Skript.
- Wird die CLI außerhalb der Repository-Wurzel ausgeführt, kann
  `SYRINCS_HOME` auf das Repository zeigen.

MIDI:

- MIDI-Kanäle sind nullbasiert; `channel=9` entspricht General-MIDI-Kanal 10.
- Die Geräteauswahl verwendet zuerst ein explizites `--device`, dann
  `SYRINCS_MIDI_DEVICE`, danach `Roland Digital Piano`/`DP603` und zuletzt den
  ersten verfügbaren MIDI-Ausgang.
- Noten und DB-Akkorde nutzen MIDI nur mit `--output midi`; Rhythmus-Playback
  nutzt MIDI immer.

Klang-, Transport- und Geräteauswahl nicht in die Domäne verschieben. Bei
Adapteränderungen Schnittstellen testbar halten und reale Hardware nicht zur
Voraussetzung für Unit-Tests machen.

## PostgreSQL und lokale Runtime

`AppConfig.loadDbConfig(...)` löst Werte in dieser Reihenfolge auf:

1. `--db-url=…`, `--db-user=…`, `--db-pass=…`
2. `SYRINCS_DB_URL`, `SYRINCS_DB_USER`, `SYRINCS_DB_PASSWORD`
3. Legacy: `HINDEMITH_DB_URL`, `HINDEMITH_DB_USER`,
   `HINDEMITH_DB_PASSWORD`
4. `jdbc:postgresql://localhost:5432/hindemith`, Benutzer `syrincs`, Passwort
   `syrincs`

`syrincs init` legt `hindemithChords` und `huffmanRhythms` an und ergänzt bei
älteren Rhythmustabellen fehlende Spalten wie `rhythmstring` und `deviation`.
`scripts/init-postgres.sh` übernimmt den expliziten lokalen PostgreSQL-Setup
und ruft danach `syrincs init` auf.

Syrincs startet PostgreSQL nicht selbst. `start db` prüft nur die Verbindung;
`start` prüft die DB und startet anschließend den SuperCollider-Consumer, auch
wenn die DB-Prüfung fehlschlägt. `status` liefert nur dann Exit-Code 0, wenn DB
und Consumer erreichbar bzw. aktiv sind.

Bei DB-Änderungen Schema-Initialisierung, Repository-Mapping und Tests zusammen
betrachten. Konfiguration bleibt in `AppConfig` bzw. Runtime/Adaptern und wird
nicht als fester Wert in Domäne oder Use Cases dupliziert.

## Änderungs- und Verifikationspraxis

- Bevorzuge kleine, fokussierte Änderungen und bewahre bestehende APIs, sofern
  die Aufgabe keinen Bruch verlangt.
- Lies bei fachlichen Änderungen zuerst die relevanten Tests; dort sind viele
  Hindemith- und Rhythmusbeispiele kodifiziert.
- Ergänze Tests in derselben Schicht wie das geänderte Verhalten: Domänenlogik
  in Domain-Tests, Orchestrierung in Use-Case-Tests, Parsing/CLI/Adapter in den
  entsprechenden Adaptertests.
- Verwende Maven zur Verifikation. Wenn die vollständige Suite wegen der
  Umgebung nicht möglich ist, führe den engsten sinnvollen Testausschnitt aus
  und dokumentiere die Einschränkung.
- Aktualisiere bei öffentlich sichtbaren Änderungen auch `README.md`,
  Picocli-Hilfe bzw. Completion und gegebenenfalls `supercollider/README.md`.
- Keine generierten Dateien aus `target/`, keine lokalen Samples und keine
  Maschinenkonfiguration als Projektänderung einchecken.

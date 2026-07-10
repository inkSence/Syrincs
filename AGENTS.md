# AGENTS.md

Diese Hinweise gelten für das gesamte Repository.

## Einstieg und Quellen der Wahrheit

- Lies zuerst `README.md` und prüfe danach `git status --short`. Der
  Arbeitsbaum kann absichtlich unfertig sein; vorhandene Änderungen gehören
  dem Benutzer und dürfen weder zurückgesetzt noch überschrieben oder
  ungefragt umformatiert werden.
- Beantworte Verhaltensfragen anhand von Produktionscode und den zugehörigen
  Tests. Für die öffentliche Syntax sind
  `src/main/java/syrincs/c_adapters/cli/RootCmd.java`, die CLI-Tests und die
  tatsächliche Ausgabe von `syrincs --help` maßgeblich.
- `README.md` beschreibt den aktuellen Gesamtstand. `supercollider/README.md`
  ist die Detailreferenz für OSC-Protokoll, Presets, Szenen, Rollen, Effekte
  und Samples.
- Die PDFs unter `doc/` sind fachliche Quellen. `doc/Gruppen.md` dokumentiert
  dagegen eine ältere Zwischenfassung mit 14 Gruppen; der aktuelle Code
  klassifiziert 18 Gruppen.
- `target/`, IDE-Metadaten und lokale Audio-/MIDI-Artefakte sind keine Quellen
  der Wahrheit. Bearbeite oder versioniere sie nicht, sofern es nicht
  ausdrücklich verlangt wird.

## Projekt und Architektur

Syrincs ist eine Java-21-/Maven-CLI für algorithmische Harmonik und Rhythmik:

- Akkorde werden nach Hindemith analysiert, drei- bis fünfstimmig erzeugt, in
  PostgreSQL gespeichert und standardmäßig über SuperCollider/OSC gespielt.
- Rhythmen kommen entweder aus RDL-0-Dateien oder aus `x`/`o`-Onset-Strings.
  Letztere werden mit einem projektinternen Huffman-Maß bewertet und können in
  PostgreSQL gespeichert werden.
- Noten und DB-Akkorde verwenden standardmäßig OSC; RDL- und Huffman-Rhythmus-
  Playback läuft immer über `javax.sound.midi`.
- Picocli stellt die öffentliche CLI bereit. `syrincs.Main` ist der
  Composition Root und verdrahtet je einen OSC- und MIDI-Interactor mit
  gemeinsamen PostgreSQL- und Rhythmus-Playback-Adaptern.

Die Schichten sind:

- `src/main/java/syrincs/a_domain/`: Musikdomäne für Akkorde, Hindemith,
  Rhythmus und Statistik; Scale-/Counterpoint-Entwürfe sind nicht an die CLI
  angebunden.
- `src/main/java/syrincs/b_application/`: Use Cases, Ports, DTOs und
  `AppDefaults`.
- `src/main/java/syrincs/c_adapters/`: Picocli, RDL-Parser, MIDI, OSC,
  PostgreSQL und lokale Runtime.
- `src/main/java/syrincs/d_frameworksAndDrivers/`: Auflösung der
  DB-Konfiguration.
- `src/main/java/syrincs/Main.java`: Verdrahtung und Filterung der globalen
  DB-Argumente.
- `supercollider/`: OSC-Consumer und seine eigenständige Dokumentation.

Halte die Abhängigkeitsrichtung ein: Domäne und Anwendung dürfen keine
Picocli-, JDBC-, OSC- oder `javax.sound.midi`-Typen kennen. Externe Technik
gehört in Adapter; Use Cases sprechen über Ports. Eine bekannte Altlast ist
der ungenutzte Application-Import in
`src/main/java/syrincs/a_domain/Interval.java`; nicht als Muster fortführen
und bei einer ohnehin nötigen Änderung dort entfernen.

Benutzerausgaben gehören in CLI bzw. Adapter. Füge in Domänenobjekten und Use
Cases keine neuen `System.out`-/`System.err`-Ausgaben ein. Der
`HuffmanRhythmTest` prüft dies ausdrücklich für die Rhythmusdomäne.

## Build, Start und Verifikation

Standardbefehle:

```bash
mvn test
mvn exec:java -Dexec.args="--help"
mvn package -Dsyrincs.skipCompletionInstall=true
target/app/bin/syrincs --help
```

Es gibt keinen Maven Wrapper; verwende das installierte `mvn`. JDK 21 ist
durch `maven.compiler.release` vorgegeben. Das einfache JAR enthält seine
Laufzeitabhängigkeiten nicht; das empfohlene Startartefakt liegt nach dem
Paketbau unter `target/app/`.

Ein normales `mvn package` installiert zusätzlich Picocli-Bash-Completion
unter `~/.local/share/bash-completion/completions/syrincs`. Unterdrücke diesen
Home-Verzeichnis-Seiteneffekt bei automatischer Verifikation mit
`-Dsyrincs.skipCompletionInstall=true`, sofern die Completion-Installation
nicht selbst Teil der Aufgabe ist.

Sinnvolle gezielte Testausschnitte:

```bash
# Hindemith-Klassifikation und Erzeugung
mvn -Dtest='ChordAnalysisTest,Series1Test,Series2Test,AnalyseChordByHindemithUseCaseTest,GenerateChordsUseCaseTest' test

# Huffman, RDL, Mapping und MIDI-Sequenz
mvn -Dtest='HuffmanRhythmTest,RhythmTest,RhythmE2ETest,GenerateAndPersistRhythmUseCaseTest,RootCmdRhythmCliTest,SequenceBuilderTest' test

# CLI, Completion und lokale Runtime
mvn -Dtest='RootCmdChordsCliTest,RootCmdCompletionCliTest,RootCmdRhythmCliTest,LocalRuntimeTest' test
```

Umgebungsabhängige Tests:

- `SuperColliderOscOutputAdapterTest` öffnet lokale UDP-Sockets und
  überspringt sich, wenn die Umgebung UDP verbietet.
- `SuperColliderManualSoundEngineTest` ist ein manueller Hörtest und läuft nur
  mit `-DrunScAudioTest=true`.
- Reale MIDI-Sendetests in `SendToMidiUseCaseTest` überspringen sich ohne
  `Roland Digital Piano`/`DP603`.
- Die Unit-Suite startet weder PostgreSQL noch SuperCollider. Reale DB-Abläufe
  brauchen eine erreichbare lokale PostgreSQL-Instanz; PostgreSQL-Repository
  und `AppConfig` haben derzeit keine eigenen Integrationstests.

Fehlendes MIDI, UDP, SuperCollider oder PostgreSQL ist als Umgebungsgrenze zu
melden. Ändere keine fachlichen Defaults und lockere keine Validierung, nur um
lokal grüne Tests zu erhalten.

## CLI-Konventionen

Die sichtbaren Hauptbefehle sind `devices`, `init`, `start`, `status`, `play`,
`calculate` und `analyze`. Zusätzlich existieren die versteckten
Kompatibilitäts-/Wartungsbefehle `list` (Alias für `devices`) und `delete`
(TRUNCATE der Akkordtabelle). Veröffentliche, entferne oder ändere sie nicht
beiläufig.

Korrekte Beispiele:

```bash
mvn exec:java -Dexec.args="analyze chord 60 64 67"
mvn exec:java -Dexec.args='analyze rhythm "xooo xoxo xooo xoxo"'
mvn exec:java -Dexec.args="calculate chords 48 84"
mvn exec:java -Dexec.args="play rhythm --in data/beat.rdl"
mvn exec:java -Dexec.args="play rhythm info 3 5 7"
```

`analyze 60 64 67` und `calculate 48 84` sind unterstützte Kurzformen.
`calculate chord`/`rhythm` sind Aliasse der pluralen Unterbefehle.
`play` ohne Unterbefehl sendet Note 60 für 500 ms über den Standardausgang.

Einige Picocli-Optionen von `play note` und `play chords` sind historisch ohne
führende Bindestriche definiert:

```bash
mvn exec:java -Dexec.args="play note note 60 vel 0.5 dur 500"
mvn exec:java -Dexec.args="play chords numNotes 3 4 groups 1 2 rootNote 60 range 24 duration 200 channel 0"
```

Diese Syntax und ihre Aliasse nicht beiläufig auf reines GNU-Style umstellen.
`duration`/`dur` akzeptiert aus Kompatibilitätsgründen beide Formen mit und
ohne `--`; `--output`, DB-Flags, Rhythmusoptionen und `play sc` verwenden
führende Bindestriche.

Weitere CLI-Regeln:

- `--output` akzeptiert `sc`, `supercollider`, `osc`, `midi`, `jdk-midi` und
  `jdk`; Default ist SuperCollider.
- `play note` und `play chords` messen Dauer in Millisekunden. `play sc` und
  `play sc chord` messen sie in Sekunden.
- DB-Flags funktionieren nur als `--db-url=…`, `--db-user=…` und
  `--db-pass=…`, vor oder nach dem Kommando. `Main.filterDbArgs(...)` entfernt
  sie vor Picocli; Änderungen daran immer mit `AppConfig.loadDbConfig(...)`
  abstimmen.
- Bei öffentlich sichtbaren Änderungen `RootCmd`, CLI-Tests, Root-Hilfe,
  `RootCmdCompletionCliTest`, `README.md` und gegebenenfalls
  `supercollider/README.md` gemeinsam aktualisieren.

Bekannte CLI-Inkonsistenzen nicht unbemerkt „korrigieren“:

- Die Hilfe von `play note` nennt MIDI `0..127`, aber `Tone` akzeptiert
  derzeit nur `21..108`.
- Die Maven-Artefaktversion ist `0.1`, die Picocli-Ausgabe meldet
  `syrincs 1.0`.
- `play chords` lädt standardmäßig nur Gruppen `1..9`, obwohl die Domäne
  Gruppen `1..18` unterstützt.

## Hindemith- und Akkorddomäne

Wichtige Einstiegspunkte:

- `src/main/java/syrincs/a_domain/hindemith/ChordAnalysis.java`
- `src/main/java/syrincs/a_domain/hindemith/ChordSpecification.java`
- `src/main/java/syrincs/a_domain/hindemith/ChordSpecificationRepository.java`
- `src/main/java/syrincs/a_domain/hindemith/ChordRules.java`
- `src/main/java/syrincs/a_domain/hindemith/Series1.java`
- `src/main/java/syrincs/a_domain/hindemith/Series2.java`
- `src/main/java/syrincs/a_domain/chord/NoteCombinator.java`
- `src/main/java/syrincs/b_application/GenerateChordsUseCase.java`
- `src/test/java/syrincs/b_application/AnalyseChordByHindemithUseCaseTest.java`
- `doc/Gruppen.md`

Aktuelles Verhalten:

- `ChordAnalysis.analyze(...)` verlangt mindestens drei Noten, sortiert eine
  Kopie und liefert Spalte A/B, konkrete MIDI-Grundnote, Gruppe `1..18` und
  Rahmenintervall.
- `ChordSpecificationRepository` enthält 18 geordnet geprüfte, unveränderlich
  veröffentlichte Spezifikationen. Ihre Reihenfolge ist fachlich relevant.
- Die umfangreichsten Gruppenbeispiele stehen im Use-Case-Test; bei Änderungen
  an Reihe 1/2, Grundtonwahl, Regeln oder Spezifikationen diese Beispiele
  bewusst prüfen.
- `NoteCombinator` erzeugt streng aufsteigende Kombinationen mit eindeutigen
  Pitch Classes. Die CLI verdrahtet maximal drei Oktaven, also höchstens 36
  Halbtöne Spannweite, und erzeugt Akkorde mit drei, vier und fünf Noten.
- Die Bereichsgrenzen von `calculate chords MIN MAX` sind inklusive. Große
  Bereiche erzeugen alle Kombinationen zunächst im Speicher und können viel
  RAM, Laufzeit und DB-Platz beanspruchen.
- Persistenz nutzt Batch-Inserts und dedupliziert nicht. Wiederholte
  Berechnungen hängen weitere Zeilen an.
- `play chords` filtert nach exakter gespeicherter Grundnote, Gruppenauswahl,
  Stimmenzahl und maximaler Spannweite. Keine Treffer bedeuten einen stillen
  No-op.

Fachliche Änderungen mit Tests unter
`src/test/java/syrincs/a_domain/hindemith/` und
`AnalyseChordByHindemithUseCaseTest` abdecken. Erzeugung und Filterung gehören
zusätzlich in `GenerateChordsUseCaseTest` bzw.
`GetHindemithChordsFromDbUseCaseTest` und die CLI-Tests.

## Huffman-Rhythmik und RDL-0

Wichtige Einstiegspunkte:

- `src/main/java/syrincs/a_domain/rhythm/Rhythm.java`
- `src/main/java/syrincs/a_domain/rhythm/HuffmanRhythm.java`
- `src/main/java/syrincs/a_domain/rhythm/RhythmMapperFromOnsetStringToKickAndSnare.java`
- `src/main/java/syrincs/a_domain/statistics/StandardDeviation.java`
- `src/main/java/syrincs/b_application/GenerateAndPersistRhythmUseCase.java`
- `src/main/java/syrincs/b_application/PlayHuffmanRhythmsUseCase.java`
- `src/main/java/syrincs/b_application/ValidatePatternsUseCase.java`
- `src/main/java/syrincs/c_adapters/RhythmFileParser.java`
- `src/main/java/syrincs/c_adapters/midi/SequenceBuilder.java`

Zwei Formate dürfen nicht vermischt werden:

- Huffman-Onsets: `x` = Einsatz, `o` = kein neuer Einsatz. Whitespace wird
  entfernt, Groß-/Kleinschreibung normalisiert. Der aktuelle Analysepfad hat
  vier Positionen pro Beat; bei 4/4 muss die Länge ein positives Vielfaches
  von 16 sein.
- RDL-0: `x` = Hit, `-` = Pause. `|` und Whitespace werden entfernt;
  Kommentare beginnen mit `#`. Ein vollständiges Beispiel liegt in
  `data/beat.rdl`.

Huffman-Regeln:

- Die Informationsberechnung führt ihren Playing-Zustand über Beatgrenzen
  fort. `Deviation` ist die Populationsstandardabweichung der
  Beat-Informationswerte. Die kodifizierten Erwartungswerte in
  `HuffmanRhythmTest` sind fachliche Verträge.
- `calculate rhythms` erzeugt genau `2^16 = 65.536` Pattern für einen
  4/4-Takt im 16tel-Raster, verwendet 120 BPM und persistiert in Batches von
  1024. Wiederholte Aufrufe deduplizieren nicht.
- `play rhythm info ...` fragt je Grad Kandidaten mit strikt
  `deviation > AppDefaults.MIN_HUFFMAN_RHYTHM_DEVIATION` (`0.7`) ab und wählt
  zufällig einen. Fehlende einzelne Grade werden übersprungen; nur wenn für
  keinen Grad ein Kandidat existiert, schlägt der Aufruf fehl.
- Die DB speichert kein Tempo. Geladene Rhythmen erhalten deshalb
  `AppDefaults.DEFAULT_TEMPO_BPM` (`120`).
- Der Kick-/Snare-Mapper akzeptiert exakt einen 16-Schritt-Takt. Mehrere
  ausgewählte DB-Rhythmen werden erst einzeln gemappt und danach in
  Anfragereihenfolge zu einem MIDI-Pattern verbunden.

RDL-/Playback-Regeln:

- Der Parser kennt `time`, `tempo`, `res-per-beat`, `bars`, `voice` und
  `pattern`; fehlende Headerwerte werden mit 4/4, 120 BPM, vier Schritten pro
  Beat und einem Takt ergänzt.
- Eine Voice benötigt `note`, `channel` und `vel`; `gate` ist optional und
  standardmäßig 50 Prozent.
- Der aktuelle Validator verlangt Voice-Deklarationen für `kick` und `snare`
  und genau diese beiden Pattern, passende Längen, positive Raster-/Taktwerte,
  MIDI-Noten/Velocity `0..127` und Kanäle `0..15`.
- `SequenceBuilder` verwendet PPQ 480, setzt Note-Off anhand des Gate-Anteils
  und fügt standardmäßig zwei Sekunden Nachlaufzeit als End-of-Track-Abstand
  an.
- RDL- und Huffman-Playback laufen immer über MIDI; es gibt keinen
  OSC-Rhythmuspfad.

Änderungen am Informationsmaß in `HuffmanRhythmTest`, Mehrtaktverhalten in
`RhythmTest`, Orchestrierung in Application-Tests, RDL/CLI in
`RootCmdRhythmCliTest` und MIDI-Zeitpunkte in `SequenceBuilderTest` abdecken.

## MIDI, OSC und SuperCollider

MIDI:

- Kanäle sind intern nullbasiert; `channel=9` entspricht General-MIDI-Kanal
  10.
- Die Geräteauflösung verwendet zuerst ein explizites `--device`, danach
  `SYRINCS_MIDI_DEVICE`, dann `Roland Digital Piano`/`DP603` und schließlich
  den ersten verfügbaren MIDI-Ausgang. Explizite Treffer sind
  case-insensitive Teilstring-Suchen über Name, Beschreibung und Hersteller.
- Nur `play rhythm` und `play rhythm info` haben `--device`.
  `play note --output midi` und `play chords --output midi` beginnen bei der
  Umgebungsvariable.
- Halte Hardwarezugriffe hinter den vorhandenen Ports und `SequencePlayer`,
  damit Unit-Tests Fakes verwenden können.

OSC/SuperCollider:

- `SuperColliderOscOutputAdapter` sendet UDP standardmäßig an
  `127.0.0.1:57120`. Ein erfolgreiches Senden bestätigt nicht, dass ein
  Consumer läuft oder Audio erzeugt wurde.
- Java und der Consumer müssen für `/note`, `/chord`, `/drum`, `/fx`, `/set`,
  `/ramp`, `/scene` und `/role` kompatibel bleiben. Bei Protokolländerungen
  Adapter, Pakettests, `RootCmd`, `supercollider/syrincs_osc_consumer.scd` und
  `supercollider/README.md` gemeinsam ändern.
- `syrincs start sc` startet `scripts/start-supercollider-consumer.sh` im
  Vordergrund. `SC_LANG` überschreibt `sclang`, `SC_SCRIPT` den Consumerpfad;
  `Ctrl+C` beendet den Prozess.
- Außerhalb der Repository-Wurzel kann `SYRINCS_HOME` auf das Repository
  zeigen. `LocalRuntime` berücksichtigt außerdem das Appassembler-`app.home`.
- Lokale Samples sind optional und haben synthetische Fallbacks. Audio unter
  `supercollider/samples/` wird durch `.gitignore` ausgeschlossen; nur
  `.gitkeep` wird versioniert.

Klang, Transport, Geräteauswahl und blockierendes Senden nicht in die Domäne
verschieben. Reale Hardware oder ein laufender Consumer dürfen keine
Voraussetzung für normale Unit-Tests werden.

## PostgreSQL und lokale Runtime

`AppConfig.loadDbConfig(...)` löst Werte in dieser Reihenfolge auf:

1. `--db-url=…`, `--db-user=…`, `--db-pass=…`
2. `SYRINCS_DB_URL`, `SYRINCS_DB_USER`, `SYRINCS_DB_PASSWORD`
3. Legacy: `HINDEMITH_DB_URL`, `HINDEMITH_DB_USER`,
   `HINDEMITH_DB_PASSWORD`
4. `jdbc:postgresql://localhost:5432/hindemith`, Benutzer `syrincs`, Passwort
   `syrincs`

Leere Werte gelten als nicht gesetzt. Die bestehende Guardrail verweigert den
DB-Benutzer `philipp`; Konfigurationsänderungen müssen diesen bewussten Schutz
und die CLI-Filterung mitbetrachten.

`syrincs init` legt `hindemithChords` und `huffmanRhythms` idempotent an. Bei
älteren Rhythmustabellen ergänzt es `rhythmstring`, `deviation` und bei Bedarf
eine automatische ID-Erzeugung. Schema, SQL-Mapping und README-Tabelle bei
DB-Änderungen gemeinsam aktualisieren.

Repository-Verhalten beachten:

- Beide Generatoren hängen Datensätze an; es gibt keine Unique Constraints
  oder automatische Deduplizierung.
- Der Akkordadapter gibt bei einigen einfachen Leseabfragen nach SQL-Fehlern
  nur eine Warnung und eine leere Liste zurück, während gefilterte Abfragen
  und der Rhythmusadapter Exceptions werfen. Vereinheitliche dieses Verhalten
  nicht beiläufig, weil es CLI-Fehlerpfade verändert.
- `PostgresRhythmRepository` rekonstruiert Information und Deviation beim
  Laden aus dem Onset-String; das SELECT lädt diese Werte nicht als
  Domänenzustand.

Lokale Runtime:

- Syrincs startet PostgreSQL nicht selbst. `start db` prüft nur die
  bestehende Verbindung.
- `start sc` startet den Consumer im Vordergrund. `start` prüft erst die DB
  und startet SuperCollider auch nach fehlgeschlagener DB-Prüfung.
- `status` liefert nur Exit-Code 0, wenn DB und Consumer erreichbar bzw.
  aktiv sind. Der Consumerstatus ist eine Prozesssuche nach
  `syrincs_osc_consumer.scd`, kein OSC-Healthcheck.
- `scripts/init-postgres.sh` übernimmt explizite privilegierte Einrichtung.
  `SYRINCS_DB_RESET=1` löscht die konfigurierte Datenbank; diesen Opt-in nie
  ohne ausdrücklichen Benutzerauftrag ausführen. `SYRINCS_DB_NAME` und
  `SYRINCS_DB_RESET` gehören nur zum Skript, nicht zur Java-Konfiguration.

## Änderungs- und Dokumentationspraxis

- Bevorzuge kleine, fokussierte Änderungen und bewahre öffentliche APIs und
  historische CLI-Aliasse, sofern die Aufgabe keinen Bruch verlangt.
- Lies vor fachlichen Änderungen die relevanten Tests. Ergänze Tests in der
  Schicht des geänderten Verhaltens: Domain-Regeln in Domain-Tests,
  Orchestrierung in Use-Case-Tests, Parsing/CLI/Adapter in den entsprechenden
  Adaptertests.
- Verifiziere mindestens den engsten sinnvollen Maven-Ausschnitt, bei
  querschnittlichen Änderungen möglichst `mvn test`. Dokumentiere echte
  Umgebungsgrenzen und übersprungene manuelle/Hardwaretests.
- Bei neuen Framework-Abhängigkeiten zuerst einen Application-Port bzw. ein
  frameworkfreies DTO vorsehen; keine JDBC-, Picocli-, OSC- oder MIDI-Typen in
  Domain/Application leaken lassen.
- Änderungen an Defaults zentral in `AppDefaults`, `AppConfig`, Picocli oder
  dem zuständigen Adapter vornehmen, nicht als duplizierte Magic Numbers.
- Öffentlich sichtbare Änderungen in `README.md` nachziehen. SuperCollider-
  Protokoll und Klangengine zusätzlich in `supercollider/README.md`
  dokumentieren; Gruppenänderungen außerdem bewusst mit `doc/Gruppen.md` und
  den fachlichen PDFs abgleichen.
- Keine generierten Dateien aus `target/`, keine lokalen Samples, keine
  Zugangsdaten und keine Maschinen-/IDE-Konfiguration einchecken.

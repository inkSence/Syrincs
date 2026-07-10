# Syrincs

Syrincs ist eine Java-21-CLI für algorithmische Harmonik und Rhythmik. Die
Anwendung analysiert und erzeugt Akkorde nach Paul Hindemith, bewertet binäre
16tel-Rhythmen mit einem projektinternen Huffman-Informationsmaß und spielt
musikalisches Material über SuperCollider/OSC oder `javax.sound.midi` aus.
PostgreSQL speichert erzeugte Akkorde und Rhythmen.

Der aktuelle Schwerpunkt liegt auf vier durchgängigen Wegen:

- Hindemith-Akkorde ohne externe Dienste analysieren;
- drei- bis fünfstimmige Akkorde erzeugen, speichern und abspielen;
- RDL-0-Dateien über MIDI wiedergeben;
- alle 65.536 binären 4/4-Rhythmen bewerten, speichern, auswählen und über
  MIDI spielen.

Scale- und Counterpoint-Modelle sind im Domänenpaket vorhanden, aber noch
nicht an die öffentliche CLI angebunden.

## Schnellstart ohne externe Dienste

Für die fachlichen Analysen werden nur JDK 21 und Maven benötigt:

```bash
mvn test
mvn exec:java -Dexec.args="--help"
mvn exec:java -Dexec.args="analyze chord 60 64 67"
mvn exec:java -Dexec.args='analyze rhythm "xooo xoxo xooo xoxo"'
```

Die beiden Analysen liefern aktuell:

```text
[ANALYZE] Notes=[60, 64, 67] | Column=A_TRITONE_FREE | Root=60 | Group=1 | Frame=7
[ANALYZE] Rhythm=xoooxoxoxoooxoxo | Info=3 | Deviation=0.433013 | Beats=[xooo, xoxo, xooo, xoxo]
```

Externe Komponenten werden erst für Persistenz oder Playback gebraucht:

| Ziel | Zusätzlich erforderlich |
| --- | --- |
| Akkord oder Onset-Rhythmus analysieren | nichts |
| Noten und Akkorde standardmäßig hören | SuperCollider mit `sclang` |
| RDL- oder Huffman-Rhythmen hören | MIDI-Ausgang |
| Akkorde oder Rhythmen erzeugen und speichern | PostgreSQL |
| Gespeicherte Akkorde hören | PostgreSQL plus SuperCollider oder MIDI |
| Gespeicherte Rhythmen hören | PostgreSQL plus MIDI |

## Bauen und starten

### Voraussetzungen

- JDK 21
- Maven
- optional PostgreSQL mit JDBC-Zugriff
- optional SuperCollider mit `sclang`
- optional ein MIDI-Ausgang

Die Laufzeitabhängigkeiten sind Picocli und der PostgreSQL-JDBC-Treiber. Die
Tests verwenden JUnit 5; konkrete Versionen stehen in [`pom.xml`](pom.xml).

Während der Entwicklung können Befehle direkt über Maven ausgeführt werden:

```bash
mvn exec:java -Dexec.args="devices"
mvn exec:java -Dexec.args="analyze chord 60 64 67"
```

Ein Paket ohne Installation der Bash-Completion entsteht mit:

```bash
mvn package -Dsyrincs.skipCompletionInstall=true
target/app/bin/syrincs --help
```

Der Appassembler legt das Startskript und alle Laufzeitabhängigkeiten unter
`target/app/` ab. Das einfache Projekt-JAR enthält seine Abhängigkeiten nicht
und ist daher nicht das empfohlene Startartefakt.

Die folgenden Beispiele verwenden kurz `syrincs`. Im Repository kann dafür
`target/app/bin/syrincs` oder der entsprechende `mvn exec:java`-Aufruf stehen.

### Bash-Completion

Ein normaler Paketbau installiert bzw. aktualisiert die Picocli-Completion
unter `~/.local/share/bash-completion/completions/syrincs`:

```bash
mvn package
exec bash
```

Der Property-Schalter `-Dsyrincs.skipCompletionInstall=true` unterdrückt
diesen Schreibzugriff in das Home-Verzeichnis.

## CLI-Überblick

```text
syrincs
├── devices                    MIDI-Ausgänge anzeigen
├── init                       PostgreSQL-Schema anlegen/migrieren
├── start [all|db|sc]          DB prüfen und/oder SC starten
├── status                     DB- und SC-Prozessstatus anzeigen
├── analyze
│   ├── chord NOTES...         Hindemith-Analyse
│   └── rhythm ONSETS          Huffman-Analyse
├── calculate
│   ├── chords MIN MAX         Akkorde erzeugen und speichern
│   └── rhythms                alle 2^16 Onset-Pattern speichern
└── play
    ├── note                   einzelne Note; Default: OSC
    ├── chords                 DB-Akkorde; Default: OSC
    ├── rhythm [--in FILE]     RDL-0 über MIDI
    │   └── info GRADES...     DB-Rhythmen über MIDI
    └── sc                     direkte Preset-/OSC-Steuerung
        ├── chord
        ├── drum
        ├── fx
        ├── set
        ├── ramp
        ├── scene
        ├── role
        ├── scene-demo
        └── demo
```

Die verbindliche Syntax liefert Picocli:

```bash
syrincs --help
syrincs play chords --help
syrincs play rhythm --help
syrincs play sc --help
```

`analyze 60 64 67` und `calculate 48 84` sind unterstützte Kurzformen für
die jeweiligen Akkordbefehle. `play` ohne Unterbefehl sendet die Standardnote
60 für 500 ms über den Standardausgang.

### Wichtige CLI-Konventionen

Einige Optionen von `play note` und `play chords` sind historisch ohne
führende Bindestriche definiert. Diese Syntax ist beabsichtigt:

```bash
syrincs play note note 60 vel 0.5 dur 500
syrincs play chords numNotes 3 4 groups 1 2 rootNote 60 range 24 duration 200 channel 0
```

`--output`, DB-Flags, Rhythmusoptionen und `play sc` verwenden dagegen
führende Bindestriche. Zeitangaben haben zwei verschiedene Einheiten:

| Befehl | Dauer |
| --- | --- |
| `play note`, `play chords` | Millisekunden |
| `play sc`, `play sc chord` | Sekunden |

`--output` akzeptiert `sc`, `supercollider` oder `osc` sowie `midi`,
`jdk-midi` oder `jdk`. Ohne Angabe wird SuperCollider gewählt.

## Hindemith-Akkorde

### Analysieren

```bash
syrincs analyze chord 60 64 67
syrincs analyze 60 64 67
```

Die Eingaben werden sortiert. Das Ergebnis enthält:

- `Column`: `A_TRITONE_FREE` oder `B_WITH_TRITONE`;
- `Root`: Grundton als konkrete MIDI-Note;
- `Group`: Hindemith-Gruppe `1..18`;
- `Frame`: Abstand zwischen tiefster und höchster Note in Halbtönen.

Die Analyse verlangt mindestens drei Noten. Die Gruppenzuordnung basiert auf
Reihe 2, der Grundtonlogik und 18 geordnet geprüften Spezifikationen in
`ChordSpecificationRepository`.

Beispiele aus den Tests:

```text
[60, 64, 67] -> Spalte A, Grundton 60, Gruppe 1
[60, 63, 68] -> Spalte A, Grundton 68, Gruppe 2
[60, 64, 70] -> Spalte B, Grundton 60, Gruppe 3
[60, 66, 69] -> Spalte B, Grundton 66, Gruppe 18
```

[`doc/Gruppen.md`](doc/Gruppen.md) beschreibt eine ältere Zwischenfassung mit
14 Gruppen. Für das aktuelle Verhalten sind Produktionscode und Tests mit 18
Gruppen maßgeblich.

### Erzeugen und speichern

```bash
syrincs calculate chords 48 84
syrincs calculate 48 84
```

Die Grenzen sind inklusive. Erzeugt werden Kombinationen mit drei, vier und
fünf verschiedenen Pitch Classes. Ihre maximale Spannweite ist auf drei
Oktaven begrenzt; anschließend werden sie analysiert und gesammelt in
PostgreSQL geschrieben.

Große Notenbereiche können entsprechend viel Arbeitsspeicher, Rechenzeit und
Datenbankplatz benötigen. Wiederholte Aufrufe hängen neue Zeilen an und
entfernen keine Duplikate.

### Gespeicherte Akkorde spielen

```bash
# Standard: SuperCollider/OSC
syrincs play chords numNotes 3 4 groups 1 2 rootNote 60 range 24 duration 200

# Explizit über MIDI, Kanal nullbasiert
syrincs play chords numNotes 3 groups 1 2 rootNote 60 channel 0 --output midi
```

Ohne Filterangaben gelten diese Defaults:

| Filter | Default | Bedeutung |
| --- | --- | --- |
| `numNotes` | `3 4 5` | erlaubte Akkordgrößen |
| `groups` | `1..9` | standardmäßig geladene Gruppen |
| `rootNote` | `60` | exakte gespeicherte Grundnote |
| `range` | `24` | maximale Spannweite in Halbtönen |
| `duration` | `200` | Dauer in Millisekunden |
| `channel` | `0` | nur bei MIDI verwendet |

Die Domäne unterstützt Gruppen `1..18`; Gruppen `10..18` müssen beim
Playback derzeit explizit gewählt werden. Liefert die Datenbank keine Treffer,
endet der Befehl ohne Playback.

## Huffman-Rhythmik

### Onset-Format und Analyse

```bash
syrincs analyze rhythm "xooo xoxo xooo xoxo"
```

Im einfachen Onset-Format bedeutet `x` Einsatz und `o` kein neuer Einsatz.
Groß-/Kleinschreibung spielt keine Rolle, Whitespace wird entfernt. Der
Analysepfad verwendet 4/4, 120 BPM und vier Positionen pro Beat. Die
normalisierte Länge muss deshalb ein positives Vielfaches von 16 sein.

`Info` ist die Anzahl der Codesymbole, welche die interne Zustandsmaschine
erzeugt. Ihr Spielzustand wird über Beatgrenzen fortgeführt. `Deviation` ist
die Populationsstandardabweichung der Informationswerte der einzelnen Beats.

Kodifizierte Beispiele:

```text
xooo xooo xooo xooo -> Information 1
xooo oooo oooo oooo -> Information 2
xoxo xoxo xoxo xoxo -> Information 5
xxxx xxxx xxxx xxxx -> Information 9
xxox xxox xxox xxox -> Information 17
```

### Alle Pattern erzeugen

```bash
syrincs calculate rhythms
```

Der Befehl erzeugt alle `2^16 = 65.536` binären Pattern eines 4/4-Takts im
16tel-Raster, berechnet Information und Deviation und speichert sie mit
Batch-Inserts. Auch hier hängen Wiederholungen weitere Zeilen an.

### Nach Informationsgrad spielen

```bash
syrincs play rhythm info 3 5 7
syrincs play rhythm info --device "Virtual Out" 3 5 7
```

Für jeden Informationsgrad lädt Syrincs Kandidaten mit `deviation > 0.7` und
wählt zufällig einen aus. Grade ohne Kandidaten werden übersprungen; nur wenn
für keinen angefragten Grad ein Kandidat existiert, endet der Befehl mit
Fehler und einem Hinweis auf `init` und `calculate rhythms`.

Die ausgewählten Takte werden in der angefragten Reihenfolge zu einem
durchgehenden MIDI-Pattern verbunden. Eine regelbasierte Gewichtung verteilt
jeden Onset auf Kick oder Snare: Backbeats bevorzugen die Snare, Downbeats und
Antizipationen eher die Kick. Gespeicherte Rhythmen enthalten kein Tempo;
DB-Playback verwendet daher aktuell 120 BPM.

## RDL-0

[`data/beat.rdl`](data/beat.rdl) ist ein vollständiges Beispiel:

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

Abspielen:

```bash
syrincs play rhythm
syrincs play rhythm --in data/beat.rdl
syrincs play rhythm --in data/beat.rdl --device DP603
```

Ohne `--in` wird `data/beat.rdl` verwendet. Für RDL-0 gelten folgende Regeln:

- Kommentare beginnen mit `#`.
- Der Header kennt `time`, `tempo`, `res-per-beat` und `bars`.
- Fehlende Headerwerte werden zu `4/4`, 120 BPM, vier Schritten pro Beat und
  einem Takt ergänzt.
- Eine Voice benötigt `note`, `channel` und `vel`; `gate` ist optional und
  standardmäßig 50 Prozent.
- `note` und `vel` liegen in `0..127`, `channel` in `0..15`.
- Im Pattern steht `x` für Hit und `-` für Pause; `|` und Whitespace werden
  ignoriert.
- Die aktuelle Validierung verlangt genau `kick` und `snare` sowie für beide
  eine zum Header passende Patternlänge.

RDL-0 (`x`/`-`) und das Huffman-Onset-Format (`x`/`o`) sind zwei verschiedene
Formate. RDL- und Huffman-Playback laufen immer über MIDI. Der erzeugten
MIDI-Sequenz werden standardmäßig zwei Sekunden Nachlauf angefügt, damit
Synth- und Effektfahnen ausklingen können.

## MIDI

Verfügbare Ausgänge zeigt:

```bash
syrincs devices
```

Die Auswahl eines MIDI-Ausgangs erfolgt in dieser Reihenfolge:

1. `--device` bei `play rhythm` oder `play rhythm info`;
2. `SYRINCS_MIDI_DEVICE`;
3. ein Ausgang mit `Roland Digital Piano` oder `DP603` im Namen;
4. der erste verfügbare MIDI-Ausgang.

Explizite Angaben sind case-insensitive Teilstring-Suchen über Name,
Beschreibung und Hersteller. `play note --output midi` und
`play chords --output midi` besitzen derzeit keine eigene `--device`-Option;
für sie beginnt die Auswahl bei `SYRINCS_MIDI_DEVICE`.

MIDI-Kanäle sind in RDL und `play chords` nullbasiert. `channel=9` entspricht
dem General-MIDI-Kanal 10.

## SuperCollider und OSC

Terminal 1:

```bash
syrincs start sc
```

Terminal 2:

```bash
syrincs play note note 60 vel 0.5 dur 500
syrincs play sc chord 60 64 67 --preset organ.full --duration 1.5
syrincs play sc drum drum.kick
syrincs play sc scene scene.chorale
syrincs play sc demo
```

Der Consumer läuft im Vordergrund und wird mit `Ctrl+C` beendet. Alternativ:

```bash
bash scripts/start-supercollider-consumer.sh
```

Das Startskript verwendet `SC_LANG` (Default `sclang`) und optional
`SC_SCRIPT`. Der Java-Adapter sendet standardmäßig an `127.0.0.1:57120`.
`play sc` kann Ziel, Preset, Pan, Velocity und Dauer mit `--host`, `--port`,
`--preset`, `--pan`, `--velocity` und `--duration` überschreiben.

OSC wird verbindungslos per UDP gesendet. Eine erfolgreiche CLI-Ausgabe
bestätigt daher das Senden des Datagramms, nicht dessen Wiedergabe durch einen
laufenden Consumer.

Der Consumer unterstützt tonale und Drum-Presets, globale Reverb-/Delay-/
Chorus-Effekte, Parameterautomation, Szenen, Rollen und optionale lokale
Samples mit synthetischen Fallbacks. Protokoll, Presetlisten, Samplepfade und
Hörtests stehen in der
[`SuperCollider-Dokumentation`](supercollider/README.md).

## PostgreSQL

### Konfiguration

Die lokalen Defaults sind:

```text
URL:      jdbc:postgresql://localhost:5432/hindemith
User:     syrincs
Passwort: syrincs
```

Die Auflösung erfolgt in dieser Reihenfolge:

1. `--db-url=…`, `--db-user=…`, `--db-pass=…`
2. `SYRINCS_DB_URL`, `SYRINCS_DB_USER`, `SYRINCS_DB_PASSWORD`
3. `HINDEMITH_DB_URL`, `HINDEMITH_DB_USER`, `HINDEMITH_DB_PASSWORD`
4. lokale Defaults

Die CLI-Flags verwenden zwingend die Form mit `=` und können vor oder nach
dem Kommando stehen:

```bash
syrincs --db-url=jdbc:postgresql://localhost:5432/hindemith init
syrincs status --db-user=syrincs --db-pass=syrincs
```

### Lokale Einrichtung

Syrincs startet PostgreSQL nicht selbst. Auf einem System mit `sudo` und den
PostgreSQL-Werkzeugen kann das mitgelieferte Skript Rolle, Datenbank und Schema
einrichten:

```bash
sudo systemctl enable --now postgresql
bash scripts/init-postgres.sh
```

Das Skript kennt zusätzlich `SYRINCS_DB_NAME` und den destruktiven Opt-in
`SYRINCS_DB_RESET=1`. Diese beiden Variablen gehören nur zum Setup-Skript;
die Java-Anwendung verwendet für die Verbindung `SYRINCS_DB_URL`.

Für eine bereits vorhandene Datenbank genügt:

```bash
export SYRINCS_DB_URL='jdbc:postgresql://localhost:5432/hindemith'
export SYRINCS_DB_USER='syrincs'
export SYRINCS_DB_PASSWORD='syrincs'
syrincs init
```

### Schema

`syrincs init` arbeitet idempotent und legt zwei Tabellen an:

| Tabelle | Inhalt | Spalten |
| --- | --- | --- |
| `hindemithChords` | analysierte Akkorde | `id`, `notes`, `numNotes`, `minNote`, `maxNote`, `rootNote`, `chordGroup` |
| `huffmanRhythms` | Onsets und Bewertung | `id`, `rhythmstring`, `numerator`, `denominator`, `info`, `deviation` |

Bei älteren Rhythmustabellen ergänzt die Migration `rhythmstring`,
`deviation` und bei Bedarf eine automatische ID-Erzeugung. Sie löscht keine
Anwendungsdaten.

## Lokale Runtime

```bash
syrincs status
syrincs start db
syrincs start sc
syrincs start
```

- `status` prüft die DB-Verbindung und sucht nach einem laufenden Prozess mit
  `syrincs_osc_consumer.scd`; Exit-Code 0 gibt es nur, wenn beides aktiv ist.
- `start db` prüft ausschließlich die bestehende PostgreSQL-Verbindung.
- `start sc` startet den SuperCollider-Consumer im Vordergrund.
- `start` prüft zunächst die DB und startet SuperCollider auch dann, wenn die
  DB-Prüfung fehlschlägt.

Wird Syrincs außerhalb der Repository-Wurzel ausgeführt, kann `SYRINCS_HOME`
auf das Repository zeigen, damit das Startskript und der Consumer gefunden
werden.

## Architektur

Das Projekt ist grob nach Clean Architecture gegliedert. Fachliche Abläufe
liegen in Domäne und Anwendung; Picocli, JDBC, OSC und `javax.sound.midi`
werden in äußeren Adaptern verdrahtet.

```text
Picocli / Runtime ──> Use Cases ──> Domänenmodelle
                            |
                           Ports
                            ^
                            |
              PostgreSQL / MIDI / OSC
```

```text
src/main/java/syrincs/
  a_domain/                Akkorde, Hindemith, Rhythmus, Statistik,
                           noch unverdrahtete Scale-/Counterpoint-Modelle
  b_application/           Use Cases, Ports und Anwendungsdefaults
  c_adapters/              CLI, RDL, MIDI, OSC, PostgreSQL und Runtime
  d_frameworksAndDrivers/  Konfigurationsauflösung
  Main.java                Composition Root

src/test/java/syrincs/     Tests entlang der Produktionspakete
data/                      RDL-Beispiel
doc/                       fachliche Quellen und ältere Notizen
scripts/                   PostgreSQL- und SuperCollider-Startskripte
supercollider/             OSC-Consumer, Presets, Szenen, FX und Dokumentation
```

Wichtige Einstiegspunkte:

| Frage | Code |
| --- | --- |
| Wie wird die Anwendung verdrahtet? | `syrincs.Main` |
| Welche CLI gibt es? | `c_adapters.cli.RootCmd` |
| Wie wird ein Akkord klassifiziert? | `a_domain.hindemith.ChordAnalysis` |
| Wo stehen die 18 Gruppenkriterien? | `a_domain.hindemith.ChordSpecificationRepository` |
| Wie wird Rhythmusinformation berechnet? | `a_domain.rhythm.HuffmanRhythm` |
| Wie werden Onsets auf Kick/Snare verteilt? | `a_domain.rhythm.RhythmMapperFromOnsetStringToKickAndSnare` |
| Wie wird RDL-0 gelesen? | `c_adapters.RhythmFileParser` |
| Wie wird MIDI gebaut und gespielt? | `c_adapters.midi.SequenceBuilder`, `RhythmPlaybackService` |
| Wie wird OSC gesendet? | `c_adapters.osc.SuperColliderOscOutputAdapter` |
| Wie wird persistiert? | `c_adapters.postgres` |
| Wie werden lokale Dienste geprüft? | `c_adapters.runtime.LocalRuntime` |

## Tests

Die vollständige Suite:

```bash
mvn test
```

Ein schneller fachlicher Ausschnitt:

```bash
mvn -Dtest='HuffmanRhythmTest,RhythmTest,RootCmdChordsCliTest,LocalRuntimeTest' test
```

Umgebungsabhängige Tests verhalten sich bewusst defensiv:

- UDP-basierte OSC-Tests überspringen sich, wenn kein lokaler UDP-Port geöffnet
  werden darf.
- Reale MIDI-Sendetests überspringen sich ohne `Roland Digital Piano` oder
  `DP603`.
- `SuperColliderManualSoundEngineTest` läuft nur mit
  `-DrunScAudioTest=true`.
- Datenbankgestützte CLI-Abläufe erfordern eine erreichbare lokale
  PostgreSQL-Instanz; die aktuelle Unit-Suite ersetzt diese nicht durch einen
  eingebetteten PostgreSQL-Server.

Fehlendes MIDI, UDP, SuperCollider oder PostgreSQL ist eine Umgebungsgrenze und
kein Grund, fachliche Defaults oder Validierung zu lockern.

## Bekannte Grenzen und Inkonsistenzen

- Huffman-Generierung und DB-Playback sind auf einen 4/4-Takt im
  16tel-Raster und 120 BPM spezialisiert.
- Rhythmus-Playback nutzt immer MIDI; ein OSC-Rhythmuspfad ist nicht
  implementiert.
- `calculate rhythms` erzeugt immer alle 65.536 Pattern und dedupliziert die
  Tabelle nicht.
- Die öffentliche `play chords`-Voreinstellung lädt Gruppen `1..9`, obwohl die
  Domäne Gruppen `1..18` klassifiziert.
- `play note` nennt in der Picocli-Hilfe den MIDI-Bereich `0..127`; das
  verwendete `Tone`-Domänenobjekt akzeptiert aktuell tatsächlich `21..108`.
- Maven-Artefaktversion (`0.1`) und Picocli-Versionsausgabe (`syrincs 1.0`)
  sind noch nicht vereinheitlicht.
- `doc/Gruppen.md` beschreibt 14 Gruppen und ist älter als die aktuelle
  18-Gruppen-Implementierung.
- Scale und Counterpoint sind Domänenentwürfe ohne CLI-Use-Case.
- SuperCollider bietet eine synthetische Preset-Engine und optionale lokale
  Samples, aber keine mitgelieferte Sample-Library, Plugin-Bridge, GUI oder
  DAW-artige Automation.

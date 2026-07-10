# Syrincs

Syrincs ist eine Java-21-Kommandozeilenanwendung für algorithmische Harmonik
und Rhythmik. Sie analysiert und erzeugt Akkorde nach Paul Hindemith, bewertet
16tel-Rhythmen mit einem projektinternen Huffman-Informationsmaß und gibt
musikalisches Material über SuperCollider/OSC oder MIDI aus. PostgreSQL dient
als Speicher für erzeugte Akkorde und Rhythmen.

## In fünf Minuten zum ersten Ergebnis

Für die ersten Analysen werden nur ein JDK 21 und Maven benötigt. PostgreSQL,
SuperCollider und ein MIDI-Gerät sind dafür nicht nötig.

```bash
mvn test
mvn exec:java -Dexec.args="--help"
mvn exec:java -Dexec.args="analyze chord 60 64 67"
mvn exec:java -Dexec.args='analyze rhythm "xooo xoxo xooo xoxo"'
```

Die beiden Analysen liefern:

```text
[ANALYZE] Notes=[60, 64, 67] | Column=A_TRITONE_FREE | Root=60 | Group=1 | Frame=7
[ANALYZE] Rhythm=xoooxoxoxoooxoxo | Info=3 | Deviation=0.433013 | Beats=[xooo, xoxo, xooo, xoxo]
```

Damit läuft bereits die fachliche Kernlogik. Externe Komponenten werden erst
für folgende Wege gebraucht:

| Ziel | Zusätzlich erforderlich | Einstieg |
| --- | --- | --- |
| Akkorde und Rhythmen analysieren | nichts | `analyze …` |
| Klänge über OSC hören | SuperCollider und `sclang` | `start sc`, dann `play note …` |
| RDL-Rhythmen hören | MIDI-Ausgang | `devices`, dann `play rhythm …` |
| Material erzeugen und speichern | PostgreSQL | `init`, dann `calculate …` |
| Gespeichertes Material spielen | PostgreSQL plus SC oder MIDI | `play chords …` / `play rhythm info …` |

## Funktionsüberblick

Syrincs kann:

- Akkorde nach Hindemith analysieren und dabei Spalte A/B, Grundton, Gruppe
  und Rahmenintervall bestimmen;
- drei- bis fünfstimmige Akkorde in einem MIDI-Notenbereich erzeugen und in
  PostgreSQL speichern;
- Einzelnoten und gespeicherte Akkorde standardmäßig per OSC an
  SuperCollider senden, alternativ explizit über MIDI;
- Rhythmusdateien im kleinen RDL-0-Format lesen und mit dem JDK-MIDI-Sequencer
  wiedergeben;
- 4/4-Onset-Strings im 16tel-Raster analysieren, alle `2^16` Kombinationen
  erzeugen und nach Informationsgrad aus PostgreSQL auswählen und spielen.

## Bauen und ausführen

### Voraussetzungen

| Funktion | Voraussetzung |
| --- | --- |
| Build, Tests und Analyse | JDK 21, Maven |
| Persistenz und DB-Playback | PostgreSQL mit JDBC-Zugriff |
| Standardausgabe von Noten und Akkorden | SuperCollider mit `sclang` |
| Rhythmus- oder explizites MIDI-Playback | verfügbares MIDI-Out |

Die Laufzeitabhängigkeiten sind Picocli und der PostgreSQL-JDBC-Treiber; die
Tests verwenden JUnit 5. Die genauen Versionen stehen in [`pom.xml`](pom.xml).

### Entwicklungsstart

Beliebige Befehle lassen sich direkt über Maven ausführen:

```bash
mvn exec:java -Dexec.args="devices"
mvn exec:java -Dexec.args="analyze chord 60 64 67"
```

### Paket bauen

```bash
mvn package
target/app/bin/syrincs --help
```

`mvn package` erzeugt mit dem Appassembler ein Startskript samt
Laufzeitabhängigkeiten unter `target/app/`. Das einfache Projekt-JAR allein
enthält diese Abhängigkeiten nicht.

Die folgenden Beispiele verwenden kurz `syrincs`. Im Repository kann dafür
immer `target/app/bin/syrincs` oder der entsprechende `mvn exec:java`-Aufruf
eingesetzt werden.

### Bash-Completion

Der Paketbau installiert bzw. aktualisiert standardmäßig die Completion unter
`~/.local/share/bash-completion/completions/syrincs`. Eine neue Shell oder
`exec bash` lädt sie. Ohne diesen Seiteneffekt wird so gebaut:

```bash
mvn package -Dsyrincs.skipCompletionInstall=true
```

## Befehlslandkarte

```text
syrincs
├── analyze chord …       Hindemith-Analyse ohne Datenbank
├── analyze rhythm …      Huffman-Analyse ohne Datenbank
├── devices               MIDI-Ausgänge anzeigen
├── init                  PostgreSQL-Schema anlegen/aktualisieren
├── calculate chords …    Akkorde erzeugen und speichern
├── calculate rhythms     alle 65.536 Onset-Pattern speichern
├── play note …           einzelne Note, standardmäßig über OSC
├── play chords …         gespeicherte Akkorde, standardmäßig über OSC
├── play rhythm …         RDL-0-Datei über MIDI
├── play rhythm info …    gespeicherte Rhythmen über MIDI
├── play sc …             erweiterte SuperCollider-/OSC-Befehle
├── start [all|db|sc]     lokale Abhängigkeiten prüfen/starten
└── status                PostgreSQL- und SC-Status anzeigen
```

Die jeweils gültigen Argumente zeigt Picocli an, zum Beispiel mit
`syrincs play chords --help`. Der Root-Aufruf `syrincs --help` ergänzt die
Hilfe für die wichtigsten `play`-Unterbefehle.

## Typische Arbeitsabläufe

### 1. Einen Akkord analysieren

```bash
syrincs analyze chord 60 64 67
```

`syrincs analyze 60 64 67` ist eine unterstützte Kurzform. Die Ausgabe
enthält:

- `Column`: `A_TRITONE_FREE` oder `B_WITH_TRITONE`
- `Root`: ermittelter Grundton als MIDI-Note
- `Group`: aktuelle Hindemith-Gruppe `1..18`
- `Frame`: Abstand zwischen tiefster und höchster Note

### 2. Einen Onset-Rhythmus analysieren

```bash
syrincs analyze rhythm "xooo xoxo xooo xoxo"
```

Hier steht `x` für einen Einsatz und `o` für keinen neuen Einsatz. Whitespace
wird ignoriert. Der aktuelle Analysepfad erwartet ein 4/4-Metrum mit vier
Positionen pro Beat; die Länge muss daher ein positives Vielfaches von 16 sein.

`Info` ist die Summe der von der internen Zustandsmaschine erzeugten
Codesymbole. `Deviation` ist die Standardabweichung der einzelnen
Beat-Informationswerte und beschreibt damit deren Ungleichverteilung.

### 3. SuperCollider-Ausgabe ausprobieren

Terminal 1:

```bash
syrincs start sc
```

Terminal 2:

```bash
syrincs play note note 60 vel 0.5 dur 500
syrincs play sc chord 60 64 67 --preset organ.full
syrincs play sc drum drum.kick
```

Der Consumer läuft im Vordergrund und lauscht standardmäßig auf UDP-Port
`57120`. Presets, Szenen, Rollen, Effekte, Samples und OSC-Automation sind in
der [`SuperCollider-Dokumentation`](supercollider/README.md) beschrieben.

Alternativ lässt sich der Consumer ohne Java-CLI starten:

```bash
bash scripts/start-supercollider-consumer.sh
```

### 4. MIDI-Ausgabe verwenden

```bash
syrincs devices
syrincs play note note 60 --output midi
syrincs play chords numNotes 3 4 groups 1 2 rootNote 60 range 24 duration 200 channel 0 --output midi
```

Einige Optionen von `play note` und `play chords` sind historisch ohne
führende Bindestriche definiert. Die gezeigte Syntax ist beabsichtigt.
MIDI-Kanäle sind intern nullbasiert; `channel=9` entspricht dem
General-MIDI-Kanal 10.

Ohne explizites Gerät wird in dieser Reihenfolge gewählt:

1. der Wert von `SYRINCS_MIDI_DEVICE`,
2. ein MIDI-Out mit `Roland Digital Piano` oder `DP603` im Namen,
3. der erste verfügbare MIDI-Ausgang.

### 5. Eine RDL-0-Datei abspielen

```bash
syrincs play rhythm --in data/beat.rdl
syrincs play rhythm --in data/beat.rdl --device DP603
```

Rhythmus-Playback läuft unabhängig vom Standardausgang für Noten und Akkorde
immer über `javax.sound.midi`.

## PostgreSQL einrichten

Die lokalen Defaults sind:

```text
URL:      jdbc:postgresql://localhost:5432/hindemith
User:     syrincs
Passwort: syrincs
```

Unter einem System mit `sudo` und PostgreSQL-Werkzeugen richtet das
mitgelieferte Skript Rolle, Datenbank und Anwendungsschema ein:

```bash
sudo systemctl enable --now postgresql
bash scripts/init-postgres.sh
```

Für eine vorhandene Datenbank genügt:

```bash
export SYRINCS_DB_URL='jdbc:postgresql://localhost:5432/hindemith'
export SYRINCS_DB_USER='syrincs'
export SYRINCS_DB_PASSWORD='syrincs'
syrincs init
```

Die Konfiguration wird in dieser Reihenfolge aufgelöst:

1. CLI-Argumente `--db-url=…`, `--db-user=…`, `--db-pass=…`
2. `SYRINCS_DB_URL`, `SYRINCS_DB_USER`, `SYRINCS_DB_PASSWORD`
3. die Legacy-Variablen `HINDEMITH_DB_URL`, `HINDEMITH_DB_USER`,
   `HINDEMITH_DB_PASSWORD`
4. die lokalen Defaults

DB-Argumente dürfen vor oder nach dem eigentlichen Kommando stehen:

```bash
syrincs --db-url=jdbc:postgresql://localhost:5432/hindemith init
```

`syrincs init` legt diese Anwendungstabellen idempotent an bzw. ergänzt
fehlende Rhythmusspalten:

| Tabelle | Inhalt | Wichtige Spalten |
| --- | --- | --- |
| `hindemithChords` | analysierte Akkorde | `notes`, `numNotes`, `minNote`, `maxNote`, `rootNote`, `chordGroup` |
| `huffmanRhythms` | Onset-Strings und Bewertung | `rhythmstring`, `numerator`, `denominator`, `info`, `deviation` |

Syrincs startet PostgreSQL nicht selbst. Es erwartet die Datenbank als
Systemdienst und prüft lediglich deren Erreichbarkeit.

### Daten erzeugen und abspielen

```bash
# Drei- bis fünfstimmige Akkorde zwischen den MIDI-Grenzen speichern
syrincs calculate chords 48 84

# Unterstützte Kurzform
syrincs calculate 48 84

# Gespeicherte Akkorde filtern und spielen (Default: SuperCollider)
syrincs play chords numNotes 3 4 groups 1 2 rootNote 60 range 24

# Alle 65.536 binären 16tel-Pattern speichern
syrincs calculate rhythms

# Je Informationsgrad einen zufälligen Rhythmus per MIDI spielen
syrincs play rhythm info 3 5 7
```

Beim Rhythmus-Playback werden nur Kandidaten mit `deviation > 0.7`
berücksichtigt. Fehlen Tabelle oder Spalten, hilft `syrincs init`; ist die
Tabelle leer, muss zuerst `syrincs calculate rhythms` laufen.

Wiederholte `calculate`-Aufrufe hängen weitere Datensätze an; sie ersetzen
vorhandene Daten nicht.

## Lokale Runtime

```bash
syrincs status    # PostgreSQL und OSC-Consumer prüfen
syrincs start db  # nur DB-Verbindung prüfen
syrincs start sc  # nur SuperCollider im Vordergrund starten
syrincs start     # DB prüfen, dann SuperCollider starten
```

`start` und `start sc` laufen bis `Ctrl+C` im Vordergrund. Wird Syrincs nicht
aus dem Repository gestartet, kann `SYRINCS_HOME` auf dessen Wurzel zeigen,
damit das SuperCollider-Startskript gefunden wird.

## RDL-0-Format

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

- Kommentare beginnen mit `#`.
- `time`, `tempo`, `res-per-beat` und `bars` bilden den Header. Fehlende Werte
  fallen auf `4/4`, 120 BPM, vier Schritte pro Beat und einen Takt zurück.
- Eine Voice benötigt `note` (`0..127`), `channel` (`0..15`) und `vel`
  (`0..127`); `gate` ist optional und standardmäßig `50` Prozent.
- Im Pattern bedeutet `x` Hit und `-` Pause; `|` und Whitespace werden
  ignoriert.
- Die aktuelle Validierung verlangt genau die Stimmen `kick` und `snare` und
  eine zum Header passende Patternlänge.

Das RDL-Zeichenpaar `x`/`-` unterscheidet sich bewusst vom Huffman-Onset-Format
`x`/`o`.

## Architektur und Code-Einstieg

Das Projekt folgt grob Clean Architecture. Die Domäne kennt weder Picocli,
JDBC, OSC noch `javax.sound.midi`; äußere Adapter implementieren Ports der
Anwendungsschicht.

```text
CLI / Runtime ──> Use Cases ──> Hindemith-, Akkord- und Rhythmusdomäne
                        |
                     Ports
                        ^
                        |
             PostgreSQL-, MIDI- und OSC-Adapter
```

```text
src/main/java/syrincs/
  a_domain/                Akkorde, Hindemith, Rhythmus, Statistik
  b_application/           Use Cases, Ports und Anwendungsdefaults
  c_adapters/              CLI, RDL, MIDI, OSC, PostgreSQL und Runtime
  d_frameworksAndDrivers/  Konfigurationsauflösung
  Main.java                Composition Root

src/test/java/syrincs/     Tests spiegeln die Produktionspakete
data/                      RDL-Beispieldaten
doc/                       fachliche Quellen und Projektnotizen
scripts/                   PostgreSQL- und SuperCollider-Startskripte
supercollider/              OSC-Consumer und eigene Dokumentation
```

Gute Einstiegspunkte für einen Code-Rundgang:

| Frage | Einstiegspunkt |
| --- | --- |
| Wie wird alles verdrahtet? | `syrincs.Main` |
| Welche CLI-Befehle gibt es? | `c_adapters.cli.RootCmd` |
| Wie wird ein Akkord klassifiziert? | `a_domain.hindemith.ChordAnalysis` |
| Wo stehen die Gruppenkriterien? | `a_domain.hindemith.ChordSpecificationRepository` |
| Wie entsteht das Rhythmus-Informationsmaß? | `a_domain.rhythm.HuffmanRhythm` |
| Wie werden Onsets auf Kick/Snare verteilt? | `a_domain.rhythm.RhythmMapperFromOnsetStringToKickAndSnare` |
| Wie wird RDL gelesen? | `c_adapters.RhythmFileParser` |
| Wie werden Daten gespeichert? | `c_adapters.postgres` |
| Wie werden MIDI-Sequenzen gebaut? | `c_adapters.midi.SequenceBuilder` |
| Wie werden lokale Dienste geprüft? | `c_adapters.runtime.LocalRuntime` |

## Fachlicher Implementierungsstand

### Hindemith-Gruppen

`ChordAnalysis` sortiert die Noten, ermittelt Spalte und Grundton über die
beiden Hindemith-Intervallreihen und ordnet anschließend eine
Gruppenspezifikation zu. `ChordSpecificationRepository` enthält derzeit 18
Spezifikationen und liefert die Gruppen `1..18`.

[`doc/Gruppen.md`](doc/Gruppen.md) beschreibt eine ältere konzeptionelle
Zwischenfassung mit 14 Gruppen. Für das aktuelle Verhalten sind daher Code
und Tests maßgeblich.

Beispiele aus den Tests:

```text
[60, 64, 67] -> Spalte A, Grundton 60, Gruppe 1
[60, 63, 68] -> Spalte A, Grundton 68, Gruppe 2
[60, 64, 70] -> Spalte B, Grundton 60, Gruppe 3
[60, 66, 69] -> Spalte B, Grundton 66, Gruppe 18
```

### Huffman-Rhythmik

`HuffmanRhythm` verarbeitet jeden Beat mit einer endlichen Zustandsmaschine;
der Spielzustand wird über Beatgrenzen fortgeführt. Das projektinterne
Informationsmaß ist die Anzahl der dabei erzeugten Codesymbole.

```text
xooo xooo xooo xooo -> Information 1
xooo oooo oooo oooo -> Information 2
xoxo xoxo xoxo xoxo -> Information 5
xxxx xxxx xxxx xxxx -> Information 9
xxox xxox xxox xxox -> Information 17
```

Beim DB-Playback verteilt
`RhythmMapperFromOnsetStringToKickAndSnare` den ausgewählten Onset-String auf
Kick und Snare. Mehrere Informationsgrade werden zu einem durchgehenden
MIDI-Pattern zusammengefügt.

## Tests

Die vollständige Suite:

```bash
mvn test
```

Ein schneller, fachlich repräsentativer Ausschnitt:

```bash
mvn -Dtest='HuffmanRhythmTest,RhythmTest,RootCmdChordsCliTest,LocalRuntimeTest' test
```

Einige Tests hängen von der lokalen Umgebung ab:

- OSC-Tests benötigen einen lokalen UDP-Port und überspringen sich, wenn UDP
  nicht erlaubt ist.
- Echte MIDI-Sendetests werden ohne passendes Gerät (`Roland Digital Piano`
  oder `DP603`) übersprungen.
- Laufzeit- und Persistenzbefehle benötigen eine erreichbare PostgreSQL-Instanz.
- Der umfangreiche SuperCollider-Hörtest ist ein bewusst manueller Test.

## Aktuelle Grenzen

- Huffman-Analyse und -Generierung sind auf 4/4 im 16tel-Raster
  spezialisiert.
- Rhythmus-Playback läuft weiterhin über `javax.sound.midi`, auch wenn Noten
  und Akkorde standardmäßig SuperCollider verwenden.
- `calculate rhythms` erzeugt immer alle 65.536 binären Pattern; Tempo und
  Metrum sind dabei noch nicht frei konfigurierbar.
- Die ältere Gruppendokumentation und die aktuelle 18-Gruppen-Implementierung
  sind noch nicht vollständig synchronisiert.
- Die Domänenpakete `Scale` und `counterpoint` enthalten zusätzliche
  musikalische Modelle, sind aber derzeit nicht in die öffentliche CLI
  verdrahtet.

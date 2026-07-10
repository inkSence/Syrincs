# Syrincs

Syrincs ist ein Java-21-Werkzeug für algorithmische Harmonik und Rhythmik. Die
Kommandozeilenanwendung analysiert und erzeugt Akkorde nach Paul Hindemith,
bewertet 16tel-Rhythmen mit einem projektinternen Huffman-Informationsmaß und
gibt musikalisches Material über SuperCollider/OSC oder MIDI aus. Akkorde und
generierte Rhythmen können in PostgreSQL gespeichert werden.

## Der schnellste Einstieg

Vorausgesetzt werden ein JDK 21 und Maven. Für die ersten Analysen sind weder
PostgreSQL noch ein MIDI-Gerät noch SuperCollider nötig:

```bash
git clone <repository-url>
cd Syrincs
mvn test
mvn exec:java -Dexec.args="--help"
mvn exec:java -Dexec.args="analyze chord 60 64 67"
mvn exec:java -Dexec.args='analyze rhythm "xooo xoxo xooo xoxo"'
```

Die beiden Analysen liefern beispielsweise:

```text
[ANALYZE] Notes=[60, 64, 67] | Column=A_TRITONE_FREE | Root=60 | Group=1 | Frame=7
[ANALYZE] Rhythm=xoooxoxoxoooxoxo | Info=3 | Deviation=0.433013 | Beats=[xooo, xoxo, xooo, xoxo]
```

Damit lässt sich die fachliche Kernlogik direkt ausprobieren. PostgreSQL wird
erst für `init`, `calculate`, datenbankgestütztes Playback und Runtime-Checks
benötigt. Eine Audioausgabe erfordert zusätzlich SuperCollider oder ein
MIDI-Ausgabegerät.

## Was das Projekt kann

- Akkorde nach Hindemith analysieren: Spalte A/B, Grundton, Gruppe und
  Rahmenintervall bestimmen.
- Drei- bis fünfstimmige Akkorde in einem MIDI-Notenbereich erzeugen und in
  PostgreSQL persistieren.
- Einzelnoten und gespeicherte Akkorde standardmäßig per OSC an SuperCollider
  senden; MIDI ist explizit wählbar.
- Rhythmusdateien im kleinen RDL-0-Format lesen und mit dem JDK-MIDI-Sequencer
  wiedergeben.
- 4/4-Onset-Strings im 16tel-Raster analysieren, alle `2^16` Kombinationen
  erzeugen und nach Informationsgrad aus der Datenbank spielen.

## Voraussetzungen

| Funktion | Voraussetzung |
| --- | --- |
| Build, Tests und Analyse | JDK 21, Maven |
| Persistenz und DB-Playback | PostgreSQL mit JDBC-Zugriff |
| Standardausgabe von Noten/Akkorden | SuperCollider und `sclang` |
| Rhythmus- oder explizites MIDI-Playback | verfügbares MIDI-Out |

Die wichtigsten verwendeten Bibliotheken sind Picocli, der PostgreSQL-JDBC-
Treiber und JUnit 5. Die genauen Versionen stehen in `pom.xml`.

## Bauen und starten

```bash
mvn test
mvn package
```

`mvn package` erzeugt einschließlich der Laufzeitabhängigkeiten das Startskript:

```bash
target/app/bin/syrincs --help
```

Während der Entwicklung kann jeder Befehl über Maven ausgeführt werden:

```bash
mvn exec:java -Dexec.args="devices"
```

Die folgenden Abschnitte verwenden der Lesbarkeit halber `syrincs`. Ohne ein
installiertes Kommando kann jeweils `target/app/bin/syrincs` oder
`mvn exec:java -Dexec.args="…"` verwendet werden.

### Bash-Completion

Der Paketbau installiert bzw. aktualisiert standardmäßig die Bash-Completion.
Eine neue Shell oder `exec bash` lädt sie. Wer diesen Seiteneffekt nicht möchte,
baut so:

```bash
mvn package -Dsyrincs.skipCompletionInstall=true
```

## Typische Arbeitsabläufe

### Akkord analysieren

```bash
syrincs analyze chord 60 64 67
```

`analyze 60 64 67` ist eine unterstützte Kurzform. Die Ausgabe enthält:

- `Column`: `A_TRITONE_FREE` oder `B_WITH_TRITONE`
- `Root`: ermittelter Grundton als MIDI-Note
- `Group`: aktuelle Hindemith-Gruppe `1..18`
- `Frame`: Abstand zwischen tiefster und höchster Note

### Rhythmus analysieren

```bash
syrincs analyze rhythm "xooo xoxo xooo xoxo"
```

Für diesen Analysepfad gilt `x` als Einsatz und `o` als kein neuer Einsatz.
Whitespace wird ignoriert; aktuell werden 4/4 und ein 16tel-Raster erwartet.

### SuperCollider-Ausgabe ausprobieren

In Terminal 1:

```bash
syrincs start sc
```

In Terminal 2:

```bash
syrincs play note note 60 vel 0.5 dur 500
syrincs play sc chord 60 64 67 --preset organ.full
syrincs play sc drum drum.kick
```

Der Consumer lauscht standardmäßig auf UDP-Port `57120`. Presets, Szenen,
Rollen, Effekte und OSC-Automation sind ausführlich in
[`supercollider/README.md`](supercollider/README.md) beschrieben.

### Explizit über MIDI ausgeben

```bash
syrincs devices
syrincs play note note 60 --output midi
syrincs play chords numNotes 3 4 groups 1 2 rootNote 60 range 24 duration 200 channel 0 --output midi
```

Einige Optionen von `play note` und `play chords` sind historisch ohne führende
Bindestriche definiert. Die oben gezeigte Syntax ist beabsichtigt. MIDI-Kanäle
sind nullbasiert; `channel=9` entspricht General-MIDI-Kanal 10.

### RDL-0-Datei abspielen

```bash
syrincs play rhythm --in data/beat.rdl
syrincs play rhythm --in data/beat.rdl --device DP603
```

Ohne `--device` verwendet Syrincs zuerst `SYRINCS_MIDI_DEVICE`, danach die
bekannten Namen `Roland Digital Piano`/`DP603` und zuletzt das erste verfügbare
MIDI-Out.

## PostgreSQL einrichten

Die lokalen Defaults sind:

```text
URL:      jdbc:postgresql://localhost:5432/hindemith
User:     syrincs
Passwort: syrincs
```

Unter einem System mit `sudo` und PostgreSQL-Werkzeugen richtet das mitgelieferte
Skript Rolle, Datenbank und Anwendungsschema ein:

```bash
sudo systemctl enable --now postgresql
bash scripts/init-postgres.sh
```

Alternativ eine bereits vorhandene Datenbank konfigurieren und nur das Schema
initialisieren:

```bash
export SYRINCS_DB_URL='jdbc:postgresql://localhost:5432/hindemith'
export SYRINCS_DB_USER='syrincs'
export SYRINCS_DB_PASSWORD='syrincs'
syrincs init
```

Die Konfiguration wird in dieser Priorität aufgelöst:

1. CLI-Argumente `--db-url=…`, `--db-user=…`, `--db-pass=…`
2. `SYRINCS_DB_URL`, `SYRINCS_DB_USER`, `SYRINCS_DB_PASSWORD`
3. die Legacy-Variablen `HINDEMITH_DB_URL`, `HINDEMITH_DB_USER`,
   `HINDEMITH_DB_PASSWORD`
4. die oben genannten Defaults

Die DB-Argumente können vor oder nach dem eigentlichen Kommando stehen, zum
Beispiel:

```bash
syrincs --db-url=jdbc:postgresql://localhost:5432/hindemith init
```

### Daten erzeugen und verwenden

```bash
# Akkorde zwischen den MIDI-Grenzen erzeugen und speichern
syrincs calculate chords 48 84

# unterstützte Kurzform
syrincs calculate 48 84

# gespeicherte Akkorde spielen (Default: SuperCollider)
syrincs play chords numNotes 3 4 groups 1 2 rootNote 60 range 24

# alle 65.536 4/4-Rhythmen erzeugen und speichern
syrincs calculate rhythms

# je Informationsgrad einen zufälligen Rhythmus per MIDI spielen
syrincs play rhythm info 3 5 7
```

Beim letzten Befehl werden nur Kandidaten mit
`deviation > 0.7` berücksichtigt. Fehlen Tabelle oder Spalten, erneut
`syrincs init` ausführen; gibt es noch keine Kandidaten, zuerst
`syrincs calculate rhythms` ausführen.

## Lokale Runtime

Syrincs startet PostgreSQL nicht selbst, sondern erwartet es als Systemdienst:

```bash
syrincs status    # PostgreSQL und OSC-Consumer prüfen
syrincs start db  # nur DB-Verbindung prüfen
syrincs start sc  # nur SuperCollider im Vordergrund starten
syrincs start     # DB prüfen, dann SuperCollider starten
```

`syrincs start` und `syrincs start sc` laufen bis `Ctrl+C` im Vordergrund.

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
- `time`, `tempo`, `res-per-beat` und `bars` bilden den Header.
- Eine Voice definiert `note` (`0..127`), `channel` (`0..15`), `vel`
  (`0..127`) und `gate` (`0..100`).
- Im Pattern bedeutet `x` Hit und `-` Pause; `|` und Whitespace werden
  ignoriert.
- Die aktuelle Wiedergabe validiert genau die Stimmen `kick` und `snare`.

Die RDL-Zeichen unterscheiden sich bewusst vom Huffman-Onset-Format
(`x`/`o`).

## Architektur und Orientierung im Code

Das Projekt folgt grob Clean Architecture. Abhängigkeiten zeigen von den
äußeren Adaptern zur Anwendungs- und Domänenschicht; die Domäne kennt weder
Picocli noch JDBC, OSC oder `javax.sound.midi`.

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
supercollider/              OSC-Consumer, Presets und Audio-Routing
```

Gute Einstiegspunkte für die wichtigsten Abläufe:

| Thema | Einstiegspunkt |
| --- | --- |
| Verdrahtung der Anwendung | `syrincs.Main` |
| CLI und verfügbare Befehle | `c_adapters.cli.RootCmd` |
| Hindemith-Analyse | `a_domain.hindemith.ChordAnalysis` |
| Gruppendefinitionen | `a_domain.hindemith.ChordSpecificationRepository` |
| Huffman-Informationsmaß | `a_domain.rhythm.HuffmanRhythm` |
| RDL-Parsing | `c_adapters.RhythmFileParser` |
| PostgreSQL-Adapter | `c_adapters.postgres` |
| Lokaler DB-/SC-Betrieb | `c_adapters.runtime.LocalRuntime` |

## Fachlicher Stand

### Hindemith-Gruppen

`ChordSpecificationRepository` enthält derzeit 18 Spezifikationen und liefert
die Gruppen `1..18`. [`doc/Gruppen.md`](doc/Gruppen.md) dokumentiert eine ältere
konzeptionelle Zwischenfassung mit 14 Gruppen. Für das aktuelle Verhalten sind
deshalb Code und Tests maßgeblich.

Beispiele aus den Tests:

```text
[60, 64, 67] -> Spalte A, Grundton 60, Gruppe 1
[60, 63, 68] -> Spalte A, Grundton 68, Gruppe 2
[60, 64, 70] -> Spalte B, Grundton 60, Gruppe 3
[60, 66, 69] -> Spalte B, Grundton 66, Gruppe 18
```

### Huffman-Rhythmik

`HuffmanRhythm` verarbeitet jeden Beat mit einer endlichen Zustandsmaschine.
Das projektinterne Informationsmaß ist die Summe der dabei erzeugten
Codesymbole; zusätzlich wird die Standardabweichung der Beat-Informationswerte
gespeichert.

```text
xooo xooo xooo xooo -> Information 1
xooo oooo oooo oooo -> Information 2
xoxo xoxo xoxo xoxo -> Information 5
xxxx xxxx xxxx xxxx -> Information 9
xxox xxox xxox xxox -> Information 17
```

Beim DB-Playback verteilt
`RhythmMapperFromOnsetStringToKickAndSnare` einen Onset-String auf Kick und
Snare.

## Tests

Die vollständige Suite:

```bash
mvn test
```

Ein schneller, fachlich repräsentativer Ausschnitt:

```bash
mvn -Dtest='HuffmanRhythmTest,RhythmTest,RootCmdChordsCliTest,LocalRuntimeTest' test
```

Einige Integrationstests hängen von der Umgebung ab:

- Der OSC-Test benötigt einen lokalen UDP-Port und überspringt sich, wenn UDP
  nicht erlaubt ist.
- Echte MIDI-Sendetests werden ohne passendes Gerät (`Roland Digital Piano`
  oder `DP603`) übersprungen.
- Laufzeit- und Persistenzbefehle benötigen eine erreichbare PostgreSQL-Instanz.

Diese Fälle sind Umgebungsanforderungen; die Anwendung ändert ihre Defaults
nicht automatisch, um sie zu umgehen.

## Aktuelle Grenzen

- Huffman-Analyse und -Generierung sind derzeit auf 4/4 im 16tel-Raster
  spezialisiert.
- Rhythmus-Playback läuft weiterhin über `javax.sound.midi`, auch wenn Noten
  und Akkorde standardmäßig SuperCollider verwenden.
- `calculate rhythms` erzeugt immer alle 65.536 binären 16tel-Patterns; Tempo
  und Metrum sind dabei noch nicht frei konfigurierbar.
- Die ältere Gruppendokumentation und die aktuelle 18-Gruppen-Implementierung
  sind noch nicht vollständig synchronisiert.

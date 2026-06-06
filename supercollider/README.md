# SuperCollider OSC Consumer

Dieser Ordner enthält den lokalen SuperCollider-Consumer für Syrincs. Syrincs
sendet musikalische Preset-Namen per OSC; SuperCollider entscheidet anhand
dieser Presets, welcher SynthDef mit welchen Parametern gespielt wird.

## Projektanbindung

- Sprache/Build: Java 21 mit Maven.
- CLI: Picocli in `syrincs.c_adapters.cli.RootCmd`.
- OSC-Ausgabe: `SuperColliderOscOutputAdapter` sendet UDP/OSC ohne zusätzliche
  Maven-Abhängigkeit.
- SuperCollider-Skript: `supercollider/syrincs_osc_consumer.scd`.

Die Generator- und Hindemith-Logik wird für diesen Consumer nicht umgebaut. Der
Proof of Concept ergänzt einen kleinen vertikalen Slice:

```text
Syrincs CLI -> SuperColliderOscOutputAdapter -> OSC -> SuperCollider preset -> SynthDef -> Audio
```

## SuperCollider starten

Headless ohne IDE:

```bash
bash scripts/start-supercollider-consumer.sh
```

Das Skript startet `sclang`, führt `supercollider/syrincs_osc_consumer.scd` aus
und läuft im Vordergrund. Stoppen kannst du es mit `Ctrl+C`.

Alternativ kannst du in der SuperCollider IDE
`supercollider/syrincs_osc_consumer.scd` öffnen und den kompletten Block
ausführen.

Im Terminal bzw. Post Window sollte stehen:

```text
Syrincs OSC consumer listening on /note, /chord and /drum at UDP 57120
```

## OSC-API

Einzelne Note:

```text
/note preset midiNote velocity duration pan
```

Beispiel:

```text
/note pad.warm 60 0.7 1.5 0.0
```

Akkord:

```text
/chord preset midiNote1 midiNote2 ... velocity duration pan
```

Bei `/chord` gilt: erstes Argument ist das Preset, die letzten drei Argumente
sind `velocity`, `duration` und `pan`; alles dazwischen sind MIDI-Noten.

Drum:

```text
/drum drumPreset velocity pan
```

Beispiel:

```text
/drum drum.kick 0.9 0.0
```

## Presets

Aktuell definiert:

- `test.sine`
- `test.saw`
- `test.pulse`
- `bass.round`
- `pad.warm`
- `organ.full`
- `pluck.harplike`
- `drum.kick`
- `drum.snare`
- `drum.hat.closed`

Zusätzlich existiert `basic.sine` als Kompatibilitäts-Alias für den ersten
SuperCollider-Proof-of-Concept.

## Syrincs-Hörtests

Ein einzelner Ton:

```bash
mvn exec:java -Dexec.args="play sc 60 --preset test.sine --velocity 0.7 --duration 0.5"
```

Mehrere einzelne Noten mit demselben Preset:

```bash
mvn exec:java -Dexec.args="play sc 60 64 67 --preset pad.warm --velocity 0.45 --duration 1.2"
```

Ein echter `/chord`:

```bash
mvn exec:java -Dexec.args="play sc chord 60 64 67 --preset organ.full --velocity 0.55 --duration 1.0"
```

Ein einzelner Drum-Hit:

```bash
mvn exec:java -Dexec.args="play sc drum drum.kick --velocity 0.9"
```

Der lokale Preset-Demo-Test:

```bash
mvn exec:java -Dexec.args="play sc demo"
```

Wenn es funktioniert, hörst du nacheinander eine Sinusnote, einen Orgelakkord,
einen warmen Pad-Akkord, eine gezupfte Figur und ein kleines Drum-Pattern.
SuperCollider schreibt parallel die empfangenen OSC-Nachrichten ins Post
Window bzw. Terminal.

## Automatisierter Smoke-Test

Der JUnit-Test startet keinen SuperCollider-Server. Er bindet lokal einen
UDP-Port und prüft, dass Syrincs OSC-Pakete für `/note`, `/chord` und `/drum`
erzeugt:

```bash
mvn -Dtest=SuperColliderOscOutputAdapterTest test
```

## Linux Audio

Falls kein Klang kommt:

- Prüfen, ob der SuperCollider-Server wirklich gebootet ist.
- Prüfen, ob `sclang`/SuperCollider Audio an das richtige Gerät ausgibt.
- Bei PipeWire/JACK mit `qpwgraph`, `helvum` oder ähnlichen Tools prüfen, ob
  SuperCollider mit dem Systemausgang verbunden ist.
- Bei PulseAudio/PipeWire zusätzlich `pavucontrol` prüfen.
- Sicherstellen, dass kein anderes Programm UDP-Port `57120` exklusiv nutzt.

## Nächste sinnvolle Schritte

- App-seitig eine allgemeinere Output-Abstraktion neben `MidiOutputPort`.
- Preset-Datei oder Preset-Registry statt hart kodierter SC-Dictionary-Einträge.
- Einfache Parameterautomation per OSC.
- Kleine FX-Busse für Reverb und Delay.
- Drum-Pattern-Ausgabe aus der Rhythmus-Domäne an `/drum`.

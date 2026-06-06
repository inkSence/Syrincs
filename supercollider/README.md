# SuperCollider OSC Consumer

Dieser Ordner enthält einen ersten lokalen Proof of Concept für Syrincs als
OSC-Producer und SuperCollider als hörbaren Consumer.

## Projektanbindung

- Sprache/Build: Java 21 mit Maven.
- CLI: Picocli in `syrincs.c_adapters.cli.RootCmd`.
- Bestehende Ausgabegrenze: `MidiOutputPort`.
- JDK-MIDI-Ausgabe: `JdkMidiOutputAdapter` über `javax.sound.midi`.
- SuperCollider-PoC: `SuperColliderOscOutputAdapter` sendet UDP/OSC ohne neue
  Maven-Abhängigkeit.

Der PoC baut die Generator- und Hindemith-Logik nicht um. Er ergänzt nur einen
zweiten Consumer-Pfad für lokale Hörtests ohne Roland-Piano.

## OSC-Konvention

Syrincs sendet zunächst einzelne Noten an:

```text
/note synth midiNote velocity duration pan
```

Beispiel:

```text
/note basic.sine 60 0.7 0.5 0.0
```

- `synth`: Preset- oder Synth-Name, aktuell nur `basic.sine`
- `midiNote`: MIDI-Note `0..127`
- `velocity`: Lautstärke `0..1`
- `duration`: Dauer in Sekunden
- `pan`: Panorama `-1..1`

## SuperCollider starten

Empfohlen ist der headless Start ohne IDE:

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
Syrincs OSC consumer listening on /note at UDP 57120
```

Das Skript bootet den Server, definiert einen einfachen Sinus-Synth mit
ADSR-Hüllkurve und lauscht auf UDP-Port `57120`.

## Syrincs-Test ausführen

Ein einzelner Ton:

```bash
mvn exec:java -Dexec.args="play sc 60 --velocity 0.7 --duration 0.5"
```

Ein einfacher C-Dur-Akkord:

```bash
mvn exec:java -Dexec.args="play sc 60 64 67 --synth basic.sine --velocity 0.55 --duration 1.2 --pan 0.0"
```

Wenn es funktioniert, hörst du den Ton bzw. Akkord und SuperCollider schreibt
die empfangene `/note`-Nachricht ins Post Window.

## Linux Audio

Falls kein Klang kommt:

- Prüfen, ob der SuperCollider-Server wirklich gebootet ist.
- Prüfen, ob `sclang`/SuperCollider Audio an das richtige Gerät ausgibt.
- Bei PipeWire/JACK mit `qpwgraph`, `helvum` oder ähnlichen Tools prüfen, ob
  SuperCollider mit dem Systemausgang verbunden ist.
- Bei PulseAudio/PipeWire zusätzlich `pavucontrol` prüfen.
- Sicherstellen, dass kein anderes Programm UDP-Port `57120` exklusiv nutzt.

## Automatisierter Smoke-Test

Der JUnit-Test `SuperColliderOscOutputAdapterTest` startet keinen
SuperCollider-Server. Er bindet lokal einen UDP-Port und prüft, dass Syrincs ein
OSC-Paket mit `/note`, Typen `,sifff` und `basic.sine` sendet:

```bash
mvn -Dtest=SuperColliderOscOutputAdapterTest test
```

## Nächste sinnvolle Schritte

- Kleiner Preset-Layer für `basic.sine`, `basic.saw`, `basic.pulse`
- Drum-Adressen wie `/drum kick velocity duration`
- FX-Busse in SuperCollider für Reverb/Delay
- OSC-Parametersteuerung für Tempo, Synth-Parameter und Automation

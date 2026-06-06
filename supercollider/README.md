# SuperCollider OSC Consumer

Dieser Ordner enthält den lokalen SuperCollider-Consumer für Syrincs. Syrincs
sendet musikalische Preset-Namen per OSC; SuperCollider entscheidet intern,
welcher SynthDef mit welchen Default-Parametern gespielt wird.

Die aktuelle Stufe bleibt bewusst klein:

```text
Syrincs CLI
-> SuperColliderOscOutputAdapter
-> OSC
-> Preset
-> Synth/Drum
-> Dry/FX Sends
-> Reverb/Delay/Chorus
-> Master Limiter
-> Audio
```

Nicht enthalten sind Samples, Plugin-Bridge, DAW-artige Effektketten,
Automation ueber Zeit oder eine realistische Orchesteremulation.

## SuperCollider Starten

Headless ohne IDE:

```bash
bash scripts/start-supercollider-consumer.sh
```

Das Skript startet `sclang`, lädt `supercollider/syrincs_osc_consumer.scd` und
läuft im Vordergrund. Stoppen kannst du es mit `Ctrl+C`.

Alternativ kannst du in der SuperCollider IDE
`supercollider/syrincs_osc_consumer.scd` öffnen und den kompletten Block
ausführen.

Wenn der Consumer bereit ist, erscheint:

```text
Syrincs OSC consumer listening on /note, /chord, /drum and /fx at UDP 57120
```

## OSC-API

Einzelne Note:

```text
/note preset midiNote velocity duration pan
```

Akkord:

```text
/chord preset midiNote1 midiNote2 ... velocity duration pan
```

Konvention fuer `/chord`: erstes Argument ist das Preset, die letzten drei
Argumente sind `velocity`, `duration` und `pan`; alles dazwischen sind
MIDI-Noten.

Drum:

```text
/drum drumPreset velocity pan
```

Effektsteuerung:

```text
/fx effectName enabled paramName paramValue
```

Beispiele:

```text
/fx reverb 1 mix 0.25
/fx reverb 1 room 0.7
/fx delay 1 time 0.375
/fx delay 1 feedback 0.35
/fx chorus 0 mix 0.0
/fx master 1 volume 0.8
```

`enabled = 0` schaltet Reverb, Delay oder Chorus praktisch aus, indem der
Mix auf `0` gesetzt wird. Beim Delay wird zusaetzlich das Feedback auf `0`
gesetzt. Fuer `master` setzt `enabled = 0` konservative Master-Defaults.
Unbekannte Effekte oder Parameter werden geloggt und ignoriert.

Die bestehenden `/note`-, `/chord`- und `/drum`-Formate bleiben kompatibel.

## Audio-Routing

Der Consumer nutzt zentrale Stereo-Busse:

- `~dryBus`: trockene Instrumente und Drums
- `~reverbBus`: Reverb-Send
- `~delayBus`: Delay-Send
- `~chorusBus`: Chorus-Send
- `~masterBus`: FX-Returns

Die Node-Reihenfolge ist:

```text
~voiceGroup -> ~fxGroup -> ~masterGroup
```

Voices schreiben trocken auf `~dryBus` und optional skaliert auf die
FX-Send-Busse. Reverb, Delay und Chorus laufen als globale Synths genau einmal
und schreiben auf `~masterBus`. `syrincsMasterOut` summiert Dry und FX, filtert
optional, begrenzt den Pegel mit `Limiter` und schreibt auf Hardware-Out `0/1`.

## Globale FX

- `syrincsReverbFx`: `mix`, `room`, `damp`, `amp`
- `syrincsDelayFx`: `mix`, `time`, `feedback`, `amp`
- `syrincsChorusFx`: `mix`, `rate`, `depth`, `amp`
- `syrincsMasterOut`: `volume`, `drive`, `lowpass`, `highpass`

Sicherheitsgrenzen:

- Delay-Zeit wird auf `0.03..1.5` Sekunden begrenzt.
- Delay-Feedback wird auf maximal `0.72` begrenzt.
- FX-Mix-Werte sind konservativ geklemmt.
- Der Master-Out nutzt `LeakDC` und `Limiter`.

## SynthDef-Familien

SuperCollider enthält mehrere kleine SynthDef-Familien. Die App kennt diese
Namen nicht; sie sendet nur Presets.

- `syrincsBasicWave`: Testsounds mit Sine, Triangle, Saw, Pulse und Noise.
- `syrincsSubVoice`: Bass, Lead, Pads, Strings und Brass-artige Synth-Sounds.
- `syrincsFmVoice`: FM-E-Piano, Mallets und Bells.
- `syrincsPluckVoice`: Karplus-Strong-artige Plucks.
- `syrincsOrganVoice`: additive Orgelregister.
- `syrincsWindVoice`: einfache Wind-Andeutungen mit Noise, Filter und Vibrato.
- `syrincsKick`, `syrincsSnare`, `syrincsHatClosed`, `syrincsHatOpen`,
  `syrincsTom`: synthetische Drums ohne Samples.

## Preset-Struktur

Presets werden im SuperCollider-Skript zentral in `~presets` registriert. Ein
Preset kann diese Felder verwenden:

```text
synth, wave, ampScale, atk, dec, sus, rel, cutoff, rq, pan,
detune, drive, modRatio, modIndex, decay, coef, reverbSend,
delaySend, chorusSend, family, description
```

Zusaetzliche interne Felder wie `harm2`, `harm3`, `harm4`, `noiseMix`,
`vibratoRate`, `vibratoDepth`, `pitchStart`, `pitchEnd`, `click`, `tone` und
`drum` werden von einzelnen SynthDef-Familien genutzt.

Nicht jedes Preset setzt jedes Feld. Fehlende Werte werden ueber zentrale
Defaults ergaenzt. Velocity wird musikalisch auf Amplitude skaliert, Duration
wird begrenzt und Pan wird auf `-1..1` geklemmt. Akkorde werden leicht
abgesenkt, damit sie nicht sofort uebersteuern.

Preset-Sends sind bewusst niedrig gehalten:

- Pads und Strings nutzen Reverb und Chorus.
- Orgel nutzt etwas Reverb und nur wenig Chorus.
- Plucks und FM-Keys nutzen etwas Reverb und optional Delay.
- Leads koennen wenig Delay senden.
- Bass bleibt fast trocken.
- Kick bleibt trocken, Snare und Hats bekommen nur wenig Reverb.

Fallback-Verhalten:

- unbekanntes tonales Preset: Warnung und Fallback auf `test.sine`
- unbekanntes Drum-Preset: Warnung und Fallback auf `drum.kick`
- Drum-Preset auf `/note` oder tonales Preset auf `/drum`: Warnung und
  passender Fallback

## Presets Nach Familie

Test -> `syrincsBasicWave`:

- `test.sine`
- `test.triangle`
- `test.saw`
- `test.pulse`
- `test.noise`

Bass -> `syrincsSubVoice`:

- `bass.sub`
- `bass.round`
- `bass.pulse`
- `bass.soft`

Lead:

- `lead.sine` -> `syrincsBasicWave`
- `lead.saw` -> `syrincsSubVoice`
- `lead.square` -> `syrincsSubVoice`

Pads / Strings / Brass -> `syrincsSubVoice`:

- `pad.warm`
- `pad.dark`
- `pad.string`
- `strings.pad`
- `strings.slow`
- `brass.soft`
- `brass.bright`

Organ -> `syrincsOrganVoice`:

- `organ.soft`
- `organ.full`
- `organ.bright`
- `pad.organ`

Keys / Mallets -> `syrincsFmVoice`:

- `keys.fm_epiano`
- `keys.mallet`
- `keys.bell`

Plucked -> `syrincsPluckVoice`:

- `pluck.harplike`
- `pluck.guitarish`
- `pluck.pizzicato`

Wind -> `syrincsWindVoice`:

- `wind.fluteish`
- `wind.clarinetish`
- `wind.oboeish`

Drums:

- `drum.kick` -> `syrincsKick`
- `drum.kick.deep` -> `syrincsKick`
- `drum.snare` -> `syrincsSnare`
- `drum.snare.tight` -> `syrincsSnare`
- `drum.hat.closed` -> `syrincsHatClosed`
- `drum.hat.open` -> `syrincsHatOpen`
- `drum.tom.low` -> `syrincsTom`
- `drum.tom.high` -> `syrincsTom`

Kompatibilitaet:

- `basic.sine` ist ein Alias auf `test.sine`.

## Sinnvolle Hindemith-Presets

Fuer Hindemith-Akkorde sind besonders brauchbar:

- `organ.full`: klare harmonische Darstellung
- `organ.soft`: weniger dicht, gut fuer lange Tests
- `pad.warm`: weicher Akkordklang
- `strings.pad`: langsamer, tragender Klang
- `brass.soft`: klarer, aber weniger hart als `brass.bright`
- `wind.fluteish`: einfache monophone/akkordische Wind-Skizze

Fuer schnelle rhythmische Figuren:

- `pluck.harplike`
- `keys.fm_epiano`
- `keys.mallet`

## CLI-Beispiele

Einzelne Note:

```bash
mvn exec:java -Dexec.args="play sc 60 --preset test.sine"
mvn exec:java -Dexec.args="play sc 60 --preset wind.fluteish --velocity 0.6 --duration 1.0"
```

Akkord:

```bash
mvn exec:java -Dexec.args="play sc chord 60 64 67 --preset organ.full"
mvn exec:java -Dexec.args="play sc chord 48 55 60 64 --preset strings.pad --velocity 0.5 --duration 2.0"
```

Drums:

```bash
mvn exec:java -Dexec.args="play sc drum drum.kick"
mvn exec:java -Dexec.args="play sc drum drum.hat.open --velocity 0.45 --pan -0.2"
```

FX:

```bash
mvn exec:java -Dexec.args="play sc fx reverb mix 0.25"
mvn exec:java -Dexec.args="play sc fx delay time 0.375"
mvn exec:java -Dexec.args="play sc fx delay feedback 0.35"
mvn exec:java -Dexec.args="play sc fx chorus mix 0.18"
mvn exec:java -Dexec.args="play sc fx master volume 0.8"
mvn exec:java -Dexec.args="play sc fx delay mix 0.0 --off"
```

`--synth` existiert noch als Alias fuer `--preset`, neue Beispiele sollten aber
`--preset` verwenden.

## Manueller Hoertest

Starte in einem Terminal den Consumer:

```bash
bash scripts/start-supercollider-consumer.sh
```

Starte in einem zweiten Terminal die Demo:

```bash
mvn exec:java -Dexec.args="play sc demo"
```

Die Demo sendet:

- einen trockenen Akkord mit `organ.full`
- denselben Akkord mit aktiviertem Reverb
- `pad.warm` und `strings.pad` mit Chorus/Reverb
- kurze Figuren mit `pluck.harplike`, `keys.fm_epiano`, `keys.bell`
- Bass mit `bass.round` und `bass.sub`
- synthetische Drums mit Kick, Snare, Closed/Open Hat und Low/High Tom
- am Ende konservative FX- und Master-Werte

Erfolgskriterien:

- du hoerst die Sequenz,
- die Familien sind grob unterscheidbar,
- Reverb, Delay und Chorus sind im Verlauf der Demo hoerbar,
- SuperCollider loggt eingehende `/note`, `/chord`, `/drum` und
  `/fx`-Nachrichten,
- unbekannte Presets beenden den Consumer nicht, sondern erzeugen eine Warnung.

## Automatisierter Smoke-Test

Der JUnit-Test startet keinen SuperCollider-Server. Er bindet lokal einen
UDP-Port und prueft, dass Syrincs OSC-Pakete fuer `/note`, `/chord`, `/drum`
und `/fx` erzeugt:

```bash
mvn -Dtest=SuperColliderOscOutputAdapterTest test
```

## Linux Audio

Falls kein Klang kommt:

- pruefen, ob der SuperCollider-Server wirklich gebootet ist,
- pruefen, ob `sclang`/SuperCollider Audio an das richtige Geraet ausgibt,
- bei PipeWire/JACK mit `qpwgraph`, `helvum` oder aehnlichen Tools pruefen, ob
  SuperCollider mit dem Systemausgang verbunden ist,
- bei PulseAudio/PipeWire zusaetzlich `pavucontrol` pruefen,
- sicherstellen, dass kein anderes Programm UDP-Port `57120` exklusiv nutzt.

## Grenzen Dieser Stufe

- keine Samples
- keine Plugin-Bridge
- keine DAW-artige Effektkette
- keine Automation ueber Zeit
- keine realistische Orchesteremulation
- keine neue Java-Output-Architektur
- keine Datenbank- oder Runtime-Konsolidierung

## Naechste Sinnvolle Schritte

- Presets spaeter aus einer Datei oder Registry laden.
- App-seitig eine allgemeinere Output-Abstraktion neben `MidiOutputPort`.
- Parameterautomation per OSC.
- Feinere FX-Presets oder Szenen fuer Reverb/Delay/Chorus.
- Rhythmus-Domaene spaeter auf `/drum` routen.

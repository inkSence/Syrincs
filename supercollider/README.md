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
-> Synth/Drum oder optionaler Sample-Player
-> Dry/FX Sends
-> Reverb/Delay/Chorus
-> Master Limiter
-> Audio
```

Nicht enthalten sind grosse Sample-Libraries, Multisampling, Velocity-Layer,
Round-Robin, Plugin-Bridge, DAW-artige Effektketten, Automation-Lanes oder
eine realistische Orchesteremulation.

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
Syrincs OSC consumer listening on /note, /chord, /drum, /fx, /set and /ramp at UDP 57120
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

Parameter setzen:

```text
/set target param value
```

Parameter ueber Zeit veraendern:

```text
/ramp target param value seconds
```

Targets:

- `master`
- `reverb`
- `delay`
- `chorus`
- `preset:<name>`
- `family:<name>`

Beispiele:

```text
/set master volume 0.8
/set reverb mix 0.25
/set delay feedback 0.35
/set preset:pad.warm cutoff 1200
/set preset:pad.warm reverbSend 0.45
/set family:pad cutoff 900
/ramp reverb mix 0.4 2.0
/ramp master volume 0.6 1.5
/ramp family:pad cutoff 5000 4.0
```

Die bestehenden `/note`-, `/chord`-, `/drum`- und `/fx`-Formate bleiben
kompatibel.

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
- Delay-Feedback wird auf maximal `0.85` begrenzt; der Delay-Synth selbst
  begrenzt zusaetzlich konservativ.
- FX-Mix-Werte sind konservativ geklemmt.
- Der Master-Out nutzt `LeakDC` und `Limiter`.

## Parameterautomation

`/set` und `/ramp` teilen sich eine kleine Automationsschicht. Globale Targets
veraendern laufende FX-/Master-Synths. Preset- und Family-Targets veraendern
die gespeicherten Presetwerte fuer zukuenftige Noten, Akkorde oder Drums.
Bereits laufende tonale Voices werden in dieser Stufe nicht nachtraeglich
referenziert.

Preset-Aenderungen gelten nur fuer die laufende SuperCollider-Session. Nach
dem erneuten Laden von `syrincs_osc_consumer.scd` werden die Original-Presets
wiederhergestellt. Es gibt bewusst keine Persistenz.

Automatisierbare Preset-/Family-Parameter sind numerische Klangparameter wie:

```text
wave, ampScale, atk, dec, sus, rel, cutoff, rq, pan,
detune, drive, modRatio, modIndex, decay, coef, transpose,
noiseMix, vibratoRate, vibratoDepth, harm2, harm3, harm4,
noise, body, snap, spread, reverbSend, delaySend, chorusSend,
rootMidi, rate, startPos, freq, pitchStart, pitchEnd, click, tone
```

Nicht per OSC mutiert werden technische oder strukturelle Felder wie `synth`,
`sample`, `sampleFallback`, `family`, `description` oder `drum`.

Zentrale Parametergrenzen:

- `volume`: `0.0..1.2`
- `mix`: `0.0..1.0`
- `feedback`: `0.0..0.85`
- `time`: `0.03..1.5`
- `depth`: `0.0..0.05`
- `rate`: `0.01..10.0`
- `cutoff`: `40..18000`
- `rq`: `0.05..1.0`
- `drive`: `0.0..1.0`
- `reverbSend`, `delaySend`, `chorusSend`: `0.0..1.0`
- `pan`: `-1.0..1.0`

Wenn fuer dasselbe `target:param` eine neue Rampe gestartet wird, stoppt sie
die vorherige Rampe. Das verhindert konkurrierende Automation auf demselben
Parameter.

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
  `syrincsTom`, `syrincsClap`, `syrincsRim`, `syrincsClick`: synthetische
  Drum-Fallbacks.
- `syrincsSampleOneShot`: optionaler Sample-Player fuer Drum-One-Shots.
- `syrincsSampleTonal`: optionaler Sample-Player fuer einfache tonale
  C4-Samples mit Playback-Rate-Transposition.

## Preset-Struktur

Presets werden im SuperCollider-Skript zentral in `~presets` registriert. Ein
Preset kann diese Felder verwenden:

```text
synth, wave, ampScale, atk, dec, sus, rel, cutoff, rq, pan,
detune, drive, modRatio, modIndex, decay, coef, reverbSend,
delaySend, chorusSend, sample, sampleFallback, rootMidi, rate,
startPos, family, description
```

Zusaetzliche interne Felder wie `harm2`, `harm3`, `harm4`, `noiseMix`,
`vibratoRate`, `vibratoDepth`, `pitchStart`, `pitchEnd`, `click`, `tone` und
`drum` werden von einzelnen SynthDef-Familien genutzt. Drum-Presets koennen
zusaetzlich `noise`, `body`, `snap` und `spread` setzen.

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
- Clap, Rim und Toms bekommen moderate, konservative Sends.

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
- `strings.sample` -> `syrincsSampleTonal`, Fallback `strings.pad`

Organ -> `syrincsOrganVoice`:

- `organ.soft`
- `organ.full`
- `organ.bright`
- `pad.organ`

Keys / Mallets -> `syrincsFmVoice`:

- `keys.fm_epiano`
- `keys.mallet`
- `keys.bell`
- `keys.piano.sample` -> `syrincsSampleTonal`, Fallback `keys.fm_epiano`

Plucked -> `syrincsPluckVoice`:

- `pluck.harplike`
- `pluck.guitarish`
- `pluck.pizzicato`
- `pluck.sample` -> `syrincsSampleTonal`, Fallback `pluck.harplike`

Wind -> `syrincsWindVoice`:

- `wind.fluteish`
- `wind.clarinetish`
- `wind.oboeish`

Drums:

- `drum.kick` -> `syrincsKick`
- `drum.kick.deep` -> `syrincsKick`
- `drum.kick.short` -> `syrincsKick`
- `drum.snare` -> `syrincsSnare`
- `drum.snare.tight` -> `syrincsSnare`
- `drum.snare.noisy` -> `syrincsSnare`
- `drum.hat.closed` -> `syrincsHatClosed`
- `drum.hat.open` -> `syrincsHatOpen`
- `drum.tom.low` -> `syrincsTom`
- `drum.tom.mid` -> `syrincsTom`
- `drum.tom.high` -> `syrincsTom`
- `drum.clap` -> `syrincsClap`
- `drum.rim` -> `syrincsRim`
- `drum.click` -> `syrincsClick`
- `drum.kick.sample` -> `syrincsSampleOneShot`, Fallback `drum.kick`
- `drum.snare.sample` -> `syrincsSampleOneShot`, Fallback `drum.snare`
- `drum.hat.closed.sample` -> `syrincsSampleOneShot`, Fallback `drum.hat.closed`
- `drum.hat.open.sample` -> `syrincsSampleOneShot`, Fallback `drum.hat.open`
- `drum.clap.sample` -> `syrincsSampleOneShot`, Fallback `drum.clap`

## Synthetische Drum-Engine

Die synthetische Drum-Engine bleibt als stabiler Fallback erhalten. Die App
sendet nur Drum-Presetnamen ueber `/drum`; SuperCollider waehlt intern SynthDef,
Sample-Player und Parameter.

Drum-SynthDefs:

- `syrincsKick`: Sinus/Triangle-Body, Pitch-Envelope und optionaler Click.
- `syrincsSnare`: Noise, Snap und kurzer getunter Body.
- `syrincsHatClosed`: kurze highpass-/bandpass-gefilterte Noise-Hat.
- `syrincsHatOpen`: laengere Hat-Variante mit kontrolliertem Ausklang.
- `syrincsTom`: gestimmter Body mit Pitch-Envelope.
- `syrincsClap`: mehrere kurze Noise-Impulse mit diffuserem Tail.
- `syrincsRim`: kurzer tonaler Rimshot-Akzent.
- `syrincsClick`: kleiner Click fuer Metronom- oder Ghost-Akzente.

Velocity-Verhalten:

- Velocity steuert zuerst die Amplitude.
- Kick bekommt bei hoeherer Velocity etwas mehr Click und Drive.
- Snare bekommt bei hoeherer Velocity mehr Snap/Noise.
- Hats werden bei hoeherer Velocity leicht heller.
- Velocity `0.2` bleibt hoerbar leiser; Velocity `1.0` wird durch
  konservative Preset-Pegel und den Master-Limiter abgefangen.

Drum-FX-Sends:

- Kick-Presets sind fast trocken.
- Snare-Presets haben wenig Reverb.
- Hats haben sehr wenig Reverb.
- Toms haben moderaten Reverb.
- Clap hat etwas mehr Reverb und sehr wenig Delay.
- Rim kann sehr wenig Delay bekommen.

Kompatibilitaet:

- `basic.sine` ist ein Alias auf `test.sine`.

## Optionaler Sample-Layer

Der Sample-Layer ist optional. Das Repository enthaelt nur die Verzeichnisse,
keine grossen Audiodateien:

```text
supercollider/samples/
supercollider/samples/drums/
supercollider/samples/keys/
supercollider/samples/instruments/
```

Diese lokalen Dateien werden beim Start des Consumers gesucht:

```text
supercollider/samples/drums/kick.wav          -> sample.kick
supercollider/samples/drums/snare.wav         -> sample.snare
supercollider/samples/drums/hat_closed.wav    -> sample.hat.closed
supercollider/samples/drums/hat_open.wav      -> sample.hat.open
supercollider/samples/drums/clap.wav          -> sample.clap
supercollider/samples/keys/piano_c4.wav       -> sample.piano.c4
supercollider/samples/instruments/pluck_c4.wav -> sample.pluck.c4
supercollider/samples/instruments/strings_c4.wav -> sample.strings.c4
```

WAV ist der dokumentierte Standardpfad; AIFF/AIF kann SuperCollider je nach
System ebenfalls lesen. Die Registry erwartet aktuell aber die oben genannten
WAV-Dateinamen. Grosse Audio-Dateien unter `supercollider/samples/` sind in
`.gitignore` ausgeschlossen; nur `.gitkeep`-Dateien werden versioniert.

Sample-Presets:

- `drum.kick.sample`
- `drum.snare.sample`
- `drum.hat.closed.sample`
- `drum.hat.open.sample`
- `drum.clap.sample`
- `keys.piano.sample`
- `pluck.sample`
- `strings.sample`

Wenn ein Sample fehlt, loggt SuperCollider eine Warnung und nutzt das im Preset
hinterlegte synthetische Fallback-Preset. Dadurch funktionieren Sample-Presets
auch ohne lokale Audiodateien hoerbar weiter.

Beispiel:

```bash
cp ~/Samples/kick.wav supercollider/samples/drums/kick.wav
bash scripts/start-supercollider-consumer.sh
mvn exec:java -Dexec.args="play sc drum drum.kick.sample"
```

Tonales Beispiel mit einem C4-Piano-Sample:

```bash
cp ~/Samples/piano_c4.wav supercollider/samples/keys/piano_c4.wav
bash scripts/start-supercollider-consumer.sh
mvn exec:java -Dexec.args="play sc 60 --preset keys.piano.sample --duration 1.0"
```

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
mvn exec:java -Dexec.args="play sc 60 --preset keys.piano.sample --velocity 0.7 --duration 1.0"
```

Akkord:

```bash
mvn exec:java -Dexec.args="play sc chord 60 64 67 --preset organ.full"
mvn exec:java -Dexec.args="play sc chord 48 55 60 64 --preset strings.pad --velocity 0.5 --duration 2.0"
```

Drums:

```bash
mvn exec:java -Dexec.args="play sc drum drum.kick"
mvn exec:java -Dexec.args="play sc drum drum.kick.sample"
mvn exec:java -Dexec.args="play sc drum drum.hat.open --velocity 0.45 --pan -0.2"
mvn exec:java -Dexec.args="play sc drum drum.clap --velocity 0.7 --pan 0.1"
mvn exec:java -Dexec.args="play sc drum drum.clap.sample --velocity 0.7 --pan 0.1"
mvn exec:java -Dexec.args="play sc drum drum.tom.mid --velocity 0.6"
mvn exec:java -Dexec.args="play sc drum drum.click --velocity 0.35"
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

Automation:

```bash
mvn exec:java -Dexec.args="play sc set master volume 0.8"
mvn exec:java -Dexec.args="play sc set reverb mix 0.25"
mvn exec:java -Dexec.args="play sc set preset:pad.warm cutoff 1200"
mvn exec:java -Dexec.args="play sc set family:strings chorusSend 0.3"
mvn exec:java -Dexec.args="play sc ramp reverb mix 0.4 2.0"
mvn exec:java -Dexec.args="play sc ramp family:pad cutoff 5000 4.0"
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
- einen Cutoff-Ramp auf `preset:pad.warm`
- einen kurzen Reverb-Mix-Ramp
- kurze Figuren mit `pluck.harplike`, `keys.fm_epiano`, `keys.bell`
- Bass mit `bass.round` und `bass.sub`
- ein Drum-Pattern mit Kick auf 1/3, Snare auf 2/4, Closed Hats als Achtel,
  Open Hat, Clap, Rim und Tom-Fill mit Low/Mid/High Tom
- am Ende konservative FX- und Master-Werte

Erfolgskriterien:

- du hoerst die Sequenz,
- die Familien sind grob unterscheidbar,
- Reverb, Delay und Chorus sind im Verlauf der Demo hoerbar,
- Automation ist im Pad-Abschnitt als oeffnender Filter hoerbar,
- SuperCollider loggt eingehende `/note`, `/chord`, `/drum`, `/fx`, `/set`
  und `/ramp`-Nachrichten,
- unbekannte Presets beenden den Consumer nicht, sondern erzeugen eine Warnung.

Die Demo bleibt bewusst synthetisch stabil. Sample-Presets kannst du separat
testen; wenn die Dateien fehlen, sollten Warnungen erscheinen und trotzdem
synthetische Fallbacks klingen:

```bash
mvn exec:java -Dexec.args="play sc drum drum.kick.sample"
mvn exec:java -Dexec.args="play sc drum drum.snare.sample"
mvn exec:java -Dexec.args="play sc 60 --preset keys.piano.sample --duration 1.0"
mvn exec:java -Dexec.args="play sc 64 --preset pluck.sample --duration 0.7"
```

## Automatisierter Smoke-Test

Der JUnit-Test startet keinen SuperCollider-Server. Er bindet lokal einen
UDP-Port und prueft, dass Syrincs OSC-Pakete fuer `/note`, `/chord`, `/drum`
`/fx`, `/set` und `/ramp` erzeugt. Sample-Presetnamen werden dabei wie normale
Preset-Strings geprueft; echte Audiodateien sind nicht noetig:

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

- keine mitgelieferte Sample-Library
- keine grossen Audiodateien im Repository
- kein Multi-Sampling
- keine Round-Robin- oder Velocity-Layer-Samples
- keine SFZ- oder SoundFont-Engine
- keine realistischen Akustikdrums als vollstaendige Library
- keine Plugin-Bridge
- keine Plugin-Parameterautomation
- keine DAW-artige Effektkette
- keine Automation-Lanes oder Timeline
- keine Persistenz fuer Preset-Aenderungen
- keine komplexe Voice-ID-Steuerung
- keine realistische Orchesteremulation
- keine neue Java-Output-Architektur
- keine Datenbank- oder Runtime-Konsolidierung

## Naechste Sinnvolle Schritte

- Presets spaeter aus einer Datei oder Registry laden.
- App-seitig eine allgemeinere Output-Abstraktion neben `MidiOutputPort`.
- Preset-Aenderungen optional persistierbar machen.
- Komplexere Automation-Lanes nur bei konkretem Bedarf.
- Feinere FX-Presets oder Szenen fuer Reverb/Delay/Chorus.
- Rhythmus-Domaene spaeter auf `/drum` routen.

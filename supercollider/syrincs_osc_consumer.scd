(
thisProcess.openUDPPort(57120);
s.boot;

s.waitForBoot({
    SynthDef(\syrincsTone, { |freq = 440, amp = 0.2, sustain = 0.5, pan = 0,
        wave = 0, atk = 0.01, dec = 0.08, sus = 0.65, rel = 0.12, cutoff = 9000|

        var gate = Line.kr(1, 0, sustain.max(0.01));
        var env = EnvGen.kr(Env.adsr(atk, dec, sus, rel), gate: gate, doneAction: 2);
        var sine = SinOsc.ar(freq);
        var saw = Saw.ar(freq);
        var pulse = Pulse.ar(freq, 0.48);
        var organ = Mix.new([
            SinOsc.ar(freq),
            SinOsc.ar(freq * 2, 0, 0.45),
            SinOsc.ar(freq * 3, 0, 0.20)
        ]) * 0.75;
        var sig = SelectX.ar(wave.clip(0, 3), [sine, saw, pulse, organ]);

        sig = LPF.ar(sig, cutoff.clip(80, 16000));
        Out.ar(0, Pan2.ar(sig * env * amp, pan.clip(-1, 1)));
    }).add;

    SynthDef(\syrincsPluck, { |freq = 440, amp = 0.2, sustain = 1.0, pan = 0, coef = 0.45|
        var trig = Impulse.kr(0);
        var env = EnvGen.kr(Env.perc(0.002, sustain.max(0.05)), doneAction: 2);
        var sig = Pluck.ar(WhiteNoise.ar(amp), trig, 0.2, freq.reciprocal, sustain.max(0.05), coef.clip(0.1, 0.9));

        Out.ar(0, Pan2.ar(sig * env, pan.clip(-1, 1)));
    }).add;

    SynthDef(\syrincsKick, { |amp = 0.8, pan = 0|
        var env = EnvGen.kr(Env.perc(0.001, 0.34), doneAction: 2);
        var pitch = EnvGen.kr(Env([82, 38], [0.055], \exp));
        var sig = SinOsc.ar(pitch) + (BPF.ar(WhiteNoise.ar(0.08), 95, 0.35));

        Out.ar(0, Pan2.ar(sig * env * amp, pan.clip(-1, 1)));
    }).add;

    SynthDef(\syrincsSnare, { |amp = 0.65, pan = 0|
        var env = EnvGen.kr(Env.perc(0.002, 0.22), doneAction: 2);
        var noise = BPF.ar(WhiteNoise.ar, 2100, 0.7);
        var tone = SinOsc.ar(185, 0, 0.25);
        var sig = (noise + tone) * env;

        Out.ar(0, Pan2.ar(sig * amp, pan.clip(-1, 1)));
    }).add;

    SynthDef(\syrincsHatClosed, { |amp = 0.45, pan = 0|
        var env = EnvGen.kr(Env.perc(0.001, 0.075), doneAction: 2);
        var sig = HPF.ar(WhiteNoise.ar, 7000) * env;

        Out.ar(0, Pan2.ar(sig * amp, pan.clip(-1, 1)));
    }).add;

    ~fallback = { |value, fallback|
        if(value.isNil) { fallback } { value };
    };

    ~presets = Dictionary.new;
    ~presets.put("test.sine", (synth: \syrincsTone, wave: 0, ampScale: 0.25, atk: 0.01, dec: 0.08, sus: 0.65, rel: 0.12, cutoff: 12000));
    ~presets.put("test.saw", (synth: \syrincsTone, wave: 1, ampScale: 0.18, atk: 0.01, dec: 0.10, sus: 0.55, rel: 0.12, cutoff: 6500));
    ~presets.put("test.pulse", (synth: \syrincsTone, wave: 2, ampScale: 0.18, atk: 0.008, dec: 0.09, sus: 0.50, rel: 0.10, cutoff: 5200));
    ~presets.put("bass.round", (synth: \syrincsTone, wave: 0.35, ampScale: 0.30, transpose: -12, atk: 0.01, dec: 0.12, sus: 0.70, rel: 0.16, cutoff: 900));
    ~presets.put("pad.warm", (synth: \syrincsTone, wave: 1.25, ampScale: 0.14, atk: 0.35, dec: 0.35, sus: 0.75, rel: 0.85, cutoff: 2200));
    ~presets.put("organ.full", (synth: \syrincsTone, wave: 3, ampScale: 0.18, atk: 0.015, dec: 0.04, sus: 0.90, rel: 0.18, cutoff: 9000));
    ~presets.put("pluck.harplike", (synth: \syrincsPluck, ampScale: 0.24, coef: 0.38));
    ~presets.put("drum.kick", (synth: \syrincsKick, ampScale: 0.95, drum: true));
    ~presets.put("drum.snare", (synth: \syrincsSnare, ampScale: 0.75, drum: true));
    ~presets.put("drum.hat.closed", (synth: \syrincsHatClosed, ampScale: 0.48, drum: true));

    // Compatibility alias for the first proof of concept.
    ~presets.put("basic.sine", ~presets.at("test.sine"));

    ~presetFor = { |presetName|
        var name = presetName.asString;
        var preset = ~presets.at(name);

        if(preset.isNil) {
            ("Syrincs unknown preset: " ++ name).warn;
        };
        preset;
    };

    ~playNote = { |presetName, midiNote, velocity = 0.7, duration = 0.5, pan = 0|
        var preset = ~presetFor.(presetName);
        var synth;
        var note;
        var amp;

        if(preset.notNil) {
            synth = preset[\synth];
            note = (midiNote.asInteger + ~fallback.(preset[\transpose], 0)).clip(0, 127);
            amp = velocity.asFloat.clip(0, 1) * ~fallback.(preset[\ampScale], 0.25);

            if(synth == \syrincsPluck) {
                Synth(synth, [
                    \freq, note.midicps,
                    \amp, amp,
                    \sustain, duration.asFloat.max(0.05),
                    \pan, pan.asFloat.clip(-1, 1),
                    \coef, ~fallback.(preset[\coef], 0.45)
                ]);
            } {
                Synth(synth, [
                    \freq, note.midicps,
                    \amp, amp,
                    \sustain, duration.asFloat.max(0.01),
                    \pan, pan.asFloat.clip(-1, 1),
                    \wave, ~fallback.(preset[\wave], 0),
                    \atk, ~fallback.(preset[\atk], 0.01),
                    \dec, ~fallback.(preset[\dec], 0.08),
                    \sus, ~fallback.(preset[\sus], 0.65),
                    \rel, ~fallback.(preset[\rel], 0.12),
                    \cutoff, ~fallback.(preset[\cutoff], 9000)
                ]);
            };
        };
    };

    ~playChord = { |presetName, midiNotes, velocity = 0.7, duration = 1.0, pan = 0|
        var notes = midiNotes.asArray;
        var width = if(notes.size <= 1) { 0 } { 0.32 };

        notes.do({ |note, index|
            var offset = if(notes.size <= 1) { 0 } { ((index / (notes.size - 1)) - 0.5) * width };
            ~playNote.(presetName, note, velocity, duration, (pan + offset).clip(-1, 1));
        });
    };

    ~playDrum = { |presetName, velocity = 0.8, pan = 0|
        var preset = ~presetFor.(presetName);

        if(preset.notNil) {
            if(~fallback.(preset[\drum], false)) {
                Synth(preset[\synth], [
                    \amp, velocity.asFloat.clip(0, 1) * ~fallback.(preset[\ampScale], 0.7),
                    \pan, pan.asFloat.clip(-1, 1)
                ]);
            } {
                ("Syrincs preset is not playable as drum: " ++ presetName.asString).warn;
            };
        };
    };

    OSCdef(\syrincsNote, { |msg|
        var preset = msg[1].asString;
        var midiNote = msg[2].asInteger.clip(0, 127);
        var velocity = msg[3].asFloat.clip(0, 1);
        var duration = msg[4].asFloat.max(0.01);
        var pan = if(msg.size > 5) { msg[5].asFloat.clip(-1, 1) } { 0.0 };

        ["Syrincs /note", preset, midiNote, velocity, duration, pan].postln;
        ~playNote.(preset, midiNote, velocity, duration, pan);
    }, '/note', recvPort: 57120);

    OSCdef(\syrincsChord, { |msg|
        var preset;
        var notes;
        var velocity;
        var duration;
        var pan;

        if(msg.size < 6) {
            ("Syrincs invalid /chord message: " ++ msg).warn;
        } {
            preset = msg[1].asString;
            notes = msg.copyRange(2, msg.size - 4).collect({ |note| note.asInteger.clip(0, 127) });
            velocity = msg[msg.size - 3].asFloat.clip(0, 1);
            duration = msg[msg.size - 2].asFloat.max(0.01);
            pan = msg[msg.size - 1].asFloat.clip(-1, 1);

            ["Syrincs /chord", preset, notes, velocity, duration, pan].postln;
            ~playChord.(preset, notes, velocity, duration, pan);
        };
    }, '/chord', recvPort: 57120);

    OSCdef(\syrincsDrum, { |msg|
        var preset = msg[1].asString;
        var velocity = if(msg.size > 2) { msg[2].asFloat.clip(0, 1) } { 0.8 };
        var pan = if(msg.size > 3) { msg[3].asFloat.clip(-1, 1) } { 0.0 };

        ["Syrincs /drum", preset, velocity, pan].postln;
        ~playDrum.(preset, velocity, pan);
    }, '/drum', recvPort: 57120);

    "Syrincs OSC consumer listening on /note, /chord and /drum at UDP 57120".postln;
});
)

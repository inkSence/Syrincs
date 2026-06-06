(
thisProcess.openUDPPort(57120);
s.boot;

s.waitForBoot({
    Routine({
        [~reverbFx, ~delayFx, ~chorusFx, ~masterOut].do({ |node|
            if(node.notNil) { node.free };
        });
        [~voiceGroup, ~fxGroup, ~masterGroup].do({ |group|
            if(group.notNil) {
                group.freeAll;
                group.free;
            };
        });
        [~dryBus, ~reverbBus, ~delayBus, ~chorusBus, ~masterBus].do({ |bus|
            if(bus.notNil) { bus.free };
        });

    SynthDef(\syrincsBasicWave, { |freq = 440, amp = 0.2, sustain = 0.5, pan = 0,
        wave = 0, atk = 0.01, dec = 0.08, sus = 0.65, rel = 0.12,
        cutoff = 9000, rq = 0.35, detune = 0, drive = 0, modRatio = 2, modIndex = 0,
        decay = 0.5, coef = 0.45, noiseMix = 0, vibratoRate = 5, vibratoDepth = 0,
        harm2 = 0.4, harm3 = 0.18, harm4 = 0.08,
        out = 0, reverbOut = 0, delayOut = 0, chorusOut = 0,
        reverbSend = 0, delaySend = 0, chorusSend = 0|

        var dur = sustain.clip(0.03, 8.0);
        var gate = Line.kr(1, 0, dur);
        var env = EnvGen.kr(Env.adsr(atk.max(0.001), dec.max(0.001), sus.clip(0, 1), rel.max(0.001)), gate: gate, doneAction: 2);
        var vib = SinOsc.kr(vibratoRate.clip(0.1, 9), 0, vibratoDepth.clip(0, 0.035), 1);
        var baseFreq = freq * vib;
        var det = detune.clip(0, 0.035);
        var freqA = baseFreq * (1 - det);
        var freqB = baseFreq * (1 + det);
        var sine = Mix.new([SinOsc.ar(freqA), SinOsc.ar(freqB)]) * 0.5;
        var triangle = Mix.new([LFTri.ar(freqA), LFTri.ar(freqB)]) * 0.5;
        var saw = Mix.new([Saw.ar(freqA), Saw.ar(freqB)]) * 0.5;
        var pulse = Mix.new([Pulse.ar(freqA, 0.48), Pulse.ar(freqB, 0.52)]) * 0.5;
        var noise = BPF.ar(PinkNoise.ar, baseFreq.clip(80, 8000), rq.clip(0.05, 1));
        var sig = SelectX.ar(wave.clip(0, 4), [sine, triangle, saw, pulse, noise]);
        var voice;

        sig = (sig * (1 + drive.clip(0, 4))).tanh;
        sig = RLPF.ar(sig, cutoff.clip(80, 16000), rq.clip(0.05, 1));
        voice = Pan2.ar(sig * env * amp, pan.clip(-1, 1));
        Out.ar(out, voice);
        Out.ar(reverbOut, voice * reverbSend.clip(0, 1));
        Out.ar(delayOut, voice * delaySend.clip(0, 1));
        Out.ar(chorusOut, voice * chorusSend.clip(0, 1));
    }).add;

    SynthDef(\syrincsSubVoice, { |freq = 440, amp = 0.2, sustain = 0.5, pan = 0,
        wave = 2, atk = 0.01, dec = 0.12, sus = 0.7, rel = 0.16,
        cutoff = 1600, rq = 0.35, detune = 0.008, drive = 0, modRatio = 2, modIndex = 0,
        decay = 0.5, coef = 0.45, noiseMix = 0.01, vibratoRate = 5, vibratoDepth = 0,
        harm2 = 0.35, harm3 = 0.12, harm4 = 0.05,
        out = 0, reverbOut = 0, delayOut = 0, chorusOut = 0,
        reverbSend = 0, delaySend = 0, chorusSend = 0|

        var dur = sustain.clip(0.03, 8.0);
        var gate = Line.kr(1, 0, dur);
        var env = EnvGen.kr(Env.adsr(atk.max(0.001), dec.max(0.001), sus.clip(0, 1), rel.max(0.001)), gate: gate, doneAction: 2);
        var vib = SinOsc.kr(vibratoRate.clip(0.1, 8), 0, vibratoDepth.clip(0, 0.03), 1);
        var baseFreq = freq * vib;
        var det = detune.clip(0, 0.045);
        var tri = Mix.new([LFTri.ar(baseFreq), LFTri.ar(baseFreq * (1 + det))]) * 0.5;
        var saw = Mix.new([Saw.ar(baseFreq), Saw.ar(baseFreq * (1 + det)), Saw.ar(baseFreq * (1 - det), 0.55)]) * 0.38;
        var pulse = Mix.new([Pulse.ar(baseFreq, 0.42), Pulse.ar(baseFreq * (1 + det), 0.56)]) * 0.45;
        var sine = SinOsc.ar(baseFreq) + SinOsc.ar(baseFreq * 2, 0, harm2.clip(0, 1));
        var sig = SelectX.ar(wave.clip(0, 3), [sine, tri, saw, pulse]);
        var voice;

        sig = sig + PinkNoise.ar(noiseMix.clip(0, 0.12));
        sig = (sig * (1 + drive.clip(0, 5))).tanh;
        sig = RLPF.ar(sig, cutoff.clip(70, 14000), rq.clip(0.05, 1));
        voice = Pan2.ar(sig * env * amp, pan.clip(-1, 1));
        Out.ar(out, voice);
        Out.ar(reverbOut, voice * reverbSend.clip(0, 1));
        Out.ar(delayOut, voice * delaySend.clip(0, 1));
        Out.ar(chorusOut, voice * chorusSend.clip(0, 1));
    }).add;

    SynthDef(\syrincsFmVoice, { |freq = 440, amp = 0.2, sustain = 0.5, pan = 0,
        wave = 0, atk = 0.006, dec = 0.25, sus = 0.35, rel = 0.28,
        cutoff = 9000, rq = 0.35, detune = 0, drive = 0, modRatio = 2, modIndex = 0.5,
        decay = 0.5, coef = 0.45, noiseMix = 0, vibratoRate = 5, vibratoDepth = 0,
        harm2 = 0.4, harm3 = 0.18, harm4 = 0.08,
        out = 0, reverbOut = 0, delayOut = 0, chorusOut = 0,
        reverbSend = 0, delaySend = 0, chorusSend = 0|

        var dur = sustain.clip(0.05, 6.0);
        var gate = Line.kr(1, 0, dur);
        var ampEnv = EnvGen.kr(Env.adsr(atk.max(0.001), dec.max(0.001), sus.clip(0, 1), rel.max(0.001)), gate: gate, doneAction: 2);
        var modEnv = EnvGen.kr(Env.perc(0.002, (decay + dur).clip(0.08, 4.0)));
        var vib = SinOsc.kr(vibratoRate.clip(0.1, 8), 0, vibratoDepth.clip(0, 0.02), 1);
        var mod = SinOsc.ar(freq * modRatio.clip(0.25, 8) * vib, 0, freq * modIndex.clip(0, 4) * modEnv);
        var carrier = SinOsc.ar((freq * vib) + mod);
        var body = SinOsc.ar(freq * 2.01, 0, harm2.clip(0, 0.9)) + SinOsc.ar(freq * 3.98, 0, harm3.clip(0, 0.6));
        var sig = carrier + body;
        var voice;

        sig = RLPF.ar(sig, cutoff.clip(600, 14000), rq.clip(0.05, 1));
        sig = (sig * (1 + drive.clip(0, 3))).tanh;
        voice = Pan2.ar(sig * ampEnv * amp, pan.clip(-1, 1));
        Out.ar(out, voice);
        Out.ar(reverbOut, voice * reverbSend.clip(0, 1));
        Out.ar(delayOut, voice * delaySend.clip(0, 1));
        Out.ar(chorusOut, voice * chorusSend.clip(0, 1));
    }).add;

    SynthDef(\syrincsPluckVoice, { |freq = 440, amp = 0.2, sustain = 0.8, pan = 0,
        wave = 0, atk = 0.002, dec = 0.08, sus = 0.1, rel = 0.12,
        cutoff = 9000, rq = 0.35, detune = 0, drive = 0, modRatio = 2, modIndex = 0,
        decay = 1.0, coef = 0.45, noiseMix = 0, vibratoRate = 5, vibratoDepth = 0,
        harm2 = 0.4, harm3 = 0.18, harm4 = 0.08,
        out = 0, reverbOut = 0, delayOut = 0, chorusOut = 0,
        reverbSend = 0, delaySend = 0, chorusSend = 0|

        var dur = sustain.clip(0.05, 6.0);
        var trig = Impulse.ar(0);
        var env = EnvGen.kr(Env.perc(atk.max(0.001), dur), doneAction: 2);
        var source = WhiteNoise.ar(amp.max(0.001)) + PinkNoise.ar(noiseMix.clip(0, 0.08));
        var sig = Pluck.ar(source, trig, 0.2, freq.reciprocal, decay.clip(0.05, 6), coef.clip(0.1, 0.9));
        var voice;

        sig = LPF.ar(sig, cutoff.clip(400, 16000));
        sig = (sig * (1 + drive.clip(0, 5))).tanh;
        voice = Pan2.ar(sig * env, pan.clip(-1, 1));
        Out.ar(out, voice);
        Out.ar(reverbOut, voice * reverbSend.clip(0, 1));
        Out.ar(delayOut, voice * delaySend.clip(0, 1));
        Out.ar(chorusOut, voice * chorusSend.clip(0, 1));
    }).add;

    SynthDef(\syrincsOrganVoice, { |freq = 440, amp = 0.2, sustain = 0.8, pan = 0,
        wave = 0, atk = 0.012, dec = 0.04, sus = 0.9, rel = 0.16,
        cutoff = 9000, rq = 0.35, detune = 0, drive = 0, modRatio = 2, modIndex = 0,
        decay = 0.5, coef = 0.45, noiseMix = 0, vibratoRate = 5.2, vibratoDepth = 0.001,
        harm2 = 0.55, harm3 = 0.30, harm4 = 0.16,
        out = 0, reverbOut = 0, delayOut = 0, chorusOut = 0,
        reverbSend = 0, delaySend = 0, chorusSend = 0|

        var dur = sustain.clip(0.03, 8.0);
        var gate = Line.kr(1, 0, dur);
        var env = EnvGen.kr(Env.adsr(atk.max(0.001), dec.max(0.001), sus.clip(0, 1), rel.max(0.001)), gate: gate, doneAction: 2);
        var vib = SinOsc.kr(vibratoRate.clip(0.1, 7), 0, vibratoDepth.clip(0, 0.02), 1);
        var f = freq * vib;
        var sig = Mix.new([
            SinOsc.ar(f),
            SinOsc.ar(f * 2, 0, harm2.clip(0, 1)),
            SinOsc.ar(f * 3, 0, harm3.clip(0, 1)),
            SinOsc.ar(f * 4, 0, harm4.clip(0, 1)),
            SinOsc.ar(f * 0.5, 0, wave.clip(0, 1) * 0.25)
        ]);
        var voice;

        sig = sig * 0.55;
        sig = (sig * (1 + drive.clip(0, 3))).tanh;
        sig = LPF.ar(sig, cutoff.clip(600, 14000));
        voice = Pan2.ar(sig * env * amp, pan.clip(-1, 1));
        Out.ar(out, voice);
        Out.ar(reverbOut, voice * reverbSend.clip(0, 1));
        Out.ar(delayOut, voice * delaySend.clip(0, 1));
        Out.ar(chorusOut, voice * chorusSend.clip(0, 1));
    }).add;

    SynthDef(\syrincsWindVoice, { |freq = 440, amp = 0.2, sustain = 0.8, pan = 0,
        wave = 0, atk = 0.12, dec = 0.14, sus = 0.72, rel = 0.28,
        cutoff = 5200, rq = 0.32, detune = 0, drive = 0, modRatio = 2, modIndex = 0,
        decay = 0.5, coef = 0.45, noiseMix = 0.05, vibratoRate = 5.1, vibratoDepth = 0.006,
        harm2 = 0.3, harm3 = 0.12, harm4 = 0.04,
        out = 0, reverbOut = 0, delayOut = 0, chorusOut = 0,
        reverbSend = 0, delaySend = 0, chorusSend = 0|

        var dur = sustain.clip(0.08, 8.0);
        var gate = Line.kr(1, 0, dur);
        var env = EnvGen.kr(Env.adsr(atk.max(0.001), dec.max(0.001), sus.clip(0, 1), rel.max(0.001)), gate: gate, doneAction: 2);
        var vib = SinOsc.kr(vibratoRate.clip(0.1, 8), 0, vibratoDepth.clip(0, 0.025), 1);
        var f = freq * vib;
        var flute = SinOsc.ar(f) + WhiteNoise.ar(noiseMix.clip(0, 0.12));
        var clarinet = Pulse.ar(f, 0.42) * 0.55 + SinOsc.ar(f * 3, 0, harm3.clip(0, 0.6));
        var oboe = Saw.ar(f) * 0.35 + Pulse.ar(f * 2, 0.48, harm2.clip(0, 0.7));
        var sig = SelectX.ar(wave.clip(0, 2), [flute, clarinet, oboe]);
        var voice;

        sig = RLPF.ar(sig, cutoff.clip(500, 9000), rq.clip(0.05, 1));
        voice = Pan2.ar(sig * env * amp, pan.clip(-1, 1));
        Out.ar(out, voice);
        Out.ar(reverbOut, voice * reverbSend.clip(0, 1));
        Out.ar(delayOut, voice * delaySend.clip(0, 1));
        Out.ar(chorusOut, voice * chorusSend.clip(0, 1));
    }).add;

    SynthDef(\syrincsKick, { |freq = 120, amp = 0.8, pan = 0, decay = 0.34,
        drive = 0, pitchStart = 82, pitchEnd = 38, click = 0.05,
        cutoff = 7000, rq = 0.5, tone = 185,
        out = 0, reverbOut = 0, delayOut = 0, chorusOut = 0,
        reverbSend = 0, delaySend = 0, chorusSend = 0|

        var env = EnvGen.kr(Env.perc(0.001, decay.clip(0.08, 1.2)), doneAction: 2);
        var clickEnv = EnvGen.kr(Env.perc(0.001, 0.018));
        var pitch = EnvGen.kr(Env([pitchStart, pitchEnd], [0.055], \exp));
        var sig = SinOsc.ar(pitch) + BPF.ar(WhiteNoise.ar(click.clip(0, 0.3)), 2600, 0.5) * clickEnv;
        var voice;

        sig = (sig * (1 + drive.clip(0, 7))).tanh;
        voice = Pan2.ar(sig * env * amp, pan.clip(-1, 1));
        Out.ar(out, voice);
        Out.ar(reverbOut, voice * reverbSend.clip(0, 1));
        Out.ar(delayOut, voice * delaySend.clip(0, 1));
        Out.ar(chorusOut, voice * chorusSend.clip(0, 1));
    }).add;

    SynthDef(\syrincsSnare, { |freq = 120, amp = 0.65, pan = 0, decay = 0.22,
        cutoff = 2100, rq = 0.7, drive = 0, pitchStart = 82,
        pitchEnd = 38, click = 0.05, tone = 185,
        out = 0, reverbOut = 0, delayOut = 0, chorusOut = 0,
        reverbSend = 0, delaySend = 0, chorusSend = 0|

        var env = EnvGen.kr(Env.perc(0.002, decay.clip(0.05, 0.8)), doneAction: 2);
        var noise = BPF.ar(WhiteNoise.ar, cutoff.clip(500, 9000), rq.clip(0.05, 1));
        var body = SinOsc.ar(tone.clip(80, 350), 0, 0.25);
        var sig = (noise + body) * env;
        var voice;

        sig = (sig * (1 + drive.clip(0, 7))).tanh;
        voice = Pan2.ar(sig * amp, pan.clip(-1, 1));
        Out.ar(out, voice);
        Out.ar(reverbOut, voice * reverbSend.clip(0, 1));
        Out.ar(delayOut, voice * delaySend.clip(0, 1));
        Out.ar(chorusOut, voice * chorusSend.clip(0, 1));
    }).add;

    SynthDef(\syrincsHatClosed, { |freq = 120, amp = 0.45, pan = 0,
        decay = 0.075, drive = 0, pitchStart = 82, pitchEnd = 38,
        click = 0.05, cutoff = 7600, rq = 0.5, tone = 185,
        out = 0, reverbOut = 0, delayOut = 0, chorusOut = 0,
        reverbSend = 0, delaySend = 0, chorusSend = 0|

        var env = EnvGen.kr(Env.perc(0.001, decay.clip(0.02, 0.22)), doneAction: 2);
        var sig = HPF.ar(WhiteNoise.ar, cutoff.clip(3500, 14000));
        var voice;

        sig = BPF.ar(sig, cutoff.clip(3500, 14000), rq.clip(0.05, 1));
        voice = Pan2.ar(sig * env * amp, pan.clip(-1, 1));
        Out.ar(out, voice);
        Out.ar(reverbOut, voice * reverbSend.clip(0, 1));
        Out.ar(delayOut, voice * delaySend.clip(0, 1));
        Out.ar(chorusOut, voice * chorusSend.clip(0, 1));
    }).add;

    SynthDef(\syrincsHatOpen, { |freq = 120, amp = 0.42, pan = 0,
        decay = 0.42, drive = 0, pitchStart = 82, pitchEnd = 38,
        click = 0.05, cutoff = 6800, rq = 0.42, tone = 185,
        out = 0, reverbOut = 0, delayOut = 0, chorusOut = 0,
        reverbSend = 0, delaySend = 0, chorusSend = 0|

        var env = EnvGen.kr(Env.perc(0.002, decay.clip(0.12, 1.0)), doneAction: 2);
        var sig = HPF.ar(WhiteNoise.ar, cutoff.clip(3000, 13000));
        var voice;

        sig = BPF.ar(sig, cutoff.clip(3000, 13000), rq.clip(0.05, 1));
        voice = Pan2.ar(sig * env * amp, pan.clip(-1, 1));
        Out.ar(out, voice);
        Out.ar(reverbOut, voice * reverbSend.clip(0, 1));
        Out.ar(delayOut, voice * delaySend.clip(0, 1));
        Out.ar(chorusOut, voice * chorusSend.clip(0, 1));
    }).add;

    SynthDef(\syrincsTom, { |freq = 120, amp = 0.65, pan = 0, decay = 0.35,
        drive = 0, pitchStart = 82, pitchEnd = 78, click = 0.05,
        cutoff = 7000, rq = 0.5, tone = 185,
        out = 0, reverbOut = 0, delayOut = 0, chorusOut = 0,
        reverbSend = 0, delaySend = 0, chorusSend = 0|

        var env = EnvGen.kr(Env.perc(0.004, decay.clip(0.08, 1.0)), doneAction: 2);
        var pitch = EnvGen.kr(Env([freq, pitchEnd], [decay.clip(0.08, 1.0)], \exp));
        var sig = Mix.new([SinOsc.ar(pitch), LFTri.ar(pitch * 1.01, 0, 0.35)]) + BPF.ar(WhiteNoise.ar(0.04), freq * 1.8, 0.4);
        var voice;

        sig = (sig * (1 + drive.clip(0, 7))).tanh;
        voice = Pan2.ar(sig * env * amp, pan.clip(-1, 1));
        Out.ar(out, voice);
        Out.ar(reverbOut, voice * reverbSend.clip(0, 1));
        Out.ar(delayOut, voice * delaySend.clip(0, 1));
        Out.ar(chorusOut, voice * chorusSend.clip(0, 1));
    }).add;

    SynthDef(\syrincsReverbFx, { |inBus = 0, out = 0, mix = 0.16, room = 0.55, damp = 0.45, amp = 0.9|
        var input = In.ar(inBus, 2);
        var amount = mix.clip(0, 0.65);
        var wet = FreeVerb2.ar(input[0], input[1], amount, room.clip(0.05, 0.9), damp.clip(0, 1));

        Out.ar(out, LeakDC.ar(wet) * amount * amp.clip(0, 1.2));
    }).add;

    SynthDef(\syrincsDelayFx, { |inBus = 0, out = 0, mix = 0, time = 0.375, feedback = 0.28, amp = 0.85|
        var input = In.ar(inBus, 2);
        var fb = feedback.clip(0, 0.72);
        var local = LocalIn.ar(2);
        var delayed = DelayC.ar(input + (local * fb), 1.5, time.clip(0.03, 1.5));

        LocalOut.ar(LeakDC.ar(delayed));
        Out.ar(out, delayed * mix.clip(0, 0.55) * amp.clip(0, 1.2));
    }).add;

    SynthDef(\syrincsChorusFx, { |inBus = 0, out = 0, mix = 0.08, rate = 0.33, depth = 0.012, amp = 0.8|
        var input = In.ar(inBus, 2);
        var delayBase = 0.018;
        var delayTimes = [
            delayBase + SinOsc.kr(rate.clip(0.05, 6), 0, depth.clip(0.001, 0.035)),
            delayBase + SinOsc.kr(rate.clip(0.05, 6) * 1.17, pi, depth.clip(0.001, 0.035))
        ];
        var wet = DelayC.ar(input, 0.06, delayTimes);

        Out.ar(out, LeakDC.ar(wet) * mix.clip(0, 0.55) * amp.clip(0, 1.2));
    }).add;

    SynthDef(\syrincsMasterOut, { |dryIn = 0, fxIn = 0, out = 0, volume = 0.82,
        drive = 0, lowpass = 18000, highpass = 30|

        var dry = In.ar(dryIn, 2);
        var fx = In.ar(fxIn, 2);
        var driveAmount = drive.clip(0, 3);
        var sig = LeakDC.ar(dry + fx);

        sig = HPF.ar(sig, highpass.clip(20, 1000));
        sig = LPF.ar(sig, lowpass.clip(1000, 20000));
        sig = (sig * (1 + driveAmount)).tanh / (1 + (driveAmount * 0.25));
        sig = Limiter.ar(sig * volume.clip(0, 1.2), 0.92, 0.01);
        Out.ar(out, sig);
    }).add;

    s.sync;

    ~dryBus = Bus.audio(s, 2);
    ~reverbBus = Bus.audio(s, 2);
    ~delayBus = Bus.audio(s, 2);
    ~chorusBus = Bus.audio(s, 2);
    ~masterBus = Bus.audio(s, 2);

    ~voiceGroup = Group.head(s);
    ~fxGroup = Group.after(~voiceGroup);
    ~masterGroup = Group.after(~fxGroup);

    ~reverbFx = Synth.tail(~fxGroup, \syrincsReverbFx, [\inBus, ~reverbBus, \out, ~masterBus]);
    ~delayFx = Synth.tail(~fxGroup, \syrincsDelayFx, [\inBus, ~delayBus, \out, ~masterBus]);
    ~chorusFx = Synth.tail(~fxGroup, \syrincsChorusFx, [\inBus, ~chorusBus, \out, ~masterBus]);
    ~masterOut = Synth.tail(~masterGroup, \syrincsMasterOut, [\dryIn, ~dryBus, \fxIn, ~masterBus, \out, 0]);

    ~fallback = { |value, fallback|
        if(value.isNil) { fallback } { value };
    };

    ~durationFor = { |duration|
        duration.asFloat.clip(0.03, 8.0);
    };

    ~ampFor = { |velocity, preset, fallbackAmp = 0.22|
        velocity.asFloat.clip(0, 1).sqrt * ~fallback.(preset[\ampScale], fallbackAmp);
    };

    ~panFor = { |pan, preset|
        (pan.asFloat + ~fallback.(preset[\pan], 0)).clip(-1, 1);
    };

    ~sendFor = { |preset, key|
        ~fallback.(preset[key], 0).asFloat.clip(0, 1);
    };

    ~defaults = (
        synth: \syrincsBasicWave,
        wave: 0,
        ampScale: 0.22,
        atk: 0.01,
        dec: 0.08,
        sus: 0.65,
        rel: 0.12,
        cutoff: 9000,
        rq: 0.35,
        pan: 0,
        detune: 0,
        drive: 0,
        modRatio: 2,
        modIndex: 0,
        decay: 0.5,
        coef: 0.45,
        transpose: 0,
        noiseMix: 0,
        vibratoRate: 5,
        vibratoDepth: 0,
        harm2: 0.4,
        harm3: 0.18,
        harm4: 0.08,
        reverbSend: 0,
        delaySend: 0,
        chorusSend: 0,
        family: "test",
        description: "",
        drum: false
    );

    ~presets = Dictionary.new;
    ~putPreset = { |name, family, description, values|
        var preset = ~defaults.copy;

        preset.put(\family, family);
        preset.put(\description, description);
        values.keysValuesDo({ |key, value| preset.put(key, value) });
        ~presets.put(name, preset);
    };

    ~putPreset.("test.sine", "test", "Clean sine test tone", (synth: \syrincsBasicWave, wave: 0, ampScale: 0.24, cutoff: 12000));
    ~putPreset.("test.triangle", "test", "Triangle test tone", (synth: \syrincsBasicWave, wave: 1, ampScale: 0.22, cutoff: 9000));
    ~putPreset.("test.saw", "test", "Saw test tone", (synth: \syrincsBasicWave, wave: 2, ampScale: 0.17, cutoff: 6500, detune: 0.003));
    ~putPreset.("test.pulse", "test", "Pulse test tone", (synth: \syrincsBasicWave, wave: 3, ampScale: 0.17, cutoff: 5200));
    ~putPreset.("test.noise", "test", "Filtered noise test tone", (synth: \syrincsBasicWave, wave: 4, ampScale: 0.14, cutoff: 5000, rq: 0.22, atk: 0.005, rel: 0.08));

    ~putPreset.("bass.sub", "bass", "Low sine sub bass", (synth: \syrincsSubVoice, wave: 0, ampScale: 0.28, transpose: -24, cutoff: 380, atk: 0.012, dec: 0.12, sus: 0.85, rel: 0.12, harm2: 0.12));
    ~putPreset.("bass.round", "bass", "Round low bass", (synth: \syrincsSubVoice, wave: 0.35, ampScale: 0.27, transpose: -12, cutoff: 820, atk: 0.01, dec: 0.12, sus: 0.70, rel: 0.16, harm2: 0.28));
    ~putPreset.("bass.pulse", "bass", "Soft pulse bass", (synth: \syrincsSubVoice, wave: 3, ampScale: 0.24, transpose: -12, cutoff: 1150, atk: 0.008, dec: 0.09, sus: 0.62, rel: 0.12, drive: 0.35));
    ~putPreset.("bass.soft", "bass", "Quiet rounded bass", (synth: \syrincsSubVoice, wave: 1, ampScale: 0.22, transpose: -12, cutoff: 720, atk: 0.018, dec: 0.16, sus: 0.72, rel: 0.22));

    ~putPreset.("lead.sine", "lead", "Plain sine lead", (synth: \syrincsBasicWave, wave: 0, ampScale: 0.20, atk: 0.006, dec: 0.08, sus: 0.70, rel: 0.10, cutoff: 11000));
    ~putPreset.("lead.saw", "lead", "Bright saw lead", (synth: \syrincsSubVoice, wave: 2, ampScale: 0.16, atk: 0.004, dec: 0.08, sus: 0.60, rel: 0.09, cutoff: 7000, detune: 0.006, drive: 0.25, delaySend: 0.12));
    ~putPreset.("lead.square", "lead", "Square lead", (synth: \syrincsSubVoice, wave: 3, ampScale: 0.16, atk: 0.004, dec: 0.06, sus: 0.58, rel: 0.08, cutoff: 6000, drive: 0.18));

    ~putPreset.("pad.warm", "pad", "Warm slow pad", (synth: \syrincsSubVoice, wave: 2, ampScale: 0.12, atk: 0.35, dec: 0.35, sus: 0.75, rel: 0.85, cutoff: 2100, rq: 0.45, detune: 0.014, noiseMix: 0.015, reverbSend: 0.24, chorusSend: 0.18));
    ~putPreset.("pad.dark", "pad", "Darker filtered pad", (synth: \syrincsSubVoice, wave: 1.8, ampScale: 0.12, atk: 0.45, dec: 0.45, sus: 0.70, rel: 1.2, cutoff: 1050, rq: 0.38, detune: 0.01, reverbSend: 0.22, chorusSend: 0.10));
    ~putPreset.("pad.string", "pad", "String-like pad", (synth: \syrincsSubVoice, wave: 2, ampScale: 0.12, atk: 0.28, dec: 0.34, sus: 0.78, rel: 1.0, cutoff: 2550, rq: 0.32, detune: 0.02, reverbSend: 0.26, chorusSend: 0.16));
    ~putPreset.("pad.organ", "pad", "Soft organ pad", (synth: \syrincsOrganVoice, wave: 0.2, ampScale: 0.13, atk: 0.12, dec: 0.18, sus: 0.90, rel: 0.6, cutoff: 5200, harm2: 0.35, harm3: 0.14, harm4: 0.05, reverbSend: 0.16, chorusSend: 0.06));

    ~putPreset.("keys.fm_epiano", "keys", "Minimal FM electric piano", (synth: \syrincsFmVoice, ampScale: 0.18, atk: 0.006, dec: 0.30, sus: 0.42, rel: 0.34, cutoff: 8200, modRatio: 2, modIndex: 0.42, harm2: 0.12, harm3: 0.03, reverbSend: 0.14, delaySend: 0.08));
    ~putPreset.("keys.mallet", "keys", "Short mallet tone", (synth: \syrincsFmVoice, ampScale: 0.18, atk: 0.003, dec: 0.18, sus: 0.15, rel: 0.16, cutoff: 9600, modRatio: 3, modIndex: 0.34, decay: 0.35));
    ~putPreset.("keys.bell", "keys", "Simple bell tone", (synth: \syrincsFmVoice, ampScale: 0.15, atk: 0.003, dec: 0.45, sus: 0.10, rel: 0.65, cutoff: 11000, modRatio: 2.7, modIndex: 0.72, harm2: 0.18, harm3: 0.08, decay: 0.9, reverbSend: 0.20, delaySend: 0.06));

    ~putPreset.("pluck.harplike", "pluck", "Bright harp-like pluck", (synth: \syrincsPluckVoice, ampScale: 0.22, cutoff: 9800, decay: 1.5, coef: 0.38, reverbSend: 0.20, delaySend: 0.10));
    ~putPreset.("pluck.guitarish", "pluck", "Muted guitar-like pluck", (synth: \syrincsPluckVoice, ampScale: 0.20, cutoff: 4200, decay: 0.9, coef: 0.55, drive: 0.22, noiseMix: 0.03, reverbSend: 0.12, delaySend: 0.06));
    ~putPreset.("pluck.pizzicato", "pluck", "Short pizzicato pluck", (synth: \syrincsPluckVoice, ampScale: 0.19, cutoff: 3600, decay: 0.42, coef: 0.72, reverbSend: 0.10));

    ~putPreset.("organ.soft", "organ", "Soft drawbar organ", (synth: \syrincsOrganVoice, wave: 0.1, ampScale: 0.13, atk: 0.018, dec: 0.05, sus: 0.85, rel: 0.22, cutoff: 5200, harm2: 0.34, harm3: 0.12, harm4: 0.03, reverbSend: 0.14));
    ~putPreset.("organ.full", "organ", "Full organ", (synth: \syrincsOrganVoice, wave: 0.35, ampScale: 0.16, atk: 0.015, dec: 0.04, sus: 0.90, rel: 0.18, cutoff: 9000, harm2: 0.62, harm3: 0.32, harm4: 0.16, reverbSend: 0.18, chorusSend: 0.04));
    ~putPreset.("organ.bright", "organ", "Bright organ", (synth: \syrincsOrganVoice, wave: 0.2, ampScale: 0.15, atk: 0.01, dec: 0.04, sus: 0.92, rel: 0.14, cutoff: 12500, drive: 0.14, harm2: 0.70, harm3: 0.46, harm4: 0.25, reverbSend: 0.12));

    ~putPreset.("wind.fluteish", "wind", "Simple flute-like sketch", (synth: \syrincsWindVoice, wave: 0, ampScale: 0.15, atk: 0.12, dec: 0.12, sus: 0.76, rel: 0.35, cutoff: 5200, rq: 0.28, noiseMix: 0.035, vibratoRate: 5.1, vibratoDepth: 0.004, reverbSend: 0.16));
    ~putPreset.("wind.clarinetish", "wind", "Simple clarinet-like sketch", (synth: \syrincsWindVoice, wave: 1, ampScale: 0.14, atk: 0.09, dec: 0.12, sus: 0.74, rel: 0.30, cutoff: 3800, rq: 0.35, noiseMix: 0.025, vibratoRate: 4.8, vibratoDepth: 0.003, harm3: 0.22));
    ~putPreset.("wind.oboeish", "wind", "Simple oboe-like sketch", (synth: \syrincsWindVoice, wave: 2, ampScale: 0.13, atk: 0.07, dec: 0.10, sus: 0.70, rel: 0.26, cutoff: 4600, rq: 0.42, noiseMix: 0.03, vibratoRate: 5.4, vibratoDepth: 0.004, harm2: 0.35));

    ~putPreset.("brass.soft", "brass", "Soft synthetic brass", (synth: \syrincsSubVoice, wave: 2.8, ampScale: 0.14, atk: 0.10, dec: 0.18, sus: 0.72, rel: 0.32, cutoff: 2500, rq: 0.28, drive: 0.12, harm2: 0.38, reverbSend: 0.16));
    ~putPreset.("brass.bright", "brass", "Bright synthetic brass", (synth: \syrincsSubVoice, wave: 2.5, ampScale: 0.15, atk: 0.055, dec: 0.12, sus: 0.68, rel: 0.25, cutoff: 5000, rq: 0.24, drive: 0.24, harm2: 0.44));
    ~putPreset.("strings.pad", "strings", "Slow string pad", (synth: \syrincsSubVoice, wave: 2, ampScale: 0.11, atk: 0.55, dec: 0.35, sus: 0.78, rel: 1.4, cutoff: 2900, rq: 0.32, detune: 0.022, reverbSend: 0.28, chorusSend: 0.18));
    ~putPreset.("strings.slow", "strings", "Very slow string layer", (synth: \syrincsSubVoice, wave: 1.7, ampScale: 0.10, atk: 0.9, dec: 0.45, sus: 0.82, rel: 1.8, cutoff: 2350, rq: 0.35, detune: 0.018, reverbSend: 0.30, chorusSend: 0.16));

    ~putPreset.("drum.kick", "drum", "Basic kick drum", (synth: \syrincsKick, ampScale: 0.88, decay: 0.34, drive: 0.15, pitchStart: 82, pitchEnd: 38, click: 0.06, drum: true));
    ~putPreset.("drum.kick.deep", "drum", "Deeper kick drum", (synth: \syrincsKick, ampScale: 0.92, decay: 0.50, drive: 0.22, pitchStart: 70, pitchEnd: 31, click: 0.04, drum: true));
    ~putPreset.("drum.snare", "drum", "Basic snare drum", (synth: \syrincsSnare, ampScale: 0.68, decay: 0.22, cutoff: 2100, rq: 0.7, drive: 0.1, tone: 185, reverbSend: 0.12, drum: true));
    ~putPreset.("drum.snare.tight", "drum", "Tight short snare", (synth: \syrincsSnare, ampScale: 0.62, decay: 0.12, cutoff: 2900, rq: 0.55, drive: 0.18, tone: 205, reverbSend: 0.08, drum: true));
    ~putPreset.("drum.hat.closed", "drum", "Closed hi-hat", (synth: \syrincsHatClosed, ampScale: 0.38, decay: 0.075, cutoff: 7600, rq: 0.50, pan: -0.08, reverbSend: 0.04, drum: true));
    ~putPreset.("drum.hat.open", "drum", "Open hi-hat", (synth: \syrincsHatOpen, ampScale: 0.34, decay: 0.42, cutoff: 6800, rq: 0.42, pan: -0.12, reverbSend: 0.06, drum: true));
    ~putPreset.("drum.tom.low", "drum", "Low tom", (synth: \syrincsTom, ampScale: 0.62, freq: 125, pitchEnd: 74, decay: 0.42, drive: 0.08, pan: -0.12, drum: true));
    ~putPreset.("drum.tom.high", "drum", "High tom", (synth: \syrincsTom, ampScale: 0.56, freq: 185, pitchEnd: 115, decay: 0.30, drive: 0.06, pan: 0.12, drum: true));

    // Compatibility alias for the first proof of concept.
    ~presets.put("basic.sine", ~presets.at("test.sine"));

    ~presetForNote = { |presetName|
        var name = presetName.asString;
        var preset = ~presets.at(name);

        if(preset.isNil) {
            ("Syrincs unknown tonal preset '" ++ name ++ "', using fallback test.sine").warn;
            preset = ~presets.at("test.sine");
        } {
            if(preset[\family] == "drum") {
                ("Syrincs preset '" ++ name ++ "' is a drum preset, using fallback test.sine for tonal playback").warn;
                preset = ~presets.at("test.sine");
            };
        };
        preset;
    };

    ~presetForDrum = { |presetName|
        var name = presetName.asString;
        var preset = ~presets.at(name);

        if(preset.isNil) {
            ("Syrincs unknown drum preset '" ++ name ++ "', using fallback drum.kick").warn;
            preset = ~presets.at("drum.kick");
        } {
            if(~fallback.(preset[\drum], false).not) {
                ("Syrincs preset '" ++ name ++ "' is not playable as drum, using fallback drum.kick").warn;
                preset = ~presets.at("drum.kick");
            };
        };
        preset;
    };

    ~playNote = { |presetName, midiNote, velocity = 0.7, duration = 0.5, pan = 0|
        var preset = ~presetForNote.(presetName);
        var synth = preset[\synth];
        var note = (midiNote.asInteger + ~fallback.(preset[\transpose], 0)).clip(0, 127);
        var amp = ~ampFor.(velocity, preset, 0.22);
        var dur = ~durationFor.(duration);
        var outPan = ~panFor.(pan, preset);

        Synth(synth, [
            \freq, note.midicps,
            \amp, amp,
            \sustain, dur,
            \pan, outPan,
            \wave, ~fallback.(preset[\wave], 0),
            \atk, ~fallback.(preset[\atk], 0.01),
            \dec, ~fallback.(preset[\dec], 0.08),
            \sus, ~fallback.(preset[\sus], 0.65),
            \rel, ~fallback.(preset[\rel], 0.12),
            \cutoff, ~fallback.(preset[\cutoff], 9000),
            \rq, ~fallback.(preset[\rq], 0.35),
            \detune, ~fallback.(preset[\detune], 0),
            \drive, ~fallback.(preset[\drive], 0),
            \modRatio, ~fallback.(preset[\modRatio], 2),
            \modIndex, ~fallback.(preset[\modIndex], 0),
            \decay, ~fallback.(preset[\decay], dur),
            \coef, ~fallback.(preset[\coef], 0.45),
            \noiseMix, ~fallback.(preset[\noiseMix], 0),
            \vibratoRate, ~fallback.(preset[\vibratoRate], 5),
            \vibratoDepth, ~fallback.(preset[\vibratoDepth], 0),
            \harm2, ~fallback.(preset[\harm2], 0.4),
            \harm3, ~fallback.(preset[\harm3], 0.18),
            \harm4, ~fallback.(preset[\harm4], 0.08),
            \out, ~dryBus,
            \reverbOut, ~reverbBus,
            \delayOut, ~delayBus,
            \chorusOut, ~chorusBus,
            \reverbSend, ~sendFor.(preset, \reverbSend),
            \delaySend, ~sendFor.(preset, \delaySend),
            \chorusSend, ~sendFor.(preset, \chorusSend)
        ], target: ~voiceGroup);
    };

    ~playChord = { |presetName, midiNotes, velocity = 0.7, duration = 1.0, pan = 0|
        var notes = midiNotes.asArray;
        var chordVelocity = velocity.asFloat * if(notes.size > 3) { 0.85 } { 1.0 };
        var width = if(notes.size <= 1) { 0 } { 0.32 };

        notes.do({ |note, index|
            var offset = if(notes.size <= 1) { 0 } { ((index / (notes.size - 1)) - 0.5) * width };
            ~playNote.(presetName, note, chordVelocity, duration, (pan + offset).clip(-1, 1));
        });
    };

    ~playDrum = { |presetName, velocity = 0.8, pan = 0|
        var preset = ~presetForDrum.(presetName);
        var synth = preset[\synth];
        var amp = ~ampFor.(velocity, preset, 0.55);
        var outPan = ~panFor.(pan, preset);

        Synth(synth, [
            \freq, ~fallback.(preset[\freq], 120),
            \amp, amp,
            \pan, outPan,
            \decay, ~fallback.(preset[\decay], 0.3),
            \drive, ~fallback.(preset[\drive], 0),
            \pitchStart, ~fallback.(preset[\pitchStart], 82),
            \pitchEnd, ~fallback.(preset[\pitchEnd], 38),
            \click, ~fallback.(preset[\click], 0.05),
            \cutoff, ~fallback.(preset[\cutoff], 7000),
            \rq, ~fallback.(preset[\rq], 0.5),
            \tone, ~fallback.(preset[\tone], 185),
            \out, ~dryBus,
            \reverbOut, ~reverbBus,
            \delayOut, ~delayBus,
            \chorusOut, ~chorusBus,
            \reverbSend, ~sendFor.(preset, \reverbSend),
            \delaySend, ~sendFor.(preset, \delaySend),
            \chorusSend, ~sendFor.(preset, \chorusSend)
        ], target: ~voiceGroup);
    };

    ~clipFxParam = { |effect, param, value|
        var v = value.asFloat;

        case
        { param == \mix } { v.clip(0, if(effect == "delay") { 0.55 } { 0.65 }) }
        { param == \room } { v.clip(0.05, 0.9) }
        { param == \damp } { v.clip(0, 1) }
        { param == \time } { v.clip(0.03, 1.5) }
        { param == \feedback } { v.clip(0, 0.72) }
        { param == \rate } { v.clip(0.05, 6) }
        { param == \depth } { v.clip(0.001, 0.035) }
        { param == \amp } { v.clip(0, 1.2) }
        { param == \volume } { v.clip(0, 1.2) }
        { param == \drive } { v.clip(0, 3) }
        { param == \lowpass } { v.clip(1000, 20000) }
        { param == \highpass } { v.clip(20, 1000) }
        { v };
    };

    ~setFx = { |effectName, enabled = 1, paramName = "mix", value = 0|
        var effect = effectName.asString;
        var param = paramName.asString.asSymbol;
        var isEnabled = enabled.asInteger != 0;
        var target = nil;
        var allowed = [];
        var clipped;

        case
        { effect == "reverb" } {
            target = ~reverbFx;
            allowed = [\mix, \room, \damp, \amp];
        }
        { effect == "delay" } {
            target = ~delayFx;
            allowed = [\mix, \time, \feedback, \amp];
        }
        { effect == "chorus" } {
            target = ~chorusFx;
            allowed = [\mix, \rate, \depth, \amp];
        }
        { effect == "master" } {
            target = ~masterOut;
            allowed = [\volume, \drive, \lowpass, \highpass];
        }
        {
            ("Syrincs unknown /fx effect '" ++ effect ++ "'").warn;
        };

        if(target.notNil) {
            if(isEnabled.not) {
                if(effect == "master") {
                    target.set(\volume, 0.82, \drive, 0, \lowpass, 18000, \highpass, 30);
                    "Syrincs /fx master reset to conservative defaults".postln;
                } {
                    if(effect == "delay") {
                        target.set(\mix, 0, \feedback, 0);
                    } {
                        target.set(\mix, 0);
                    };
                    ("Syrincs /fx " ++ effect ++ " disabled").postln;
                };
            } {
                if(allowed.includes(param).not) {
                    ("Syrincs unknown /fx parameter '" ++ param.asString ++ "' for effect '" ++ effect ++ "'").warn;
                } {
                    clipped = ~clipFxParam.(effect, param, value);
                    target.set(param, clipped);
                    ["Syrincs /fx", effect, param, clipped].postln;
                };
            };
        };
    };

    OSCdef(\syrincsNote, { |msg|
        var preset = msg[1].asString;
        var midiNote = msg[2].asInteger.clip(0, 127);
        var velocity = msg[3].asFloat.clip(0, 1);
        var duration = ~durationFor.(msg[4]);
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
            duration = ~durationFor.(msg[msg.size - 2]);
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

    OSCdef(\syrincsFx, { |msg|
        var effect;
        var enabled;
        var param;
        var value;

        if(msg.size < 5) {
            ("Syrincs invalid /fx message: " ++ msg).warn;
        } {
            effect = msg[1].asString;
            enabled = msg[2].asInteger;
            param = msg[3].asString;
            value = msg[4].asFloat;

            ["Syrincs /fx", effect, enabled, param, value].postln;
            ~setFx.(effect, enabled, param, value);
        };
    }, '/fx', recvPort: 57120);

    ("Syrincs OSC consumer listening on /note, /chord, /drum and /fx at UDP 57120; presets: " ++ ~presets.size).postln;
    }).play;
});
)

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
        cutoff = 7000, rq = 0.5, tone = 185, noise = 0.02, body = 0.9,
        snap = 0.0, spread = 0.02, vel = 1,
        out = 0, reverbOut = 0, delayOut = 0, chorusOut = 0,
        reverbSend = 0, delaySend = 0, chorusSend = 0|

        var velocity = vel.clip(0, 1);
        var env = EnvGen.kr(Env.perc(0.001, decay.clip(0.07, 0.95), 1, -4), doneAction: 2);
        var clickEnv = EnvGen.kr(Env.perc(0.001, 0.012 + (velocity * 0.008), 1, -7));
        var pitch = EnvGen.kr(Env([pitchStart.clip(45, 170), pitchEnd.clip(25, 95)], [0.038 + (decay.clip(0.07, 0.95) * 0.04)], \exp));
        var bodySig = SinOsc.ar(pitch) + LFTri.ar(pitch * 0.5, 0, body.clip(0, 1) * 0.18);
        var clickSig = BPF.ar(WhiteNoise.ar(click.clip(0, 0.35) * (0.65 + (velocity * 0.35))), cutoff.clip(900, 9000), rq.clip(0.08, 1)) * clickEnv;
        var sig = (bodySig * body.clip(0, 1)) + clickSig + (PinkNoise.ar(noise.clip(0, 0.12)) * env * 0.12);
        var voice;

        sig = (sig * (1 + drive.clip(0, 4) + (velocity * 0.18))).tanh;
        voice = Pan2.ar(sig * env * amp.clip(0, 0.95) * 0.86, pan.clip(-1, 1));
        Out.ar(out, voice);
        Out.ar(reverbOut, voice * reverbSend.clip(0, 1));
        Out.ar(delayOut, voice * delaySend.clip(0, 1));
        Out.ar(chorusOut, voice * chorusSend.clip(0, 1));
    }).add;

    SynthDef(\syrincsSnare, { |freq = 120, amp = 0.65, pan = 0, decay = 0.22,
        cutoff = 2100, rq = 0.7, drive = 0, pitchStart = 82,
        pitchEnd = 38, click = 0.05, tone = 185, noise = 0.72,
        body = 0.28, snap = 0.35, spread = 0.02, vel = 1,
        out = 0, reverbOut = 0, delayOut = 0, chorusOut = 0,
        reverbSend = 0, delaySend = 0, chorusSend = 0|

        var velocity = vel.clip(0, 1);
        var env = EnvGen.kr(Env.perc(0.002, decay.clip(0.045, 0.85), 1, -4), doneAction: 2);
        var bodyEnv = EnvGen.kr(Env.perc(0.001, (decay * 0.45).clip(0.035, 0.28), 1, -5));
        var snapEnv = EnvGen.kr(Env.perc(0.001, (0.025 + (velocity * 0.025)).clip(0.015, 0.08), 1, -8));
        var tuned = SinOsc.ar(tone.clip(90, 420), 0, body.clip(0, 1)) + SinOsc.ar(tone.clip(90, 420) * 1.62, 0, body.clip(0, 1) * 0.18);
        var noiseSig = BPF.ar(WhiteNoise.ar(noise.clip(0, 1) * (0.72 + (velocity * 0.35))), cutoff.clip(700, 9500) * (1 + (velocity * 0.08)), rq.clip(0.08, 1));
        var snapSig = HPF.ar(WhiteNoise.ar(snap.clip(0, 1) * (0.5 + velocity)), 4500);
        var sig = (noiseSig * env) + (tuned * bodyEnv) + (snapSig * snapEnv);
        var voice;

        sig = (sig * (1 + drive.clip(0, 4))).tanh;
        voice = Pan2.ar(sig * amp.clip(0, 0.9) * 0.78, pan.clip(-1, 1));
        Out.ar(out, voice);
        Out.ar(reverbOut, voice * reverbSend.clip(0, 1));
        Out.ar(delayOut, voice * delaySend.clip(0, 1));
        Out.ar(chorusOut, voice * chorusSend.clip(0, 1));
    }).add;

    SynthDef(\syrincsHatClosed, { |freq = 120, amp = 0.45, pan = 0,
        decay = 0.075, drive = 0, pitchStart = 82, pitchEnd = 38,
        click = 0.05, cutoff = 7600, rq = 0.5, tone = 185, noise = 0.75,
        body = 0.12, snap = 0.2, spread = 0.02, vel = 1,
        out = 0, reverbOut = 0, delayOut = 0, chorusOut = 0,
        reverbSend = 0, delaySend = 0, chorusSend = 0|

        var velocity = vel.clip(0, 1);
        var bright = (cutoff.clip(3600, 11500) * (1 + (velocity * 0.10))).clip(3600, 12500);
        var env = EnvGen.kr(Env.perc(0.001, decay.clip(0.018, 0.20), 1, -8), doneAction: 2);
        var noiseSig = HPF.ar(WhiteNoise.ar(noise.clip(0, 1)), bright * 0.45);
        var metal = Mix.new([
            BPF.ar(noiseSig, bright * 0.74, rq.clip(0.08, 1)),
            BPF.ar(noiseSig, bright * 1.12, rq.clip(0.08, 1) * 0.8)
        ]) * 0.55;
        var sig = (metal + HPF.ar(WhiteNoise.ar(snap.clip(0, 0.7)), bright)) * env;
        var voice;

        sig = (sig * (1 + drive.clip(0, 2))).tanh;
        voice = Pan2.ar(sig * amp.clip(0, 0.72) * 0.56, pan.clip(-1, 1));
        Out.ar(out, voice);
        Out.ar(reverbOut, voice * reverbSend.clip(0, 1));
        Out.ar(delayOut, voice * delaySend.clip(0, 1));
        Out.ar(chorusOut, voice * chorusSend.clip(0, 1));
    }).add;

    SynthDef(\syrincsHatOpen, { |freq = 120, amp = 0.42, pan = 0,
        decay = 0.42, drive = 0, pitchStart = 82, pitchEnd = 38,
        click = 0.05, cutoff = 6800, rq = 0.42, tone = 185, noise = 0.70,
        body = 0.10, snap = 0.16, spread = 0.02, vel = 1,
        out = 0, reverbOut = 0, delayOut = 0, chorusOut = 0,
        reverbSend = 0, delaySend = 0, chorusSend = 0|

        var velocity = vel.clip(0, 1);
        var bright = (cutoff.clip(3200, 10500) * (1 + (velocity * 0.08))).clip(3200, 11500);
        var env = EnvGen.kr(Env.perc(0.002, decay.clip(0.12, 0.85), 1, -5), doneAction: 2);
        var noiseSig = HPF.ar(WhiteNoise.ar(noise.clip(0, 1)), bright * 0.38);
        var metal = Mix.new([
            BPF.ar(noiseSig, bright * 0.68, rq.clip(0.08, 1)),
            BPF.ar(noiseSig, bright * 1.05, rq.clip(0.08, 1) * 0.75),
            BPF.ar(noiseSig, bright * 1.34, rq.clip(0.08, 1) * 0.62)
        ]) * 0.38;
        var sig = (metal + HPF.ar(WhiteNoise.ar(snap.clip(0, 0.6)), bright)) * env;
        var voice;

        sig = (sig * (1 + drive.clip(0, 2))).tanh;
        voice = Pan2.ar(sig * amp.clip(0, 0.68) * 0.50, pan.clip(-1, 1));
        Out.ar(out, voice);
        Out.ar(reverbOut, voice * reverbSend.clip(0, 1));
        Out.ar(delayOut, voice * delaySend.clip(0, 1));
        Out.ar(chorusOut, voice * chorusSend.clip(0, 1));
    }).add;

    SynthDef(\syrincsTom, { |freq = 120, amp = 0.65, pan = 0, decay = 0.35,
        drive = 0, pitchStart = 160, pitchEnd = 110, click = 0.05,
        cutoff = 7000, rq = 0.5, tone = 185, noise = 0.04, body = 0.85,
        snap = 0.08, spread = 0.02, vel = 1,
        out = 0, reverbOut = 0, delayOut = 0, chorusOut = 0,
        reverbSend = 0, delaySend = 0, chorusSend = 0|

        var velocity = vel.clip(0, 1);
        var env = EnvGen.kr(Env.perc(0.003, decay.clip(0.10, 0.95), 1, -4), doneAction: 2);
        var pitch = EnvGen.kr(Env([pitchStart.clip(60, 360), pitchEnd.clip(45, 260)], [(decay * 0.38).clip(0.05, 0.32)], \exp));
        var bodySig = SinOsc.ar(pitch) + LFTri.ar(pitch * 1.01, 0, 0.34);
        var attack = BPF.ar(WhiteNoise.ar(noise.clip(0, 0.25) * (0.6 + velocity)), cutoff.clip(500, 9000), rq.clip(0.08, 1)) * EnvGen.kr(Env.perc(0.001, 0.028, 1, -8));
        var sig = (bodySig * body.clip(0, 1) * env) + attack;
        var voice;

        sig = (sig * (1 + drive.clip(0, 3))).tanh;
        voice = Pan2.ar(sig * amp.clip(0, 0.85) * 0.72, pan.clip(-1, 1));
        Out.ar(out, voice);
        Out.ar(reverbOut, voice * reverbSend.clip(0, 1));
        Out.ar(delayOut, voice * delaySend.clip(0, 1));
        Out.ar(chorusOut, voice * chorusSend.clip(0, 1));
    }).add;

    SynthDef(\syrincsClap, { |freq = 120, amp = 0.58, pan = 0, decay = 0.28,
        drive = 0, pitchStart = 82, pitchEnd = 38, click = 0.05,
        cutoff = 2600, rq = 0.55, tone = 185, noise = 0.78, body = 0.18,
        snap = 0.36, spread = 0.024, vel = 1,
        out = 0, reverbOut = 0, delayOut = 0, chorusOut = 0,
        reverbSend = 0, delaySend = 0, chorusSend = 0|

        var velocity = vel.clip(0, 1);
        var cluster = EnvGen.kr(Env([0, 1, 0, 0.82, 0, 0.62, 0], [0.002, spread.clip(0.008, 0.055), 0.002, spread.clip(0.008, 0.055), 0.002, decay.clip(0.08, 0.65)], -5), doneAction: 2);
        var tail = EnvGen.kr(Env.perc(0.004, decay.clip(0.08, 0.7), 1, -4));
        var noiseSig = BPF.ar(WhiteNoise.ar(noise.clip(0, 1) * (0.72 + (velocity * 0.25))), cutoff.clip(900, 7000), rq.clip(0.08, 1));
        var snapSig = HPF.ar(WhiteNoise.ar(snap.clip(0, 1)), 1800) * tail * 0.42;
        var sig = (noiseSig * cluster) + snapSig + (SinOsc.ar(tone.clip(120, 600), 0, body.clip(0, 0.5)) * tail * 0.18);
        var voice;

        sig = (sig * (1 + drive.clip(0, 3))).tanh;
        voice = Pan2.ar(sig * amp.clip(0, 0.82) * 0.70, pan.clip(-1, 1));
        Out.ar(out, voice);
        Out.ar(reverbOut, voice * reverbSend.clip(0, 1));
        Out.ar(delayOut, voice * delaySend.clip(0, 1));
        Out.ar(chorusOut, voice * chorusSend.clip(0, 1));
    }).add;

    SynthDef(\syrincsRim, { |freq = 120, amp = 0.48, pan = 0, decay = 0.075,
        drive = 0, pitchStart = 82, pitchEnd = 38, click = 0.05,
        cutoff = 5200, rq = 0.45, tone = 980, noise = 0.18, body = 0.55,
        snap = 0.45, spread = 0.02, vel = 1,
        out = 0, reverbOut = 0, delayOut = 0, chorusOut = 0,
        reverbSend = 0, delaySend = 0, chorusSend = 0|

        var velocity = vel.clip(0, 1);
        var env = EnvGen.kr(Env.perc(0.001, decay.clip(0.025, 0.18), 1, -7), doneAction: 2);
        var bodySig = SinOsc.ar(tone.clip(350, 2200), 0, body.clip(0, 1)) + SinOsc.ar(tone.clip(350, 2200) * 1.7, 0, body.clip(0, 1) * 0.24);
        var snapSig = BPF.ar(WhiteNoise.ar((noise + snap).clip(0, 1) * (0.5 + velocity)), cutoff.clip(1400, 10000), rq.clip(0.08, 1));
        var sig = (bodySig + snapSig) * env;
        var voice;

        sig = (sig * (1 + drive.clip(0, 3))).tanh;
        voice = Pan2.ar(sig * amp.clip(0, 0.78) * 0.62, pan.clip(-1, 1));
        Out.ar(out, voice);
        Out.ar(reverbOut, voice * reverbSend.clip(0, 1));
        Out.ar(delayOut, voice * delaySend.clip(0, 1));
        Out.ar(chorusOut, voice * chorusSend.clip(0, 1));
    }).add;

    SynthDef(\syrincsClick, { |freq = 120, amp = 0.38, pan = 0, decay = 0.035,
        drive = 0, pitchStart = 82, pitchEnd = 38, click = 0.05,
        cutoff = 6500, rq = 0.35, tone = 1800, noise = 0.15, body = 0.42,
        snap = 0.35, spread = 0.02, vel = 1,
        out = 0, reverbOut = 0, delayOut = 0, chorusOut = 0,
        reverbSend = 0, delaySend = 0, chorusSend = 0|

        var velocity = vel.clip(0, 1);
        var env = EnvGen.kr(Env.perc(0.001, decay.clip(0.012, 0.09), 1, -8), doneAction: 2);
        var toneSig = SinOsc.ar(tone.clip(700, 3600), 0, body.clip(0, 1));
        var snapSig = HPF.ar(WhiteNoise.ar((noise + snap).clip(0, 1) * (0.45 + velocity)), cutoff.clip(2000, 12000));
        var sig = (toneSig + snapSig) * env;
        var voice;

        sig = (sig * (1 + drive.clip(0, 2))).tanh;
        voice = Pan2.ar(sig * amp.clip(0, 0.65) * 0.52, pan.clip(-1, 1));
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
        noise: 0.0,
        body: 0.8,
        snap: 0.2,
        spread: 0.02,
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

    ~putPreset.("drum.kick", "drum", "Defined synthetic kick", (synth: \syrincsKick, ampScale: 0.78, decay: 0.30, drive: 0.12, pitchStart: 92, pitchEnd: 42, click: 0.055, cutoff: 3900, rq: 0.45, body: 0.92, noise: 0.018, drum: true));
    ~putPreset.("drum.kick.deep", "drum", "Deeper longer kick", (synth: \syrincsKick, ampScale: 0.82, decay: 0.48, drive: 0.18, pitchStart: 76, pitchEnd: 32, click: 0.035, cutoff: 3200, rq: 0.42, body: 0.96, noise: 0.012, drum: true));
    ~putPreset.("drum.kick.short", "drum", "Short tight kick", (synth: \syrincsKick, ampScale: 0.72, decay: 0.17, drive: 0.10, pitchStart: 104, pitchEnd: 46, click: 0.075, cutoff: 4600, rq: 0.42, body: 0.82, noise: 0.016, drum: true));

    ~putPreset.("drum.snare", "drum", "Balanced synthetic snare", (synth: \syrincsSnare, ampScale: 0.58, decay: 0.22, cutoff: 2300, rq: 0.68, drive: 0.08, tone: 185, noise: 0.70, body: 0.30, snap: 0.36, reverbSend: 0.11, drum: true));
    ~putPreset.("drum.snare.tight", "drum", "Tight short snare", (synth: \syrincsSnare, ampScale: 0.54, decay: 0.12, cutoff: 3200, rq: 0.52, drive: 0.14, tone: 210, noise: 0.58, body: 0.24, snap: 0.46, reverbSend: 0.07, drum: true));
    ~putPreset.("drum.snare.noisy", "drum", "Noisy wider snare", (synth: \syrincsSnare, ampScale: 0.55, decay: 0.34, cutoff: 1800, rq: 0.82, drive: 0.06, tone: 165, noise: 0.92, body: 0.22, snap: 0.54, reverbSend: 0.16, drum: true));

    ~putPreset.("drum.hat.closed", "drum", "Closed hi-hat", (synth: \syrincsHatClosed, ampScale: 0.32, decay: 0.065, cutoff: 7200, rq: 0.48, noise: 0.68, snap: 0.16, pan: -0.10, reverbSend: 0.025, drum: true));
    ~putPreset.("drum.hat.open", "drum", "Open hi-hat", (synth: \syrincsHatOpen, ampScale: 0.30, decay: 0.40, cutoff: 6500, rq: 0.42, noise: 0.62, snap: 0.12, pan: -0.14, reverbSend: 0.04, drum: true));

    ~putPreset.("drum.tom.low", "drum", "Low tom", (synth: \syrincsTom, ampScale: 0.54, pitchStart: 150, pitchEnd: 84, decay: 0.44, drive: 0.06, cutoff: 2300, noise: 0.045, body: 0.88, pan: -0.18, reverbSend: 0.10, drum: true));
    ~putPreset.("drum.tom.mid", "drum", "Mid tom", (synth: \syrincsTom, ampScale: 0.50, pitchStart: 190, pitchEnd: 116, decay: 0.36, drive: 0.05, cutoff: 3000, noise: 0.040, body: 0.84, pan: 0.02, reverbSend: 0.09, drum: true));
    ~putPreset.("drum.tom.high", "drum", "High tom", (synth: \syrincsTom, ampScale: 0.47, pitchStart: 245, pitchEnd: 155, decay: 0.28, drive: 0.04, cutoff: 3800, noise: 0.035, body: 0.80, pan: 0.18, reverbSend: 0.08, drum: true));

    ~putPreset.("drum.clap", "drum", "Synthetic hand clap", (synth: \syrincsClap, ampScale: 0.48, decay: 0.30, cutoff: 2600, rq: 0.58, noise: 0.82, body: 0.12, snap: 0.38, spread: 0.026, pan: 0.08, reverbSend: 0.18, delaySend: 0.035, drum: true));
    ~putPreset.("drum.rim", "drum", "Short rimshot accent", (synth: \syrincsRim, ampScale: 0.42, decay: 0.075, cutoff: 5400, rq: 0.44, tone: 980, noise: 0.16, body: 0.55, snap: 0.42, pan: 0.10, reverbSend: 0.07, delaySend: 0.025, drum: true));
    ~putPreset.("drum.click", "drum", "Small click for metronome accents", (synth: \syrincsClick, ampScale: 0.34, decay: 0.032, cutoff: 6900, rq: 0.35, tone: 1850, noise: 0.12, body: 0.44, snap: 0.32, pan: 0.0, reverbSend: 0.02, drum: true));

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
        var vel = velocity.asFloat.clip(0, 1);
        var amp = ~ampFor.(vel, preset, 0.55);
        var outPan = ~panFor.(pan, preset);
        var cutoff = ~fallback.(preset[\cutoff], 7000) * (1 + (vel * 0.08));

        Synth(synth, [
            \freq, ~fallback.(preset[\freq], 120),
            \amp, amp,
            \pan, outPan,
            \decay, ~fallback.(preset[\decay], 0.3),
            \drive, (~fallback.(preset[\drive], 0) + (vel * 0.08)).clip(0, 4),
            \pitchStart, ~fallback.(preset[\pitchStart], 82),
            \pitchEnd, ~fallback.(preset[\pitchEnd], 38),
            \click, (~fallback.(preset[\click], 0.05) * (0.8 + (vel * 0.45))).clip(0, 0.35),
            \cutoff, cutoff,
            \rq, ~fallback.(preset[\rq], 0.5),
            \tone, ~fallback.(preset[\tone], 185),
            \noise, (~fallback.(preset[\noise], 0.0) * (0.75 + (vel * 0.35))).clip(0, 1),
            \body, ~fallback.(preset[\body], 0.8),
            \snap, (~fallback.(preset[\snap], 0.2) * (0.7 + (vel * 0.45))).clip(0, 1),
            \spread, ~fallback.(preset[\spread], 0.02),
            \vel, vel,
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

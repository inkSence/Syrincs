(
thisProcess.openUDPPort(57120);
s.boot;

s.waitForBoot({
    SynthDef(\syrincsBasic, { |freq = 440, amp = 0.2, sustain = 0.5, pan = 0|
        var gate = Line.kr(1, 0, sustain.max(0.01));
        var env = EnvGen.kr(Env.adsr(0.01, 0.08, 0.65, 0.12), gate: gate, doneAction: 2);
        var sig = SinOsc.ar(freq) * env * amp;
        Out.ar(0, Pan2.ar(sig, pan));
    }).add;

    OSCdef(\syrincsNote, { |msg|
        var synth = msg[1].asString;
        var midiNote = msg[2].asInteger.clip(0, 127);
        var velocity = msg[3].asFloat.clip(0, 1);
        var duration = msg[4].asFloat.max(0.01);
        var pan = if(msg.size > 5) { msg[5].asFloat.clip(-1, 1) } { 0.0 };

        ["Syrincs /note", synth, midiNote, velocity, duration, pan].postln;

        Synth(\syrincsBasic, [
            \freq, midiNote.midicps,
            \amp, velocity * 0.25,
            \sustain, duration,
            \pan, pan
        ]);
    }, '/note', recvPort: 57120);

    "Syrincs OSC consumer listening on /note at UDP 57120".postln;
});
)

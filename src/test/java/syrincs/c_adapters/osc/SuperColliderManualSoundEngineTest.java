package syrincs.c_adapters.osc;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Manual audio integration test for the SuperCollider sound engine.
 *
 * Run only when the SuperCollider consumer is already running:
 * mvn -Dtest=SuperColliderManualSoundEngineTest -DrunScAudioTest=true test
 */
class SuperColliderManualSoundEngineTest {
    private static final String RUN_PROPERTY = "runScAudioTest";
    private static final long STEP_MS = Long.getLong("sc.audioTestStepMillis", 320L);

    @Test
    void playsManualSoundEngineTour() throws Exception {
        assumeTrue(
                Boolean.getBoolean(RUN_PROPERTY),
                "Manual audio test skipped. Run with -D" + RUN_PROPERTY + "=true after starting the SuperCollider consumer."
        );

        String host = System.getProperty("sc.host", SuperColliderOscOutputAdapter.DEFAULT_HOST);
        int port = Integer.getInteger("sc.port", SuperColliderOscOutputAdapter.DEFAULT_PORT);
        SuperColliderOscOutputAdapter sc = new SuperColliderOscOutputAdapter(
                host,
                port,
                SuperColliderOscOutputAdapter.DEFAULT_PRESET,
                SuperColliderOscOutputAdapter.DEFAULT_PAN
        );

        System.out.println("[SC-MANUAL] Sending manual sound engine tour to " + host + ":" + port);
        System.out.println("[SC-MANUAL] If there is no sound, check that supercollider/syrincs_osc_consumer.scd is running.");

        try {
            resetSound(sc);
            sleepMillis(700);

            section("A Basic presets");
            playBasicPresets(sc);

            section("B Same chord through synth families");
            playFamilyChordTour(sc);

            section("C Hindemith-like chord clarity");
            playHindemithStyleChords(sc);

            section("D Synthetic drums");
            playDrumTour(sc);

            section("E Global effects");
            playEffectsTour(sc);

            section("F Automation");
            playAutomationTour(sc);

            section("G Scenes and roles");
            playSceneRoleTour(sc);

            section("H Sample fallbacks");
            playSampleFallbackTour(sc);
        } finally {
            try {
                resetSound(sc);
                System.out.println("[SC-MANUAL] Reset conservative FX and master values.");
            } catch (IOException e) {
                System.out.println("[SC-MANUAL] Could not reset SuperCollider state: " + e.getMessage());
            }
        }
    }

    private static void playBasicPresets(SuperColliderOscOutputAdapter sc) throws Exception {
        String[] presets = {
                "test.sine",
                "test.triangle",
                "test.saw",
                "test.pulse",
                "test.noise"
        };
        int[] notes = {60, 62, 64, 65, 67};
        double[] pans = {-0.55, -0.25, 0.0, 0.25, 0.55};

        for (int i = 0; i < presets.length; i++) {
            sc.sendNote(presets[i], notes[i], 0.58, 0.45, pans[i]);
            sleepMillis(650);
        }
    }

    private static void playFamilyChordTour(SuperColliderOscOutputAdapter sc) throws Exception {
        int[] chord = {48, 55, 60, 64};
        String[] presets = {
                "organ.full",
                "pad.warm",
                "strings.pad",
                "brass.soft",
                "wind.fluteish",
                "keys.fm_epiano",
                "pluck.harplike"
        };

        for (String preset : presets) {
            sc.sendChord(preset, chord, 0.56, 1.35, 0.0);
            sleepMillis(1_650);
        }
    }

    private static void playHindemithStyleChords(SuperColliderOscOutputAdapter sc) throws Exception {
        int[][] chords = {
                {48, 55, 60, 66},
                {50, 57, 62, 68},
                {47, 54, 60, 65},
                {52, 59, 64, 70}
        };

        playProgression(sc, "organ.full", chords, 0.58, 1.15, 1_400);
        playProgression(sc, "strings.slow", chords, 0.48, 1.5, 1_650);

        sc.sendScene("scene.hindemith_lab");
        sleepMillis(600);
        playProgression(sc, "role:harmony", chords, 0.52, 1.35, 1_500);
    }

    private static void playDrumTour(SuperColliderOscOutputAdapter sc) throws Exception {
        String[] drums = {
                "drum.kick",
                "drum.kick.deep",
                "drum.snare",
                "drum.snare.tight",
                "drum.hat.closed",
                "drum.hat.open",
                "drum.tom.low",
                "drum.tom.mid",
                "drum.tom.high",
                "drum.clap",
                "drum.rim",
                "drum.click"
        };

        for (String drum : drums) {
            sc.sendDrum(drum, 0.68, 0.0);
            sleepMillis(360);
        }

        sleepMillis(500);
        playDrumPattern(sc);
    }

    private static void playEffectsTour(SuperColliderOscOutputAdapter sc) throws Exception {
        int[] chord = {48, 55, 60, 64};

        sc.sendFx("reverb", true, "mix", 0.0);
        sc.sendFx("delay", true, "mix", 0.0);
        sc.sendFx("chorus", true, "mix", 0.0);
        sleepMillis(350);

        sc.sendChord("organ.full", chord, 0.58, 1.2, 0.0);
        sleepMillis(1_500);

        sc.sendFx("reverb", true, "mix", 0.25);
        sc.sendFx("reverb", true, "room", 0.68);
        sleepMillis(350);
        sc.sendChord("organ.full", chord, 0.58, 1.6, 0.0);
        sleepMillis(1_900);

        sc.sendFx("chorus", true, "mix", 0.20);
        sc.sendChord("pad.warm", chord, 0.48, 1.8, 0.0);
        sleepMillis(2_100);

        sc.sendFx("delay", true, "mix", 0.18);
        sc.sendFx("delay", true, "feedback", 0.30);
        playArpeggio(sc, "pluck.harplike", new int[]{60, 64, 67, 72, 67, 64}, 0.62, 0.35, 230);
        sleepMillis(900);

        sc.sendFx("delay", true, "feedback", 0.12);
        sc.sendFx("delay", true, "mix", 0.0);
    }

    private static void playAutomationTour(SuperColliderOscOutputAdapter sc) throws Exception {
        int[] chord = {48, 55, 60, 64};

        sc.sendSet("family:pad", "cutoff", 900.0);
        sleepMillis(350);
        sc.sendChord("pad.warm", chord, 0.48, 1.2, 0.0);
        sleepMillis(1_400);

        sc.sendRamp("family:pad", "cutoff", 5_000.0, 4.0);
        for (int i = 0; i < 4; i++) {
            sc.sendChord("pad.warm", chord, 0.48, 1.0, 0.0);
            sleepMillis(1_100);
        }

        sc.sendRamp("reverb", "mix", 0.35, 2.0);
        sleepMillis(700);
        sc.sendChord("strings.pad", chord, 0.48, 1.5, 0.0);
        sleepMillis(1_800);
        sc.sendRamp("reverb", "mix", 0.12, 2.0);
        sleepMillis(1_000);
    }

    private static void playSceneRoleTour(SuperColliderOscOutputAdapter sc) throws Exception {
        int[] harmony = {48, 55, 60, 64};

        sc.sendScene("scene.chorale");
        sleepMillis(700);
        playRolePhrase(sc, harmony);

        sc.sendScene("scene.electronic");
        sleepMillis(700);
        playRolePhrase(sc, harmony);

        sc.sendScene("scene.hindemith_lab");
        sleepMillis(500);
    }

    private static void playSampleFallbackTour(SuperColliderOscOutputAdapter sc) throws Exception {
        sc.sendDrum("drum.kick.sample", 0.82, -0.10);
        sleepMillis(360);
        sc.sendDrum("drum.snare.sample", 0.70, 0.10);
        sleepMillis(360);
        sc.sendDrum("drum.hat.closed.sample", 0.50, -0.25);
        sleepMillis(360);
        sc.sendDrum("drum.hat.open.sample", 0.48, 0.25);
        sleepMillis(550);

        sc.sendNote("keys.piano.sample", 60, 0.64, 0.9, -0.15);
        sleepMillis(1_000);
        sc.sendNote("pluck.sample", 64, 0.64, 0.65, 0.15);
        sleepMillis(850);
        sc.sendChord("strings.sample", new int[]{48, 55, 60, 64}, 0.50, 1.5, 0.0);
        sleepMillis(1_700);
    }

    private static void playRolePhrase(SuperColliderOscOutputAdapter sc, int[] harmony) throws Exception {
        sc.sendNote("role:bass", 36, 0.62, 1.0, -0.15);
        sc.sendChord("role:harmony", harmony, 0.54, 1.25, 0.0);
        sleepMillis(1_450);
        sc.sendNote("role:melody", 72, 0.58, 0.6, 0.18);
        sleepMillis(700);
        playArpeggio(sc, "role:counter", new int[]{67, 64, 60, 57}, 0.50, 0.28, 260);
        sleepMillis(650);
    }

    private static void playProgression(
            SuperColliderOscOutputAdapter sc,
            String preset,
            int[][] chords,
            double velocity,
            double durationSeconds,
            long waitMillis
    ) throws Exception {
        for (int[] chord : chords) {
            sc.sendChord(preset, chord, velocity, durationSeconds, 0.0);
            sleepMillis(waitMillis);
        }
    }

    private static void playArpeggio(
            SuperColliderOscOutputAdapter sc,
            String preset,
            int[] notes,
            double velocity,
            double durationSeconds,
            long stepMillis
    ) throws Exception {
        double pan = -0.25;
        for (int note : notes) {
            sc.sendNote(preset, note, velocity, durationSeconds, pan);
            pan = -pan;
            sleepMillis(stepMillis);
        }
    }

    private static void playDrumPattern(SuperColliderOscOutputAdapter sc) throws Exception {
        for (int step = 0; step < 32; step++) {
            if (step % 2 == 0) {
                sc.sendDrum("drum.hat.closed", 0.42, -0.28);
            }
            if (step == 14 || step == 30) {
                sc.sendDrum("drum.hat.open", 0.46, 0.28);
            }
            if (step == 0 || step == 16) {
                sc.sendDrum("drum.kick", 0.84, 0.0);
            }
            if (step == 8 || step == 24) {
                sc.sendDrum("drum.kick.deep", 0.76, -0.05);
            }
            if (step == 4 || step == 12 || step == 20 || step == 28) {
                sc.sendDrum("drum.snare", 0.72, 0.08);
            }
            if (step == 12 || step == 28) {
                sc.sendDrum("drum.clap", 0.48, 0.18);
            }
            if (step == 25) {
                sc.sendDrum("drum.tom.low", 0.66, -0.22);
            }
            if (step == 27) {
                sc.sendDrum("drum.tom.mid", 0.63, 0.0);
            }
            if (step == 29) {
                sc.sendDrum("drum.tom.high", 0.60, 0.22);
            }
            if (step == 31) {
                sc.sendDrum("drum.rim", 0.50, 0.12);
            }
            sleepMillis(185);
        }
    }

    private static void resetSound(SuperColliderOscOutputAdapter sc) throws IOException {
        sc.sendFx("master", true, "volume", 0.8);
        sc.sendFx("master", true, "drive", 0.0);
        sc.sendFx("reverb", true, "mix", 0.12);
        sc.sendFx("delay", true, "mix", 0.0);
        sc.sendFx("delay", true, "feedback", 0.12);
        sc.sendFx("chorus", true, "mix", 0.0);
        sc.sendSet("family:pad", "cutoff", 2_600.0);
        sc.sendSet("preset:pad.warm", "reverbSend", 0.22);
        sc.sendSet("preset:pad.warm", "chorusSend", 0.18);
    }

    private static void section(String name) throws InterruptedException {
        System.out.println("[SC-MANUAL] " + name);
        sleepMillis(STEP_MS);
    }

    private static void sleepMillis(long millis) throws InterruptedException {
        Thread.sleep(millis);
    }
}

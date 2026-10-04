import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import syrincs.a_domain.rhythm.HuffmanRhythm;
import syrincs.a_domain.rhythm.RhythmMapperFromOnsetStringToKickAndSnare;
import syrincs.b_application.AppDefaults;
import syrincs.b_application.ValidatePatternsUseCase;
import syrincs.c_adapters.RhythmFileParser;

/** Local documentation helper: exports real Syrincs mapping without opening MIDI. */
public class OnsetsToRdl {
    public static void main(String[] args) {
        try {
            export(args);
        } catch (Exception e) {
            System.err.println("[ERROR] " + e.getMessage());
            System.exit(1);
        }
    }

    private static void export(String[] args) throws Exception {
        if (args.length < 3) {
            throw new IllegalArgumentException("Aufruf: onsets-zu-rdl.sh DATEI.rdl WIEDERHOLUNGEN "
                    + "\"ONSETS\" [\"ONSETS\" ...] oder DATEI.rdl WIEDERHOLUNGEN --from-output AUSGABE.txt");
        }
        Path output = Path.of(args[0]);
        int repeats = Integer.parseInt(args[1]);
        if (repeats < 1 || repeats > 64) {
            throw new IllegalArgumentException("Wiederholungen müssen zwischen 1 und 64 liegen.");
        }
        List<String> inputs = new ArrayList<>();
        if (args[2].equals("--from-output")) {
            if (args.length != 4) throw new IllegalArgumentException("--from-output benötigt genau eine Ausgabedatei.");
            var rhythmField = Pattern.compile("\\bRhythm=([xoXO]+)(?=\\s*(?:\\||$))");
            var meterField = Pattern.compile("\\bTime=([^|\\s]+)");
            for (String line : Files.readAllLines(Path.of(args[3]))) {
                if (!line.startsWith("[CANDIDATE]") && !line.startsWith("[SELECTED]")
                        && !line.startsWith("[ANALYZE]")) continue;
                var meter = meterField.matcher(line);
                if (meter.find() && !meter.group(1).equals("4/4")) {
                    throw new IllegalArgumentException("Der Hörhelfer unterstützt nur 4/4; gefunden: " + meter.group(1));
                }
                var rhythm = rhythmField.matcher(line);
                if (!rhythm.find()) throw new IllegalArgumentException("Kein lesbares Rhythm-Feld: " + line);
                inputs.add(rhythm.group(1));
            }
        } else {
            for (int i = 2; i < args.length; i++) inputs.add(args[i]);
        }
        if (inputs.isEmpty()) throw new IllegalArgumentException("Keine Rhythmen in der Eingabe; keine RDL-Datei erzeugt.");

        var mapper = RhythmMapperFromOnsetStringToKickAndSnare.defaultMapper(
                RhythmMapperFromOnsetStringToKickAndSnare.Style.DEFAULT);
        StringBuilder kick = new StringBuilder();
        StringBuilder snare = new StringBuilder();
        List<String> normalizedInputs = new ArrayList<>();
        for (String input : inputs) {
            var rhythm = new HuffmanRhythm(4, 4, AppDefaults.DEFAULT_TEMPO_BPM, input);
            String onsets = rhythm.getOnsetList();
            normalizedInputs.add(onsets);
            for (int offset = 0; offset < onsets.length(); offset += 16) {
                var mapped = mapper.map(onsets.substring(offset, offset + 16));
                kick.append(mapped.bdMask.replace('o', '-'));
                snare.append(mapped.snMask.replace('o', '-'));
            }
        }
        int bars = Math.multiplyExact(kick.length() / 16, repeats);
        StringBuilder rdl = new StringBuilder("# Lokale Hörtour: Original-Onsets in Eingabereihenfolge\n");
        for (int i = 0; i < normalizedInputs.size(); i++) {
            rdl.append("# Rhythm ").append(i + 1).append(": ").append(normalizedInputs.get(i)).append('\n');
        }
        rdl.append("# Die gesamte Folge wird ").append(repeats).append(" Mal gespielt.\n")
                .append("time: 4/4\ntempo: ").append(AppDefaults.DEFAULT_TEMPO_BPM)
                .append("\nres-per-beat: 4\nbars: ").append(bars).append("\n\n");
        for (var voice : RhythmMapperFromOnsetStringToKickAndSnare.defaultVoiceSpecs()) {
            rdl.append("voice ").append(voice.name).append(" note=").append(voice.note)
                    .append(" channel=").append(voice.channel).append(" vel=").append(voice.velocity)
                    .append(" gate=").append(voice.gatePercent).append('\n');
        }
        rdl.append("\npattern kick: ").append(group(kick.toString().repeat(repeats)))
                .append("\npattern snare: ").append(group(snare.toString().repeat(repeats))).append('\n');

        // Validate using the production parser and validator before replacing the requested file.
        Path temporary = Files.createTempFile("syrincs-hoertour-", ".rdl");
        try {
            Files.writeString(temporary, rdl);
            var parsed = new RhythmFileParser().parse(temporary.toString());
            new ValidatePatternsUseCase().validate(parsed.pattern, parsed.spec, parsed.voices);
            if (output.toAbsolutePath().getParent() != null) Files.createDirectories(output.toAbsolutePath().getParent());
            Files.writeString(output, rdl);
        } finally {
            Files.deleteIfExists(temporary);
        }
        System.out.println("[RDL] " + output + " | Bars=" + bars + " | Repeats=" + repeats);
        for (int i = 0; i < normalizedInputs.size(); i++) {
            System.out.println("[SOURCE] Position=" + (i + 1) + " | Rhythm=" + normalizedInputs.get(i));
        }
    }

    private static String group(String steps) {
        StringBuilder grouped = new StringBuilder("|");
        for (int i = 0; i < steps.length(); i += 4) grouped.append(' ').append(steps, i, i + 4).append(" |");
        return grouped.toString();
    }
}

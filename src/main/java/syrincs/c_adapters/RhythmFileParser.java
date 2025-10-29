package syrincs.c_adapters;

import syrincs.a_domain.rhythm.Pattern;
import syrincs.a_domain.rhythm.PatternHeader;
import syrincs.a_domain.rhythm.RhythmSpec;
import syrincs.a_domain.rhythm.VoiceSpec;

import java.io.*;
import java.util.*;

/**
 * Minimal RDL-0 parser according to spec. Ignores '|' tokens. Comments start with '#'.
 */
public class RhythmFileParser {

    public static class MidiData {
        public final RhythmSpec spec;
        public final Pattern pattern;
        public final List<VoiceSpec> voices; // parsed voices with channel/note/vel
        public MidiData(RhythmSpec spec, Pattern pattern, List<VoiceSpec> voices) {
            this.spec = spec; this.pattern = pattern; this.voices = voices;
        }
    }

    // Lightweight state holder for parsing
    private static class ParseState {
        Integer timeNum, timeDen, tempo, resPerBeat, bars;
        final Pattern pattern = new Pattern();
        final List<VoiceSpec> voices = new ArrayList<>();
        final Map<String, List<Boolean>> tmpPatterns = new LinkedHashMap<>();
    }

    public MidiData parse(String file) throws IOException, ParseException {
        try (BufferedReader br = new BufferedReader(openReader(file))) {
            ParseState st = new ParseState();
            String line; int lineNo = 0;
            while ((line = br.readLine()) != null) {
                lineNo++;
                String raw = stripCommentsAndTrim(line);
                if (raw.isEmpty()) continue;
                if (!dispatchLine(raw, lineNo, st)) {
                    throw new ParseException(lineNo, "Unknown statement: '"+raw+"'");
                }
            }
            finalizePatterns(st);
            PatternHeader header = new PatternHeader(st.timeNum, st.timeDen, st.tempo, st.resPerBeat, st.bars);
            RhythmSpec spec = toRhythmSpec(header);
            return new MidiData(spec, st.pattern, st.voices);
        }
    }

    private Reader openReader(String inFile) throws FileNotFoundException {
        if (inFile != null && !inFile.isBlank()) {
            return new FileReader(inFile);
        }
        throw new IllegalArgumentException("No rhythm input file.");
    }

    private String stripCommentsAndTrim(String line) {
        int h = line.indexOf('#');
        if (h >= 0) line = line.substring(0, h);
        return line.trim();
    }

    private boolean dispatchLine(String raw, int lineNo, ParseState st) throws ParseException {
        if (raw.startsWith("time:")) { parseTime(raw, lineNo, st); return true; }
        if (raw.startsWith("tempo:")) { st.tempo = parseIntStrict(raw.substring(7).trim(), lineNo, "tempo"); return true; }
        if (raw.startsWith("res-per-beat:")) { st.resPerBeat = parseIntStrict(raw.substring(14).trim(), lineNo, "res-per-beat"); return true; }
        if (raw.startsWith("bars:")) { st.bars = parseIntStrict(raw.substring(6).trim(), lineNo, "bars"); return true; }
        if (raw.startsWith("voice ")) { parseVoice(raw, lineNo, st); return true; }
        if (raw.startsWith("pattern ")) { parsePattern(raw, lineNo, st); return true; }
        return false;
    }

    private void parseTime(String raw, int lineNo, ParseState st) throws ParseException {
        String v = raw.substring(5).trim();
        String[] xy = v.split("/");
        if (xy.length != 2) throw new ParseException(lineNo, "Invalid time signature: '"+v+"'");
        st.timeNum = parseIntStrict(xy[0].trim(), lineNo, "time numerator");
        st.timeDen = parseIntStrict(xy[1].trim(), lineNo, "time denominator");
    }

    private void parseVoice(String raw, int lineNo, ParseState st) throws ParseException {
        // voice <ident>  note=36 channel=10 vel=90 [gate=...]
        String rest = raw.substring(6).trim();
        String[] parts = rest.split("\\s+");
        if (parts.length < 4) throw new ParseException(lineNo, "Invalid voice declaration: '"+raw+"'");
        String name = parts[0];
        Integer note = null, channel = null, vel = null, gate = null;
        for (int i = 1; i < parts.length; i++) {
            String p = parts[i];
            if (p.startsWith("note=")) note = parseIntStrict(p.substring(5), lineNo, "note");
            else if (p.startsWith("channel=")) channel = parseIntStrict(p.substring(8), lineNo, "channel");
            else if (p.startsWith("vel=")) vel = parseIntStrict(p.substring(4), lineNo, "vel");
            else if (p.startsWith("gate=")) gate = parseIntStrict(p.substring(5), lineNo, "gate");
        }
        if (note == null || channel == null || vel == null)
            throw new ParseException(lineNo, "Voice declaration requires note=, channel=, vel=");
        int gatePercent = (gate != null) ? gate : 50;
        st.voices.add(new VoiceSpec(name, channel, note, vel, gatePercent));
    }

    private void parsePattern(String raw, int lineNo, ParseState st) throws ParseException {
        int colon = raw.indexOf(':');
        if (colon < 0) throw new ParseException(lineNo, "Pattern line missing colon: '"+raw+"'");
        String name = raw.substring(8, colon).trim();
        String body = raw.substring(colon + 1);
        List<Boolean> hits = st.tmpPatterns.computeIfAbsent(name, k -> new ArrayList<>());
        for (int i = 0; i < body.length(); i++) {
            char c = body.charAt(i);
            if (c == 'x' || c == 'X') hits.add(Boolean.TRUE);
            else if (c == '-') hits.add(Boolean.FALSE);
            else if (c == '|' || Character.isWhitespace(c)) {
                // ignore
            } else {
                throw new ParseException(lineNo, "Invalid token '" + c + "' in pattern for voice '" + name + "'");
            }
        }
    }

    private void finalizePatterns(ParseState st) {
        for (var e : st.tmpPatterns.entrySet()) {
            List<Boolean> list = e.getValue();
            boolean[] arr = new boolean[list.size()];
            for (int i = 0; i < arr.length; i++) arr[i] = list.get(i);
            st.pattern.put(e.getKey(), arr);
        }
    }

    private RhythmSpec toRhythmSpec(PatternHeader h) {
        int tn = h.timeNum() != null ? h.timeNum() : 4;
        int td = h.timeDen() != null ? h.timeDen() : 4;
        int tempo = h.tempo() != null ? h.tempo() : 120;
        int rpb = h.resPerBeat() != null ? h.resPerBeat() : 4;
        int bars = h.bars() != null ? h.bars() : 1;
        return new RhythmSpec(tn, td, tempo, rpb, bars);
    }

    private int parseIntStrict(String s, int lineNo, String what) throws ParseException {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException nfe) {
            throw new ParseException(lineNo, "Invalid integer for "+what+": '"+s+"'");
        }
    }

    public static class ParseException extends Exception {
        public final int line;
        public ParseException(int line, String msg) { super("Line "+line+": "+msg); this.line = line; }
    }
}

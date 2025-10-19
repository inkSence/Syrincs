package syrincs.c_adapters;

import syrincs.a_domain.rhythm.Pattern;
import syrincs.a_domain.rhythm.PatternHeader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.*;

/**
 * Minimal RDL-0 parser according to spec. Ignores '|' tokens. Comments start with '#'.
 */
public class RhythmFileParser {

    public static class Result {
        public final PatternHeader header;
        public final Pattern pattern;
        public final Map<String, VoiceDecl> voices; // parsed voices with channel/note/vel
        public Result(PatternHeader header, Pattern pattern, Map<String, VoiceDecl> voices) {
            this.header = header; this.pattern = pattern; this.voices = voices;
        }
    }

    public static class VoiceDecl {
        public final String name; public final int note; public final int channel; public final int vel;
        public VoiceDecl(String name, int note, int channel, int vel) {
            this.name = name; this.note = note; this.channel = channel; this.vel = vel;
        }
    }

    public Result parse(Reader reader) throws IOException, ParseException {
        BufferedReader br = new BufferedReader(reader);
        // collect header values immutably
        Integer timeNum = null, timeDen = null, tempo = null, ppq = null, resPerBeat = null, bars = null;
        Pattern pattern = new Pattern();
        Map<String, VoiceDecl> voices = new LinkedHashMap<>();
        Map<String, List<Boolean>> tmpPatterns = new LinkedHashMap<>();

        String line;
        int lineNo = 0;
        while ((line = br.readLine()) != null) {
            lineNo++;
            String raw = line;
            int h = raw.indexOf('#');
            if (h >= 0) raw = raw.substring(0, h);
            raw = raw.trim();
            if (raw.isEmpty()) continue;

            if (raw.startsWith("time:")) {
                String v = raw.substring(5).trim();
                String[] xy = v.split("/");
                if (xy.length != 2) throw new ParseException(lineNo, "Invalid time signature: '"+v+"'");
                timeNum = parseIntStrict(xy[0].trim(), lineNo, "time numerator");
                timeDen = parseIntStrict(xy[1].trim(), lineNo, "time denominator");
            } else if (raw.startsWith("tempo:")) {
                tempo = parseIntStrict(raw.substring(7).trim(), lineNo, "tempo");
            } else if (raw.startsWith("ppq:")) {
                ppq = parseIntStrict(raw.substring(4).trim(), lineNo, "ppq");
            } else if (raw.startsWith("res-per-beat:")) {
                resPerBeat = parseIntStrict(raw.substring(14).trim(), lineNo, "res-per-beat");
            } else if (raw.startsWith("bars:")) {
                bars = parseIntStrict(raw.substring(6).trim(), lineNo, "bars");
            } else if (raw.startsWith("voice ")) {
                // voice <ident>  note=36 channel=10 vel=90
                String rest = raw.substring(6).trim();
                String[] parts = rest.split("\\s+");
                if (parts.length < 4) throw new ParseException(lineNo, "Invalid voice declaration: '"+raw+"'");
                String name = parts[0];
                Integer note = null, channel = null, vel = null;
                for (int i=1;i<parts.length;i++) {
                    String p = parts[i];
                    if (p.startsWith("note=")) note = parseIntStrict(p.substring(5), lineNo, "note");
                    else if (p.startsWith("channel=")) channel = parseIntStrict(p.substring(8), lineNo, "channel");
                    else if (p.startsWith("vel=")) vel = parseIntStrict(p.substring(4), lineNo, "vel");
                }
                if (note==null||channel==null||vel==null) throw new ParseException(lineNo, "Voice declaration requires note=, channel=, vel=");
                voices.put(name, new VoiceDecl(name, note, channel, vel));
            } else if (raw.startsWith("pattern ")) {
                int colon = raw.indexOf(':');
                if (colon < 0) throw new ParseException(lineNo, "Pattern line missing colon: '"+raw+"'");
                String name = raw.substring(8, colon).trim();
                String body = raw.substring(colon+1);
                List<Boolean> hits = tmpPatterns.computeIfAbsent(name, k -> new ArrayList<>());
                for (int i=0;i<body.length();i++) {
                    char c = body.charAt(i);
                    if (c=='x' || c=='X') hits.add(Boolean.TRUE);
                    else if (c=='-') hits.add(Boolean.FALSE);
                    else if (c=='|' || Character.isWhitespace(c)) {
                        // ignore
                    } else {
                        throw new ParseException(lineNo, "Invalid token '"+c+"' in pattern for voice '"+name+"'");
                    }
                }
            } else {
                throw new ParseException(lineNo, "Unknown statement: '"+raw+"'");
            }
        }

        for (var e : tmpPatterns.entrySet()) {
            boolean[] arr = new boolean[e.getValue().size()];
            for (int i=0;i<arr.length;i++) arr[i] = e.getValue().get(i);
            pattern.put(e.getKey(), arr);
        }

        PatternHeader header = new PatternHeader(timeNum, timeDen, tempo, ppq, resPerBeat, bars);
        return new Result(header, pattern, voices);
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

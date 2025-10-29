package syrincs.a_domain.rhythm;

import java.util.*;

/**
 * Domain-Klasse für das Mapping eines 16-Zeichen-Onset-Strings (nur 'x'/'o') im 4/4-Takt
 * mit 16tel-Raster (Index 0..15) auf Bassdrum (BD) und Snare (SN).
 *
 * Hintergrund und Herleitung:
 * Ausgangsfrage war, wie ein reiner Onset-String (x/o) ohne Dynamik/Artikulation sinnvoll
 * auf BD und SN verteilt werden kann. Aus gängiger Pop/Rock/Funk-Praxis leiten wir drei
 * separate, aber kombinierbare Heuristiken ab:
 *  (1) Snare-Backbeat auf Zählzeit 2 und 4 (Index 4 und 12),
 *  (2) Kick-Downbeats auf 1 und 3 (Index 0 und 8) plus Antizipationen kurz vor den Backbeats
 *      (typisch 16tel 'a' bzw. 'e' vor dem Backbeat),
 *  (3) Snare-Ghosts kurz nach den Backbeats (z. B. 16tel 'e'/'&' nach 2/4).
 * Da diese Muster in der Praxis überlappen, verwenden wir additives Scoring:
 * Jede Regel vergibt Gewichte für Rasterpositionen; die Summe bildet die Präferenz.
 *
 * Ansatz:
 *  - Modularität: Jede Heuristik ist als Rule kapselbar, separat (de-)aktivierbar und gewichtbar.
 *  - Additives Scoring: Für jede Position 0..15 entstehen BD- und SN-Scores durch Summation aller Regeln.
 *  - Entscheidungsschicht: Für jeden Onset (Zeichen 'x') entscheidet der höhere Score.
 *    Bei Gleichstand gilt ein klarer Tie-Breaker: Backbeat (4/12) -> Snare, sonst -> Kick.
 *    Optional kann ein Backbeat-Bias gesetzt werden.
 *  - Styles: Zusätzliche Stil-Regeln (z. B. FOUR_ON_FLOOR: Kick auf 0/4/8/12) werden einfach addiert.
 *
 * Eigenschaften:
 *  - Eingabe: exakt 16 Zeichen, nur 'x' (Onset) oder 'o' (Rest); Whitespace wird ignoriert.
 *  - Ausgabe: BD/SN-Masken (je 16 Zeichen 'x'/'o'), Positionslisten (Indizes) sowie
 *    die BD/SN-Score-Arrays (für Analyse/Debugging).
 *  - Erweiterbarkeit: Weitere Groovekonzepte (z. B. Halftime-Backbeat auf 3, genre-spezifische Anschub-
 *    oder Ghost-Muster) lassen sich als zusätzliche Rules implementieren, ohne die Zuordnung zu ändern.
 *
 * Verwendung:
 *  - Erzeuge einen Mapper mit den gewünschten Regeln (z. B. defaultMapper(Style.DEFAULT)
 *    oder mit FOUR_ON_FLOOR) und rufe map(sixteenXO) auf, um BD/SN zu erhalten.
 *
 * Nutzen:
 *  - Der Ansatz ist robust bei Regel-Überlappungen, transparent (Scores sichtbar) und domänennah
 *    (Backbeat/Downbeat/Ghosts). Er bildet verbreitete Praxis ab und bleibt flexibel für Stil-Präferenzen.
 */

public final class RhythmMapperFromOnsetStringToKickAndSnare {

    public enum Style { DEFAULT, FOUR_ON_FLOOR }

    /** Ergebnisobjekt für das Mapping. */
    public static final class Result {
        public final String input;        // 16 Zeichen x/o
        public final String bdMask;       // 16 Zeichen x/o (Kick)
        public final String snMask;       // 16 Zeichen x/o (Snare)
        public final List<Integer> bdIdx; // Kick-Positionen
        public final List<Integer> snIdx; // Snare-Positionen
        public final double[] bdScores;   // Debug/Analyse
        public final double[] snScores;

        Result(String input, String bdMask, String snMask,
               List<Integer> bdIdx, List<Integer> snIdx,
               double[] bdScores, double[] snScores) {
            this.input = input;
            this.bdMask = bdMask;
            this.snMask = snMask;
            this.bdIdx = Collections.unmodifiableList(bdIdx);
            this.snIdx = Collections.unmodifiableList(snIdx);
            this.bdScores = Arrays.copyOf(bdScores, bdScores.length);
            this.snScores = Arrays.copyOf(snScores, snScores.length);
        }

        @Override public String toString() {
            return "Input : " + input + System.lineSeparator() +
                    "BD    : " + bdMask + "  " + bdIdx + System.lineSeparator() +
                    "SN    : " + snMask + "  " + snIdx;
        }
    }

    /** Regel-Interface: trägt Gewichte auf BD/SN-Scores ein. */
    public interface Rule {
        void apply(double[] bdScores, double[] snScores);
        default String name() { return getClass().getSimpleName(); }
    }

    private final List<Rule> rules;
    private final double backbeatBias; // optionaler Zusatz für Snare auf Backbeat

    private RhythmMapperFromOnsetStringToKickAndSnare(List<Rule> rules, double backbeatBias) {
        this.rules = List.copyOf(rules);
        this.backbeatBias = backbeatBias;
    }

    /** Erzeugt einen Mapper mit typischen Pop/Rock/Funk-Regeln. */
    public static RhythmMapperFromOnsetStringToKickAndSnare defaultMapper(Style style) {
        List<Rule> base = new ArrayList<>();
        base.add(Rules.kickDownbeats(3.0));      // Kick: 1 & 3
        base.add(Rules.kickAnticipation(2.0, 1.0)); // Kick: vor 2/4
        base.add(Rules.snareBackbeat(4.0));      // Snare: 2 & 4
        base.add(Rules.snareGhosts(1.5, 1.0));   // Snare: kurz nach 2/4
        if (style == Style.FOUR_ON_FLOOR) {
            base.add(Rules.styleFourOnFloor(2.0)); // Kick: alle Viertel
        }
        // leichter Bias zugunsten Backbeat als Tie-Breaker-Hilfe
        return new RhythmMapperFromOnsetStringToKickAndSnare(base, 0.0);
    }

    /** Hauptfunktion: mappt einen 16er x/o-String auf BD/SN. */
    public Result map(String sixteenXO) {
        String s = normalizeAndValidate(sixteenXO);
        int n = 16;

        // 1) Onset-Positionen extrahieren
        List<Integer> onsets = new ArrayList<>(8);
        for (int i = 0; i < n; i++) if (s.charAt(i) == 'x') onsets.add(i);

        // 2) Scores aufbauen
        double[] bd = new double[n];
        double[] sn = new double[n];
        for (Rule r : rules) r.apply(bd, sn);
        // Backbeat-Bias (leichte Bevorzugung der Snare auf 2 und 4)
        if (backbeatBias != 0.0) { sn[4] += backbeatBias; sn[12] += backbeatBias; }

        // 3) Zuordnen nach Scores (+ Tie-Breaker)
        boolean[] bdMask = new boolean[n];
        boolean[] snMask = new boolean[n];
        List<Integer> bdIdx = new ArrayList<>();
        List<Integer> snIdx = new ArrayList<>();
        for (int p : onsets) {
            double b = bd[p], sScore = sn[p];
            if (sScore > b) {
                snMask[p] = true; snIdx.add(p);
            } else if (b > sScore) {
                bdMask[p] = true; bdIdx.add(p);
            } else {
                // Gleichstand: Snare wenn Backbeat, sonst Kick
                if (p == 4 || p == 12) { snMask[p] = true; snIdx.add(p); }
                else { bdMask[p] = true; bdIdx.add(p); }
            }
        }

        // 4) Masken-Strings bauen
        String bdOut = maskToXO(bdMask);
        String snOut = maskToXO(snMask);

        return new Result(s, bdOut, snOut, bdIdx, snIdx, bd, sn);
    }

    /**
     * Baut aus dem Mapping-Ergebnis ein Pattern mit genau zwei Voices: "kick" und "snare".
     * Die Länge beträgt immer 16 Schritte (ein 4/4‑Takt bei resPerBeat=4).
     */
    public static Pattern toPattern(Result r) {
        Objects.requireNonNull(r, "result is null");
        boolean[] kick = xoToMask(r.bdMask);
        boolean[] snare = xoToMask(r.snMask);
        Pattern p = new Pattern();
        p.put("kick", kick);
        p.put("snare", snare);
        return p;
    }

    /**
     * Liefert Standard-VoiceSpecs für Kick und Snare (GM-Drums):
     * channel=9 (MIDI-Kanal 10), Kick=36, Snare=38, vel=90, gate=50.
     * Diese Defaults sind bewusst hier gekapselt, damit die Domain unabhängig von Adaptern bleibt.
     */
    public static List<VoiceSpec> defaultVoiceSpecs() {
        VoiceSpec kick = new VoiceSpec("kick", 9, 36, 90, 50);
        VoiceSpec snare = new VoiceSpec("snare", 9, 38, 90, 50);
        return List.of(kick, snare);
    }

    /**
     * Erzeugt eine einfache RhythmSpec mit den üblichen Defaults für 16 Schritte:
     * 4/4, tempo=120, resPerBeat=4, bars=1.
     */
    public static RhythmSpec defaultSpec() {
        return new RhythmSpec(4, 4, 120, 4, 1);
    }

    /** Kleines DTO, um Pattern, Spec und Voices gebündelt zurückzugeben. */
    public static final class PlaybackBundle {
        public final Pattern pattern;
        public final RhythmSpec spec;
        public final List<VoiceSpec> voices;
        public PlaybackBundle(Pattern pattern, RhythmSpec spec, List<VoiceSpec> voices) {
            this.pattern = pattern; this.spec = spec; this.voices = voices;
        }
    }

    /**
     * Komfort-Helfer: nimmt einen 16‑Zeichen‑Onset‑String und liefert ein Pattern
     * zusammen mit einer Default‑Spec und den Standard‑Voices zurück.
     */
    public static PlaybackBundle buildForPlayback(String sixteenXO) {
        RhythmMapperFromOnsetStringToKickAndSnare mapper = RhythmMapperFromOnsetStringToKickAndSnare.defaultMapper(Style.DEFAULT);
        Result res = mapper.map(sixteenXO);
        Pattern pat = toPattern(res);
        return new PlaybackBundle(pat, defaultSpec(), defaultVoiceSpecs());
    }

    // ============ Regeln (modular) ============
    public static final class Rules {
        private static final int[] BACKBEATS = {4, 12};
        private static final int[] DOWNBEATS = {0, 8};
        private static final int[] QUARTERS  = {0, 4, 8, 12};

        public static Rule snareBackbeat(double w) {
            return (bd, sn) -> { for (int p : BACKBEATS) sn[p] += w; };
        }
        public static Rule snareGhosts(double wAfter1, double wAfter2) {
            return (bd, sn) -> {
                for (int b : BACKBEATS) {
                    sn[(b + 1) & 15] += wAfter1; // „e“
                    sn[(b + 2) & 15] += wAfter2; // „&“
                }
            };
        }
        public static Rule kickDownbeats(double w) {
            return (bd, sn) -> { for (int p : DOWNBEATS) bd[p] += w; };
        }
        public static Rule kickAnticipation(double wBefore1, double wBefore2) {
            return (bd, sn) -> {
                for (int b : BACKBEATS) {
                    bd[(b - 1) & 15] += wBefore1; // „a“ vor Backbeat
                    bd[(b - 2) & 15] += wBefore2; // „e“ vor Backbeat
                }
            };
        }
        public static Rule styleFourOnFloor(double w) {
            return (bd, sn) -> { for (int p : QUARTERS) bd[p] += w; };
        }
    }

    // ============ Hilfen ============
    private static String normalizeAndValidate(String s) {
        Objects.requireNonNull(s, "pattern is null");
        String t = s.replaceAll("\\s+", ""); // Whitespace ignorieren
        if (t.length() != 16) {
            throw new IllegalArgumentException("Erwarte 16 Zeichen (x/o), erhalten: " + t.length());
        }
        for (int i = 0; i < 16; i++) {
            char c = Character.toLowerCase(t.charAt(i));
            if (c != 'x' && c != 'o')
                throw new IllegalArgumentException("Nur 'x' oder 'o' erlaubt, Fehler bei Index " + i);
            if (c != t.charAt(i)) { // normalisieren auf lowercase
                char[] arr = t.toCharArray(); arr[i] = c; t = new String(arr);
            }
        }
        return t;
    }

    private static String maskToXO(boolean[] mask) {
        StringBuilder sb = new StringBuilder(16);
        for (boolean b : mask) sb.append(b ? 'x' : 'o');
        return sb.toString();
    }

    private static boolean[] xoToMask(String xo) {
        if (xo == null || xo.length() != 16) throw new IllegalArgumentException("Expected 16-char x/o string");
        boolean[] out = new boolean[16];
        for (int i = 0; i < 16; i++) {
            char c = Character.toLowerCase(xo.charAt(i));
            if (c == 'x') out[i] = true;
            else if (c == 'o') out[i] = false;
            else throw new IllegalArgumentException("Invalid char at index "+i+": '"+xo.charAt(i)+"'");
        }
        return out;
    }

    // ============ kleines Demo ============
    public static void main(String[] args) {
        RhythmMapperFromOnsetStringToKickAndSnare mapper = RhythmMapperFromOnsetStringToKickAndSnare.defaultMapper(Style.DEFAULT);
        // Optionales CLI-Argument verwenden, sonst gültigen 16er Default
        String input = (args != null && args.length > 0) ? String.join(" ", args) : "xooo xoxo xooo xooo";
        try {
            Result r = mapper.map(input);
            System.out.println(r);

            // Four-on-the-floor Vergleich
            RhythmMapperFromOnsetStringToKickAndSnare mapper4 = RhythmMapperFromOnsetStringToKickAndSnare.defaultMapper(Style.FOUR_ON_FLOOR);
            System.out.println("\nFour-on-the-floor:");
            System.out.println(mapper4.map(r.input));
        } catch (IllegalArgumentException ex) {
            System.err.println("[ERROR] " + ex.getMessage());
            System.err.println("Usage: RhythmMapperFromOnsetStringToKickAndSnare <16 x/o Zeichen>\n" +
                    "Beispiele: 'xooo xoxo xooo xooo' oder 'xoooxoxoxoooxooo'");
            System.exit(1);
        }
    }
}

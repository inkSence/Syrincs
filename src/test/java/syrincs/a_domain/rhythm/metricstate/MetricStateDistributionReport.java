package syrincs.a_domain.rhythm.metricstate;

import syrincs.a_domain.rhythm.HuffmanRhythm;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Helper program (not a JUnit test): computes metric-state-v1 (IMPULSE, 4/4, 16 positions) and the
 * old symbol-count measure (symbols x 2) for all 65,536 one-bar patterns and prints Markdown tables.
 * Pattern order: mask 0..65535, most significant bit = position 0.
 */
public final class MetricStateDistributionReport {
    private MetricStateDistributionReport() {}

    record Row(int mask, String pattern, int value, int old, long paths, int sequences, String sequence) {
        int diff() {
            return value - old;
        }
    }

    static String pattern(int mask) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            if (i > 0 && i % 4 == 0) sb.append(' ');
            sb.append(((mask >> (15 - i)) & 1) == 1 ? 'x' : 'o');
        }
        return sb.toString();
    }

    static String pct(long n, long total) {
        return String.format(java.util.Locale.ROOT, "%.2f %%", 100.0 * n / total);
    }

    public static void main(String[] args) {
        long start = System.nanoTime();
        MetricFrame frame = new MetricFrame(4, 16, SoundCondition.IMPULSE);
        MetricStateAnalyzer analyzer = new MetricStateAnalyzer();
        List<Row> rows = new ArrayList<>(65536);
        List<String> failures = new ArrayList<>();
        for (int mask = 0; mask < 65536; mask++) {
            String p = pattern(mask);
            try {
                MetricStateAnalysis a = analyzer.analyze(frame, p, 1);
                int old = new HuffmanRhythm(4, 4, 120, p).getInformation() * 2;
                String seq = String.join(" ", a.minimalSequenceCodes().iterator().next());
                rows.add(new Row(mask, p, a.valueBits(), old, a.minimalPathCount().longValueExact(),
                        a.minimalSequenceCodes().size(), seq));
            } catch (RuntimeException e) {
                failures.add(p + " -> " + e.getClass().getSimpleName() + ": " + e.getMessage());
            }
        }
        long total = rows.size();
        System.out.println("<!-- patterns analysed: " + total + ", failures: " + failures.size() + " -->");

        System.out.println("## Ausnahmen\n");
        if (failures.isEmpty()) System.out.println("Keine Exception bei 65536 Mustern.\n");
        else failures.forEach(f -> System.out.println("- `" + f + "`"));

        TreeMap<Integer, Long> newHist = new TreeMap<>();
        TreeMap<Integer, Long> oldHist = new TreeMap<>();
        for (Row r : rows) {
            newHist.merge(r.value(), 1L, Long::sum);
            oldHist.merge(r.old(), 1L, Long::sum);
        }
        System.out.println("## Häufigkeit je Wert\n");
        System.out.println("| Wert (Bit) | Muster neu | Anteil neu | Muster alt (Symbole x 2) | Anteil alt |");
        System.out.println("| ---: | ---: | ---: | ---: | ---: |");
        TreeMap<Integer, Boolean> keys = new TreeMap<>();
        newHist.keySet().forEach(k -> keys.put(k, true));
        oldHist.keySet().forEach(k -> keys.put(k, true));
        for (int k : keys.keySet()) {
            long n = newHist.getOrDefault(k, 0L), o = oldHist.getOrDefault(k, 0L);
            System.out.println("| " + k + " | " + n + " | " + pct(n, total) + " | " + o + " | " + pct(o, total) + " |");
        }
        System.out.println();
        for (String label : new String[] {"neu", "alt"}) {
            boolean isNew = label.equals("neu");
            TreeMap<Integer, Long> h = isNew ? newHist : oldHist;
            int min = h.firstKey(), max = h.lastKey();
            int mode = h.entrySet().stream().max(Map.Entry.<Integer, Long>comparingByValue()
                    .thenComparing(Map.Entry.<Integer, Long>comparingByKey().reversed())).get().getKey();
            long cum = 0;
            int median = -1;
            for (var e : h.entrySet()) {
                cum += e.getValue();
                if (cum >= total / 2) { median = e.getKey(); break; }
            }
            double mean = h.entrySet().stream().mapToDouble(e -> (double) e.getKey() * e.getValue()).sum() / total;
            System.out.println("- Maß " + label + ": kleinster Wert " + min + ", größter Wert " + max
                    + ", häufigster Wert " + mode + " (" + h.get(mode) + " Muster), Median " + median
                    + ", Mittelwert " + String.format(java.util.Locale.ROOT, "%.3f", mean)
                    + ", verschiedene Werte " + h.size());
        }
        System.out.println();

        long multiPath = rows.stream().filter(r -> r.paths() > 1).count();
        long multiSeq = rows.stream().filter(r -> r.sequences() > 1).count();
        Row maxPaths = rows.stream().max(Comparator.comparingLong(Row::paths).thenComparing(r -> -r.mask())).get();
        System.out.println("## Mehrdeutigkeit\n");
        System.out.println("- Muster mit mehr als einem minimalen Weg: " + multiPath + " (" + pct(multiPath, total) + ")");
        System.out.println("- Muster mit mehr als einer verschiedenen minimalen Anweisungsfolge: " + multiSeq + " (" + pct(multiSeq, total) + ")");
        System.out.println("- Größte Zahl minimaler Wege: " + maxPaths.paths() + " bei `" + maxPaths.pattern()
                + "` (Wert " + maxPaths.value() + ")");
        long maxSeq = rows.stream().mapToLong(Row::sequences).max().getAsLong();
        Row maxSeqRow = rows.stream().filter(r -> r.sequences() == maxSeq).findFirst().get();
        System.out.println("- Größte Zahl verschiedener minimaler Folgen: " + maxSeq + " bei `" + maxSeqRow.pattern() + "`");
        System.out.println();
        System.out.println("Mehrdeutige Muster je Wert:\n");
        System.out.println("| Wert | Muster | mehrdeutig (Wege) | Anteil | mehrdeutig (Folgen) |");
        System.out.println("| ---: | ---: | ---: | ---: | ---: |");
        for (int k : newHist.keySet()) {
            long n = newHist.get(k);
            long mp = rows.stream().filter(r -> r.value() == k && r.paths() > 1).count();
            long ms = rows.stream().filter(r -> r.value() == k && r.sequences() > 1).count();
            System.out.println("| " + k + " | " + n + " | " + mp + " | " + pct(mp, n) + " | " + ms + " |");
        }
        System.out.println();

        long eq = rows.stream().filter(r -> r.diff() == 0).count();
        long less = rows.stream().filter(r -> r.diff() < 0).count();
        long more = rows.stream().filter(r -> r.diff() > 0).count();
        System.out.println("## Vergleich mit dem alten Maß\n");
        System.out.println("| Beziehung | Muster | Anteil |");
        System.out.println("| --- | ---: | ---: |");
        System.out.println("| gleich | " + eq + " | " + pct(eq, total) + " |");
        System.out.println("| neues Maß kleiner | " + less + " | " + pct(less, total) + " |");
        System.out.println("| neues Maß größer | " + more + " | " + pct(more, total) + " |");
        System.out.println();
        TreeMap<Integer, Long> diffHist = new TreeMap<>();
        rows.forEach(r -> diffHist.merge(r.diff(), 1L, Long::sum));
        System.out.println("Verteilung der Differenz (neu minus alt):\n");
        System.out.println("| Differenz | Muster | Anteil |");
        System.out.println("| ---: | ---: | ---: |");
        diffHist.forEach((d, n) -> System.out.println("| " + d + " | " + n + " | " + pct(n, total) + " |"));
        System.out.println();

        Comparator<Row> byMask = Comparator.comparingInt(Row::mask);
        for (boolean neg : new boolean[] {true, false}) {
            List<Row> sorted = rows.stream()
                    .sorted(Comparator.<Row>comparingInt(r -> neg ? r.diff() : -r.diff()).thenComparing(byMask))
                    .limit(5).toList();
            System.out.println(neg ? "Größte Differenz, neues Maß kleiner (Auswahl: Differenz, dann Maske aufsteigend):\n"
                    : "Größte Differenz, neues Maß größer (Auswahl: Differenz, dann Maske aufsteigend):\n");
            System.out.println("| Muster | neu | alt | Differenz | minimale Folge |");
            System.out.println("| --- | ---: | ---: | ---: | --- |");
            for (Row r : sorted)
                System.out.println("| `" + r.pattern() + "` | " + r.value() + " | " + r.old() + " | " + r.diff()
                        + " | `" + r.sequence() + "` |");
            System.out.println();
        }

        System.out.println("## Mittelwert je Einsatzzahl\n");
        System.out.println("| Einsätze | Muster | Mittel neu | Mittel alt | kleinster neu | größter neu |");
        System.out.println("| ---: | ---: | ---: | ---: | ---: | ---: |");
        for (int c = 0; c <= 16; c++) {
            final int cc = c;
            List<Row> sel = rows.stream().filter(r -> Integer.bitCount(r.mask()) == cc).toList();
            double mn = sel.stream().mapToInt(Row::value).average().getAsDouble();
            double mo = sel.stream().mapToInt(Row::old).average().getAsDouble();
            System.out.println("| " + c + " | " + sel.size() + " | " + String.format(java.util.Locale.ROOT, "%.3f", mn)
                    + " | " + String.format(java.util.Locale.ROOT, "%.3f", mo) + " | "
                    + sel.stream().mapToInt(Row::value).min().getAsInt() + " | "
                    + sel.stream().mapToInt(Row::value).max().getAsInt() + " |");
        }
        System.out.println();

        System.out.println("## Auffälligkeiten\n");
        for (int mask : new int[] {0, 65535}) {
            Row r = rows.get(mask);
            System.out.println("- `" + r.pattern() + "`: neu " + r.value() + ", alt " + r.old() + ", Folge `" + r.sequence()
                    + "`, minimale Wege " + r.paths());
        }
        List<Row> two = rows.stream().filter(r -> r.value() == 2).toList();
        System.out.println("- Muster mit Wert 2 (" + two.size() + "): "
                + two.stream().map(r -> "`" + r.pattern() + "` (Folge `" + r.sequence() + "`)").toList());
        int minV = newHist.firstKey();
        List<Row> minRows = rows.stream().filter(r -> r.value() == minV).toList();
        System.out.println("- Kleinster Wert " + minV + " bei " + minRows.size() + " Mustern.");
        int maxV = newHist.lastKey();
        List<Row> maxRows = rows.stream().filter(r -> r.value() == maxV).limit(5).toList();
        System.out.println("- Größter Wert " + maxV + " bei " + newHist.get(maxV) + " Mustern, z. B. "
                + maxRows.stream().map(r -> "`" + r.pattern() + "`").toList());
        long oneBar = rows.stream().filter(r -> r.value() % 2 != 0).count();
        System.out.println("- Muster mit ungeradem Wert: " + oneBar);
        System.out.println();
        System.out.println("Laufzeit: " + (System.nanoTime() - start) / 1_000_000 + " ms");

        // constants for the test
        System.err.println("COUNTS " + newHist);
        System.err.println("OLDCOUNTS " + oldHist);
        System.err.println("AMBIG_PATHS " + multiPath + " AMBIG_SEQ " + multiSeq);
    }
}

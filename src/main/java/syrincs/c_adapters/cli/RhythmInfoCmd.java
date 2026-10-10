package syrincs.c_adapters.cli;

import static syrincs.b_application.rhythm.information.GenerationRequest.*;

import picocli.CommandLine.*;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Model.OptionSpec;

import syrincs.a_domain.rhythm.information.*;
import syrincs.b_application.rhythm.information.*;
import syrincs.c_adapters.rhythm.information.RhythmInformationJson;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.Callable;

@Command(
        name = "rhythm-info",
        mixinStandardHelpOptions = true,
        description =
                "Exact description-v1 rhythm information in bits; no database or audio access",
        subcommands = {
            RhythmInfoCmd.Analyze.class,
            RhythmInfoCmd.Generate.class,
            RhythmInfoCmd.Decode.class
        })
public class RhythmInfoCmd implements Runnable {
    @ParentCommand RootCmd root;
    @Spec CommandSpec spec;

    @Override
    public void run() {
        spec.commandLine().usage(spec.commandLine().getOut());
    }

    private RhythmInformationFacade facade() {
        if (root.informationFacade == null)
            throw new IllegalStateException("Rhythm information service is not configured");
        return root.informationFacade;
    }

    static class Common {
        @Option(
                names = "--measure",
                defaultValue = "description-v1",
                description =
                        "Versioned measure (description-v1); existing commands keep legacy units")
        String measure;

        @Option(
                names = "--scope",
                defaultValue = "global",
                description = "global, isolated or causal; each has a different target sum")
        String scope;

        @Option(
                names = "--window-slots",
                description = "Positive equal window size for local scopes; must divide N")
        Integer windowSlots;

        @Option(
                names = "--boundaries",
                description = "Alternative explicit complete partition, e.g. 0,4,8,12,16")
        String boundaries;

        @Option(
                names = "--bar-slots",
                description = "Explicit local profile grouping; bars must align with windows")
        Integer barSlots;

        @Option(
                names = "--history",
                defaultValue = "",
                description = "Actual contiguous x/o history, only for causal scope")
        String history;

        @Option(
                names = "--tempo",
                description = "Positive exact quarter BPM; requires --ticks-per-quarter")
        String tempo;

        @Option(
                names = "--ticks-per-quarter",
                description = "Positive integer raster; requires --tempo")
        Integer ticks;

        @Option(
                names = "--slot-duration-ms",
                description = "Alternative exact slot duration; excludes tempo/raster options")
        String duration;

        @Option(
                names = "--time-signature",
                description = "Explicit notation Z/N with tempo/raster; no inferred beat")
        String signature;

        @Option(
                names = "--references",
                description = "rhythm-info-references-v1 JSON file with explicit references")
        Path references;

        @Option(
                names = "--reference-profile",
                description = "Optional partial time check: london-time-window-v1")
        String referenceProfile;

        @Option(
                names = "--request",
                description = "rhythm-info-request-v1 JSON; excludes semantic command-line inputs")
        Path request;

        @Option(
                names = "--format",
                defaultValue = "text",
                description = "text or json; JSON is one result document on stdout")
        String format;

        @Option(
                names = "--alternatives-limit",
                defaultValue = "" + RhythmInformationDefaults.ALTERNATIVES_LIMIT,
                description =
                        "Positive display limit for optimal codes; does not limit proof or count")
        int alternativesLimit;

        void validateDisplay() {
            if (!Set.of("text", "json").contains(format))
                throw new IllegalArgumentException("Format must be text or json");
            if (alternativesLimit <= 0)
                throw new IllegalArgumentException("Alternatives limit must be positive");
        }

        AnalysisSettings settings(int slots, RhythmInformationJson json) throws IOException {
            if (!measure.equals(ExactDescriptionAnalyzer.VERSION))
                throw new IllegalArgumentException("Unsupported measure: " + measure);
            Scope value =
                    switch (scope) {
                        case "global" -> Scope.GLOBAL_DESCRIPTION;
                        case "isolated" -> Scope.ISOLATED_WINDOWS;
                        case "causal" -> Scope.CAUSAL_WINDOWS;
                        default ->
                                throw new IllegalArgumentException(
                                        "Scope must be global, isolated or causal");
                    };
            if (duration != null && (tempo != null || ticks != null))
                throw new IllegalArgumentException("Choose slot duration or tempo/raster");
            Integer num = null, den = null;
            if (signature != null) {
                String[] parts = signature.split("/", -1);
                if (parts.length != 2)
                    throw new IllegalArgumentException("Time signature must be Z/N");
                num = Integer.valueOf(parts[0]);
                den = Integer.valueOf(parts[1]);
            }
            AnalysisFrame frame =
                    new AnalysisFrame(
                            slots,
                            duration == null ? null : Rational.parse(duration),
                            tempo == null ? null : Rational.parse(tempo),
                            ticks,
                            num,
                            den);
            Segmentation segmentation = null;
            if (value == Scope.GLOBAL_DESCRIPTION) {
                if (windowSlots != null || boundaries != null || barSlots != null)
                    throw new IllegalArgumentException("Global scope does not accept segmentation");
            } else {
                if ((windowSlots == null) == (boundaries == null))
                    throw new IllegalArgumentException(
                            "Local scope needs exactly --window-slots or --boundaries");
                segmentation =
                        windowSlots != null
                                ? Segmentation.equal(slots, windowSlots, barSlots)
                                : new Segmentation(
                                        csv(boundaries),
                                        barSlots == null
                                                ? List.of()
                                                : Segmentation.equal(slots, barSlots, null)
                                                        .boundaries());
            }
            return new AnalysisSettings(
                    frame,
                    value,
                    segmentation,
                    history,
                    references == null ? List.of() : json.readReferences(references),
                    referenceProfile);
        }

        RhythmInformationJson.Input request(
                CommandSpec spec, String kind, boolean hasPositional, RhythmInformationJson json)
                throws IOException {
            if (hasPositional)
                throw new IllegalArgumentException(
                        "--request cannot be combined with positional content");
            for (OptionSpec option : spec.commandLine().getParseResult().matchedOptions())
                if (Arrays.stream(option.names())
                        .noneMatch(
                                n ->
                                        Set.of("--request", "--format", "--alternatives-limit")
                                                .contains(n)))
                    throw new IllegalArgumentException(
                            "--request cannot be combined with semantic option "
                                    + option.longestName());
            return json.readRequest(request, kind);
        }
    }

    private abstract static class Leaf implements Callable<Integer> {
        @ParentCommand RhythmInfoCmd parent;
        @Spec CommandSpec spec;
        @Mixin Common options;
        final RhythmInformationJson json = new RhythmInformationJson();

        @Override
        public final Integer call() {
            try {
                options.validateDisplay();
                return execute();
            } catch (IllegalArgumentException e) {
                error("INVALID_REQUEST", e.getMessage());
                return 2;
            } catch (IOException e) {
                error("IO_ERROR", e.getMessage());
                return 1;
            } catch (RuntimeException e) {
                error("CALCULATION_FAILED", e.getMessage());
                return 1;
            }
        }

        private void error(String code, String message) {
            spec.commandLine()
                    .getErr()
                    .println(
                            options.format.equals("json")
                                    ? json.error(code, String.valueOf(message))
                                    : "[RHYTHM-INFO] " + code + ": " + message);
        }

        abstract int execute() throws IOException;

        void outputAnalysis(AnalysisResult result, List<Rational> sourceTimes) {
            PrintWriter out = spec.commandLine().getOut();
            if (options.format.equals("json"))
                out.println(json.analysis(result, options.alternativesLimit, sourceTimes));
            else {
                text(result.primary(), out);
                if (result.additionalGlobal() != null) {
                    out.println("Additional global analysis:");
                    text(result.additionalGlobal(), out);
                }
                if (result.legacyComparison() != null)
                    out.println("Legacy (CODE_SYMBOLS): " + result.legacyComparison());
            }
        }
    }

    @Command(
            name = "analyze",
            mixinStandardHelpOptions = true,
            description =
                    "Analyze x/o onsets exactly in bits; time and meter are explicit conditions")
    public static class Analyze extends Leaf {
        @Parameters(
                index = "0",
                arity = "0..1",
                description = "Nonempty x/o onset word, 1..128 slots")
        String onsets;

        @Option(
                names = "--include-global",
                description = "Add global minimum with its own key to a local analysis")
        boolean includeGlobal;

        @Option(
                names = "--compare-legacy",
                description =
                        "Separate CODE_SYMBOLS result when explicit frame is compatible with legacy"
                                + " 4/4")
        boolean compareLegacy;

        @Override
        int execute() throws IOException {
            OnsetSequence word;
            AnalysisSettings settings;
            List<Rational> events = List.of();
            if (options.request != null) {
                var request = options.request(spec, "analyze", onsets != null, json);
                word = request.sequence();
                settings = request.settings();
                events = request.sourceEventTimesMs();
                includeGlobal = request.includeGlobal();
                compareLegacy = request.compareLegacy();
            } else {
                if (onsets == null)
                    throw new IllegalArgumentException("Provide onsets or --request");
                word = new OnsetSequence(onsets);
                settings = options.settings(word.slots(), json);
            }
            outputAnalysis(
                    parent.facade().analyze(word, settings, includeGlobal, compareLegacy), events);
            return 0;
        }
    }

    @Command(
            name = "decode",
            mixinStandardHelpOptions = true,
            description =
                    "Reconstruct a complete description; nonminimal valid witnesses are accepted")
    public static class Decode extends Leaf {
        @Option(names = "--slots", description = "Known target length, 1..128")
        Integer slots;

        @Option(names = "--code", description = "Complete binary message; no ignored trailing bits")
        String code;

        @Option(
                names = "--analyze",
                description =
                        "Also compute the minimum; witness length alone is not an information"
                                + " value")
        boolean analyze;

        @Override
        int execute() throws IOException {
            AnalysisSettings settings;
            if (options.request != null) {
                var request = options.request(spec, "decode", false, json);
                settings = request.settings();
                code = request.code();
                analyze = request.analyze();
            } else {
                if (slots == null || code == null)
                    throw new IllegalArgumentException("Provide --slots and --code, or --request");
                settings = options.settings(slots, json);
            }
            var result = parent.facade().decode(code, settings, analyze);
            var out = spec.commandLine().getOut();
            if (options.format.equals("json"))
                out.println(json.decoded(result, options.alternativesLimit));
            else {
                out.printf(
                        "[DECODED] Rhythm=%s | WitnessBits=%d%n",
                        result.sequence().normalizedOnsets(), result.witnessLength());
                if (result.analysis() != null) text(result.analysis().primary(), out);
            }
            return 0;
        }
    }

    @Command(
            name = "generate",
            mixinStandardHelpOptions = true,
            description =
                    "Generate by exact bits and AND-constraints; completeness and budget are"
                            + " reported")
    public static class Generate extends Leaf {
        @Option(names = "--slots", description = "Target slots, 1..128; exhaustive requires N<=16")
        Integer slots;

        @Option(names = "--info", description = "Exact primary information target in bits")
        Integer info;

        @Option(names = "--info-min", description = "Inclusive target minimum; requires --info-max")
        Integer infoMin;

        @Option(names = "--info-max", description = "Inclusive target maximum; requires --info-min")
        Integer infoMax;

        @Option(names = "--strategy", description = "Required: exhaustive, code-length or random")
        String strategy;

        @Option(
                names = "--attempt-budget",
                description = "Positive integer count budget, required for code-length/random")
        BigInteger budget;

        @Option(
                names = "--limit",
                defaultValue = "" + RhythmInformationDefaults.RESULT_LIMIT,
                description = "Positive requested result count; does not stop systematic search")
        int limit;

        @Option(
                names = "--selection",
                defaultValue = "first",
                description = "first or random-without-replacement; latter needs complete search")
        String selection;

        @Option(names = "--seed", description = "Required LONG for random search/selection")
        Long seed;

        @Option(names = "--onset-min", description = "Inclusive minimum onset count")
        Integer onsetMin;

        @Option(names = "--onset-max", description = "Inclusive maximum onset count")
        Integer onsetMax;

        @Option(names = "--density-min", description = "Inclusive exact density in 0..1")
        String densityMin;

        @Option(names = "--density-max", description = "Inclusive exact density in 0..1")
        String densityMax;

        @Option(
                names = "--required-positions",
                description = "CSV of required zero-based positions")
        String required;

        @Option(
                names = "--forbidden-positions",
                description = "CSV of forbidden zero-based positions")
        String forbidden;

        @Option(
                names = "--min-onset-gap",
                description = "Positive linear gap in slots; no cyclic edge assumption")
        Integer minGap;

        @Option(
                names = "--max-zero-run",
                description = "Maximum zero-run including window edges, 0..N")
        Integer maxZeros;

        @Option(
                names = "--window-profile",
                description = "Exact local window-bit profile in chronological order")
        String windowProfile;

        @Option(
                names = "--bar-profile",
                description = "Exact local bar contribution profile; requires explicit grouping")
        String barProfile;

        @Option(
                names = "--deviation-min",
                description = "Inclusive exact deviation minimum of named profile")
        String deviationMin;

        @Option(
                names = "--deviation-max",
                description = "Inclusive exact deviation maximum of named profile")
        String deviationMax;

        @Option(
                names = "--deviation-basis",
                description = "windows or bars, required with deviation bounds")
        String deviationBasis;

        @Option(
                names = "--peak-window",
                description = "CSV of required one-based maximum windows; ties count")
        String peaks;

        @Option(
                names = "--require-reference-pass",
                description =
                        "Require an evaluable PASS with explicit time, references and profile")
        boolean requireReferencePass;

        @Override
        int execute() throws IOException {
            GenerationRequest request;
            if (options.request != null)
                request = options.request(spec, "generate", false, json).generation();
            else {
                if (slots == null || strategy == null)
                    throw new IllegalArgumentException(
                            "Provide --slots, an information target and --strategy, or --request");
                if (info != null && (infoMin != null || infoMax != null)
                        || info == null && (infoMin == null || infoMax == null))
                    throw new IllegalArgumentException(
                            "Choose --info or both --info-min and --info-max");
                IntRange target =
                        info != null ? new IntRange(info, info) : new IntRange(infoMin, infoMax);
                Strategy mode =
                        switch (strategy) {
                            case "exhaustive" -> Strategy.EXHAUSTIVE;
                            case "code-length" -> Strategy.CODE_LENGTH_SEARCH;
                            case "random" -> Strategy.BOUNDED_RANDOM;
                            default -> throw new IllegalArgumentException("Unknown strategy");
                        };
                Selection choose =
                        switch (selection) {
                            case "first" -> Selection.FIRST;
                            case "random-without-replacement" ->
                                    Selection.RANDOM_WITHOUT_REPLACEMENT;
                            default -> throw new IllegalArgumentException("Unknown selection");
                        };
                var events =
                        new EventConstraints(
                                onsetMin == null && onsetMax == null
                                        ? null
                                        : new IntRange(
                                                onsetMin == null ? 0 : onsetMin,
                                                onsetMax == null ? slots : onsetMax),
                                rationalRange(densityMin, densityMax),
                                set(required),
                                set(forbidden),
                                minGap,
                                maxZeros);
                var profiles =
                        new ProfileConstraints(
                                exactProfile(windowProfile),
                                exactProfile(barProfile),
                                rationalRange(deviationMin, deviationMax),
                                basis(deviationBasis),
                                set(peaks),
                                peaks == null ? null : ProfileBasis.WINDOWS);
                request =
                        new GenerationRequest(
                                options.settings(slots, json),
                                target,
                                null,
                                events,
                                profiles,
                                requireReferencePass,
                                mode,
                                budget,
                                limit,
                                choose,
                                seed);
            }
            GenerationResult result = parent.facade().generate(request);
            var out = spec.commandLine().getOut();
            if (options.format.equals("json"))
                out.println(json.generated(result, options.alternativesLimit));
            else {
                out.printf(
                        "[GENERATION] Status=%s | Complete=%s | Attempts=%s | Evaluated=%s |"
                                + " Observed=%s | Total=%s | Returned=%d | Shortfall=%d%n",
                        result.stopReason(),
                        result.searchComplete(),
                        result.attempts(),
                        result.uniqueCandidatesEvaluated(),
                        result.matchingCandidatesObserved(),
                        result.totalMatches() == null ? "UNKNOWN" : result.totalMatches(),
                        result.returnedCount(),
                        result.shortfall());
                for (var rhythm : result.rhythms()) text(rhythm.primary(), out);
            }
            return result.stopReason() == GenerationResult.StopReason.BUDGET_EXHAUSTED
                            || result.stopReason() == GenerationResult.StopReason.INTERRUPTED
                    ? 3
                    : 0;
        }
    }

    private static ProfileBasis basis(String value) {
        if (value == null) return null;
        return switch (value) {
            case "windows" -> ProfileBasis.WINDOWS;
            case "bars" -> ProfileBasis.BARS;
            default -> throw new IllegalArgumentException("Basis must be windows or bars");
        };
    }

    private static RationalRange rationalRange(String min, String max) {
        return min == null && max == null
                ? null
                : new RationalRange(
                        min == null ? null : Rational.parse(min),
                        max == null ? null : Rational.parse(max));
    }

    private static List<Integer> csv(String value) {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException("CSV must be nonempty");
        return Arrays.stream(value.split(",", -1)).map(String::trim).map(Integer::valueOf).toList();
    }

    private static Set<Integer> set(String csv) {
        if (csv == null) return Set.of();
        List<Integer> list = csv(csv);
        if (new HashSet<>(list).size() != list.size())
            throw new IllegalArgumentException("Duplicate CSV positions");
        return Set.copyOf(list);
    }

    private static List<IntRange> exactProfile(String value) {
        return value == null
                ? List.of()
                : csv(value).stream().map(i -> new IntRange(i, i)).toList();
    }

    private static void text(InformationAnalysis a, PrintWriter out) {
        out.printf(
                "[INFORMATION] Rhythm=%s | Measure=description-v1 | Scope=%s | Unit=BITS | Total=%d"
                        + " | Optimality=EXACT%n",
                a.sequence().normalizedOnsets(), a.measurementKey().scope(), a.totalBits());
        for (var w : a.windows())
            out.printf(
                    "[DESCRIPTION] Window=%d [%d,%d) | Bits=%d | Model=%d | Residual=%d | Code=%s |"
                            + " OptimalCodes=%s%n",
                    w.position(),
                    w.start(),
                    w.end(),
                    w.totalBits(),
                    w.witness().modelBits(),
                    w.witness().residualBits(),
                    w.witness().code(),
                    w.optimalCodeCount());
        if (a.windowMetrics() != null)
            out.printf(
                    "[PROFILE] Windows=%s | Bars=%s | Variance=%s | Peaks=%s%n",
                    a.windowMetrics().values(),
                    a.barContributions(),
                    a.windowMetrics().variance(),
                    a.windowMetrics().peakPositions());
        out.printf(
                "[CONDITIONS] SlotDurationMs=%s | InitialHistory=%s%n",
                a.measurementKey().frame().slotDurationMs() == null
                        ? "UNKNOWN"
                        : a.measurementKey().frame().slotDurationMs(),
                a.measurementKey().initialHistory());
        for (var r : a.referenceAssessments())
            out.printf(
                    "[REFERENCE] Id=%s | Status=%s | Profile=%s | Reason=%s%n",
                    r.reference() == null ? "ABSENT" : r.reference().id(),
                    r.status(),
                    r.profile(),
                    r.reason());
    }
}

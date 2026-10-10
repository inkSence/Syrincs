package syrincs.c_adapters.rhythm.information;

import static syrincs.b_application.rhythm.information.GenerationRequest.*;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import syrincs.a_domain.rhythm.information.*;
import syrincs.b_application.rhythm.information.*;

import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/** JSON technology and all wire-format choices stay outside Domain/Application. */
public final class RhythmInformationJson {
    public static final String REQUEST_VERSION = "rhythm-info-request-v1",
            RESULT_VERSION = "rhythm-info-result-v1";

    public record Input(
            String kind,
            AnalysisSettings settings,
            OnsetSequence sequence,
            String code,
            GenerationRequest generation,
            boolean includeGlobal,
            boolean compareLegacy,
            boolean analyze,
            List<Rational> sourceEventTimesMs) {
        public Input {
            sourceEventTimesMs = List.copyOf(sourceEventTimesMs);
        }
    }

    private final Gson gson =
            new GsonBuilder()
                    .serializeNulls()
                    .setPrettyPrinting()
                    .disableHtmlEscaping()
                    .registerTypeAdapter(
                            Rational.class,
                            (JsonSerializer<Rational>) (r, t, c) -> new JsonPrimitive(r.toString()))
                    .registerTypeAdapter(
                            BigInteger.class,
                            (JsonSerializer<BigInteger>)
                                    (r, t, c) -> new JsonPrimitive(r.toString()))
                    .registerTypeAdapter(
                            Long.class,
                            (JsonSerializer<Long>) (r, t, c) -> new JsonPrimitive(r.toString()))
                    .registerTypeAdapter(
                            AnalysisFrame.class,
                            (JsonSerializer<AnalysisFrame>) (r, t, c) -> c.serialize(frameView(r)))
                    .create();

    public Input readRequest(Path path, String expectedKind) throws IOException {
        JsonObject root = object(read(path), "request");
        keys(
                root,
                "schemaVersion",
                "kind",
                "measureId",
                "scope",
                "frame",
                "segmentation",
                "initialHistory",
                "references",
                "referenceProfile",
                "onsets",
                "eventTimesMs",
                "code",
                "target",
                "constraints",
                "search",
                "includeGlobal",
                "compareLegacy",
                "analyze");
        version(root, REQUEST_VERSION);
        String kind = string(required(root, "kind"));
        if (!kind.equals(expectedKind))
            throw new IllegalArgumentException("Request kind must be " + expectedKind);
        if (!text(root, "measureId", ExactDescriptionAnalyzer.VERSION)
                .equals(ExactDescriptionAnalyzer.VERSION))
            throw new IllegalArgumentException("Unsupported measureId");
        Scope scope = enumeration(Scope.class, text(root, "scope", "GLOBAL_DESCRIPTION"));
        AnalysisFrame frame = frame(object(required(root, "frame"), "frame"));
        Segmentation segmentation = null;
        if (root.has("segmentation")) {
            var s = object(root.get("segmentation"), "segmentation");
            keys(s, "boundaries", "barBoundaries");
            segmentation =
                    new Segmentation(
                            integers(required(s, "boundaries")),
                            s.has("barBoundaries") ? integers(s.get("barBoundaries")) : List.of());
        }
        List<MetricReference> references =
                root.has("references") ? references(root.get("references")) : List.of();
        AnalysisSettings settings =
                new AnalysisSettings(
                        frame,
                        scope,
                        segmentation,
                        text(root, "initialHistory", ""),
                        references,
                        text(root, "referenceProfile", null));
        OnsetSequence word = null;
        List<Rational> events = new ArrayList<>();
        if (kind.equals("analyze")) {
            forbid(root, "code", "target", "constraints", "search", "analyze");
            if (root.has("onsets") == root.has("eventTimesMs"))
                throw new IllegalArgumentException(
                        "Analyze request needs exactly onsets or eventTimesMs");
            if (root.has("onsets")) word = new OnsetSequence(string(root.get("onsets")));
            else {
                if (frame.slotDurationMs() == null)
                    throw new IllegalArgumentException(
                            "Event times need a known exact grid duration");
                char[] onsets = new char[frame.slots()];
                Arrays.fill(onsets, 'o');
                Rational previous = null;
                for (JsonElement e : array(root.get("eventTimesMs"))) {
                    Rational time = rational(e);
                    events.add(time);
                    if (previous != null && time.compareTo(previous) <= 0)
                        throw new IllegalArgumentException(
                                "Event times must be strictly increasing; duplicate events cannot"
                                        + " be merged");
                    previous = time;
                    Rational position = time.divide(frame.slotDurationMs());
                    if (!position.isInteger()
                            || position.compareTo(Rational.ZERO) < 0
                            || position.compareTo(Rational.of(frame.slots())) >= 0)
                        throw new IllegalArgumentException(
                                "Event time outside exact grid/window: " + time);
                    onsets[position.numerator().intValueExact()] = 'x';
                }
                word = new OnsetSequence(new String(onsets));
            }
            if (word.slots() != frame.slots())
                throw new IllegalArgumentException("Onsets/frame length mismatch");
        } else if (kind.equals("decode"))
            forbid(
                    root,
                    "onsets",
                    "eventTimesMs",
                    "target",
                    "constraints",
                    "search",
                    "includeGlobal",
                    "compareLegacy");
        else if (kind.equals("generate"))
            forbid(
                    root,
                    "onsets",
                    "eventTimesMs",
                    "code",
                    "analyze",
                    "includeGlobal",
                    "compareLegacy");
        else throw new IllegalArgumentException("Unknown request kind");
        return new Input(
                kind,
                settings,
                word,
                kind.equals("decode") ? string(required(root, "code")) : null,
                kind.equals("generate") ? generation(root, settings) : null,
                bool(root, "includeGlobal", false),
                bool(root, "compareLegacy", false),
                bool(root, "analyze", false),
                events);
    }

    public List<MetricReference> readReferences(Path path) throws IOException {
        var root = object(read(path), "references");
        keys(root, "schemaVersion", "references");
        version(root, "rhythm-info-references-v1");
        return references(required(root, "references"));
    }

    private GenerationRequest generation(JsonObject root, AnalysisSettings settings) {
        var target = object(required(root, "target"), "target");
        keys(
                target,
                "information",
                "additionalGlobalInformation",
                "windowProfile",
                "barProfile",
                "deviation",
                "deviationBasis",
                "peakPositions",
                "peakBasis");
        JsonObject e =
                root.has("constraints")
                        ? object(root.get("constraints"), "constraints")
                        : new JsonObject();
        keys(
                e,
                "onsetCount",
                "density",
                "requiredPositions",
                "forbiddenPositions",
                "minOnsetGap",
                "maxZeroRun",
                "requireReferencePass");
        var events =
                new EventConstraints(
                        e.has("onsetCount") ? range(e.get("onsetCount")) : null,
                        e.has("density") ? rationalRange(e.get("density")) : null,
                        positions(e, "requiredPositions"),
                        positions(e, "forbiddenPositions"),
                        optionalInt(e, "minOnsetGap"),
                        optionalInt(e, "maxZeroRun"));
        var profiles =
                new ProfileConstraints(
                        profile(target, "windowProfile"),
                        profile(target, "barProfile"),
                        target.has("deviation") ? rationalRange(target.get("deviation")) : null,
                        optionalEnum(target, "deviationBasis", ProfileBasis.class),
                        positions(target, "peakPositions"),
                        optionalEnum(target, "peakBasis", ProfileBasis.class));
        var search = object(required(root, "search"), "search");
        keys(search, "strategy", "attemptBudget", "resultLimit", "selection", "seed");
        return new GenerationRequest(
                settings,
                range(required(target, "information")),
                target.has("additionalGlobalInformation")
                        ? range(target.get("additionalGlobalInformation"))
                        : null,
                events,
                profiles,
                bool(e, "requireReferencePass", false),
                enumeration(Strategy.class, string(required(search, "strategy"))),
                search.has("attemptBudget") ? bigInteger(search.get("attemptBudget")) : null,
                search.has("resultLimit")
                        ? integer(search.get("resultLimit"))
                        : RhythmInformationDefaults.RESULT_LIMIT,
                enumeration(Selection.class, text(search, "selection", "FIRST")),
                search.has("seed") ? longInteger(search.get("seed")) : null);
    }

    private AnalysisFrame frame(JsonObject f) {
        keys(f, "slots", "time", "notation");
        int slots = integer(required(f, "slots"));
        Rational duration = null, bpm = null;
        Integer ticks = null, num = null, den = null;
        if (f.has("time")) {
            var t = object(f.get("time"), "time");
            keys(t, "status", "slotDurationMs", "quarterBpm", "ticksPerQuarter");
            String status = string(required(t, "status"));
            if (status.equals("UNKNOWN"))
                forbid(t, "slotDurationMs", "quarterBpm", "ticksPerQuarter");
            else if (status.equals("KNOWN")) {
                duration = t.has("slotDurationMs") ? rational(t.get("slotDurationMs")) : null;
                bpm = t.has("quarterBpm") ? rational(t.get("quarterBpm")) : null;
                ticks = optionalInt(t, "ticksPerQuarter");
                if (duration == null && bpm == null)
                    throw new IllegalArgumentException("KNOWN time needs duration or tempo/raster");
                if (duration != null && (bpm != null || ticks != null))
                    throw new IllegalArgumentException(
                            "Choose slot duration or tempo/raster, not both");
            } else throw new IllegalArgumentException("Unknown time status");
        }
        if (f.has("notation")) {
            var m = object(f.get("notation"), "notation");
            keys(m, "numerator", "denominator");
            num = integer(required(m, "numerator"));
            den = integer(required(m, "denominator"));
        }
        return new AnalysisFrame(slots, duration, bpm, ticks, num, den);
    }

    private List<MetricReference> references(JsonElement input) {
        List<MetricReference> result = new ArrayList<>();
        for (JsonElement item : array(input)) {
            var r = object(item, "reference");
            keys(r, "id", "cycleSlots", "phase", "beatLevel", "levels", "provenance", "source");
            List<MetricReference.Level> levels = new ArrayList<>();
            for (JsonElement level : array(required(r, "levels"))) {
                var l = object(level, "level");
                keys(l, "id", "kind", "positions");
                levels.add(
                        new MetricReference.Level(
                                string(required(l, "id")),
                                enumeration(
                                        MetricReference.LevelKind.class,
                                        string(required(l, "kind"))),
                                integers(required(l, "positions"))));
            }
            result.add(
                    new MetricReference(
                            string(required(r, "id")),
                            integer(required(r, "cycleSlots")),
                            integer(required(r, "phase")),
                            string(required(r, "beatLevel")),
                            levels,
                            enumeration(
                                    MetricReference.Provenance.class,
                                    string(required(r, "provenance"))),
                            string(required(r, "source"))));
        }
        return List.copyOf(result);
    }

    public String analysis(
            AnalysisResult result, int alternativesLimit, List<Rational> sourceTimes) {
        var root = envelope("analysis");
        root.put("primary", analysisView(result.primary(), alternativesLimit));
        root.put(
                "additionalGlobal",
                result.additionalGlobal() == null
                        ? null
                        : analysisView(result.additionalGlobal(), alternativesLimit));
        root.put("legacyComparison", result.legacyComparison());
        root.put("sourceEventTimesMs", sourceTimes);
        return gson.toJson(root);
    }

    public String decoded(DecodeRhythmDescriptionUseCase.Result result, int alternativesLimit) {
        var root = envelope("decoded");
        root.put("code", result.code());
        root.put("witnessLength", result.witnessLength());
        root.put("normalizedOnsets", result.sequence().normalizedOnsets());
        root.put("measurementKey", result.measurementKey());
        root.put("messages", result.messages().stream().map(this::descriptionView).toList());
        root.put(
                "analysis",
                result.analysis() == null
                        ? null
                        : analysisView(result.analysis().primary(), alternativesLimit));
        return gson.toJson(root);
    }

    public String generated(GenerationResult result, int alternativesLimit) {
        var root = envelope("generation");
        root.put("request", result.request());
        root.put("requestedCount", result.requestedCount());
        root.put("returnedCount", result.returnedCount());
        root.put("targetsSatisfiedForEveryResult", result.targetsSatisfiedForEveryResult());
        root.put("requestedCountReached", result.requestedCountReached());
        root.put("shortfall", result.shortfall());
        root.put("searchComplete", result.searchComplete());
        root.put("stopReason", result.stopReason());
        root.put("attempts", result.attempts());
        root.put("uniqueCandidatesEvaluated", result.uniqueCandidatesEvaluated());
        root.put("matchingCandidatesObserved", result.matchingCandidatesObserved());
        root.put("totalMatches", result.totalMatches());
        root.put("maskSpaceSize", result.maskSpaceSize());
        root.put("codeSpaceSize", result.codeSpaceSize());
        root.put("selectionPerformed", result.selectionPerformed());
        root.put("randomAlgorithm", result.randomAlgorithm());
        root.put("enumerationVersion", result.enumerationVersion());
        root.put("measurementKey", MeasurementKey.of(result.request().settings()));
        root.put(
                "totalMatchesUnavailableReason",
                result.searchComplete() ? null : "INCOMPLETE_SEARCH");
        root.put(
                "rhythms",
                result.rhythms().stream()
                        .map(
                                r -> {
                                    var v = map();
                                    v.put("primary", analysisView(r.primary(), alternativesLimit));
                                    v.put(
                                            "additionalGlobal",
                                            r.additionalGlobal() == null
                                                    ? null
                                                    : analysisView(
                                                            r.additionalGlobal(),
                                                            alternativesLimit));
                                    return v;
                                })
                        .toList());
        return gson.toJson(root);
    }

    public String error(String code, String message) {
        var root = envelope("error");
        root.put("code", code);
        root.put("message", message);
        return gson.toJson(root);
    }

    private Map<String, Object> analysisView(InformationAnalysis a, int limit) {
        var result = map();
        result.put("normalizedOnsets", a.sequence().normalizedOnsets());
        result.put("onsetPositions", a.sequence().positions());
        result.put("onsetCount", a.sequence().onsetCount());
        result.put("frame", a.measurementKey().frame());
        result.put("measurementKey", a.measurementKey());
        result.put("unit", a.unit());
        result.put("totalBits", a.totalBits());
        result.put("optimality", "EXACT");
        Scope scope = a.measurementKey().scope();
        result.put(
                "globalDescription",
                scope == Scope.GLOBAL_DESCRIPTION
                        ? windowView(a.windows().getFirst(), limit)
                        : null);
        result.put("isolatedTotal", scope == Scope.ISOLATED_WINDOWS ? a.totalBits() : null);
        result.put("continuationTotal", scope == Scope.CAUSAL_WINDOWS ? a.totalBits() : null);
        result.put(
                "windows",
                scope == Scope.GLOBAL_DESCRIPTION
                        ? List.of()
                        : a.windows().stream().map(w -> windowView(w, limit)).toList());
        result.put("barContributions", a.barContributions());
        result.put("windowMetrics", a.windowMetrics());
        result.put("barMetrics", a.barMetrics());
        result.put(
                "windowMetricsUnavailableReason",
                a.windowMetrics() == null ? "GLOBAL_SCOPE_HAS_NO_LOCAL_PROFILE" : null);
        result.put(
                "barMetricsUnavailableReason", a.barMetrics() == null ? "NO_BAR_GROUPING" : null);
        result.put("referenceAssessments", a.referenceAssessments());
        result.put(
                "referenceAmbiguityCount",
                Math.toIntExact(
                        a.referenceAssessments().stream()
                                .filter(r -> r.reference() != null)
                                .count()));
        return result;
    }

    private Map<String, Object> windowView(InformationAnalysis.Window w, int limit) {
        var result = map();
        result.put("position", w.position());
        result.put("start", w.start());
        result.put("end", w.end());
        result.put("normalizedOnsets", w.sequence().normalizedOnsets());
        result.put("measurementKey", w.measurementKey());
        result.put("unit", "BITS");
        result.put("totalBits", w.totalBits());
        result.put("modelBits", w.witness().modelBits());
        result.put("residualBits", w.witness().residualBits());
        result.put("canonicalCode", w.witness().code());
        result.put("descriptionTree", descriptionView(w.witness()));
        result.put("optimality", "EXACT");
        result.put("contextDictionary", w.contextDictionary());
        var ambiguity = map();
        ambiguity.put("optimalCodeCount", w.optimalCodeCount());
        ambiguity.put("alternatives", w.optimalCodes().alternatives(limit));
        ambiguity.put(
                "viewTruncated", w.optimalCodeCount().compareTo(BigInteger.valueOf(limit)) > 0);
        ambiguity.put("dag", w.optimalCodes().graph());
        result.put("ambiguitySummary", ambiguity);
        return result;
    }

    private Map<String, Object> descriptionView(Description d) {
        var result = map();
        result.put("form", d.form());
        result.put("slots", d.slots());
        result.put("code", d.code());
        result.put("totalBits", d.totalBits());
        result.put("modelBits", d.modelBits());
        result.put("residualBits", d.residualBits());
        result.put("period", d.period());
        result.put("split", d.split());
        result.put("template", d.template());
        result.put("templateIndex", d.templateIndex());
        result.put("exceptionPositions", d.exceptionPositions());
        result.put("children", d.children().stream().map(this::descriptionView).toList());
        return result;
    }

    private static Map<String, Object> frameView(AnalysisFrame f) {
        var result = map();
        result.put("slots", f.slots());
        result.put("origin", 0);
        result.put("observation", "GRID_EXACT");
        var time = map();
        time.put("status", f.slotDurationMs() == null ? "UNKNOWN" : "KNOWN");
        time.put("slotDurationMs", f.slotDurationMs());
        time.put("quarterBpm", f.quarterBpm());
        time.put("ticksPerQuarter", f.ticksPerQuarter());
        result.put("time", time);
        var notation = map();
        notation.put("numerator", f.numerator());
        notation.put("denominator", f.denominator());
        notation.put("barSlots", f.notatedBarSlots());
        result.put("notation", f.numerator() == null ? null : notation);
        return result;
    }

    private static Map<String, Object> envelope(String kind) {
        var result = map();
        result.put("schemaVersion", RESULT_VERSION);
        result.put("kind", kind);
        return result;
    }

    private static Map<String, Object> map() {
        return new LinkedHashMap<>();
    }

    private static JsonElement read(Path path) throws IOException {
        try (Reader input = Files.newBufferedReader(path, StandardCharsets.UTF_8);
                JsonReader reader = new JsonReader(input)) {
            reader.setStrictness(Strictness.STRICT);
            JsonElement result = readValue(reader);
            if (reader.peek() != JsonToken.END_DOCUMENT)
                throw new IllegalArgumentException("Extra JSON after request");
            return result;
        } catch (JsonParseException
                | NumberFormatException
                | com.google.gson.stream.MalformedJsonException
                | java.io.EOFException e) {
            throw new IllegalArgumentException("Invalid JSON: " + e.getMessage(), e);
        }
    }

    /** Use Gson's strict lexer; preserve numbers and reject duplicate object keys. */
    private static JsonElement readValue(JsonReader r) throws IOException {
        switch (r.peek()) {
            case BEGIN_OBJECT -> {
                JsonObject o = new JsonObject();
                r.beginObject();
                while (r.hasNext()) {
                    String key = r.nextName();
                    if (o.has(key))
                        throw new IllegalArgumentException("Duplicate JSON key: " + key);
                    o.add(key, readValue(r));
                }
                r.endObject();
                return o;
            }
            case BEGIN_ARRAY -> {
                JsonArray a = new JsonArray();
                r.beginArray();
                while (r.hasNext()) a.add(readValue(r));
                r.endArray();
                return a;
            }
            case STRING -> {
                return new JsonPrimitive(r.nextString());
            }
            case NUMBER -> {
                return new JsonPrimitive(new BigDecimal(r.nextString()));
            }
            case BOOLEAN -> {
                return new JsonPrimitive(r.nextBoolean());
            }
            case NULL -> {
                r.nextNull();
                return JsonNull.INSTANCE;
            }
            default -> throw new IllegalArgumentException("Expected JSON value");
        }
    }

    private static void keys(JsonObject o, String... allowed) {
        Set<String> names = Set.of(allowed);
        for (String key : o.keySet())
            if (!names.contains(key)) throw new IllegalArgumentException("Unknown field: " + key);
    }

    private static void forbid(JsonObject o, String... names) {
        for (String key : names)
            if (o.has(key)) throw new IllegalArgumentException("Field not allowed here: " + key);
    }

    private static JsonElement required(JsonObject o, String key) {
        if (!o.has(key) || o.get(key).isJsonNull())
            throw new IllegalArgumentException("Missing field: " + key);
        return o.get(key);
    }

    private static JsonObject object(JsonElement e, String name) {
        if (e == null || !e.isJsonObject())
            throw new IllegalArgumentException("Expected object: " + name);
        return e.getAsJsonObject();
    }

    private static JsonArray array(JsonElement e) {
        if (e == null || !e.isJsonArray()) throw new IllegalArgumentException("Expected array");
        return e.getAsJsonArray();
    }

    private static String string(JsonElement e) {
        if (!e.isJsonPrimitive() || !e.getAsJsonPrimitive().isString())
            throw new IllegalArgumentException("Expected string");
        return e.getAsString();
    }

    private static String numberText(JsonElement e) {
        if (!e.isJsonPrimitive() || e.getAsJsonPrimitive().isBoolean())
            throw new IllegalArgumentException("Expected exact numeric value");
        return e.getAsString();
    }

    private static BigInteger bigInteger(JsonElement e) {
        try {
            return e.isJsonPrimitive() && e.getAsJsonPrimitive().isString()
                    ? new BigInteger(e.getAsString())
                    : new BigDecimal(numberText(e)).toBigIntegerExact();
        } catch (ArithmeticException | NumberFormatException ex) {
            throw new IllegalArgumentException("Expected integer", ex);
        }
    }

    private static Long longInteger(JsonElement e) {
        try {
            return bigInteger(e).longValueExact();
        } catch (ArithmeticException ex) {
            throw new IllegalArgumentException("Seed must fit a signed LONG", ex);
        }
    }

    private static int integer(JsonElement e) {
        if (!e.isJsonPrimitive() || !e.getAsJsonPrimitive().isNumber())
            throw new IllegalArgumentException("Expected JSON integer");
        try {
            return bigInteger(e).intValueExact();
        } catch (ArithmeticException ex) {
            throw new IllegalArgumentException("Integer out of range", ex);
        }
    }

    private static Rational rational(JsonElement e) {
        return Rational.parse(numberText(e));
    }

    private static String text(JsonObject o, String key, String fallback) {
        return o.has(key) ? string(required(o, key)) : fallback;
    }

    private static Integer optionalInt(JsonObject o, String key) {
        return o.has(key) ? integer(required(o, key)) : null;
    }

    private static boolean bool(JsonObject o, String key, boolean fallback) {
        if (!o.has(key)) return fallback;
        var e = required(o, key);
        if (!e.isJsonPrimitive() || !e.getAsJsonPrimitive().isBoolean())
            throw new IllegalArgumentException("Expected boolean: " + key);
        return e.getAsBoolean();
    }

    private static void version(JsonObject o, String version) {
        if (!string(required(o, "schemaVersion")).equals(version))
            throw new IllegalArgumentException("Unsupported schemaVersion");
    }

    private static <T extends Enum<T>> T enumeration(Class<T> type, String value) {
        try {
            return Enum.valueOf(type, value);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown " + type.getSimpleName() + ": " + value, e);
        }
    }

    private static <T extends Enum<T>> T optionalEnum(JsonObject o, String key, Class<T> type) {
        return o.has(key) ? enumeration(type, string(required(o, key))) : null;
    }

    private static List<Integer> integers(JsonElement array) {
        List<Integer> result = new ArrayList<>();
        for (JsonElement e : array(array)) result.add(integer(e));
        return List.copyOf(result);
    }

    private static Set<Integer> positions(JsonObject o, String key) {
        if (!o.has(key)) return Set.of();
        List<Integer> list = integers(o.get(key));
        if (new HashSet<>(list).size() != list.size())
            throw new IllegalArgumentException("Duplicate positions: " + key);
        return Set.copyOf(list);
    }

    private static IntRange range(JsonElement e) {
        if (e.isJsonPrimitive()) {
            int value = integer(e);
            return new IntRange(value, value);
        }
        var o = object(e, "range");
        keys(o, "min", "max");
        return new IntRange(integer(required(o, "min")), integer(required(o, "max")));
    }

    private static RationalRange rationalRange(JsonElement e) {
        var o = object(e, "rational range");
        keys(o, "min", "max");
        if (!o.has("min") && !o.has("max"))
            throw new IllegalArgumentException("Empty rational range");
        return new RationalRange(
                o.has("min") ? rational(required(o, "min")) : null,
                o.has("max") ? rational(required(o, "max")) : null);
    }

    private static List<IntRange> profile(JsonObject o, String key) {
        if (!o.has(key)) return List.of();
        List<IntRange> result = new ArrayList<>();
        for (JsonElement e : array(o.get(key))) result.add(range(e));
        if (result.isEmpty()) throw new IllegalArgumentException("Empty profile");
        return List.copyOf(result);
    }
}

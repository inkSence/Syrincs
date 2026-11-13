package syrincs.c_adapters.rhythm.information;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;

import syrincs.a_domain.rhythm.metricstate.CostRange;
import syrincs.a_domain.rhythm.metricstate.MetricInterpretation;
import syrincs.a_domain.rhythm.metricstate.MetricState;
import syrincs.a_domain.rhythm.metricstate.ProtocolStep;
import syrincs.b_application.rhythm.metricstate.MetricStateResult;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * JSON mapping of the {@code metric-state-v1} analysis. Own contract version, separate from {@link
 * RhythmInformationJson}; the schema is {@code rhythm-information/metric-state-result-v1.schema.json}.
 */
public final class MetricStateJson {
    public static final String RESULT_VERSION = "metric-state-result-v1";
    public static final String SCHEMA_RESOURCE =
            "/rhythm-information/metric-state-result-v1.schema.json";

    private final Gson gson =
            new GsonBuilder()
                    .serializeNulls()
                    .setPrettyPrinting()
                    .disableHtmlEscaping()
                    .registerTypeAdapter(
                            BigInteger.class,
                            (JsonSerializer<BigInteger>)
                                    (r, t, c) -> new JsonPrimitive(r.toString()))
                    .create();

    public String result(MetricStateResult result) {
        var a = result.analysis();
        var root = map();
        root.put("schemaVersion", RESULT_VERSION);
        root.put("kind", "metric-state-analysis");
        root.put("measureVersion", a.key().measureVersion());
        var key = map();
        key.put("measureVersion", a.key().measureVersion());
        key.put("soundCondition", a.key().sound().name());
        key.put("beatSlots", a.key().beatSlots());
        key.put("barSlots", a.key().barSlots());
        root.put("measurementKey", key);
        var example = map();
        example.put("events", a.example().normalizedEvents());
        example.put("slots", a.example().slots());
        example.put("beats", a.example().beats());
        example.put("bars", a.example().bars());
        root.put("example", example);
        root.put("unit", "BITS");
        root.put("valueBits", a.valueBits());
        root.put("instructionCount", a.instructionCount());
        root.put("minimalPathCount", a.minimalPathCount());
        root.put("ambiguous", a.isAmbiguous());
        var sequences = map();
        sequences.put("distinctCount", a.minimalSequenceCodes().size());
        sequences.put("complete", a.minimalSequencesComplete());
        sequences.put("codes", a.minimalSequenceCodes());
        root.put("minimalSequences", sequences);
        root.put("displayLimit", result.displayLimit());
        root.put("minimalPathsTruncated", a.minimalPathsTruncated());
        List<Object> paths = new ArrayList<>();
        for (MetricInterpretation path : a.minimalPaths()) paths.add(path(path));
        root.put("minimalPaths", paths);
        root.put("barCosts", ranges(a.barCosts()));
        root.put("beatCosts", ranges(a.beatCosts()));
        root.put("valueAfterBars", a.valueAfterBars());
        return gson.toJson(root);
    }

    private Map<String, Object> path(MetricInterpretation path) {
        var view = map();
        view.put("instructions", path.codes());
        view.put("costBits", path.costBits());
        List<Object> steps = new ArrayList<>();
        for (ProtocolStep step : path.protocol()) {
            var s = map();
            s.put("position", step.position());
            s.put("event", step.event().name());
            s.put("barStart", step.barStart());
            s.put("stateBefore", state(step.stateBefore()));
            s.put("stateAfter", state(step.stateAfter()));
            s.put("rule", step.rule().name());
            s.put("tableRow", step.rule().row());
            s.put("instructions", step.instructions().stream().map(i -> i.code()).toList());
            s.put("costBits", step.costBits());
            steps.add(s);
        }
        view.put("protocol", steps);
        return view;
    }

    private static Map<String, Object> state(MetricState state) {
        if (state == null) return null;
        var view = map();
        view.put("mode", state.mode().name());
        view.put("delta", state.delta());
        view.put("anchor", state.anchor());
        view.put("expectation", state.expectation());
        return view;
    }

    private static List<Object> ranges(List<CostRange> ranges) {
        List<Object> result = new ArrayList<>();
        for (CostRange r : ranges) {
            var view = map();
            view.put("index", r.index());
            view.put("startPosition", r.startPosition());
            view.put("endPosition", r.endPosition());
            view.put("minBits", r.minBits());
            view.put("maxBits", r.maxBits());
            result.add(view);
        }
        return result;
    }

    private static Map<String, Object> map() {
        return new LinkedHashMap<>();
    }
}

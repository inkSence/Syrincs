package syrincs.b_application.rhythm.information;

import syrincs.a_domain.rhythm.information.*;
import syrincs.a_domain.rhythm.metricstate.MetricStateAnalyzer;
import syrincs.b_application.ports.MetricStateAnalyzerPort;
import syrincs.b_application.ports.RhythmInformationAnalyzerPort;
import syrincs.b_application.rhythm.metricstate.AnalyzeMetricStateUseCase;
import syrincs.b_application.rhythm.metricstate.MetricStateRequest;
import syrincs.b_application.rhythm.metricstate.MetricStateResult;

/** Separate composition from the existing legacy UseCaseInteractor. */
public final class RhythmInformationFacade {
    private final AnalyzeRhythmInformationUseCase analyze;
    private final DecodeRhythmDescriptionUseCase decode;
    private final GenerateRhythmsByInformationUseCase generate;
    private final AnalyzeMetricStateUseCase metricState;

    public RhythmInformationFacade() {
        this(new RhythmInformationAnalyzer()::analyze);
    }

    public RhythmInformationFacade(RhythmInformationAnalyzerPort analyzer) {
        this(analyzer, new MetricStateAnalyzer()::analyze);
    }

    public RhythmInformationFacade(
            RhythmInformationAnalyzerPort analyzer, MetricStateAnalyzerPort metricStateAnalyzer) {
        metricState = new AnalyzeMetricStateUseCase(metricStateAnalyzer);
        analyze = new AnalyzeRhythmInformationUseCase(analyzer);
        decode = new DecodeRhythmDescriptionUseCase(analyze);
        generate = new GenerateRhythmsByInformationUseCase(analyzer);
    }

    public AnalysisResult analyze(
            OnsetSequence word,
            AnalysisSettings settings,
            boolean includeGlobal,
            boolean compareLegacy) {
        return analyze.execute(word, settings, includeGlobal, compareLegacy);
    }

    public DecodeRhythmDescriptionUseCase.Result decode(
            String code, AnalysisSettings settings, boolean analyze) {
        return decode.execute(code, settings, analyze);
    }

    public GenerationResult generate(GenerationRequest request) {
        return generate.execute(request);
    }

    /** Separate third measure {@code metric-state-v1}; no conversion to the other measures. */
    public MetricStateResult analyzeMetricState(MetricStateRequest request) {
        return metricState.execute(request);
    }
}

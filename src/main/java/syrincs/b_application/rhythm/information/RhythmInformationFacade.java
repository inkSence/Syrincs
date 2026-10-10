package syrincs.b_application.rhythm.information;

import syrincs.a_domain.rhythm.information.*;
import syrincs.b_application.ports.RhythmInformationAnalyzerPort;

/** Separate composition from the existing legacy UseCaseInteractor. */
public final class RhythmInformationFacade {
    private final AnalyzeRhythmInformationUseCase analyze;
    private final DecodeRhythmDescriptionUseCase decode;
    private final GenerateRhythmsByInformationUseCase generate;

    public RhythmInformationFacade() {
        this(new RhythmInformationAnalyzer()::analyze);
    }

    public RhythmInformationFacade(RhythmInformationAnalyzerPort analyzer) {
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
}

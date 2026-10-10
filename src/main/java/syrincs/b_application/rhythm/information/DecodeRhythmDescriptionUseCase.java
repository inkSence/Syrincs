package syrincs.b_application.rhythm.information;

import syrincs.a_domain.rhythm.information.*;

import java.util.List;

public final class DecodeRhythmDescriptionUseCase {
    public record Result(
            String code,
            int witnessLength,
            OnsetSequence sequence,
            MeasurementKey measurementKey,
            List<Description> messages,
            AnalysisResult analysis) {
        public Result {
            messages = List.copyOf(messages);
        }
    }

    private final AnalyzeRhythmInformationUseCase analyzer;

    public DecodeRhythmDescriptionUseCase(AnalyzeRhythmInformationUseCase analyzer) {
        this.analyzer = analyzer;
    }

    public Result execute(String code, AnalysisSettings settings, boolean analyze) {
        var decoded =
                new DescriptionDecoder()
                        .decode(
                                code,
                                settings.frame().slots(),
                                settings.scope(),
                                settings.segmentation(),
                                settings.historyBits());
        var sequence = OnsetSequence.fromBits(decoded.bits());
        return new Result(
                code,
                code.length(),
                sequence,
                MeasurementKey.of(settings),
                decoded.messages(),
                analyze ? analyzer.execute(sequence, settings, false, false) : null);
    }
}

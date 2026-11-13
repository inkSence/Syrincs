package syrincs.a_domain.rhythm.information;

import java.math.BigInteger;
import java.util.List;

public record InformationAnalysis(
        OnsetSequence sequence,
        MeasurementKey measurementKey,
        String unit,
        int totalBits,
        List<Window> windows,
        List<Integer> barContributions,
        ProfileMetrics windowMetrics,
        ProfileMetrics barMetrics,
        List<ReferenceAssessment> referenceAssessments) {
    public InformationAnalysis {
        windows = List.copyOf(windows);
        barContributions = List.copyOf(barContributions);
        referenceAssessments = List.copyOf(referenceAssessments);
    }

    public record Window(
            int position,
            int start,
            int end,
            OnsetSequence sequence,
            MeasurementKey measurementKey,
            Description witness,
            OptimalCodes optimalCodes,
            List<String> contextDictionary) {
        public Window {
            contextDictionary = List.copyOf(contextDictionary);
        }

        public int totalBits() {
            return witness.totalBits();
        }

        public BigInteger optimalCodeCount() {
            return optimalCodes.count();
        }
    }
}

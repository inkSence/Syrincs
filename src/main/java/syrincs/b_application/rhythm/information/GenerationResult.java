package syrincs.b_application.rhythm.information;

import java.math.BigInteger;
import java.util.List;

public record GenerationResult(
        GenerationRequest request,
        int requestedCount,
        int returnedCount,
        boolean targetsSatisfiedForEveryResult,
        boolean requestedCountReached,
        int shortfall,
        boolean searchComplete,
        StopReason stopReason,
        BigInteger attempts,
        BigInteger uniqueCandidatesEvaluated,
        BigInteger matchingCandidatesObserved,
        BigInteger totalMatches,
        BigInteger maskSpaceSize,
        BigInteger codeSpaceSize,
        boolean selectionPerformed,
        String randomAlgorithm,
        String enumerationVersion,
        List<AnalysisResult> rhythms) {
    public enum StopReason {
        COMPLETE,
        NO_SOLUTION,
        BUDGET_EXHAUSTED,
        REQUESTED_COUNT_REACHED,
        INTERRUPTED
    }

    public GenerationResult {
        rhythms = List.copyOf(rhythms);
    }
}

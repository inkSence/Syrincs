package syrincs.a_domain.rhythm.information;

import java.util.List;
import java.util.Objects;

public record AnalysisSettings(
        AnalysisFrame frame,
        Scope scope,
        Segmentation segmentation,
        String initialHistory,
        List<MetricReference> references,
        String referenceProfile) {
    public AnalysisSettings {
        Objects.requireNonNull(frame);
        Objects.requireNonNull(scope);
        initialHistory = OnsetSequence.normalize(initialHistory, true);
        references = List.copyOf(references);
        if ((scope == Scope.GLOBAL_DESCRIPTION) != (segmentation == null))
            throw new IllegalArgumentException(
                    "Local scopes require explicit segmentation; global scope has none");
        if (scope != Scope.CAUSAL_WINDOWS && !initialHistory.isEmpty())
            throw new IllegalArgumentException("History requires causal scope");
        if (segmentation != null) segmentation.validate(frame);
        if (referenceProfile != null && !referenceProfile.equals(ReferenceAssessment.TIME_PROFILE))
            throw new IllegalArgumentException("Unknown reference profile");
        if (references.stream().map(MetricReference::id).distinct().count() != references.size())
            throw new IllegalArgumentException("Duplicate reference ids");
    }

    public static AnalysisSettings global(int slots) {
        return new AnalysisSettings(
                AnalysisFrame.unknown(slots), Scope.GLOBAL_DESCRIPTION, null, "", List.of(), null);
    }

    public String historyBits() {
        return initialHistory.replace('x', '1').replace('o', '0');
    }

    public AnalysisSettings global() {
        return new AnalysisSettings(
                frame, Scope.GLOBAL_DESCRIPTION, null, "", references, referenceProfile);
    }
}

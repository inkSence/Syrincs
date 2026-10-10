package syrincs.b_application.ports;

import syrincs.a_domain.rhythm.information.AnalysisSettings;
import syrincs.a_domain.rhythm.information.InformationAnalysis;
import syrincs.a_domain.rhythm.information.OnsetSequence;

@FunctionalInterface
public interface RhythmInformationAnalyzerPort {
    InformationAnalysis analyze(OnsetSequence sequence, AnalysisSettings settings);
}

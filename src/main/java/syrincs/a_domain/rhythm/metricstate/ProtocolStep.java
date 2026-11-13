package syrincs.a_domain.rhythm.metricstate;

import java.util.List;
import java.util.Objects;

/**
 * One position of one path. {@code stateBefore} is the state in which the event is evaluated,
 * i.e. after the free anchor reset of a bar change ({@code barStart}); it is {@code null} at the
 * beginning of the example (position 0), where no playing mode is initialized yet.
 */
public record ProtocolStep(
        int position,
        EventKind event,
        boolean barStart,
        MetricState stateBefore,
        MetricState stateAfter,
        TransitionRule rule,
        List<Instruction> instructions,
        int costBits) {
    public ProtocolStep {
        Objects.requireNonNull(event, "Event");
        Objects.requireNonNull(stateAfter, "State after");
        Objects.requireNonNull(rule, "Rule");
        instructions = List.copyOf(instructions);
    }
}

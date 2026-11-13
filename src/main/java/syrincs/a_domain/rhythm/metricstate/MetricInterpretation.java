package syrincs.a_domain.rhythm.metricstate;

import java.util.List;

/** One minimal path: its instruction sequence and the full protocol, one step per position. */
public record MetricInterpretation(
        List<Instruction> instructions, List<ProtocolStep> protocol, int costBits) {
    public MetricInterpretation {
        instructions = List.copyOf(instructions);
        protocol = List.copyOf(protocol);
    }

    public List<String> codes() {
        return instructions.stream().map(Instruction::code).toList();
    }
}

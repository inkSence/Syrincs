package syrincs.a_domain.rhythm.information;

/** Null time/notation components explicitly mean UNKNOWN, never a musical default. */
public record AnalysisFrame(
        int slots,
        Rational slotDurationMs,
        Rational quarterBpm,
        Integer ticksPerQuarter,
        Integer numerator,
        Integer denominator) {
    public AnalysisFrame {
        if (slots < 1 || slots > OnsetSequence.MAX_SLOTS)
            throw new IllegalArgumentException("Slots must be 1..128");
        if ((quarterBpm == null) != (ticksPerQuarter == null))
            throw new IllegalArgumentException(
                    "Tempo and ticks-per-quarter must be supplied together");
        if ((numerator == null) != (denominator == null))
            throw new IllegalArgumentException("Incomplete time signature");
        if (quarterBpm != null) {
            if (quarterBpm.compareTo(Rational.ZERO) <= 0 || ticksPerQuarter <= 0)
                throw new IllegalArgumentException("Tempo and raster must be positive");
            Rational computed = Rational.of(60000).divide(quarterBpm.multiply(ticksPerQuarter));
            if (slotDurationMs != null && !computed.equals(slotDurationMs))
                throw new IllegalArgumentException("Contradictory slot duration");
            slotDurationMs = computed;
        }
        if (slotDurationMs != null && slotDurationMs.compareTo(Rational.ZERO) <= 0)
            throw new IllegalArgumentException("Slot duration must be positive");
        if (numerator != null) {
            if (numerator <= 0 || denominator <= 0 || ticksPerQuarter == null)
                throw new IllegalArgumentException(
                        "Notation requires positive numerator/denominator and tempo raster");
            Rational length =
                    Rational.of(numerator)
                            .multiply(4)
                            .multiply(ticksPerQuarter)
                            .divide(Rational.of(denominator));
            if (!length.isInteger()
                    || length.numerator().signum() <= 0
                    || length.numerator().bitLength() > 31)
                throw new IllegalArgumentException("Notated bar length must be a positive integer");
        }
    }

    public static AnalysisFrame unknown(int slots) {
        return new AnalysisFrame(slots, null, null, null, null, null);
    }

    public Integer notatedBarSlots() {
        return numerator == null
                ? null
                : Rational.of(numerator)
                        .multiply(4)
                        .multiply(ticksPerQuarter)
                        .divide(Rational.of(denominator))
                        .numerator()
                        .intValueExact();
    }
}

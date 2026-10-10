package syrincs.b_application.rhythm.information;

public record IntRange(int min, int max) {
    public IntRange {
        if (min < 0 || max < min)
            throw new IllegalArgumentException("Invalid nonnegative inclusive integer range");
    }

    public boolean contains(int value) {
        return value >= min && value <= max;
    }

    public boolean overlaps(IntRange b) {
        return min <= b.max && b.min <= max;
    }
}

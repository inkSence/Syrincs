package syrincs.a_domain.rhythm.information;

import java.util.ArrayList;
import java.util.List;

public record Segmentation(List<Integer> boundaries, List<Integer> barBoundaries) {
    public Segmentation {
        boundaries = List.copyOf(boundaries);
        barBoundaries = List.copyOf(barBoundaries);
        validatePartition(boundaries);
        if (!barBoundaries.isEmpty()) {
            validatePartition(barBoundaries);
            if (!boundaries.getLast().equals(barBoundaries.getLast())
                    || !boundaries.containsAll(barBoundaries))
                throw new IllegalArgumentException(
                        "Bar boundaries must align with window boundaries");
        }
    }

    private static void validatePartition(List<Integer> points) {
        if (points.size() < 2 || points.getFirst() != 0)
            throw new IllegalArgumentException(
                    "A partition starts at 0 and contains nonempty windows");
        for (int i = 1; i < points.size(); i++)
            if (points.get(i) <= points.get(i - 1))
                throw new IllegalArgumentException("Boundaries must be strictly increasing");
    }

    public void validate(AnalysisFrame frame) {
        if (boundaries.getLast() != frame.slots())
            throw new IllegalArgumentException("Partition must cover exactly N slots");
        if (frame.notatedBarSlots() != null && !barBoundaries.isEmpty()) {
            for (int i = 1; i < barBoundaries.size(); i++)
                if (barBoundaries.get(i) - barBoundaries.get(i - 1) != frame.notatedBarSlots())
                    throw new IllegalArgumentException("Bar grouping contradicts notation");
        }
    }

    public static Segmentation equal(int slots, int windowSlots, Integer barSlots) {
        return new Segmentation(
                points(slots, windowSlots), barSlots == null ? List.of() : points(slots, barSlots));
    }

    private static List<Integer> points(int slots, int size) {
        if (size <= 0 || slots % size != 0)
            throw new IllegalArgumentException("Window/bar size must be positive and divide N");
        List<Integer> result = new ArrayList<>();
        for (int i = 0; i <= slots; i += size) result.add(i);
        return result;
    }
}

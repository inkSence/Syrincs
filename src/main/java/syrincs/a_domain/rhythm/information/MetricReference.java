package syrincs.a_domain.rhythm.information;

import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

/** Active levels are ordered finest to coarsest; a technical raster is not a level. */
public record MetricReference(
        String id,
        int cycleSlots,
        int phase,
        String beatLevel,
        List<Level> levels,
        Provenance provenance,
        String source) {
    public enum Provenance {
        SUPPLIED,
        CANDIDATE
    }

    public enum LevelKind {
        SUBDIVISION,
        BEAT,
        GROUP,
        CYCLE_MARKER
    }

    public record Level(String id, LevelKind kind, List<Integer> positions) {
        public Level {
            Objects.requireNonNull(kind);
            positions = List.copyOf(positions);
            if (id == null || id.isBlank()) throw new IllegalArgumentException("Level needs an id");
            if (positions.size() < (kind == LevelKind.CYCLE_MARKER ? 1 : 2))
                throw new IllegalArgumentException(
                        "Active beat/subdivision/group levels need at least two positions");
            int previous = -1;
            for (int p : positions) {
                if (p <= previous)
                    throw new IllegalArgumentException(
                            "Level positions must be sorted, unique and nonnegative");
                previous = p;
            }
        }
    }

    public MetricReference {
        Objects.requireNonNull(provenance);
        levels = List.copyOf(levels);
        if (id == null || id.isBlank() || source == null || source.isBlank())
            throw new IllegalArgumentException("Reference needs id and source");
        if (cycleSlots < 1 || phase < 0 || phase >= cycleSlots || levels.isEmpty())
            throw new IllegalArgumentException("Invalid reference cycle/phase/levels");
        if (levels.stream().map(Level::id).distinct().count() != levels.size())
            throw new IllegalArgumentException("Duplicate level ids");
        int beats = 0;
        for (int i = 0; i < levels.size(); i++) {
            Level level = levels.get(i);
            if (level.positions().getLast() >= cycleSlots)
                throw new IllegalArgumentException("Reference position outside cycle");
            if (level.id().equals(beatLevel) && level.kind() == LevelKind.BEAT) beats++;
            if (i > 0 && !levels.get(i - 1).positions().containsAll(level.positions()))
                throw new IllegalArgumentException("Reference hierarchy must be nested");
        }
        if (beats != 1 || levels.stream().filter(l -> l.kind() == LevelKind.BEAT).count() != 1)
            throw new IllegalArgumentException("Reference needs exactly one declared beat level");
    }

    public Level beat() {
        return levels.stream().filter(l -> l.id().equals(beatLevel)).findFirst().orElseThrow();
    }

    public List<Integer> inWindow(Level level, int slots) {
        return IntStream.range(0, slots)
                .filter(i -> level.positions().contains(Math.floorMod(i - phase, cycleSlots)))
                .boxed()
                .toList();
    }
}

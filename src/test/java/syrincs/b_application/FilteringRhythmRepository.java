package syrincs.b_application;

import syrincs.a_domain.rhythm.HuffmanRhythm;
import syrincs.b_application.ports.RhythmRepository;
import syrincs.b_application.ports.dto.DeviationRange;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/** Test fake with actual stored-grade/bound filtering, including legacy metadata. */
public class FilteringRhythmRepository implements RhythmRepository {
    public record Entry(HuffmanRhythm rhythm, int information, Double deviation) {
        public static Entry fresh(HuffmanRhythm rhythm) {
            return new Entry(rhythm, rhythm.getInformation(), rhythm.getStandardDeviation());
        }
    }

    private final List<Entry> entries;
    public int queries;
    public int strictQueries;
    public int rangeQueries;
    public Integer lastInformation;
    public Double lastStrictMinimum;
    public DeviationRange lastRange;

    public FilteringRhythmRepository(Entry... entries) {
        this.entries = List.of(entries);
    }

    public FilteringRhythmRepository(List<HuffmanRhythm> rhythms) {
        this(rhythms.stream().map(Entry::fresh).toArray(Entry[]::new));
    }

    @Override public List<Long> saveAll(List<HuffmanRhythm> rhythms) { throw new UnsupportedOperationException(); }
    @Override public List<HuffmanRhythm> getTwoRhythms(Integer first, Integer second) { throw new UnsupportedOperationException(); }

    @Override
    public List<HuffmanRhythm> getAllByInformation(Integer information) {
        return find(information, entry -> true);
    }

    @Override
    public List<HuffmanRhythm> getAllByInformationAndMinDeviation(Integer information, Double minimum) {
        strictQueries++;
        lastStrictMinimum = minimum;
        return find(information, entry -> entry.deviation() != null && entry.deviation() > minimum);
    }

    @Override
    public List<HuffmanRhythm> getAllByInformationAndDeviationRange(Integer information, DeviationRange range) {
        Objects.requireNonNull(range);
        rangeQueries++;
        lastRange = range;
        return find(information, entry ->
                (range.min() == null || entry.deviation() != null && entry.deviation() >= range.min())
                && (range.max() == null || entry.deviation() != null && entry.deviation() <= range.max()));
    }

    private List<HuffmanRhythm> find(Integer information, Predicate<Entry> predicate) {
        queries++;
        lastInformation = information;
        return entries.stream().filter(entry -> entry.information() == information)
                .filter(predicate).map(Entry::rhythm).toList();
    }
}

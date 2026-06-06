package syrincs.b_application;

import org.junit.jupiter.api.Test;
import syrincs.a_domain.rhythm.HuffmanRhythm;
import syrincs.b_application.ports.RhythmRepository;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GenerateAndPersistRhythmUseCaseTest {

    @Test
    void generateFromBinaryStrings_mapsPersistsAndReturnsRhythms() {
        CapturingRhythmRepository repo = new CapturingRhythmRepository();
        var useCase = new GenerateAndPersistRhythmUseCase(repo);

        List<HuffmanRhythm> rhythms = useCase.generateAllRhythmsOfFourQuarters(List.of(
                "0000000000000000",
                "1000000000000001"
        ));

        assertEquals(2, rhythms.size());
        assertEquals(List.of("oooooooooooooooo", "xoooooooooooooox"), repo.savedOnsets);
    }

    @Test
    void generateFromBinaryStrings_rejectsNonBinaryCharacters() {
        var useCase = new GenerateAndPersistRhythmUseCase(new CapturingRhythmRepository());

        assertThrows(IllegalArgumentException.class, () ->
                useCase.generateAllRhythmsOfFourQuarters(List.of("000000000000000x"))
        );
    }

    private static class CapturingRhythmRepository implements RhythmRepository {
        final List<String> savedOnsets = new ArrayList<>();

        @Override
        public List<Long> saveAll(List<HuffmanRhythm> rhythms) {
            for (HuffmanRhythm rhythm : rhythms) {
                savedOnsets.add(rhythm.getOnsetList());
            }
            List<Long> ids = new ArrayList<>(rhythms.size());
            for (long i = 1; i <= rhythms.size(); i++) {
                ids.add(i);
            }
            return ids;
        }

        @Override public List<HuffmanRhythm> getTwoRhythms(Integer id1, Integer id2) { return List.of(); }
        @Override public List<HuffmanRhythm> getAllByInformation(Integer information) { return List.of(); }
        @Override public List<HuffmanRhythm> getAllByInformationAndMinDeviation(Integer information, Double minDeviation) { return List.of(); }
    }
}

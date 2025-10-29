package syrincs.b_application.ports;

import syrincs.a_domain.rhythm.HuffmanRhythm;

import java.util.List;

/**
 * Application-side repository port for persisting HuffmanRhythm entities.
 *
 * Clean Architecture placement:
 * - This interface is a primary port and lives in the application layer (b_application/ports).
 * - Implementations belong to outer adapters (c_adapters), e.g., PostgresRhythmRepository.
 */
public interface RhythmRepository {
    /**
     * Persists all given HuffmanRhythm objects efficiently (preferably batched) and
     * returns generated database ids in insertion order.
     */
    List<Long> saveAll(List<HuffmanRhythm> rhythms);

    /**
     * Loads two rhythms by their database IDs and returns them as a list.
     * Implementations may return fewer than two items if an id is not found.
     */
    List<HuffmanRhythm> getTwoRhythms(Integer id1, Integer id2);

    /**
     * Returns all rhythms that have the given information grade (matches column 'info').
     */
    List<HuffmanRhythm> getAllByInformation(Integer information);
}

package syrincs.a_domain.rhythm.metricstate;

import java.util.Locale;
import java.util.Objects;

/**
 * A finite example: frame plus normalized event text ({@code x} onset, {@code =} tone holds,
 * {@code o} no onset / silence). The notation is separate from RDL.
 */
public record MetricExample(MetricFrame frame, String normalizedEvents) {
    public static final int MAX_SLOTS = 128;

    public MetricExample {
        Objects.requireNonNull(frame, "Frame");
        Objects.requireNonNull(normalizedEvents, "Events");
        normalizedEvents = normalizedEvents.replaceAll("(?U)\\s", "").toLowerCase(Locale.ROOT);
        int n = normalizedEvents.length();
        if (n == 0) throw new IllegalArgumentException("Events must not be empty");
        if (n > MAX_SLOTS)
            throw new IllegalArgumentException(
                    "At most " + MAX_SLOTS + " positions are supported, got " + n);
        for (int i = 0; i < n; i++) {
            char c = normalizedEvents.charAt(i);
            if (c != 'x' && c != 'o' && c != '=')
                throw new IllegalArgumentException(
                        "Events must contain only x, =, o and whitespace; found '"
                                + c
                                + "' at position "
                                + i);
            if (c != '=') continue;
            if (frame.sound() == SoundCondition.IMPULSE)
                throw new IllegalArgumentException(
                        "'=' (tone holds) at position "
                                + i
                                + " is not allowed under IMPULSE; only x and o are valid");
            if (i == 0 || normalizedEvents.charAt(i - 1) == 'o')
                throw new IllegalArgumentException(
                        "'=' (tone holds) at position "
                                + i
                                + " must directly follow x or =");
        }
        if (n % frame.barSlots() != 0)
            throw new IllegalArgumentException(
                    "Event length "
                            + n
                            + " must be a multiple of barSlots ("
                            + frame.barSlots()
                            + ")");
    }

    public static MetricExample parse(MetricFrame frame, String events) {
        return new MetricExample(frame, events);
    }

    public int slots() {
        return normalizedEvents.length();
    }

    public int bars() {
        return slots() / frame.barSlots();
    }

    public int beats() {
        return slots() / frame.beatSlots();
    }

    public boolean isBarStart(int position) {
        return position % frame.barSlots() == 0;
    }

    /** Audible event at a position. A rest onset also arises across a bar line. */
    public EventKind eventAt(int position) {
        char c = normalizedEvents.charAt(position);
        if (c == 'x') return EventKind.ONSET;
        if (c == '=') return EventKind.HOLD;
        if (frame.sound() == SoundCondition.SUSTAINED
                && position > 0
                && normalizedEvents.charAt(position - 1) != 'o') return EventKind.REST_ONSET;
        return EventKind.SILENCE;
    }
}

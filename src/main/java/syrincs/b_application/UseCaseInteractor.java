package syrincs.b_application;

import syrincs.a_domain.chord.NoteCombinator;
import syrincs.a_domain.hindemith.HindemithChord;
import syrincs.a_domain.Tone;
import syrincs.a_domain.hindemith.ChordAnalysis;
import syrincs.b_application.ports.HindemithChordRepositoryPort;
import syrincs.b_application.ports.MidiOutputPort;
import syrincs.a_domain.rhythm.Pattern;
import syrincs.a_domain.rhythm.RhythmSpec;
import syrincs.a_domain.rhythm.VoiceSpec;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UseCaseInteractor {

    private final GenerateChordsUseCase generateChordsUseCase;
    private final AnalyseChordByHindemithUseCase analyseChordByHindemithUseCase;
    private final PersistHindemithChordUseCase persistUseCase;
    private final GetHindemithChordsFromDbUseCase getHindemithChordsFromDbUseCase;
    private final SendToMidiUseCase send;
    private final HindemithChordRepositoryPort repository;
    private final Logger LOGGER = Logger.getLogger(UseCaseInteractor.class.getName());
    private List<syrincs.a_domain.hindemith.HindemithChord> hindemithChords;
    private final ValidatePatternsUseCase validate;
    private final PlaybackRhythmUseCase rhythmPlayback;


    public UseCaseInteractor(MidiOutputPort midiOutput, HindemithChordRepositoryPort repository, PlaybackRhythmUseCase playRhythm) {
        this.repository = repository;
        this.generateChordsUseCase = new GenerateChordsUseCase(
                new NoteCombinator(), new ChordAnalysis(), 3
        );
        this.analyseChordByHindemithUseCase = new AnalyseChordByHindemithUseCase();
        this.persistUseCase = new PersistHindemithChordUseCase(repository);
        this.getHindemithChordsFromDbUseCase = new GetHindemithChordsFromDbUseCase(repository);
        this.send = new SendToMidiUseCase(midiOutput);
        this.validate = new ValidatePatternsUseCase();
        this.rhythmPlayback = playRhythm;
    }

    public List<HindemithChord> findChordsFor(List<Integer> numNotes, List<Integer> groups, Integer rootNote) {
        Objects.requireNonNull(numNotes, "numNotes");
        Objects.requireNonNull(groups, "groups");
        Objects.requireNonNull(rootNote, "rootNote");

        List<HindemithChord> acc = getHindemithChordsFromDbUseCase
                .getAllOfRootNoteGroupsAndNumNotes(rootNote, groups, numNotes);
        LOGGER.log(Level.INFO, "{0} chords loaded.", acc.size() );
        return acc;
    }

    // Overload: also filter by maximum range (maxNote - minNote)
    public List<HindemithChord> findChordsFor(List<Integer> numNotes, List<Integer> groups, Integer rootNote, Integer range) {
        Objects.requireNonNull(numNotes, "numNotes");
        Objects.requireNonNull(groups, "groups");
        Objects.requireNonNull(rootNote, "rootNote");
        Objects.requireNonNull(range, "range");

        List<HindemithChord> acc = getHindemithChordsFromDbUseCase
                .getAllOfRootNoteGroupsAndNumNotes(rootNote, groups, numNotes, range);
        LOGGER.log(Level.INFO, "{0} chords loaded (range<=%d).".formatted(range), acc.size());
        return acc;
    }

    public void validatePattern(Pattern pattern, RhythmSpec spec, List<VoiceSpec> voices) throws ValidatePatternsUseCase.ValidationException {
        validate.validate(pattern, spec, voices);
    }

    public void sendToneToDevice(Tone tone, String deviceNameSubstring) throws MidiUnavailableException, InvalidMidiDataException, InterruptedException {
        // Application layer should not print; delegate to adapter
        send.sendToneToDevice(tone, deviceNameSubstring);
    }

    public void sendChordToDevice(HindemithChord hindemithChord, String deviceNameSubstring, Long duration) throws MidiUnavailableException, InvalidMidiDataException, InterruptedException {
        send.sendChordToDevice(hindemithChord, deviceNameSubstring, duration);

    }

    public void loadHindemithChordsWithMaxGroup(Integer rootNote, Integer maxGroup ){
        hindemithChords = getHindemithChordsFromDbUseCase.getAllOfRootNoteAndMaxGroup(rootNote, maxGroup);
    }

    public void loadHindemithChordsWithGroups(Integer rootNote, List<Integer> groups ){
        hindemithChords = getHindemithChordsFromDbUseCase.loadHindemithChordsWithGroups(rootNote, groups);
    }

    public syrincs.a_domain.hindemith.ChordAnalysis.Result analyzeChordByHindemith(List<Integer> midiNotes) {
        return analyseChordByHindemithUseCase.analyze(midiNotes);
    }

    public List<Long> calculateAndPersistAllChordsToFiveNotes(int minLowerNote, int maxUpperNote) {
        List<HindemithChord> chords = generateChordsUseCase.generateAllChordsToFiveNotes(minLowerNote, maxUpperNote);
        return persistUseCase.persist(chords);
    }

    public List<HindemithChord> getAllChordsFromDb() {
        return getHindemithChordsFromDbUseCase.getAll();
    }

    public void deleteHindemithChords() {
        repository.truncate();
    }

    // Play the chords using the MIDI output adapter
    public void playChords(List<Integer> numNotes, List<Integer> groups, Integer rootNote,
                           Long durationMs, String deviceNameSubstring)
            throws MidiUnavailableException, InvalidMidiDataException, InterruptedException {
        var chords = findChordsFor(numNotes, groups, rootNote);
        if (chords == null || chords.isEmpty()) {
            return;
        }
        for (var hc : chords) {
            sendChordToDevice(hc, deviceNameSubstring, durationMs);
        }
    }

    // Overload: also filter by range
    public void playChords(List<Integer> numNotes, List<Integer> groups, Integer rootNote, Integer range,
                           Long durationMs, String deviceNameSubstring)
            throws MidiUnavailableException, InvalidMidiDataException, InterruptedException {
        var chords = findChordsFor(numNotes, groups, rootNote, range);
        if (chords == null || chords.isEmpty()) {
            return;
        }
        for (var hc : chords) {
            sendChordToDevice(hc, deviceNameSubstring, durationMs);
        }
    }

    // Rhythm: domain-driven playback via RhythmPlaybackPort (no javax types leaking)
    public void playRhythm(Pattern pattern, RhythmSpec spec, List<VoiceSpec> voices, String deviceNameSubstring) throws Exception {
        validate.validate(pattern, spec, voices);
        rhythmPlayback.playRhythm(pattern, spec, voices, deviceNameSubstring);
    }
}

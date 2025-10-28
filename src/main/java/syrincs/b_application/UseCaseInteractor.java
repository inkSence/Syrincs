package syrincs.b_application;

import syrincs.a_domain.chord.NoteCombinator;
import syrincs.a_domain.hindemith.HindemithChord;
import syrincs.a_domain.Tone;
import syrincs.a_domain.hindemith.ChordAnalysis;
import syrincs.b_application.ports.HindemithChordRepositoryPort;
import syrincs.a_domain.rhythm.Pattern;
import syrincs.a_domain.rhythm.RhythmSpec;
import syrincs.a_domain.rhythm.VoiceSpec;

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
    private final AnalyseRhythmUseCase analyseRhythmUseCase;


    public UseCaseInteractor(SendToMidiUseCase send,
                             ValidatePatternsUseCase validate,
                             PlaybackRhythmUseCase playRhythm,
                             AnalyseRhythmUseCase analyseRhythmUseCase,
                             HindemithChordRepositoryPort repository,
                             GenerateChordsUseCase generateChordsUseCase,
                             AnalyseChordByHindemithUseCase analyseChordByHindemithUseCase,
                             GetHindemithChordsFromDbUseCase getHindemithChordsFromDbUseCase,
                             PersistHindemithChordUseCase persistUseCase) {
        this.send = Objects.requireNonNull(send);
        this.validate = Objects.requireNonNull(validate);
        this.rhythmPlayback = Objects.requireNonNull(playRhythm);
        this.analyseRhythmUseCase = Objects.requireNonNull(analyseRhythmUseCase);
        this.repository = Objects.requireNonNull(repository);
        this.generateChordsUseCase = Objects.requireNonNull(generateChordsUseCase);
        this.analyseChordByHindemithUseCase = Objects.requireNonNull(analyseChordByHindemithUseCase);
        this.getHindemithChordsFromDbUseCase = Objects.requireNonNull(getHindemithChordsFromDbUseCase);
        this.persistUseCase = Objects.requireNonNull(persistUseCase);
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

    public void sendToneToDevice(Tone tone, String deviceNameSubstring) {
        // Application layer should not print; delegate to adapter
        send.sendToneToDevice(tone, deviceNameSubstring);
    }

    public void sendChordToDevice(HindemithChord hindemithChord, Long duration) {
        send.sendChordToDevice(hindemithChord, duration);
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


    public void playChords(List<Integer> numNotes, List<Integer> groups, Integer rootNote, Integer range,
                           Long durationMs, Integer channelZeroBased) {
        var chords = (range == null)
                ? findChordsFor(numNotes, groups, rootNote)
                : findChordsFor(numNotes, groups, rootNote, range);
        if (chords == null || chords.isEmpty()) {
            return;
        }
        for (var chord : chords) {
            if (channelZeroBased == null) {
                sendChordToDevice(chord, durationMs);
            } else {
                send.sendChordToDevice(chord, durationMs, channelZeroBased);
            }
        }
    }

    public void playRhythm(Pattern pattern, RhythmSpec spec, List<VoiceSpec> voices) throws Exception {
        validate.validate(pattern, spec, voices);
        rhythmPlayback.playRhythm(pattern, spec, voices);
    }

    public void printRhythmFileContent(){
        System.out.println("Fake Content");
    }

    public Integer analyzeRhythm(String onsetList) {
        return analyseRhythmUseCase.analyzeInformation(onsetList);
    }
}

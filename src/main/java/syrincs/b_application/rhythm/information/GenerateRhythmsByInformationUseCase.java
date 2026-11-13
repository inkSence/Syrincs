package syrincs.b_application.rhythm.information;

import static syrincs.b_application.rhythm.information.GenerationRequest.*;
import static syrincs.b_application.rhythm.information.GenerationResult.StopReason;

import syrincs.a_domain.rhythm.information.*;
import syrincs.b_application.ports.RhythmInformationAnalyzerPort;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.BooleanSupplier;

public final class GenerateRhythmsByInformationUseCase {
    private final AnalyzeRhythmInformationUseCase analyzer;

    public GenerateRhythmsByInformationUseCase(RhythmInformationAnalyzerPort analyzer) {
        this.analyzer = new AnalyzeRhythmInformationUseCase(analyzer);
    }

    public GenerationResult execute(GenerationRequest request) {
        return execute(request, () -> Thread.currentThread().isInterrupted());
    }

    public GenerationResult execute(GenerationRequest request, BooleanSupplier interrupted) {
        GenerationCriteria criteria = new GenerationCriteria(request);
        int n = request.settings().frame().slots();
        Search search = new Search(request, criteria, interrupted);
        boolean complete = false, requestedReached = false;
        BigInteger codeSpace = null;
        switch (request.strategy()) {
            case EXHAUSTIVE -> {
                int size = 1 << n;
                for (int i = 0; i < size; i++) {
                    if (search.shouldStop()) break;
                    search.attempts = search.attempts.add(BigInteger.ONE);
                    search.candidate(word(BigInteger.valueOf(i), n), true);
                }
                complete = search.attempts.equals(BigInteger.valueOf(size));
            }
            case CODE_LENGTH_SEARCH -> {
                DescriptionDecoder decoder = new DescriptionDecoder();
                BigInteger space = BigInteger.ZERO;
                for (int length = request.information().min();
                        length <= request.information().max();
                        length++) space = space.add(BigInteger.ONE.shiftLeft(length));
                codeSpace = space;
                outer:
                for (int length = request.information().min();
                        length <= request.information().max();
                        length++) {
                    BigInteger size = BigInteger.ONE.shiftLeft(length);
                    for (BigInteger code = BigInteger.ZERO;
                            code.compareTo(size) < 0;
                            code = code.add(BigInteger.ONE)) {
                        if (search.shouldStop()) break outer;
                        search.attempts = search.attempts.add(BigInteger.ONE);
                        DescriptionDecoder.Decoded decoded;
                        try {
                            decoded =
                                    decoder.decode(
                                            binary(code, length),
                                            n,
                                            request.settings().scope(),
                                            request.settings().segmentation(),
                                            request.settings().historyBits());
                        } catch (IllegalArgumentException invalidCode) {
                            continue;
                        }
                        search.candidate(OnsetSequence.fromBits(decoded.bits()), false);
                    }
                }
                complete = search.attempts.equals(space);
            }
            case BOUNDED_RANDOM -> {
                Random random = new Random(request.seed());
                while (!search.shouldStop()) {
                    StringBuilder bits = new StringBuilder(n);
                    for (int i = 0; i < n; i++) bits.append(random.nextBoolean() ? '1' : '0');
                    search.attempts = search.attempts.add(BigInteger.ONE);
                    search.candidate(OnsetSequence.fromBits(bits.toString()), false);
                    if (search.matches.size() >= request.resultLimit()) {
                        requestedReached = true;
                        break;
                    }
                }
            }
        }
        StopReason reason =
                complete
                        ? (search.matches.isEmpty() ? StopReason.NO_SOLUTION : StopReason.COMPLETE)
                        : search.wasInterrupted
                                ? StopReason.INTERRUPTED
                                : requestedReached
                                        ? StopReason.REQUESTED_COUNT_REACHED
                                        : StopReason.BUDGET_EXHAUSTED;
        search.matches.sort(BigInteger::compareTo);
        boolean selectionPerformed = request.selection() == Selection.FIRST || complete;
        int returned = Math.min(request.resultLimit(), search.matches.size());
        if (request.selection() == Selection.RANDOM_WITHOUT_REPLACEMENT && complete) {
            Random random = new Random(request.seed());
            for (int i = 0; i < returned; i++) {
                int j = i + random.nextInt(search.matches.size() - i);
                BigInteger value = search.matches.get(i);
                search.matches.set(i, search.matches.get(j));
                search.matches.set(j, value);
            }
        }
        List<AnalysisResult> results = new ArrayList<>();
        for (int i = 0; i < returned; i++) {
            OnsetSequence word = word(search.matches.get(i), n);
            AnalysisResult result = search.analyze(word);
            if (!criteria.eventsMatch(word) || !criteria.analysisMatches(result))
                throw new IllegalStateException("Generated result failed final shared analysis");
            results.add(result);
        }
        BigInteger matching = BigInteger.valueOf(search.matches.size());
        String order =
                switch (request.strategy()) {
                    case EXHAUSTIVE -> "chronological-mask-v1";
                    case CODE_LENGTH_SEARCH -> "shortlex-code-v1";
                    case BOUNDED_RANDOM -> "chronological-nextBoolean-v1";
                };
        return new GenerationResult(
                request,
                request.resultLimit(),
                returned,
                true,
                returned == request.resultLimit(),
                request.resultLimit() - returned,
                complete,
                reason,
                search.attempts,
                search.evaluated,
                matching,
                complete ? matching : null,
                BigInteger.ONE.shiftLeft(n),
                codeSpace,
                selectionPerformed,
                request.seed() == null ? null : "java.util.Random",
                order,
                results);
    }

    private final class Search {
        final GenerationRequest request;
        final GenerationCriteria criteria;
        final BooleanSupplier interrupted;
        final Set<BigInteger> seen = new HashSet<>();
        final List<BigInteger> matches = new ArrayList<>();
        BigInteger attempts = BigInteger.ZERO, evaluated = BigInteger.ZERO;
        boolean wasInterrupted;

        Search(GenerationRequest r, GenerationCriteria c, BooleanSupplier i) {
            request = r;
            criteria = c;
            interrupted = i;
        }

        boolean shouldStop() {
            if (interrupted.getAsBoolean()) {
                wasInterrupted = true;
                return true;
            }
            return request.attemptBudget() != null
                    && attempts.compareTo(request.attemptBudget()) >= 0;
        }

        AnalysisResult analyze(OnsetSequence word) {
            return analyzer.execute(
                    word, request.settings(), request.additionalGlobalInformation() != null, false);
        }

        void candidate(OnsetSequence word, boolean allowEventPrefilter) {
            if (!seen.add(word.mask())) return;
            if (allowEventPrefilter && !criteria.eventsMatch(word)) return;
            AnalysisResult result = analyze(word);
            evaluated = evaluated.add(BigInteger.ONE);
            if (criteria.eventsMatch(word) && criteria.analysisMatches(result))
                matches.add(word.mask());
        }
    }

    private static OnsetSequence word(BigInteger mask, int n) {
        return OnsetSequence.fromBits(binary(mask, n));
    }

    private static String binary(BigInteger number, int length) {
        String bits = number.toString(2);
        return "0".repeat(length - bits.length()) + bits;
    }
}

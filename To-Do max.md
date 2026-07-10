## Mein Urteil

Syrincs wird fachlich besser, wenn es sich vom Materialkatalog zum Werkzeug für musikalische Verläufe entwickelt:

Einzelmaterial erklären → zeitliche Spannung analysieren → gezielt auswählen → kontrolliert erzeugen.

Die vorhandene To-Do xhigh.md geht bereits in die richtige Richtung. Ich würde sie an drei Stellen schärfen:

1. Vor der Akkordfolgen-Generierung eine Folgenanalyse einführen.
2. Bei Rhythmen den zeitlichen Informationsverlauf statt nur Summe und Abweichung nutzen.
3. Idempotenz mit einer Versionierung der Analyse verbinden.

## Empfohlene Priorisierung

| Prio | Aufgabe                                     | Fachlicher Gewinn                                      | Aufwand      |
| ---: | ------------------------------------------- | ------------------------------------------------------ | ------------ |
|    1 | Hindemith-Analyse verifizieren und erklären | Vertrauen in das Alleinstellungsmerkmal                | Mittel       |
|    2 | analyze progression: harmonisches Gefälle   | Hindemith wird erstmals auf Musik über Zeit angewendet | Mittel       |
|    3 | Rhythmisches Informationsprofil und Suche   | Aus Zufallsauswahl wird gestaltbare Rhythmik           | Mittel       |
|    4 | Analyseversionen und reanalyze              | Gespeicherte Ergebnisse bleiben fachlich korrekt       | Mittel       |
|    5 | MIDI-Export                                 | Ergebnisse werden reproduzierbar und DAW-tauglich      | Klein–mittel |
|    6 | Akkordfolgen nach Zielverlauf erzeugen      | Echte algorithmische Komposition                       | Groß         |
|    7 | RDL-Timing korrigieren und Stimmen öffnen   | Brauchbarer Sequencer, aber weniger projektspezifisch  | Mittel       |
|    8 | Harmonie und Rhythmus zu Phrasen verbinden  | Langfristiges, starkes Alleinstellungsmerkmal          | Groß         |


### 1. Erklärbare Hindemith-Analyse

Das ist weiterhin die richtige erste Aufgabe. Die Begründung ist jetzt noch stärker: Eine vollständige Diagnose aller 1.507 drei- bis fünfstimmigen Pitch-Class-Mengen ergab zwar keine Klassifikationslücke und keine
Transpositionsverletzung, aber 491 Mengen erfüllen gleichzeitig zwei oder drei Spezifikationen. Der Code nimmt dann einfach die erste passende Gruppe (src/main/java/syrincs/a_domain/hindemith/ChordAnalysis.java:148). Dazu
kommen //check-Markierungen in den Spezifikationen und ein Test, der eine Gruppenzuordnung ausdrücklich offenlässt (src/test/java/syrincs/a_domain/hindemith/ChordAnalysisTest.java:31).

Ein gutes --explain sollte deshalb zeigen:

- semantische Gruppe, etwa A_I_1, zusätzlich zur kompatiblen Nummer;
- Tritonus, bestes Intervall, Grundtonentscheidung und gegebenenfalls Führungston;
- erfüllte und nicht erfüllte Kriterien;
- alle passenden Gruppen und warum eine davon Vorrang hat;
- qualitativen Klangwert, ohne Gruppennummern vorschnell als lineare Messwerte zu behandeln.

Vor einer Folgenanalyse sollte außerdem der offensichtliche Multiplikationsfehler in Series1.getRelativeByDegree(...) korrigiert werden (src/main/java/syrincs/a_domain/hindemith/Series1.java:36).

### 2. Das sinnvollste neue Feature: harmonisches Gefälle

Ich würde die bisherige Aufgabe „Akkordfolgen erzeugen“ teilen. Zuerst sollte Syrincs vorhandene Folgen analysieren.

Hindemith beschreibt in den fachlichen PDFs ausdrücklich den „Wert der Klänge“ und das „harmonische Gefälle“: Schritte zu geringerem Klangwert erhöhen die Spannung, Rückwege lösen sie. Das ist wesentlich projektspezifischer
als eine generische Voice-Leading-Heuristik.

Möglicher MVP:

syrincs analyze progression \
--chord 60,64,67 \
--chord 59,62,65,67 \
--chord 60,64,67

Ausgabe pro Akkord und Übergang:

- Gruppe und Klangwert;
- Anstieg, Abstieg oder gleichbleibende Spannung;
- Verwandtschaft der Grundtöne nach Reihe 1;
- gemeinsame Töne und gesamte Stimmenbewegung;
- Tritonus-/Führungstonbewegung;
- kompakte Spannungskurve.

Damit entsteht zugleich das saubere Domain-Modell, auf dem später ein Generator aufbauen kann.

### 3. Rhythmisches Informationsprofil

Die Rhythmusdomäne berechnet intern bereits Information pro Beat, veröffentlicht aber nur Summe und Standardabweichung (src/main/java/syrincs/a_domain/rhythm/HuffmanRhythm.java:21). Diese Aggregate verlieren die zeitliche
Gestalt.

Sinnvoller als reine Trefferzahlfilter wäre daher:

- geordnete Beat-Information veröffentlichen;
- steigende, fallende, alternierende und gipfelförmige Profile beschreiben;
- Peak-Position, Onset-Dichte und metrische Gewichtung suchen;
- Kandidaten zunächst anzeigen;
- Auswahl mit --seed reproduzierbar machen;
- Suche und Playback trennen.

So wird das eigene Huffman-Maß tatsächlich zu einem Gestaltungsinstrument.

### 4. Persistenz nicht nur deduplizieren, sondern versionieren

Akkordgruppe, Grundton, Information und Deviation sind abgeleitete Analysedaten. Ändert sich eine fachliche Regel, bleiben alte DB-Zeilen derzeit unverändert. Besonders problematisch: Rhythmusabfragen filtern anhand
gespeicherter Werte und rekonstruieren anschließend das Objekt neu.

Daher würde ich statt bloßem ON CONFLICT DO NOTHING vorsehen:

- eindeutige Identität des Rohmaterials;
- classifierVersion beziehungsweise metricVersion;
- ON CONFLICT ... DO UPDATE;
- syrincs reanalyze chords|rhythms;
- PostgreSQL-Integrationstests.

### Danach

Die Akkordfolgen-Generierung sollte nicht nur einer Grundtonliste folgen, sondern einem gewünschten harmonischen Verlauf. Harmonischer Wert und melodische Stimmführung bleiben dabei getrennte, erklärbare Kostenbestandteile.

RDL würde ich erst metrisch korrigieren – SequenceBuilder behandelt derzeit jeden Beat wie eine Viertelnote (src/main/java/syrincs/c_adapters/midi/SequenceBuilder.java:44) – und danach für beliebige Stimmen öffnen.

Nicht priorisieren würde ich momentan GUI, weitere Presets, Sample-Library sowie die vorhandenen Scale-/Counterpoint-Entwürfe. Sie haben noch keinen ausreichend belastbaren fachlichen Unterbau.

Der beste nächste Meilenstein lautet damit:

Explainable Hindemith + Analyse des harmonischen Gefälles einer Akkordfolge.

Verifiziert wurde mit mvn test: 84 Tests, keine Fehler; 14 umgebungsabhängige MIDI-/OSC-/Hörtests wurden übersprungen. Der Arbeitsbaum blieb unverändert.
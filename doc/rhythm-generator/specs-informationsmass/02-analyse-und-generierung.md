# 02 – Analyse, Taktvertrag und parameterisierte Generierung

Grundlagen: [Rahmen](README.md) und [Definitionen M01–M07](01-mass-und-beschreibung.md).
Alle Schnittstellen und Befehlsnamen dieses Dokuments sind Entwürfe.

## A01 – Eine gemeinsame Bewertung

Analyse, Generator und spätere Suche MUSS dieselbe fachliche Bewertungsfunktion
mit derselben Maßversion, demselben Rahmen und derselben Kontextpolitik
verwenden. Ein Generator darf keinen vereinfachten Ersatzwert als Information
verkaufen. Akustische Ausgabe, DB-Zugriff und Zufallsauswahl gehören nicht
in die Bewertung.

Die Analyse bietet drei getrennte Geltungsbereiche:

| Scope | Bedeutung | Einheit und Aggregation |
| --- | --- | --- |
| `GLOBAL_DESCRIPTION` | `D` der gesamten Eingabe aus M04, ohne aus der Zielphrase vorab bekannten Kontext. | Bits; ein Minimum über die ganze Folge. |
| `ISOLATED_WINDOWS` | Jedes vorgegebene Fenster separat mit `D` berechnen. | Liste eigenständiger Bitlängen; Summe wird `isolatedTotal` genannt, nicht globale Phraseninformation. |
| `CAUSAL_WINDOWS` | Fenster in zeitlicher Reihenfolge mit `J` aus M06 beschreiben; K nur aus tatsächlich vorangegangenem Material bilden. | Liste von Fortsetzungsbitlängen; ihre Summe heißt `continuationTotal`. |

Alle drei Werte können in einem Analyseergebnis nebeneinander stehen.
Das erste Feld `information` ohne Scope darf nicht je nach Situation eine
andere dieser Größen meinen. `legacy-fsm-v1` liefert seine bisherigen
Felder zusätzlich unter dem alten Vertrag; sie bleiben Codesymbolzahlen.

## A02 – Ergebnisvertrag

Ein Ergebnis MUSS mindestens folgende Angaben enthalten:

```text
RhythmAnalysis
  normalizedOnsets, onsetPositions, onsetCount
  frame: N, Zeitrahmen, Raster, bekannte/fehlende Angaben
  measurementKey: measureId, grammarVersion, scope, contextPolicy,
                  segmentation, initialContextIdentity, frameIdentity
  unit: CODE_SYMBOLS oder BITS
  globalDescription: totalBits, modelBits, residualBits,
                     canonicalCode, descriptionTree, optimality,
                     ambiguitySummary
  isolatedWindows[] / causalWindows[]: jeweils eigene Ergebnisse
  metrics: Mittelwerte, Streuungen und jeweils deren Bezugsmenge
  referenceAssessments[]: benannte Hierarchien, Herkunft, Prüfstatus
  legacyComparison: optional bisherige Gesamt- und Beat-Werte
```

Nur zum jeweiligen Scope passende Felder sind zu befüllen; fehlende
Kennzahlen erhalten einen expliziten Grund, keinen erfundenen Nullwert.
Bei nebeneinander ausgegebenen Scopes erhält jedes Teilergebnis seinen
eigenen vollständigen Messschlüssel. Ein gemeinsamer Kopf beschreibt
Eingabe und primären Scope, ersetzt diese Schlüssel aber nicht.
Listen und enthaltene Daten sind unveränderlich. Quellenzeitpunkte,
Codewörter und Positionsangaben bleiben in zeitlicher Reihenfolge erhalten.
Mittelwerte und Streuungen dürfen als Gleitkommazahlen ausgegeben werden;
Kosten und Bitlängen sind exakt ganzzahlig.

`optimality=EXACT` bezeichnet Minimalität innerhalb M04, nicht gegenüber
jeder denkbaren Sprache oder jedem menschlichen Hörmodell. Anzeigegrenzen
von alternativen Beschreibungen sind vom Nachweisstatus zu trennen.

## A03 – Beat- und Taktvertrag

**Ein Beat-Profil braucht eine benannte Segmentierung.** V1 unterstützt
vollständige, nicht überlappende Fenster, die den analysierten Bereich
genau partitionieren. Diese können vorgegebene Notations-Zählzeiten,
Takte oder ausdrücklich gewählte andere Abschnitte sein. Ein Segment
hat einbasierte Position, Start, exklusive Endposition und seine Onsets.

Im kompatiblen 4/4-Modus sind Beats weiterhin Vierergruppen und Takte
Sechzehnergruppen. Im neuen Modus werden diese Grenzen nur auf Wunsch
verwendet. Ohne Taktangabe gibt es keine erfundenen Taktzahlen; ein
unvollständiger Takt wird nicht still aufgefüllt.

### Drei verschiedene Fragen an einen Takt

1. **Takt für sich:** `D(Takt)` unter seinem bekannten Fenster. Jeder Takt
   startet ohne Zielvorwissen. Die Werte sind unabhängig vergleichbar.
2. **Takt als Fortsetzung:** `J(Takt | K_vor_Takt)` unter den vorangegangenen
   Onsets. Er beschreibt die bekannte Vorgeschichte und ihren fortsetzbaren
   Zustand. Ein erster Takt ohne Vorlauf benutzt DIRECT.
3. **Takt als Summe kausaler Beat-Fenster:** Summe der J-Werte der darin
   enthaltenen Beat-Fenster. Das ist eine andere Blockeinteilung als 2.

Die gewünschte Blockeinteilung MUSS Teil des Messschlüssels sein.
`D(Takt)`, `J(Takt)` und eine Summe kleinerer J-Blöcke müssen nicht gleich
sein. Zwei Generatoren mit unterschiedlicher Blockeinteilung bedienen
nicht denselben Zielvertrag.

Als kanonischer **kausaler Informationsverlauf** des ersten musikalischen
Modus wird eine vorgegebene Beat-Segmentierung verwendet. Die Beat-Kosten
sind J der jeweiligen Fenster. Die Taktbeiträge sind Summen dieser Werte;
die Beiträge aller Takte ergeben `continuationTotal`. Dieses ist ausdrücklich
keine Zerlegung von `globalDescription.totalBits`.

Eine nachträgliche Zuordnung des global gewählten Syntaxbaums zu Beats
wird im ersten Stand nicht als zusätzliche Informationskurve veröffentlicht.
Sie wäre eine Zurechnungskonvention mit mehreren möglichen Lösungen.
Ebenso werden Differenzen globaler Präfixminima nicht als positive lokale
Informationskosten interpretiert: Bei wachsendem Präfix kann das günstigste
Modell rückblickend wechseln.

### Legacy-Taktvergleich

Zwei regelmäßige Vierteltakte besitzen im vorhandenen Maß isolierte Summen
`[1,1]`, kontinuierlich gruppierte Summen `[1,0]`. Falls Legacy-Taktergebnisse
ergänzt werden, bleiben diese beiden Modi explizit benannt. Sie werden
nicht auf die neuen Bitlängen umgebogen.

### Streuung und Maxima

Für jedes benannte Profil `b[1..k]` gilt:

```text
mean = sum(b) / k
deviation = sqrt(sum((b[i] − mean)²) / k)
peakPositions = alle einbasierten Positionen mit b[i] = max(b)
```

Die Bezugsmenge ist mindestens `LEGACY_BEATS`, `CAUSAL_BEAT_WINDOWS` oder
`ISOLATED_BAR_VALUES`. Eine Streuung der Taktbeiträge ist eine weitere
Kennzahl. Phrasenstreuung wird aus allen relevanten Beat-Fenstern berechnet,
nicht durch Mittelung der Taktstreuungen. Gleichstände und ein vollständig
konstantes Profil sind gültige Fälle. Die Größe ist weder metrischer Akzent
noch Timing-Streuung oder maximal evenness.

Für neue Bitprofile wird die Varianz exakt als rationale Zahl berechnet:
`variance = (k·sum(b[i]²) − sum(b[i])²) / k²`.
Neue Deviation-Filter besitzen endliche, nichtnegative rationale Grenzen
und vergleichen variance mit deren Quadraten; so gelten inklusive
Grenzen unabhängig von gerundeter Anzeige. Der ausgegebene Quadratwurzel-
Wert darf gerundet sein. Legacy-Filter behalten ihren bestehenden
Gleitkommavertrag, statt durch diese Ergänzung ihre Treffer zu verändern.

## A04 – Zeit und Kontext hörbar prüfen

Analyseeingabe und Playback-Tempo dürfen voneinander unterschieden sein:
Der strukturelle Wert bleibt unverändert, absolute IOIs und Referenzprüfung
werden bei geändertem Tempo neu ausgewiesen. Ein Playback-Override ändert
nicht heimlich die gespeicherte Analysebedingung.

Für Hörproben werden vorbereiteter Kontext, Einzählen, Klang, Velocity,
Artikulation und Wiederholungszahl als Darbietungsangaben festgehalten.
Einzählen ist keine zusätzliche Ereignismenge im analysierten Zielrhythmus,
außer es wird ausdrücklich als Teil von E gewählt. Eine freie gleiche
Klickfolge und eine Kick-/Snare-Darbietung mit Einzählen sind verschiedene
Prüfbedingungen. Daraus entstehen keine zusätzlichen Codegewichte.

## G01 – Zielvertrag für die Generierung

Ein Generierungsauftrag enthält:

```text
GenerationRequest
  measurement: genau eine measureId + scope + Konfiguration
  frame: N und gegebenenfalls Zeit/Notation
  initialContext: ausschließlich vor Beginn bekanntes Material
  targets:
    informationRange: inklusive Ganzzahlgrenzen
    windowTargets / beatProfile / barProfile: optional
    deviationRange: optional, mit benannter Bezugsmenge
    peakPositions: optional, mit benanntem Profil
  eventConstraints:
    onsetCountRange, densityRange: jeweils optional
    requiredPositions, forbiddenPositions
    minOnsetGapSlots, maxZeroRunSlots
  referenceConstraints: optionale explizite Referenzen / Zeitprüfung
  search: EXHAUSTIVE, CODE_LENGTH_SEARCH oder BOUNDED_RANDOM,
          budget, resultLimit
  selection: FIRST oder RANDOM_WITHOUT_REPLACEMENT
  seed: bei jeder zufälligen Strategie
```

Ein einzelner gewünschter Grad ist ein Bereich `[g,g]`. Informationen werden
nicht relativ auf eine Skala 0–100 normalisiert. Falls eine solche
Benutzerskala später gewünscht wird, braucht sie ein eigenes versioniertes
Mapping zur ausdrücklich genannten Grundgesamtheit.

informationRange bezieht sich bei GLOBAL_DESCRIPTION auf D, bei
ISOLATED_WINDOWS auf isolatedTotal und bei CAUSAL_WINDOWS auf
continuationTotal. Im Legacy-Modus gilt die bisherige Codesymbolsumme.
Profilpositionen beziehen sich ausschließlich auf die gewählte
Segmentierung; bei einem vollständigen exakten Profil muss dessen Summe
mit dem angegebenen lokalen Gesamtziel vereinbar sein. Ein zusätzlich
gewünschter globaler D-Wert wird als ausdrücklich anderer Analysefilter
geführt, nicht als Summe derselben lokalen Zahlen ausgegeben.

initialContext ist nur bei CAUSAL_WINDOWS gültig. Bei globaler oder
isolierter Generierung wird ein nichtleerer Vorlauf als unvereinbare
Konfiguration zurückgewiesen. Es gibt keine stillschweigend ignorierte
Kontextangabe; zusätzliche Kontextanalysen besitzen eigene Messschlüssel.

Das Ergebnis enthält die **konkreten Onsets**, sämtliche angewandten
Bedingungen und die vollständige zugehörige Analyse. Der Generator MUSS
jedes ausgegebene Ergebnis damit erneut auf die Zielbedingungen prüfen.
Ein passender Konstruktionscode reicht nicht: Derselbe Rhythmus kann eine
noch kürzere Beschreibung besitzen und damit den angeforderten Grad verfehlen.

## G02 – Weitere Parameter präzise definieren

| Parameter | Vertrag |
| --- | --- |
| Länge | Positive Anzahl von Rasterpositionen oder eine daraus exakt ableitbare Notationslänge; N muss in die Analysegrenze passen. |
| Tempo | Positive rationale Viertel-BPM oder explizite Rasterdauer; ändert Zeitangaben und Referenzbedingungen, keine D-Bitkosten. |
| Dichte | `onsetCount / N`; Grenzen in `[0,1]`, inklusive. Bei diskreten N über `ceil(min·N)` und `floor(max·N)` in zulässige Ganzzahlanzahlen überführen. Dezimalgrenzen exakt als rationale Werte behandeln. |
| Onset-Anzahl | Inklusive Ganzzahlgrenzen innerhalb `0..N`; gleichzeitig gesetzte Dichte bildet eine Schnittmenge. |
| Pflicht-/Verbotspositionen | Nullbasierte Mengen innerhalb `0..N−1`; Überschneidung ist ein Eingabefehler. |
| Minimaler Onset-Abstand | Positive Ganzzahl; Abstand benachbarter klingender Onsets in Slots, inklusive Grenze. Bei weniger als zwei Onsets ohne Nachbarprüfung; auch ein Abstand größer als N kann deshalb zulässig sein. Kein impliziter zyklischer Abstand über das Fensterende. |
| Größte stumme Positionsfolge | Ganzzahlgrenze in `0..N`; maximale Zahl aufeinanderfolgender Nullbits innerhalb des Fensters einschließlich Anfang und Ende. Kein Nachweis tatsächlicher Schalldauer oder Stille. |
| Profil | Geordnete genaue Werte oder inklusive Wertebereiche je ausdrücklich definiertem Fenster. Profil und Gesamtscope müssen kompatibel sein. |
| Peak | Einbasierte Menge erforderlicher Maxima des angegebenen Profils: alle geforderten Positionen müssen zu peakPositions der Analyse gehören; weitere gleiche Maxima sind erlaubt. |
| Referenz | Explizite metrische Kandidaten und gegebenenfalls Zeitprofil aus M07. Keine stillschweigende Auswahl des kürzesten Motivs als Beat. |

Alle gesetzten Bedingungen werden mit UND verknüpft. Der Generator darf
niedrige Information nicht durch hohe Dichte ersetzen; durch unterschiedliche
Dichte können gleich kurze regelmäßige Beschreibungen musikalisch
unterschiedlich ausgewählt werden. Akustischer Klang, Lautstärke und
Artikulation sind zunächst Playback-Parameter, keine Informationsfilter.

Wertebereichsfehler, ungültige Segmentierung und logisch widersprüchliche
Eingaben werden vor der Suche gemeldet. Ein logisch zulässiger, aber nicht
erreichbarer Informationsgrad ist dagegen ein normales Suchergebnis.
Die Maßfähigkeiten sind Teil der Validierung: Legacy benötigt sein
Viererraster; globale description-v1-Bitkosten liegen in `[3,N+3]`.
Ein Bereich außerhalb dieser gültigen Skala ist ein Eingabefehler.
Ein ganzzahliger Wert innerhalb der Skala kann trotzdem unerreichbar sein
und wird dann als Suchergebnis behandelt. Lokale Werte berücksichtigen
Header und Kontextindex gemäß M06; nicht alle Integer sind erreichbar.

## G03 – Gesamtheit, Vollständigkeit und Budget

Der zugrunde liegende endliche Raum für N Positionen enthält `2^N`
verschiedene Bitmasken. Notation oder Kontext macht daraus keine zusätzlichen
Ereignisfolgen; sie verändert jedoch Analyse oder Filterbedingungen.

Jeder Kandidat startet mit demselben initialContext des Auftrags. Innerhalb
einer Phrase wächst dessen Kontext chronologisch; zwischen verschiedenen
Kandidaten wird er zurückgesetzt. Bereits getestete Zufallskandidaten sind
keine musikalische Vorgeschichte des nächsten Kandidaten.

resultLimit ist eine positive Ganzzahl. BOUNDED_RANDOM und
CODE_LENGTH_SEARCH benötigen außerdem ein positives ganzzahliges
Versuchsbudget. Der erste Stand unterstützt EXHAUSTIVE für N=1..16;
größere Aufträge wählen einen der budgetbegrenzten Modi. Diese
Strategiegrenze ist unabhängig von der exakten Analysegrenze N=128.

**EXHAUSTIVE:** Alle gültigen Masken kanonisch prüfen oder durch einen
vollständigen, bewiesenermaßen gleichwertigen Suchalgorithmus abdecken.
Die erste Abnahme verlangt diesen Modus für N=16. Ein Ergebnis mit
`searchComplete=true` erlaubt die Aussage, dass ein Ziel unerreichbar ist.
Bei bewiesenem Ausschluss eines ganzen Teilraums zählt dessen Abdeckung
mit; ein Profil oder Budget darf nicht unbewiesen Treffer wegschneiden.

**BOUNDED_RANDOM:** Bis zum vorgegebenen Versuchslimit zufällige Masken
erzeugen; jede neue Maske mit dem exakten Analyzer prüfen. Alle Versuche,
auch Duplikate, zählen zum Budget. Vor Erreichen des Budgets darf beendet
werden, sobald die angeforderte Anzahl verschiedener gültiger Rhythmen
vorliegt. Dieser Modus behauptet keine vollständige Suche und keine
gleichmäßige Ziehung aus allen erfüllenden Rhythmen.

**CODE_LENGTH_SEARCH:** Für niedrige Informationsziele auf längeren
Fenstern Beschreibungen statt aller `2^N` Ereignisfolgen untersuchen.
Der vollständige Vertrag steht in G06. Der Modus hat ein Zählbudget und
kann bei vollständiger Abdeckung ebenfalls Unerreichbarkeit beweisen.

```text
GenerationResult
  requestedCount, returnedCount, targetsSatisfiedForEveryResult
  searchComplete, stopReason
  attempts, uniqueCandidatesEvaluated, matchingCandidatesObserved
  totalMatches: nur bekannt, wenn searchComplete
  seed, randomAlgorithm, enumerationVersion, measurementKey
  rhythms[]: Onsets und zugehörige exakte Analyse
```

Keine Treffer bei vollständiger Suche bedeutet `NO_SOLUTION`.
Eine abgeschlossene Suche mit mindestens einem Treffer meldet `COMPLETE`;
ein Ergebnislimit verkleinert nur die ausgegebene Liste, nicht totalMatches.
Keine Treffer bei ausgeschöpftem Budget bedeutet `BUDGET_EXHAUSTED`;
Unerreichbarkeit ist dann unbekannt. Vorzeitig ausreichend gefundene Treffer
bedeuten `REQUESTED_COUNT_REACHED`, bei unvollständiger Suche weiterhin
`searchComplete=false`. Zu wenige gefundene Treffer werden als Teilergebnis
mit expliziter Differenz gemeldet. Sie dürfen nicht als vollständiger
Generatorerfolg oder still verlängerte Pause ausgegeben werden.

Darstellungslimit und Suchbudget sind verschiedene Parameter.
Ein optionaler Wallclock-Abbruch meldet `INTERRUPTED` und zerstört nicht
die gültigen gefundenen Ergebnisse; er ist nicht reproduzierbar allein
durch einen Seed. Die reproduzierbare Grundpolitik verwendet Zählbudgets.

## G04 – Reproduzierbarkeit und Identität

Eine Rhythmusidentität besteht aus normalisierten Onsets und ihrem Rahmen.
Eine Analyseidentität ergänzt Maß, Scope, Segmentierung und Kontext.
DB-ID und zufällige Reihenfolge sind keine Inhaltsidentität. Rotation oder
Transposition der Phase ist ohne explizite entsprechende Äquivalenz kein
Duplikat; auch ähnlich klingende Patterns werden nicht still zusammengelegt.

Für den ersten Zufallsvertrag:

- genau ein `java.util.Random(seed)` pro Auftrag;
- BOUNDED_RANDOM erzeugt pro Versuch genau N `nextBoolean()`-Werte in
  Zeitreihenfolge; `true=x`, `false=o`;
- FIRST liefert kanonisch nach Bitmaskenwert sortierte Treffer bis zum Limit;
- RANDOM_WITHOUT_REPLACEMENT ist im ersten Stand nur für eine vollständig
  bekannte Trefferliste zugelassen: kanonische Ausgangsliste, partieller
  Fisher–Yates, bei Schritt i genau ein `nextInt(m−i)`, auch bei `m−i=1`;
- die Random-Instanz für diese Auswahl wird erst nach der vollständigen
  deterministischen Suche initialisiert; BOUNDED_RANDOM verwendet FIRST.

Bei CODE_LENGTH_SEARCH ist RANDOM_WITHOUT_REPLACEMENT nur nach vollständiger
Abdeckung zulässig. Ein Auftrag mit dieser Auswahl und erschöpftem Budget
meldet den Suchstatus und die kanonische Teilmenge, aber führt keine als
vollständig angekündigte Zufallsziehung durch.

Bei BOUNDED_RANDOM sortiert FIRST die tatsächlich gefundenen Treffer.
Es verspricht weder die global kleinsten Masken noch dieselbe Trefferliste
wie EXHAUSTIVE. Bei vollständiger Suche sortiert es die ganze Trefferliste.

Gleicher Auftrag, Seed, Algorithmusversion und Messvertrag müssen dieselben
Ergebnisse liefern. Der konkrete Inhalt wird immer ausgegeben; ein Seed
ist nach Maßänderung kein dauerhaft identisches musikalisches Objekt.

Zähler und Raumgrößen dürfen für `2^128` nicht in einem vorzeichenbehafteten
64-Bit-Wert überlaufen; auch größere Codewort-Räume werden exakt gezählt.
Exakte Suchraumgröße und unbekannte Trefferzahl
werden verschieden dargestellt. Anzeigelimits begrenzen nur die Ausgabe.

## G05 – Mehrtaktige Gestaltung

Für einen gewünschten Taktverlauf werden Scope und Kontextbindung gewählt:

| Ziel | Zulässige Prüfung |
| --- | --- |
| Isolierte Werte je Takt | Jeden Takt unabhängig mit D prüfen; anschließend die zusammengestellte Phrase zusätzlich analysieren. |
| Kausale Beat-Kosten und daraus summierte Taktbeiträge | Präfix in Reihenfolge aufbauen und jeden neuen Beat mit seinem tatsächlichen K prüfen. Kontext entsteht aus den gewählten vorherigen Tönen. |
| Globales Phrasenminimum | Die vollständige Folge mit D prüfen; ein passender isolierter Verlauf ersetzt diese Prüfung nicht. |

Isolierte Einzelwerte dürfen nicht als kontextuelle Ziele verwendet werden.
Ein Einzeltaktkatalog ist für kausale Werte kein hinreichender Vorfilter.
Für globale Bitkosten dürfen scheinbar teure Präfixe nicht einfach verworfen
werden: spätere Wiederholung kann die rückblickende Gesamtbeschreibung
ändern. Suchoptimierung braucht für solche Abbrüche einen gültigen Beweis.

Ein gewünschtes konstantes oder ansteigendes Profil bezieht sich immer auf
den ausdrücklich benannten Scope. Profilähnlichkeit oder Formen werden
nicht zusätzlich durch eine unbenannte Distanzmetrik eingeführt.

## G06 – Niedrige Information gezielt erzeugen

CODE_LENGTH_SEARCH ist für `description-v1` ein notwendiger zweiter
systematischer Weg: Zufällige lange Bitmasken finden sehr einfache Muster
zu selten, eine Maskenvollsuche ist dort praktisch ungeeignet.

Für den inklusiven Zielbereich `[a,b]` werden alle Bitstrings der Länge
a bis b untersucht, zuerst nach Länge, dann binär lexikografisch
(`shortlex-code-v1`). Jeder untersuchte Bitstring zählt als Versuch,
auch wenn er ungültig ist oder ein bereits gefundenes Ereigniswort liefert.
Die Untersuchung endet nach dem positiven Zählbudget oder nach vollständiger
Abdeckung. resultLimit begrenzt in diesem Modus nur die Ausgabe.

Ein scopebezogener Decoder rekonstruiert daraus entweder:

- GLOBAL_DESCRIPTION: genau eine M04-Nachricht mit Länge N;
- ISOLATED_WINDOWS: M04-Nachrichten nacheinander für die bekannten Fenster;
- CAUSAL_WINDOWS: M06-Nachrichten nacheinander, wobei H nach jeder
  rekonstruierten Fensterfolge wächst.

Nur die vollständige Phrase mit exakt verbrauchten Codebits ist ein
Kandidat. Ungültige Codes werden verworfen. Kandidaten werden dedupliziert,
mit dem gemeinsamen Analyzer erneut minimiert und gegen sämtliche
Bedingungen geprüft. Die Kodierung hat dadurch eine **Konstruktionsrolle**;
die Länge des zufällig oder systematisch gefundenen Zeugen wird niemals
ungeprüft zum Informationswert.

**Vollständigkeitsbegründung:** Jede erfüllende Folge besitzt im gewählten
Scope eine minimale Nachricht mit Länge in `[a,b]`. Die vollständige
Enumeration dieser Codewörter enthält diese Nachricht und rekonstruiert
folglich die Folge. Bei lokalen Scopes werden die minimalen
Fensternachrichten hintereinander gelesen; das rekonstruierte Präfix
bestimmt stets denselben Kontext. Ein nichtminimaler Zeuge erzeugt keine
falschen Treffer, weil die erneute Analyse den Zielbereich prüft.

Eine abgeschlossene Codesuche darf deshalb `searchComplete=true` melden,
obwohl sie nicht jede Maske einzeln besucht. Mit Budgetabbruch bleiben
Treffer korrekt, die Vollständigkeit jedoch unbekannt. Anzahl aller
untersuchten Codewörter und Anzahl analysierter verschiedener Masken werden
getrennt ausgegeben. Direkte Kosten der Quellbeschreibung werden nicht
als Suchbudgetkosten interpretiert.

Diese Strategie setzt einen vollständigen Decoder voraus und ist für
`legacy-fsm-v1` nicht verfügbar. Die erste Abnahme prüft alle drei neuen
Scopes gegen kleine Maskenvollsuchen und ein niedriges globales Ziel bei
N=128. Ein späterer Generator darf gültige Syntaxbäume effizienter
enumerieren oder konstruktiv Regeln kombinieren; Budget und Reihenfolge
erhalten dann eine eigene Strategieversion mit Vollständigkeitsnachweis.

## S01 – Architektur und Schnittstellen

Die Fachlichkeit gehört in frameworkfreie Domain-/Application-Typen:

- Domain: Frame, OnsetSequence, Beschreibungsbaum, Codec,
  InformationMeasure, ContextPolicy, Ergebnis und Referenzdefinition;
- Application: AnalyzeRhythm und GenerateRhythms mit Datenverträgen,
  Eingabevalidierung, Zählbudget und optionalen Repository-Ports;
- Adapter: Picocli, Ein-/Ausgabeformat, PostgreSQL, MIDI und OSC.

Eine neue Measure-Abstraktion darf Legacy und den neuen Analyzer verbinden,
aber nicht die Units vereinheitlichen, indem verschiedene Zahlen in ein
unbeschriftetes `information`-Feld passen. Die bestehenden öffentlichen
Methoden bleiben erhalten; die neue Generierung hängt nicht von JDBC,
MIDI-Typen oder Picocli in der Domain ab.

Der erste neue Generator ist ohne Persistenz nutzbar. Ein bestehender
Katalog kann eine optionale Quelle kompatibler Onsets sein, nicht die
einzige Definition des Suchraums. Das aktuelle Kick-/Snare-Mapping ist
auf 16 Positionen beschränkt; eine Analyse anderer Längen ist deshalb
nicht automatisch eine Zusage der Wiedergabefähigkeit.

## S02 – Öffentliche Bedienung

Die bestehenden Endpunkte behalten ihre Syntax, Defaults, Ausgaben und
Legacy-Bedeutung. Auf Philipps Vorgabe für den Implementierungsplan erhalten
die neuen Features eigene Endpunkte in einem neuen Befehlsbereich:

```text
syrincs rhythm-info analyze ONSETS ...
syrincs rhythm-info generate ...
syrincs rhythm-info decode ...
```

Dieser Entwurf ersetzt den früheren Vorschlag, `analyze rhythm` um eine
Maßauswahl zu erweitern. Insbesondere erhält der bestehende Endpunkt keine
Option `--measure`; die neue Maßauswahl gehört ausschließlich zu den neuen
Endpunkten. Root-Hilfe und Completion ergänzen den neuen Befehlsbereich.
Der [Implementierungsplan](04-implementierungsplan.md) konkretisiert Syntax,
Datenverträge, Arbeitspakete und Kompatibilitätsprüfungen.

Analyse braucht keine DB oder Audiohardware. Generierung zeigt zunächst
Rhythmen und Analysen; Speichern und Wiedergabe sind ausdrückliche
nachgelagerte Aktionen. Öffentliche Hilfe muss Informationseinheit, Scope,
Kontext, Fenster und Vollständigkeitsstatus erklären.

RDL verwendet weiter `x`/`-`, Onsets `x`/`o`; keine stille Formatvermischung.
Zeitparameter des Analyzers sind nicht allein aus einer lokal gebauten
Playback-Version abzuleiten. Auf dieser Basis gehören die Tempo- und
Einzähländerungen von VR-14/VR-13 noch nicht zum aktuellen PROJEKT-Stand;
die spätere Implementierungsplanung muss deren tatsächliche Integration
berücksichtigen.

## S03 – Daten und Versionierung

Vorhandene `info`-/`deviation`-Spalten behalten ihre Legacy-Bedeutung.
Es ist unzulässig, neue Bitwerte in dieselben Spalten zu schreiben und
anschließend alte Filter unverändert anzuwenden.

Ein späterer neuer Analysebestand benötigt getrennte Inhalts- und
Messidentitäten: Onsets/Frame getrennt von Maßversion, Scope, Kontext,
Segmentierung und Ergebnis. Bei kausalen Analysen zählt der Vorlauf zur
Identität. Gespeicherte Filterwerte und ausgegebene Werte müssen aus
derselben Version stammen; unbekannte oder veraltete Analysen dürfen nicht
als gültige neue Filtertreffer erscheinen.

Eine Migration ist ein eigener Implementierungsschritt mit expliziter
Datenstrategie. Die ersten Analyse-/Generierungsfunktionen sind davon
unabhängig. Es gibt weder automatische Katalogüberschreibung noch eine
implizite Neuinterpretation alter Grade durch diesen Vorschlag.

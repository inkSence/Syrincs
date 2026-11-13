# Gleiche Schläge: Wahl des Grundschlags und seiner Unterteilung

Literaturauswertung zu [VR-12](http://localhost:3456/tasks/20).
Recherche und Stand: 2. Oktober 2026.
Ergänzt nach vollständiger Lektüre von Levitin, Grahn & London (2018).

Diese neue Auswertung behandelt die präzisierte Leitfrage des Tasks. Auf
ausdrücklichen Wunsch bleibt die
[bisherige Auswertung](hoerpsychologie-zeitliche-gruppierung.md) unverändert;
über das Verhältnis beider Dokumente wird später entschieden. Die Vorschläge
für VR-1 und [VR-9](http://localhost:3456/tasks/17) ändern diese Tasks nicht.

## 1. Ergebnis und Begriffe

Die ausgewertete Literatur liefert keine universelle Schwelle: Die Beat-Wahl
ist tempoabhängig und mehrdeutig.
[McAuley & Semple (1999)](https://taplab.psy.msu.edu/wp-content/uploads/2020/07/McAuleySemple1999.pdf).

Für die Fragestellung müssen drei Ebenen getrennt bleiben:

- **Unterteilung:** Ereignisse beziehungsweise Rasterpositionen innerhalb
  einer Beat-Periode.
- **Grundschlag / Beat / Tactus:** die als zeitliche Referenz gewählte
  periodische Ebene. Sie besitzt einen Abstand und eine Phase.
- **Übergeordnete Gruppe / metrischer Zyklus:** eine Organisation mehrerer
  Beats. Ihr Anfang kann hervorgehoben sein, ohne selbst der Beat zu sein.

Akustische Akzente ändern das Signal, etwa den Pegel. Subjektive Akzente
bezeichnen dagegen erlebte Hervorhebungen ohne entsprechende Signaländerung.
Die Gruppenberichte gleicher Klicks und die zusätzlich eingespielten
Phasenmarkierungen sind deshalb verschiedene Fälle; siehe die Befundtabellen.

Ein Bericht „Ich höre Vierergruppen“ entscheidet daher nicht, ob vier
Unterteilungen einen Beat bilden oder vier Beats einen übergeordneten Zyklus.
Auch die stärkste subjektive Betonung bestimmt diese Unterscheidung nicht
allein. Hier wird eine gewählte Beat-Ebene vorrangig anhand einer ausdrücklich
darauf gerichteten Aufgabe beurteilt; die Grenzen der Messung bleiben sichtbar.

### Ereignisrate ist nicht Beat-Tempo

Für eine lückenlose isochrone Folge gleicher Klänge gelten folgende
Definitionen. Alle Zeiten werden in Millisekunden angegeben:

```text
Δ       = Abstand aufeinanderfolgender Onsets (IOI)
d       = Schalldauer eines Ereignisses
p       = stille Lücke zwischen Klängen; bei d ≤ Δ: p = Δ − d
N       = Gesamtzahl präsentierter Ereignisse
n       = Ereignisse pro Beat-Periode, sofern diese Unterteilung gewählt wird
T       = Beat-Abstand = n · Δ
Ereignisrate = 1000 / Δ Hz beziehungsweise 60000 / Δ Ereignisse pro Minute
Beat-Tempo  = 60000 / T BPM
G       = berichtete Gruppengröße; nicht ohne Weiteres gleich n
```

Rechenbeispiel, keine Wahrnehmungsvorhersage: Bei `Δ = 200 ms` erklingen
300 Ereignisse pro Minute. Die Kandidaten `n = 1, 2, 3, 4, 6, 8` entsprächen
Beat-Tempi von `300, 150, 100, 75, 50, 37,5 BPM`.

Bei Folgen mit ausgelassenen Klängen zählt eine Rasterunterteilung dagegen
Positionen, nicht notwendig klingende Ereignisse. Bei sehr langsamen Folgen
ist außerdem eine *schnellere* innere Referenz möglich: `T = Δ / m`. Der
Untersuchungsfall `n ≥ 1` umfasst also nicht jede mögliche Beat-Wahl.
[Duke (1989), Abstract](https://journals.sagepub.com/doi/10.2307/3344953).

Eine isolierte Gruppe aus `N` Ereignissen spannt vom ersten bis zum letzten
Onset `(N − 1) · Δ` auf. Die Wiederholungsperiode einer lückenlos wiederkehrenden
`N`-Gruppe wäre hingegen `N · Δ`. Zwei Ereignisse zeigen zunächst nur einen
Abstand, nicht bereits die Wiederkehr eines Zweierzyklus.

Das rechnerische Beat-Tempo ist vom Urteil über erlebte Geschwindigkeit
zu unterscheiden: Ereignisdichte, Klang und sichtbare Bewegung können
dieses Urteil beeinflussen, ohne die gewählte Beat-Periode zu bestimmen.
[Levitin, Grahn & London (2018), S. 53, § 2.1; S. 67–68, § 7.3](Levitin-Grahn-London-2018-Rhythm-and-Movement.pdf).

## 2. Recherche und Auswahl

Gezielte Recherche vom 2. Oktober 2026, keine systematische Vollerhebung.
Websuche, Verlagsseiten, Autorenarchive und Literaturverzeichnisse dienten
der Auswahl. Suchbegriffe umfassten:

- `isochronous identical tones preferred beat tapping every second third fourth`
- `subjective rhythmization group size tempo`
- `tempo hysteresis metrical level perception`
- `The ambiguous tactus`

Vorrang haben Studien zur freien Beat-Wahl bei gleichen isochronen Klängen.
Gruppierungsberichte ergänzen sie, bestimmen aber nicht allein die
Beat-Ebene. Kontextstudien werden nur für ihre jeweilige Zusatzfrage
berücksichtigt; Methoden, Ergebnisse und Abbildungen der in Abschnitt 7
genannten Primärquellen sind Grundlage der Befundtabellen.

Levitin, Grahn & London (2018) wurde vollständig einschließlich
Literaturverzeichnis gelesen. Die Übersicht ergänzt Messgrenzen und
Erklärungen; ihre hier referierten Originalstudien wurden nicht zusätzlich
eigenständig geprüft. Duke (1989) ist nur anhand des Abstracts berücksichtigt.
Von Martens (2011) sind Abstract und Einleitung gelesen; die vollständige
Auswertung der lokal vorhandenen PDF steht aus. Daraus werden noch keine
zusätzlichen Versuchsdetails oder Schwellen abgeleitet.

## 3. Direkte Beat-Wahl und subjektive Gruppierung

Die Stichprobengröße steht ausdrücklich bei der Population und ist nicht die
Ereigniszahl `N`. „Keine Vorgabe“ heißt keine vorgeschriebene Beat-Periode,
nicht völlige Abwesenheit von Instruktion oder vorausgehendem Hören.

| Studie / Population | Klang, Schalldauer und Lücke | Folgenlänge / Zeitbereich | Referenz und Messaufgabe | Gruppengröße, Beat-Ebene und Einschränkung |
| --- | --- | --- | --- | --- |
| [McAuley & Semple 1999](https://taplab.psy.msu.edu/wp-content/uploads/2020/07/McAuleySemple1999.pdf): 23 (14 trainiert / 9 untrainiert) | Gleiche 440-Hz-Sinustöne, 40 ms; Lücke `Δ−40 ms` | Sechs Muster, 26 Wiederholungen; Rasterabstände unten | Freies Beat-Tapping; Einübung, konstante Tempo-Blöcke | Pulse: durchgängig isochron. Andere Muster: Pausen, Raster ≠ Ereigniszahlen. |
| [Bååth 2015](https://www.sumsar.net/papers/baath_subjective_rhythmization_2015.pdf): 30 rekrutiert / 25 ausgewertet, meist Instrumentalerfahrung | Gleiche 440-Hz-Klicks, 10 ms; Lücke `Δ − 10 ms` | 15 s; Δ=150/200/300/600/900/1200/1500/2000 ms, je vier Versuche | Erste Gruppe berichten; Möglichkeiten vorher erläutert | `G=1…8` (1: keine Gruppe), **Beat-Ebene unbestimmt**. Ausschluss bei Gruppierungsberichten in allen vier 2000-ms-Versuchen. |

### 3.1 Was die direkte Beat-Messung tatsächlich ergibt

Beobachtete Pulse-Häufigkeiten, aus Tabellen 4a/b summiert:

| Δ (ms) | n=1 | n=2 | n=3 | n=4 | n=6 | n=8 | Andere |
| --- | --- | --- | --- | --- | --- | --- | --- |
| 100 | 1 | 5 | 3 | 9 | 1 | 3 | 1 |
| 200 | 13 | 3 | 1 | 5 | 0 | 1 | 0 |
| 300 | 14 | 6 | 2 | 0 | 0 | 1 | 0 |
| 400 | 17 | 5 | 0 | 0 | 0 | 0 | 1 |
| 500 | 17 | 3 | 1 | 0 | 0 | 0 | 2 |
| 600 | 19 | 3 | 0 | 0 | 0 | 0 | 1 |

Andere: `n=11` bei 100 ms, `n=5` bei 500 ms; sonst `T=Δ/2`.
Je 23 Personen; keine allgemeinen Schwellen. Für abweichende
100-ms-Fließtextzahlen gelten hier die Tabellen.
[McAuley & Semple (1999), S. 182](https://taplab.psy.msu.edu/wp-content/uploads/2020/07/McAuleySemple1999.pdf).

### 3.2 Was Gruppierungsberichte hinzufügen – und was nicht

Bei Bååth nennt Tabelle 1 folgende Berichte:

| G | Anteil an allen Versuchen (%) | Δ mit den meisten Berichten dieser Größe (ms) |
| --- | --- | --- |
| 2 | 29,5 | 600 |
| 3 | 3,5 | 300 |
| 4 | 29,6 | 200 |
| 6 | 2,2 | 200 |
| 8 | 10,4 | 150 |

Prozente beziehen sich auf **alle** Versuche, nicht den jeweiligen
Spitzenabstand. Gruppen 5/7 waren selten. Mittlere Gruppenperiode: 1881 ms;
spontanes motorisches Intervall: 622 ms – nicht dasselbe. Das
Ausschlusskriterium verhindert eine unabhängige Bestätigung einer harten
2000-ms-Grenze.
[Bååth (2015), S. 248–251, Tabelle 1](https://www.sumsar.net/papers/baath_subjective_rhythmization_2015.pdf).

**Folgerung:** Für Modellvergleiche sind 2, 3, 4, 6 und 8 begründete
Kandidaten; 1 ist der notwendige Vergleich ohne Unterteilung. Daraus folgt
weder gleiche Beliebtheit noch ein Verbot anderer Ganzzahlen. Insbesondere
belegt diese Auswahl keine psychologische Beschränkung auf bestimmte
Primfaktoren. Die Häufigkeit von `G = 4` darf nicht zur Häufigkeit von `n = 4`
umetikettiert werden.

## 4. Referenz, Kontext und Aussagekraft der Messung

Diese Studien klären Teilfragen, nicht die allgemeine Zuordnung `Δ → n`
für eine unakzentuierte isochrone Klangfolge.

| Studie / Population | Klang und Darbietung | Zeitbereich / Länge | Referenz und Messaufgabe | Ebene, Befund und Grenze |
| --- | --- | --- | --- | --- |
| [Nozaradan et al. 2011](https://doi.org/10.1523/JNEUROSCI.0411-11.2011): 8 Erwachsene mit musikalischer Erfahrung | Kontinuierlicher Ton, 333 Hz; Amplitudenmodulation 2,4 Hz und zusätzlich 11 Hz, keine Folge isolierter gleicher Klicks | Je 33 s; Beat-Periode ungefähr 417 ms | Binäres oder ternäres Metrum ausdrücklich vorstellen; EEG | Zusätzliche Antworten bei 1,2 und 0,8 Hz. **Zwei/drei Beats pro metrischem Zyklus**, nicht nachgewiesen zwei/drei Ereignisse pro neu gewähltem Beat. Keine freie Wahl oder Tempo-Schwelle. |
| [Bouwer & Honing 2015](https://doi.org/10.3389/fpsyg.2015.01094): Experimente mit 20 bzw. 24 professionellen Musikern | Woodblocks; zusätzliche Phasenmarkierung alle acht Töne, seltene Lautstärkeabweichungen; Schalldauer nicht separat angegeben | IOI 250 ms; kontinuierliche Blöcke von 5 bzw. 12 min | Erwartete binäre Beat/Offbeat-Struktur; Experiment 1: Detektion/Reaktionszeit; Experiment 2: EEG beim Filmsehen | Arbeitsreferenz 500 ms (`n = 2`). Positionsabhängige Detektions- und P1-Effekte stützen zeitliche Vorhersage/Aufmerksamkeitsausrichtung. Keine freie Beat-Wahl; wegen Markierung nicht vollständig akustisch gleich. |
| [Lenc et al. 2020](https://doi.org/10.1093/texcom/tgaa037): 16 trainierte, 16 untrainierte Erwachsene | 440-Hz-Töne und Pausen: acht Klänge auf zwölf Rasterpositionen; Schalldauer nicht separat ausgewiesen | 200-ms-Raster; je 57,6 s, 24 Muster | Regelmäßig → mehrdeutig und zeitlich umgekehrt; EEG und anschließendes Tapping | Untersuchte metrische Ebenen u. a. zwei/vier Rasterpositionen. Vorherige Regelmäßigkeit beeinflusst spätere Antworten; Tapping trainierter Personen robuster. **Kontextwechsel, kein Accelerando**; Rasterpositionen sind nicht Klangzahlen. |

### Ergänzender Übergangsbereich bei Polyrhythmen

Møller et al. (2021) untersuchten im Tempoexperiment freies Beat-Tapping
bei 100 Personen online: zwei überlagerte isochrone Cowbell-Folgen gleichen
Klangs, 18–28 s mit mindestens sechs Zyklen. Bei 3:4 wechselte die Präferenz
im Bereich 160 BPM des langsamen Pulses (375 ms) zur halben Rate des
schnellen Pulses, bei höchster Geschwindigkeit zum Gesamtzyklus. Die
Erklärung durch ungefähr 100 ms betrifft das **Unterteilungsraster, nicht
den Beat**. Nur 66,2 % der Versuche lieferten stabile kategorisierbare
Antworten. Der Befund zeigt einen bedingten Ebenenwechsel, keine Schwelle
für eine einzige isochrone Folge.
[Møller et al. (2021), „Tempo Experiment“, Abb. 4](https://doi.org/10.1371/journal.pone.0252174).

### Messaufgaben nicht gleichsetzen

- **Gruppierungsbericht:** zeigt erlebte Organisation, nicht allein deren
  Stellung zum Beat.
- **Beat-Tapping:** operationalisiert eine gewählte Referenz; Instruktion,
  motorische Grenzen und Bewegung selbst können das Ergebnis beeinflussen.
  Nicht zuordenbare Antworten bleiben als solche sichtbar.
- **Abweichungsdetektion:** zeigt Erwartungen an Positionen, ohne damit
  einen Umschaltpunkt zwischen Beat-Ebenen zu bestimmen.
- **EEG:** zeigt strukturierte Antworten, nicht automatisch den bewusst
  gewählten Grundschlag.

### Vier Ergänzungen aus Levitin, Grahn & London (2018)

Die folgenden Aussagen stammen aus der Übersichtsarbeit, nicht aus
zusätzlich hier ausgewerteten Originalstudien:

- **Zeitgenauigkeit ist aufgabenabhängig.** Dauerunterscheidung,
  Tempovergleich und unbewusste Korrektur beim Tapping haben unterschiedliche
  Empfindlichkeiten. Sie liefern weder eine gemeinsame Auflösungsgrenze des
  Gehörs noch eine Schwelle für `n`.
  [Übersicht, S. 54–55, § 2.2](Levitin-Grahn-London-2018-Rhythm-and-Movement.pdf).
- **Synchronisation enthält Vorhersage.** Periode und Phase müssen
  abgestimmt werden; erwartete Positionen können auch ohne klingendes
  Ereignis fortbestehen. Hierarchische Oszillatormodelle beschreiben solche
  Vorgänge, beweisen aber keine Minimierung der Beschreibungslänge.
  [Übersicht, S. 56–58, §§ 2.4.1–2.4.2](Levitin-Grahn-London-2018-Rhythm-and-Movement.pdf).
- **Bewegung kann Wahrnehmung verändern.** Vorherige binäre oder ternäre
  Bewegung beeinflusste die späteren EEG-Antworten beim stillen Hören
  (Chemin et al. 2014); Mittappen verbesserte zeitliche Urteile
  (Manning & Schutz 2013, 2016). Tapping ist deshalb keine völlig neutrale
  Anzeige eines bereits feststehenden Metrums.
  [Übersicht, S. 61–62, §§ 4.1–4.2](Levitin-Grahn-London-2018-Rhythm-and-Movement.pdf).
- **Beat-Wahl ist nicht durch einen bevorzugten Bereich festgelegt.**
  Die Übersicht nennt etwa 100–120 BPM als Bereich besonders ausgeprägter
  Beat-Wahrnehmung und beschreibt unterschiedliche Tactus-Wahlen bei
  gleicher Musik. Das sind keine harten Grenzen; Abschnitt 3.1 zeigt auch
  deutlich schnellere Wahlen.
  [Übersicht, S. 53–54, § 2.1](Levitin-Grahn-London-2018-Rhythm-and-Movement.pdf).

## 5. Grenzen und offene Fragen

### Konstante Tempi und Tempoänderungen

Die direkten Studien vergleichen konstante Tempi. Sie bestimmen keine
Aufwärts-/Abwärtsschwellen für freie Ebenenwechsel innerhalb eines
Accelerandos oder Ritardandos. Der Kontexteffekt von Lenc ist keine
Tempo-Hysterese; auch das Nachführen einer Referenz bei Tempoänderungen ist
nicht gleichbedeutend mit der Wahl einer anderen metrischen Ebene
(Levitin, Grahn & London, S. 60, § 3.1). Das ist eine Evidenzlücke dieser
Auswertung, keine Behauptung, entsprechende Forschung existiere nicht.

### Kurze Gruppen und Stille

Die längeren oder wiederholten Darbietungen bestimmen nicht, wann ein
isoliertes Paar oder eine Dreiergruppe einen Beat etabliert. Ereigniszahl,
Beobachtungsdauer und vorherige Referenz müssen eigenständig erfasst werden.
Gleicher Onset-Abstand bedeutet bei unterschiedlicher Schalldauer außerdem
nicht gleiche stille Lücke. Ein Onset-String unterscheidet weder kurze
Klicks von langen Tönen noch Stille von ausklingendem Schall. Ereignisfusion,
zeitliche Diskrimination und Beat-Wahl bleiben verschiedene Fragen.

### Erklärungsalternativen und Übertragbarkeit

Innere-Uhr-Modelle und Tempo-Präferenz bleiben Erklärungsalternativen
(McAuley & Semple, „General Discussion“). Neuronale Resonanz, Vorhersage,
Aufmerksamkeitsausrichtung, sensomotorische Kopplung und Beschreibungskürze
sind nicht gleichbedeutend; die ausgewählten Studien entscheiden nicht
durchgehend zwischen ihnen. Die direkt ausgewerteten Primärstudien betreffen
Erwachsene, teils ausschließlich musikalisch Trainierte. Eine
kulturunabhängige oder angeborene Grenzregel ist damit nicht belegt.

## 6. Folgerungen für VR-1 und VR-9

Grundlage sind [VR-1](http://localhost:3456/tasks/9) und die
[theoretische Skizze](informationsbegriff.md). „Gestützt“ bezeichnet
Vereinbarkeit mit einzelnen Befunden, keine Bestätigung des Gesamtansatzes.

| Aussage aus VR-1 | Einordnung | Begründung / offene Frage |
| --- | --- | --- |
| Zwei Ereignisse bestimmen eine zeitliche Spanne. | Formal gestützt | Zwei Zeitpunkte definieren einen Abstand, aber keinen eindeutigen Beat. |
| Zwei Ereignisse definieren nur bei hinreichendem Abstand ein Metrum. | Offen; als allgemeine Zeitregel einzuschränken | Die längeren Folgen bestimmen keinen Grenzwert für isolierte Paare; Abstand allein wäre keine hinreichende Bedingung. |
| Das Hören organisiert Ereignisse; ihre metrische Interpretation ist nicht eindeutig in den Onsets enthalten. | In begrenztem Sinn gestützt | Beat-Wahl, Instruktion und Vorgeschichte erlauben unterschiedliche Organisationen; die Übersicht ergänzt Bewegung als Einfluss. |
| Die gewählte Ordnung minimiert die verbleibende Beschreibungslänge einschließlich Modellkosten. | Offen | Diese Auswahlregel wurde nicht vermessen; kurze Beschreibungen können mehrere Beat-Ebenen zulassen. |
| Rhythmische Organisation spart Aufmerksamkeit. | Offen | Positionsabhängige Verarbeitung belegt keine Verringerung des gesamten Aufmerksamkeitsaufwands. |
| Verdopplung und Halbierung der Ereignisrate haben relativ zu einer Referenz Informationswert. | Als Informationsbehauptung offen | Die Referenz kann erhalten bleiben oder wechseln; feste Kosten folgen erst aus einem spezifizierten Modell. |

Für VR-9 sind konkurrierende Ebenen derselben Folge, kurze Gruppen gegenüber
Wiederholung und gleiche Fortsetzungen nach unterschiedlichen Referenzen
zu vergleichen. Die acht detaillierten **eigenen Modellprüffälle** stehen
mit Ereigniszeitpunkten, Fenstern und Kontext im
[separaten Prüffalldokument](modellprueffaelle-beat-ebene-und-unterteilung.md).
Sie sind keine zusätzlichen Wahrnehmungsbefunde.

Für Modellvergleiche sind eine gemeinsame Beschreibungssprache,
Längeneinheit und Genauigkeit festzulegen; die Modellkosten zählen mit.
Eine wahrnehmungsbezogene Genauigkeit braucht eine benannte Messaufgabe;
sie ist kein allgemeiner Grenzwert des Gehörs. Eine eindeutige
Kostenrangfolge ist zudem kein Nachweis eindeutiger menschlicher Beat-Wahl.
Nicht modellierte Hörkontexte sind als Grenze auszuweisen. Hier werden
weder metrische Informationsgewichte noch eine Kodierung oder ein
Produktionsalgorithmus festgelegt.

## 7. Quellen und Fundstellen

### Direkt ausgewertete Primärquellen

1. McAuley & Semple (1999): *The Effect of Tempo and Musical Experience on
   Perceived Beat*. Australian Journal of Psychology 51(3), 176–187.
   [DOI](https://doi.org/10.1080/00049539908255355),
   [Volltext](https://taplab.psy.msu.edu/wp-content/uploads/2020/07/McAuleySemple1999.pdf).
   Methode S. 180; Tabellen 4a/b S. 182; Diskussion S. 186.
2. Bååth (2015): *Subjective Rhythmization: A Replication and an Assessment
   of Two Theoretical Explanations*. Music Perception 33(2), 244–254.
   [DOI](https://doi.org/10.1525/mp.2015.33.2.244),
   [Volltext](https://www.sumsar.net/papers/baath_subjective_rhythmization_2015.pdf).
   Methode/Ausschluss S. 247–248; Tabelle 1 S. 249; Gruppenperiode S. 250–251.
3. Nozaradan et al. (2011): *Tagging the Neuronal Entrainment to Beat and
   Meter*. Journal of Neuroscience 31(28), 10234–10240.
   [DOI](https://doi.org/10.1523/JNEUROSCI.0411-11.2011).
   Methode S. 10234–10235, Abb. 1; Antworten S. 10236–10237, Abb. 2–4.
4. Bouwer & Honing (2015): *Temporal attending and prediction influence the
   perception of metrical rhythm: evidence from reaction times and ERPs*.
   Frontiers in Psychology 6, 1094.
   [DOI und Volltext](https://doi.org/10.3389/fpsyg.2015.01094).
   Abb. 1; „Experiment 1/2 – Materials and Methods“, Ergebnisse und
   „General discussion“. Die zusätzliche Phasenmarkierung ist Teil des
   Versuchsaufbaus.
5. Lenc et al. (2020): *Neural and Behavioral Evidence for Frequency-Selective
   Context Effects in Rhythm Processing in Humans*. Cerebral Cortex
   Communications 1(1), tgaa037.
   [DOI und Volltext](https://doi.org/10.1093/texcom/tgaa037).
   „Auditory Stimulation“, Abb. 1; „Analysis of Tapping Responses“ und
   „Frequency-Domain Analysis of EEG“, Abb. 3.
6. Møller et al. (2021): *Beat perception in polyrhythms: Time is structured
   in binary units*. PLOS ONE 16(8), e0252174.
   [DOI und Volltext](https://doi.org/10.1371/journal.pone.0252174).
   „Finger tapping analyses“; „Tempo Experiment“, Tabelle 1 und Abb. 4.

### Übersichtsarbeit

Levitin, Grahn & London (2018): *The Psychology of Music: Rhythm and
Movement*. Annual Review of Psychology 69, 51–75.
[DOI](https://doi.org/10.1146/annurev-psych-122216-011740),
[lokaler Volltext](Levitin-Grahn-London-2018-Rhythm-and-Movement.pdf).

Vollständig gelesen, aber als Übersicht verwendet. Seitenangaben meinen die
gedruckte Paginierung; S. 51 entspricht PDF-Seite 1. Relevant sind
§ 2.1 (S. 53–54), § 2.2 (S. 54–55), §§ 2.4.1–2.4.2 (S. 56–58),
§ 3.1 (S. 60), §§ 4.1–4.2 (S. 61–62) und § 7.3 (S. 67–68).
Die über diese Übersicht referierten Arbeiten von Chemin und Manning &
Schutz wurden nicht separat geprüft. Die Übersicht beschreibt den Stand
von 2018, nicht den gesamten Forschungsstand von 2026.

### Begrenzte oder noch ausstehende Originalauswertung

- Duke (1989): *Musicians' Perception of Beat in Monotonic Stimuli*.
  Journal of Research in Music Education 37(1), 61–71.
  [DOI / Verlagsabstract](https://doi.org/10.2307/3344953).
  Nur Abstract geprüft; insbesondere Darbietungsdetails und Verteilungen
  nach `n` sind hier nicht verifiziert.
- Martens (2011): *The Ambiguous Tactus: Tempo, Subdivision Benefit,
  and Three Listener Strategies*. Music Perception 28(5), 433–448.
  [DOI](https://doi.org/10.1525/mp.2011.28.5.433),
  [lokaler Volltext](Martens_The_Ambiguous_Tactus_Tempo_Subdivision_B.pdf).
  Abstract und Einleitung S. 433–435 gelesen; die vollständige Auswertung
  steht aus. Wegen der Frage nach Unterteilungsnutzen und Tactus-Strategien
  bleibt die Arbeit ein Kandidat für die Vertiefung, kein zusätzlicher
  Beleg einer hier verifizierten Schwelle.

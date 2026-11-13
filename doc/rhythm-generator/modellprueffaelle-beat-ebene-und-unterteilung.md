# Modellprüffälle zur Beat-Ebene und Unterteilung

Abgeleitet aus der [Literaturauswertung zu VR-12](hoerpsychologie-beat-ebene-und-unterteilung.md)
als Vorbereitung für [VR-9](http://localhost:3456/tasks/17).
Stand: 2. Oktober 2026.

Dieses Dokument bewahrt die detaillierten Vergleichsfälle der Auswertung.
Es ergänzt die [theoretische Skizze](informationsbegriff.md), legt aber weder
eine Kodierung noch Kosten oder einen Produktionsalgorithmus fest. Die
Prüffälle sind eigene Modellvorschläge, keine empirischen Wahrnehmungsdaten
und noch keine durchgerechneten Ergebnisse von VR-9.

## Vergleichsfälle

Die folgenden **eigenen Modellprüffälle** sind keine zusätzlichen empirischen
Ergebnisse. Zeitpunkte sind in ms. Klänge sollen innerhalb eines Vergleichs
gleich bleiben; für einen späteren Hörvergleich wären Schalldauer und Pegel
zusätzlich zu definieren. Die Auswahl 1/2/3/4/6/8 greift die untersuchten
Kandidaten auf; 5 dient als Gegenprobe, nicht als ausgeschlossene Möglichkeit.
`k` bezeichnet in den Ereignisfolgen die nichtnegativen Ganzzahlen ab 0.
`Δ` bezeichnet den Onset-Abstand. Bei lückenlosen Folgen enthält eine
Beat-Periode `T = nΔ` jeweils `n` Ereignisse.

| Vergleich | Ereignisse / Fenster / Kontext | Zu prüfen, ohne Wahrnehmung vorauszusetzen |
| --- | --- | --- |
| Rate und konkurrierende Ebenen | Jeweils `[0, 6000)`; Ereignisse bei `k · Δ < 6000`; `Δ = 100, 200, 300, 600 ms`. Kein äußerer Referenzbeat. | Kosten für `T = nΔ`, `n = 1, 2, 3, 4, 5, 6, 8` und direkte Ereignisbeschreibung vergleichen. Bei 200 ms sind z. B. 200/400/600/800 ms verschiedene Beat-Kandidaten derselben Folge. Gleich gute Modelle ausweisen. |
| Ereigniszahl gegen Darbietungsdauer | Einmal zwölf Ereignisse bei `0…11Δ` im Fenster `[0, 12Δ)`; einmal alle Ereignisse `kΔ < 6000` im festen Fenster `[0, 6000)`. Je `Δ = 200` und `600 ms`. | Bei fester Zahl unterscheidet sich die Zeitspanne, bei festem Fenster die Ereigniszahl. Gesamtinformation nicht stillschweigend als reine Tempo- oder Beat-Präferenz ausgeben. |
| Kurze Gruppe gegen Wiederholung | Ereignisse `{0, 200}` bzw. `{0, 200, 400}` im Fenster `[0, 1000)`; Vergleich mit 200-ms-Folge in `[0, 6000)`. Ohne Vorgeschichte. | Welche Modelle sind aus zwei/drei Onsets bereits unterscheidbar? Ein 400-/600-ms-Zyklus ist im Kurzfall nicht beobachtet wiedergekehrt. Unbestimmtheit statt automatisch fertigem Metrum zulassen. |
| Gleiche Fortsetzung, andere Referenz | Kontextfenster `[0, 3600)`: A `{0, 400, …, 3200}`; B `{0, 600, …, 3000}`. Gleiche Fortsetzung `{3600, 3800, …, 7000}` in `[3600, 7200)`; Beginn auf beiden Referenzen. | Bei erhaltener Referenz entspricht die Fortsetzung `n = 2` bzw. `n = 3`. Kontexterhalt, Modellwechsel und Neuanfang vergleichen; global dürfen die ganzen Folgen, lokal nur Vergangenheit und Gegenwart bekannt sein. |
| Ratewechsel und dauerhafte Unterteilung | Kontext `{0, 600, …, 2400}`; in `[3000, 6000)` entweder `{3000, 3300, …, 5700}` oder `{3000, 4200, 5400}`. | Doppelte bzw. halbe Ereignisrate bei zunächst 600-ms-Referenz. Übergangskosten von Kosten weiterer Wiederholungen trennen; spätere Beat-Neuwahl als Alternative prüfen. |
| Auslassung und stille Erwartungsposition | In `[0, 6000)` zunächst `{0, 600, …, 5400}`, dann dieselbe Folge ohne 3000. | Auslassung gegenüber einem vorhandenen Beat und Wahl eines anderen Modells vergleichen. Eine stille Position ist kein klingendes Ereignis; weniger Onsets müssen nicht weniger lokalen Aufwand bedeuten. |
| Richtung einer Tempoänderung | Zwölf IOIs `{600, 550, 500, …, 100, 50}` und dieselben IOIs umgekehrt; Onsets als kumulative Summen ab 0; Fenster `[0, 4000)`. | Beide Folgen haben 13 Onsets und dieselbe Gesamtspanne 3900 ms. Unterschiede lokaler Modellwahl oder Umschaltung wären Modellvorhersagen; empirische Hysterese-Schwellen sind hier nicht bekannt. 50 ms ist bewusst ein Grenz-/Gegenfall, kein validierter Beat-Bereich. |
| Kein erschließbarer Puls | Leeres Fenster `[0, 6000)` bzw. nur `{3000}`. | Direkte Beschreibung ohne Beat zulassen. Ein Algorithmus darf Periodizität nicht allein deshalb melden, weil nur periodische Modelle angeboten werden. |

Für alle Vergleiche sind eine gemeinsame Genauigkeit, Sprache und Längeneinheit
festzulegen; Modellkosten zählen mit. Die gemeinsame Genauigkeit ist zunächst
eine methodische Festlegung, kein allgemeiner Grenzwert des Gehörs. Soll sie
wahrnehmungsbezogen begründet werden, müssen Messaufgabe, Population und
Bedingungen benannt werden. Die in
[Abschnitt 4 der Literaturauswertung](hoerpsychologie-beat-ebene-und-unterteilung.md#4-referenz-kontext-und-aussagekraft-der-messung)
beschriebenen Empfindlichkeiten verschiedener Aufgaben dürfen nicht als
einheitliche Genauigkeit übernommen werden.

Ein reines Onset-Modell beschreibt zunächst eine zeitliche Struktur, nicht
die gesamte Hörsituation. Gleiche Onsets können je nach Instruktion,
Hörvorgeschichte und Bewegung unterschiedlich organisiert werden. Werden
solche Kontexte nicht modelliert, ist diese Grenze ausdrücklich anzugeben;
eine eindeutige Kostenrangfolge darf nicht allein deshalb als eindeutige
menschliche Beat-Wahl gelten. Daraus folgt für diesen Modellprüfauftrag
weder die Forderung nach einem Körpermodell noch nach einer zusätzlichen
Gewichtung im Informationsmaß.

Der Prüfauftrag umfasst auch das Scheitern einer erwarteten Rangfolge.
Er verlangt **keine metrischen Informationsgewichte**, keinen fest
eingebauten „idealen“ Beat und keine Festlegung des späteren
Produktionsalgorithmus.

# Kapitel 5: Metric Representations and Metric Well-Formedness

Auswertung für Syrincs und [VR-12](http://localhost:3456/tasks/20),
10. Oktober 2026. Gelesen: IMG_1837–IMG_1892, 56 Screenshots.
[Bezugsrahmen und Codeverträge](../README.md).

## Zusammenfassung

London unterscheidet die Darstellung klingender rhythmischer Figuren von
der Darstellung zeitlicher Erwartung. Notenschrift, rhythmische Gruppen,
metrische Punktgitter und kontinuierliche Wellenmodelle erfassen
unterschiedliche Aspekte. Sein eigener Vorschlag stellt ein Metrum als
koordiniertes System von Zyklen dar. Eine Stelle im Kreis steht für eine
erwartete Zeitposition; sie muss nicht durch einen Klang artikuliert sein.
[Rhythmus und metrische Darstellung](IMG_1842.PNG),
[Aufmerksamkeitspositionen](IMG_1853.PNG).

Der **N-Zyklus** enthält die Positionen der schnellsten aktiven metrischen
Ebene. Langsamere Ebenen werden als Subzyklen dargestellt; der Beat-Zyklus
trägt den Tactus. Ein N-Zyklus mit acht Positionen kann beispielsweise eine
Vierer-Beat-Ebene und eine Halbzyklusebene enthalten. N ist also weder
automatisch die Zahl der gehörten Beats noch die Zahl klingender Ereignisse.
Die aktiven Unterteilungen können wechseln, während langsamere Ebenen
erhalten bleiben.
[Aktive und mögliche Ebenen](IMG_1857.PNG),
[N-Zyklus](IMG_1862.PNG), [Beat-Zyklus](IMG_1863.PNG).

London trennt stilistische, notationale und theoretische Wohlgeformtheit.
Was in einer Tradition üblich oder in einer Notation zulässig ist, muss
keine allgemeine Wahrnehmungsgrenze sein. Seine theoretischen Bedingungen
sollen formale Koordination und psychologische Zeitbereiche zusammenführen.
Sie sind ein vorgeschlagenes Modell, kein allein aus Versuchsdaten zwingend
folgender Satz universeller Gesetze.
[Unterscheidung der Ebenen](IMG_1867.PNG),
[Grenzen empirischer Verallgemeinerung](IMG_1874.PNG).

## Die besonders relevanten Bedingungen

London formuliert zunächst Bedingungen für isochrone Ebenen. Die späteren
Kapitel ergänzen nichtisochrone Metren. Die folgende Auswahl paraphrasiert
seine WFCs; die Zahlen sind approximative Modellbedingungen.

| Bereich | Bedingung im Modell | Bedeutung für VR-12 |
| --- | --- | --- |
| Kleinste aktive Intervalle | N-Zyklus-Intervalle größer als ungefähr 100 ms | Ein Raster wird nicht allein durch höhere technische Auflösung zu einer gehörten Unterteilung. |
| Beat-Zyklus | Beat-Abstände ungefähr 400–1200 ms; ein Beat-Zyklus pro Metrum | Mehrere rechnerisch mögliche Ebenen derselben Ereignisfolge bleiben zu prüfen. |
| Größter Zyklus | Gesamtdauer ungefähr höchstens 5000 ms | Die Zahl der Elemente ist zusammen mit deren absoluter Dauer zu beurteilen. |
| Mindeststruktur | Ein Beat-Zyklus mit mindestens zwei Beats; der N-Zyklus darf selbst Beat-Zyklus sein | Nicht jeder Klang muss Unterteilung sein; die Gleichsetzung N = Beat ist aber auch nicht generell verboten. |
| Koordination | Gleiche Gesamtdauer, geschlossene kontinuierliche Zyklen und gemeinsamer Anfang | Metrum enthält Beziehungen zwischen Ebenen und ihre Phase. |
| Hierarchische Trennung | Ein Subzyklus verbindet nicht benachbarte Positionen der nächstkleineren Ebene | Beat und Unterteilung sollen nicht ohne Unterschied auf derselben Ebene vermischt werden. |

Fundstellen: [WFC 1 und 2](IMG_1876.PNG), [WFC 3](IMG_1877.PNG),
[Erklärung der Hierarchie](IMG_1878.PNG).

**Wichtige Grenze:** Der Bereich 400–1200 ms in WFC 1.2 ist enger als
der in Kapitel 2 diskutierte breite Bereich möglicher Beat-Wahrnehmung
von ungefähr 200–250 ms bis 2 s. Ebenso wird die große Zyklusdauer hier
mit ungefähr 5 s angesetzt. Die WFCs sind deshalb nicht einfach eine
unveränderte Abschrift sämtlicher empirischer Bereiche. Für unsere
Recherche müssen jeweilige Messbefunde und Londons Modellfestlegung
getrennt erhalten bleiben.

## Nutzen für den implementierten Informationsbegriff

Das Kapitel liefert eine Beschreibung dessen, **was unser Code als
Referenz bereits voraussetzt**: Raster, Beat-Periode, Phase und Beziehungen
zwischen den Ebenen. Unser Vierersegment ist keine vom Algorithmus
erschlossene Beat-Ebene. Sein Takt- und Rasterwissen wird für die heutige
Zählung nicht durch eine Suche nach konkurrierenden Metren erworben.

Für die theoretische Frage nach Beschreibungslänge ist Londons Darstellung
als Kandidatenstruktur verwertbar. Eine Beschreibung muss etwa unterscheiden,
ob acht Positionen acht Beats, vier Beats mit binärer Unterteilung oder
eine anders geschichtete Hierarchie repräsentieren. Eine vollständige
Beschreibung müsste ihre gewählten Beziehungen berücksichtigen. **London
gibt dafür aber weder Codewörter noch Längen oder eine Minimierungsregel vor.**

Ein Beispiel zur Mehrdeutigkeit: Bei Δ = 200 ms entsprechen n = 2, 3, 4
oder 6 Ereignissen pro Beat die Abstände 400, 600, 800 oder 1200 ms.
Mehrere Kandidaten passen damit ungefähr in Londons formalen Beat-Bereich.
Das ist eine Rechenprobe an seinen Bedingungen, keine Aussage, dass Hörer
alle vier gleich häufig wählen. Die vorhandene Viererzerlegung wählt eine
Struktur bereits technisch vor.

Auch die Zurechnung von Information auf Beats ist referenzabhängig. Eine
Verschiebung des Anfangs oder eine andere gewählte Beat-Ebene verändert,
welche Onsets in einer Analysegruppe liegen. Unsere Profile und ihre
Streuung beschreiben die gesetzte Segmentierung; Londons Hierarchien
begründen keine Gleichsetzung dieser Streuung mit metrischer Stärke.

## Eigene Prüffragen

1. Dieselbe Ereignisfolge unter mehreren expliziten Beat- und
   Unterteilungshypothesen darstellen. Rhythmische Oberfläche und
   angenommene Erwartungspositionen getrennt ausweisen.
2. Aufbaukosten einer Referenz gegenüber den verbleibenden Ereigniskosten
   unterscheiden. Die Wahl eines neuen Rasters darf in einem Modell der
   kürzesten Beschreibung nicht kostenlos sein.
3. Aktive Unterteilung und nur mögliche Unterteilung trennen. Die vier
   technisch verfügbaren Positionen unseres Beats belegen nicht, dass
   Hörer ständig eine Sechzehntelebene erwarten.
4. Bei einem isolierten Paar keinen vollständigen metrischen Zyklus allein
   aus einem beobachteten Abstand behaupten. Die Zyklusdarstellung setzt
   eine etablierte oder angenommene Wiederkehr bereits voraus.

**Gewicht für VR-12:** sehr hoch als begriffliche und formale Vergleichsgrundlage.
Die Bedingungen helfen, konkurrierende Ebenen zu beschreiben; sie wählen
keinen eindeutigen Beat und erklären noch keine unserer Symbolzahlen.

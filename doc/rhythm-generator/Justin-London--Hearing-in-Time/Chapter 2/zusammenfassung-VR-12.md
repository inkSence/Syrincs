# Kapitel 2: Relevant Research on Rhythmic Perception and Production

Auswertung für Syrincs und [VR-12](http://localhost:3456/tasks/20),
10. Oktober 2026. Gelesen: IMG_1696–IMG_1757, 62 Screenshots.
[Bezugsrahmen und Codeverträge](../README.md).

## Zusammenfassung

London führt verschiedene Zugänge zu zeitlicher Wahrnehmung zusammen:
Synchronisation durch Tapping, Fortsetzung ohne äußeren Puls, Urteile über
Intervallveränderungen, Erkennung von Melodien und Beurteilung von Tempo.
Diese Aufgaben prüfen unterschiedliche Fähigkeiten. Eine genaue motorische
Synchronisation ist weder gleichbedeutend mit bewusster Intervallmessung
noch mit einer frei gewählten metrischen Organisation.
[Tapping-Aufgaben](IMG_1698.PNG), [Intervallurteile](IMG_1699.PNG).

Bei manchen Tapping-Aufgaben verringert zusätzliche Unterteilung die
Variabilität. London referiert sowohl zusätzliche eigene Bewegungen zwischen
Pacing-Klicks als auch einen *subdivision benefit* beim Tapping zu einer
langsameren Ebene eines schnelleren äußeren Pulses. Das scheinbar komplexere
Verhalten kann zeitlich stabiler sein. Ein daraus abgeleiteter allgemeiner
Vorteil jeder dichteren Folge wäre jedoch unbegründet.
[Zusätzliche Bewegungen](IMG_1697.PNG), [Subdivision benefit](IMG_1699.PNG).

Der zentrale Gedanke ist eine begrenzte „metrische Hülle“: Die kürzesten
metrisch nutzbaren Zeitintervalle, der Tactus und die größten integrierten
Zyklen haben unterschiedliche Größenordnungen. London verbindet diese
Bereiche zu hierarchischen Modellen. Das Verhältnis von Unterteilung und
Beat ist dabei nicht beliebig: Er formuliert die Hypothese, dass ein Beat
zumindest die Möglichkeit einer Unterteilung voraussetzt. Diese Hypothese
ist von den referierten empirischen Befunden zu unterscheiden.
[Metrische Hülle](IMG_1703.PNG), [Hypothese](IMG_1727.PNG),
[Zusammenfassung](IMG_1754.PNG).

London untersucht anschließend, wie sich bei verändertem Tempo die
verfügbaren Unterteilungen und übergeordneten Perioden verändern.
Identische Zeitverhältnisse können dadurch qualitativ anders erscheinen.
Binäre und ternäre Hierarchien können dieselbe Gesamtperiode besitzen,
ohne dieselbe innere Organisation zu bilden. Seine berechneten Mengen
möglicher Metren sind Konsequenzen des angebotenen Modellraums, keine
Häufigkeitstabellen tatsächlich gewählter Beats.
[Unterschiedliche Hierarchie trotz gleicher Periode](IMG_1739.PNG),
[Tempoabhängige Möglichkeiten](IMG_1749.PNG).

## Was die Zeitangaben für VR-12 leisten

Die folgende Tabelle gibt **Londons Darstellung** wieder, nicht eine hier
erneut geprüfte Primärstudienauswertung. Angaben sind Näherungen mit
unterschiedlichen Messdefinitionen; sie sind keine austauschbaren Schwellen.

| Bereich / Beobachtung | Bericht im Kapitel | Fundstelle und Grenze |
| --- | --- | --- |
| Trennung beziehungsweise Reihenfolge zweier Onsets | Hirsh: etwa 2 ms für das Vorhandensein zweier Onsets, etwa 20 ms für deren Reihenfolge | [IMG_1707](IMG_1707.PNG). Keine Beat- oder Gruppierungsschwelle. |
| Kürzeste metrische Unterteilung | Zusammenführung verschiedener Ergebnisse um etwa 100 ms | [IMG_1709](IMG_1709.PNG), [IMG_1710](IMG_1710.PNG). Bei Tapping zu jedem vierten Ton nennt London im Mittel etwa 120 ms; ungleichmäßige Muster ergeben andere Werte. |
| Subjektive Rhythmisierung | Bolton: ungefähr 115–1580 ms; Fraisse diskutiert Verlust der Verbundenheit oberhalb etwa 1800 ms | [IMG_1706](IMG_1706.PNG), [IMG_1710](IMG_1710.PNG). Historische Gruppenbefunde entscheiden nicht allein, ob Gruppen Unterteilungen oder mehrere Beats sind. |
| Tactus | Je nach referiertem Befund ungefähr 200–250 ms bis etwa 2 s; starke Salienz um 600 ms | [IMG_1714](IMG_1714.PNG), [IMG_1715](IMG_1715.PNG), [IMG_1754](IMG_1754.PNG). Breiter Bereich und Präferenz, keine eindeutige Beat-Wahl. |
| Große integrierte metrische Zyklen | Etwa 5–6 s als theoretisch zusammengeführter oberer Bereich | [IMG_1703](IMG_1703.PNG), [IMG_1713](IMG_1713.PNG). Von Intervallen zwischen einzelnen Ereignissen zu unterscheiden. |
| Langsame Synchronisation | Befunde zu Strategieänderungen und zugleich Studien ohne klaren Umschaltpunkt | [IMG_1711](IMG_1711.PNG), [IMG_1712](IMG_1712.PNG). Zählen, Erfahrung und Aufgabe verändern die Aussage. |
| Unterschiede kurzer und längerer Zeitintervalle | Häufung verschiedener aufgabenbezogener Befunde um 200–250 ms | [IMG_1724](IMG_1724.PNG), [IMG_1725](IMG_1725.PNG). Diskrimination, Maskierung und kategoriale Verarbeitung messen verschiedene Dinge. |

London warnt selbst vor festen Grenzwerten: Ergebnisse sind aufgaben- und
kontextabhängig, streuen zwischen Personen und stammen vielfach aus
vereinfachten Laborreizen. Expressive Zeitgestaltung kann Übergänge
zusätzlich verwischen. Diese Einschränkung gehört zu jeder Übernahme seiner
Zahlen.
[Ausdrückliche Vorbehalte](IMG_1704.PNG), [Expressive Variation](IMG_1705.PNG).

## Bezug zur bestehenden Informationsberechnung

Bei gesetztem Beat-Abstand T entsprechen die Onset-Abstände regelmäßiger
Viertel, Achtel und Sechzehntel T, T/2 und T/4. Bei 108 BPM ist T rund
555,6 ms: Achtel liegen bei rund 277,8 ms, Sechzehntel bei rund 138,9 ms.
Das sind Umrechnungen unserer Darbietung, keine Vorhersagen aus einer Studie.
Wird das Tempo verändert, verändern sich diese absoluten Abstände, während
die bestehenden Codewerte 1, 5 und 9 gleich bleiben.

Das Kapitel macht deshalb **die gesetzte Beat-Ebene zum notwendigen Teil
der Interpretation**. `Info=5` kann die Kodierung regelmäßiger Achtel unter
einem Viertelraster bezeichnen. Der Wert sagt allein nicht, ob Hörer die
Achtel als Beats oder als Unterteilungen hören. Auch ein einfacher
Grenzwertfilter würde diese Mehrdeutigkeit nicht vollständig beseitigen.

Der Unterteilungsnutzen ist eine Gegenprobe für „mehr Codesymbole bedeuten
mehr Aufmerksamkeitsaufwand“: Zusätzliche Ereignisse können in einer
passenden Aufgabe zeitliche Orientierung verbessern. Der Befund begründet
aber keine neuen Symbolkosten und keine automatische Absenkung von `Info`.
Unsere `Deviation` streut Codesymbolzahlen, während Tapping-Variabilität
Abweichungen von Ereigniszeiten beziehungsweise Phasen misst.

## Eigene Prüffälle und offene Fragen

1. Gleiche Onsets bei mehreren Tempi vergleichen; Beat-Tapping und Berichte
   über übergeordnete Gruppen getrennt erheben. Auch instabile oder mehrere
   Deutungen als Ergebnis zulassen.
2. Dieselbe schnelle gleichmäßige Folge mit und ohne vorausgehenden
   langsameren Referenzpuls darbieten. Freie Beat-Wahl und instruiertes
   Tapping nicht als denselben Versuch ausgeben.
3. Eine dauerhaft wiederholte neue Unterteilung mit einer einmaligen
   Zwischenartikulation vergleichen. Einrichtung, weitere Wiederholung
   und Präzision der Orientierung separat betrachten.
4. Ereigniszahl und Gesamtdauer unabhängig kontrollieren. Ergebnisse
   längerer Tapping-Folgen nicht auf ein isoliertes Ereignispaar übertragen.

**Gewicht für VR-12:** sehr hoch. Das Kapitel begründet Zeitbereiche und
Messgrenzen sowie die Suche nach konkurrierenden Ebenen. Es liefert keine
universelle Funktion Δ → Ereignisse pro Beat und keinen empirischen Beweis
für unsere Codesymbolkosten oder die Minimierung einer Beschreibungslänge.

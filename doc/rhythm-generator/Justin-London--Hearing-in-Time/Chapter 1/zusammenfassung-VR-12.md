# Kapitel 1: Meter as a Kind of Attentional Behavior

Auswertung für den implementierten Informationsbegriff von Syrincs und
[VR-12](http://localhost:3456/tasks/20), 10. Oktober 2026.
Gelesener Abschnitt: IMG_1650–IMG_1695, 46 Screenshots.
[Gemeinsamer Bezugsrahmen und Codeverträge](../README.md).

## Was London entwickelt

London versteht Metrum als eine besondere Form zeitlich ausgerichteter
Aufmerksamkeit. Regelmäßigkeiten ermöglichen die Erwartung künftiger
Ereignisse. Das Hören nimmt diese Regelmäßigkeiten aber nicht nur entgegen:
Es bildet und erhält ein Bezugssystem, das die folgenden Ereignisse
organisiert. *Entrainment* bezeichnet die zeitliche Abstimmung des Hörens
mit einer äußeren Folge; London verbindet sie mit Aufmerksamkeit und
sensomotorischer Bereitschaft.
[Definition und Einordnung](IMG_1660.PNG).

Eine Folge gleicher, gleichmäßig wiederkehrender Schläge kann subjektiv
Akzente und Gruppen erhalten, obwohl das Signal diese Unterschiede nicht
enthält. London diskutiert Zweier-, Dreier- und Viererorganisationen als
Beispiele subjektiver Rhythmisierung. Das ist keine Messung einer
allgemeingültigen Regel, welche Gruppengröße bei welchem Tempo entsteht.
[Subjektive Organisation](IMG_1663.PNG).

Ein einzelner regelmäßiger Puls genügt in seiner Theorie noch nicht für
Metrum. Erforderlich ist mindestens eine weitere koordinierte Ebene.
Typisch sind Unterteilung, Tactus und eine übergeordnete Ordnung der Beats;
weitere Ebenen können hinzukommen oder wegfallen. Die Beat-Ebene ist dabei
eine zeitliche Referenz zwischen kürzeren und längeren Zusammenhängen.
Der Zähler einer Taktangabe bezeichnet nicht immer die Anzahl der gehörten
Beats, insbesondere bei zusammengesetzten Metren.
[Mindesthierarchie](IMG_1671.PNG), [Referenzebene](IMG_1673.PNG),
[Notation und wechselnde Unterteilung](IMG_1674.PNG).

Vorgeschichte beeinflusst die Deutung. Derselbe Ausschnitt kann nach
unterschiedlichen metrischen Vorbereitungen anders organisiert werden.
Ein etabliertes Metrum kann trotz Synkopierungen, Pausen und widersprechender
Oberflächenmerkmale erhalten bleiben. London erklärt metrische Akzente
über unterschiedlich starke zeitliche Erwartungen; sie sind nicht einfach
Lautstärkeakzente. Seine Darstellung dynamischer Aufmerksamkeitsmodelle
zeigt, wie Erwartungen mit fortgesetzter Darbietung stärker und zeitlich
genauer werden können.
[Kontextbeispiele](IMG_1665.PNG), [Synkopierung](IMG_1668.PNG),
[Aufbau von Erwartung](IMG_1684.PNG), [Hierarchie](IMG_1687.PNG).

Schließlich unterscheidet London die Perspektiven von Komponisten,
Ausführenden und Hörenden. Ein notiertes oder für die Ausführung benötigtes
Raster kann für den Hörer unzugänglich sein. Das Babbitt-Beispiel macht
diese Trennung besonders deutlich.
[Notierter und nicht gehörter Bezug](IMG_1690.PNG).

## Verwertbarkeit für unser Informationsmaß

**Gestützt wird die Relevanz einer fortgeführten Referenz.** Der Playing-
Zustand unseres Algorithmus führt Kontext über Beatgrenzen fort. Das passt
als begrenzte Analogie zur Idee, dass ein Ereignis nach bereits aufgebauter
Erwartung anders eingeordnet wird als zu Beginn. Der Boolean-Zustand bildet
allerdings weder die Stärke einer Erwartung noch eine frei gewählte
metrische Hierarchie ab. Seine Fortführung ist keine empirische Bestätigung
der konkreten Codesymbolkosten.

**Das Vierersegment im Code ist eine gesetzte Analysegruppe.** London gibt
keinen Grund, vier Rasterpositionen unter jeder Hörbedingung mit einem
wahrgenommenen Beat gleichzusetzen. Für `xoxo xoxo xoxo xoxo` ist daher zu
fragen, ob die Zwischenereignisse als Unterteilung des gesetzten Beats oder
als Beats einer schnelleren Referenz gehört werden. Die subjektive
Organisation in Vierergruppen beantwortet diese Frage allein nicht.

**Vorhersagbarkeit ist nicht automatisch Informationsersparnis.** Londons
Beschreibung ökonomischer Wahrnehmung und gezielter Aufmerksamkeit ist für
unsere Ausgangsthese anschlussfähig. Das Kapitel misst aber weder eine
verringerte Gesamtsumme an Aufmerksamkeit noch die kürzeste verbleibende
Beschreibung. Es begründet auch nicht, warum die bestehende Maschine für
wiederholte Achtel insgesamt 5 und für wiederholte Viertel 1 ausgibt.

**`Deviation` ist kein Maß metrischer Betonung.** Bei uns streuen lokale
Codesymbolzahlen. Bei London entstehen metrische Akzente aus einer
koordinierten Erwartungshierarchie. Die beiden Größen können Gegenstand
eines Vergleichs sein; sie dürfen nicht als dieselbe Größe bezeichnet werden.

## Konkrete Prüffragen aus diesem Kapitel

1. Dieselbe gleichmäßige Fortsetzung nach einem etablierten 400-ms- und
   einem 600-ms-Beat vergleichen. Bei 200-ms-Onset-Abständen wären zwei
   beziehungsweise drei Ereignisse pro fortgeführtem Beat möglich. Das ist
   ein eigener Prüfvorschlag, keine von London gemessene Zuordnung.
2. `xoxo` über viele Beats fortsetzen und ausdrücklich getrennt untersuchen:
   Beginn einer Unterteilung, weitere Wiederholung und möglicher Wechsel
   der Beat-Ebene. Der Code setzt den Unterteilungszustand pro Beat zurück;
   Londons Theorie fordert Aufmerksamkeit für die erhaltene Organisation.
3. Ausgelassene Ereignisse nach aufgebautem Puls gegenüber derselben
   Ereignisfolge ohne Vorbereitung hören. Keine neuen Onsets bedeuten
   nicht notwendig fehlende Erwartung.
4. Bei jedem Vergleich notierte Rasterposition, klingenden Onset, subjektiven
   Akzent, Beat-Tapping und übergeordnete Gruppenberichte separat erfassen.

**Gewicht für VR-12:** hoch für Begriffe, Kontext und die Trennung der Ebenen;
gering für konkrete Tempo- oder Gruppierungsschwellen. Die Zahlen des
bestehenden Informationsmaßes bleiben zu erklärende Codeverträge.

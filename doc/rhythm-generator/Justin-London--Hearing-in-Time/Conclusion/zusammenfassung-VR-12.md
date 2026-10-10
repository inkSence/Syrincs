# Conclusion: Metrische und rhythmische Komplexität

Auswertung für Syrincs und [VR-12](http://localhost:3456/tasks/20),
10. Oktober 2026. Gelesen: IMG_2130–IMG_2157, 27 vorhandene Screenshots.
Die Dateinummer IMG_2156 ist nicht vorhanden; IMG_2155 und IMG_2157
schließen im lesbaren Text unmittelbar aneinander an.
[Bezugsrahmen und Codeverträge](../README.md).

## Zusammenfassung

London führt Metrum als koordinierte zeitliche Aufmerksamkeit und
sensomotorische Synchronisation zusammen. Zeitgrenzen und die Beziehungen
zwischen Unterteilung, Beat und größeren Zyklen begrenzen die möglichen
metrischen Organisationen. Die kreisförmigen Darstellungen sollen diese
Hierarchien einschließlich ihres Auf- und Abbaus erfassen. Sie bilden
eine metrische Organisation ab, die nicht mit der klingenden Oberfläche
identisch ist, aber durch sie angeregt und gestützt wird.
[Rückblick](IMG_2131.PNG), [Darstellung und Unterteilung](IMG_2133.PNG).

Die nochmals aufgelisteten Wohlgeformtheitsbedingungen kombinieren
Zeitbereiche, Mindestanforderungen, Beziehungen zwischen Zyklen und
Regelmäßigkeit. London betont ausdrücklich, dass sie **keine metrischen
Präferenzregeln** sind: Sie entscheiden nicht, welche Oberfläche
welches Metrum erzeugen muss. Mehrdeutige Oberflächen und erlernte
musikalische Kontexte verhindern eine solche unmittelbare Zuordnung.
Die Many Meters Hypothesis ergänzt die abstrakten Strukturtypen durch
erlernte, kontextspezifische zeitliche Verhaltensmuster.
[Definitionen](IMG_2134.PNG), [Bedingungen](IMG_2135.PNG),
[Keine Präferenzregeln](IMG_2137.PNG), [MMH im Rückblick](IMG_2141.PNG).

Die Coda behandelt die für Syrincs entscheidende **Komplexitätsfrage**.
London beschreibt, wie er die anfängliche Absicht aufgab, Metren einfach
nach Anzahl der Ebenen oder Ungleichheit ihrer Beats zu ordnen. Er
referiert Pressings Unterscheidung mehrerer Komplexitätsbegriffe,
darunter eine informationsbezogene Komplexität über die kürzeste
Beschreibung, die ein Objekt oder Verhalten beschreibt oder erzeugt.
Damit kommt die Idee minimaler Beschreibung im Buch ausdrücklich vor;
sie wird hier aber weder formal ausgearbeitet noch auf einen Huffman-Code
oder unsere Symbolwerte bezogen.
[Ursprüngliches Vorhaben](IMG_2143.PNG), [Komplexitätsbegriffe](IMG_2144.PNG).

An metrisch unterschiedlich platzierten Zweitonfiguren argumentiert
London, dass unterschiedliche Schwierigkeit nicht notwendig aus
unterschiedlichen Metren stammt. Das gleiche metrische Gerüst kann
verschiedene Beziehungen zu einer rhythmischen Figur eingehen.
Eine einfache Hierarchie bei extremem Tempo kann schwerer aufrechtzuhalten
sein als eine reichere, gut gestützte Hierarchie bei mittlerem Tempo.
Fehlende Beats und störende Oberflächenereignisse können gerade die
Aufrechterhaltung einer einfachen Referenz erschweren.
[Pressings Beispiele](IMG_2145.PNG), [Londons Gegenargument](IMG_2147.PNG),
[Komplexität und Schwierigkeit](IMG_2148.PNG), [Fehlende Beats](IMG_2149.PNG).

Die drei abschließenden Gedanken betreffen die Gemeinsamkeiten isochroner
und nichtisochroner Metren, die zentrale Rolle von Unterteilungen und
die Verbindung zwischen musikalischer Praxis und Wahrnehmungsforschung.
Auch nicht klingende, nur potenzielle Unterteilungen können für die
Bewegungsqualität eines Beats wesentlich sein. Unterteilungsdichte
verändert seinen Charakter, ohne dadurch automatisch Schwierigkeit
oder Informationsgehalt festzulegen.
[Gemeinsamkeiten](IMG_2150.PNG), [Unterteilungen](IMG_2153.PNG),
[Dichte und latente Unterteilung](IMG_2154.PNG).

## Verwertbarkeit für den Informationsbegriff

**Der stärkste Anschluss ist die Relation zwischen Oberfläche und
Referenz.** Unser bestehender Code berechnet Kosten unter einem gesetzten
Viererraster. Londons Analyse legt nahe, diesen Wert zunächst als
Beschreibung der rhythmischen Oberfläche unter genau dieser Referenz
zu lesen. Er ist weder ein Maß der Komplexität aller denkbaren Metren
noch automatisch ein Maß der Schwierigkeit, die Referenz zu finden.

**Die theoretische Minimalbeschreibung bleibt eine eigene Hypothese.**
Die Erwähnung dieses Komplexitätsbegriffs bei Pressing ist ein geeigneter
Literaturanschluss für unsere theoretische Skizze. Sie beweist nicht,
dass die aktuelle Zustandsmaschine eine minimale Beschreibung erzeugt
oder dass Menschen diesen Wert optimieren. Dazu wären eine festgelegte
Beschreibungssprache, Modellkosten, ein überprüfter Vergleich mit
Alternativen und passende empirische Aufgaben nötig.

**Formale Zulässigkeit beantwortet VR-12 nicht allein.** Dass mehrere
Beats und Unterteilungen in Londons Zeitfenster passen, legt die
hörbare Beat-Wahl gleicher Schläge nicht eindeutig fest. Seine
ausdrückliche Abwesenheit von Präferenzregeln ist hier eine Grenze
des Buches, kein Grund, eine eigene feste Zweier- oder Dreierregel
als nachgewiesen anzunehmen.

**Dichte, Kosten, Unsicherheit und Schwierigkeit getrennt halten.**
Eine schnellere regelmäßige Folge kann stabilisieren; eine dünne Folge
kann viele zu ergänzende Beats enthalten. Unsere `Info` und `Deviation`
beschreiben dagegen deterministische Codeeigenschaften. Empirische
Orientierungszeit, Beat-Wahl, Synchronisationsfehler und subjektive
Schwierigkeit sind weitere Größen, deren Beziehung erst geprüft
werden muss.

## Eigene Konsequenzen für die Klärung

1. Das existierende Maß mit seinen gesetzten Referenzen und Codeverträgen
   dokumentieren; seine Werte nicht nachträglich als Wahrnehmungsbefunde
   umdeuten.
2. Für gleiche Schläge getrennte Versuchsbedingungen mit freier und
   zuvor etablierter Beat-Referenz vorsehen. Gruppierungsberichte und
   Synchronisationsmaße nicht gleichsetzen.
3. Wiederholte Unterteilungen und Tempoänderungen als Gegenproben
   verwenden: Konstante oder wachsende Codekosten können mit veränderter
   Erwartbarkeit und metrischer Stabilität einhergehen.
4. Erst bei einer Erweiterung des theoretischen Modells konkurrierende
   Referenzen und deren Kosten einführen. Aus Londons Wohlgeformtheit
   allein keine Kodier- oder Präferenzregel ableiten.

**Gewicht für VR-12:** sehr hoch für die Begriffsabgrenzung und den Bezug
zum schon implementierten Maß. Die Conclusion liefert eine besonders
klare Begründung dafür, Beschreibungskosten und wahrnehmungsbezogene
Interpretation gemeinsam zu untersuchen, ihre Zahlen aber nicht
ungeprüft gleichzusetzen.

# Kapitel 4: Meter-Rhythm Interactions I: Ground Rules

Auswertung für Syrincs und [VR-12](http://localhost:3456/tasks/20),
10. Oktober 2026. Gelesen: IMG_1806–IMG_1836, 31 Screenshots.
[Bezugsrahmen und Codeverträge](../README.md).

## Zusammenfassung

London behandelt Metrum als Hintergrund für die Wahrnehmung rhythmischer
Figuren. Die gleiche Figur kann vor verschiedenem Hintergrund unterschiedlich
erscheinen. In den referierten Aufgaben ist es schwieriger, bei fortlaufender
Figur das etablierte Metrum umzudeuten, als die Figur bei erhaltenem Metrum
zu verändern. Das macht den Unterschied zwischen einer Änderung der
Ereignisse und einer Änderung ihres Bezugssystems sichtbar.
[Umdeutungsaufgaben](IMG_1808.PNG).

Er unterscheidet metrische Erkennung von metrischer Fortsetzung. Erkennung
kann durch bekannte musikalische Schablonen oder durch die Erschließung
zeitlicher Perioden geschehen. Vertraute Auftakte, Backbeat-Figuren oder
bereits im Stück gehörte Motive tragen Informationen bei, die eine bloße
Abstandsliste nicht vollständig enthält. Nach der Etablierung wird die
Organisation aktiv auf neue Ereignisse projiziert und gegebenenfalls
angepasst.
[Erkennung und Fortsetzung](IMG_1813.PNG), [Schablonen](IMG_1814.PNG),
[Fortsetzung](IMG_1822.PNG).

Das Beispiel „Ode to Joy“ zeigt, wie ein identisches Motiv bei verschiedenen
Tempi auf anderen Ebenen gezählt werden kann. London diskutiert bei
schnellerer Darbietung die Möglichkeit, dass notierte Viertel zur
Unterteilung einer langsameren Beat-Ebene werden. Das Beispiel ist eine
musikalische Analyse, keine allgemeingültige Umschaltschwelle.
[Tempo und Beat-Ebene](IMG_1819.PNG).

Eine Oberfläche kann mehr Ereignisse anbieten, als zunächst als metrische
Referenz benötigt werden, oder weniger, als die gehörte Referenz enthält.
Im Bach-Beispiel werden schnelle Unterteilungen nach fortgesetzter
Darbietung als verlässliche Ebene etabliert. Im Brahms-Beispiel wird ein
nicht vollständig artikulierter Tactus ergänzt. London beschreibt damit
eine dynamische Beziehung zwischen Darbietung und Erwartung.
[Wiederholung und zusätzliche Ebene](IMG_1829.PNG),
[Stabilisierung](IMG_1830.PNG), [Interpolierter Beat](IMG_1832.PNG).

Polyrhythmische Oberflächen deutet London unter einem kohärenten metrischen
Hintergrund oder durch den Wechsel zwischen Hintergründen. Seine scharfe
Ablehnung gleichzeitig wahrgenommener unabhängiger Metren ist eine
theoretische Position des Buches. Sie ist von der unstrittigen Möglichkeit
mehrerer gleichzeitig klingender rhythmischer Stimmen zu unterscheiden.
[Polyrhythmus und Polymetrum](IMG_1809.PNG),
[Londons Deutung](IMG_1812.PNG).

## Besonders direkter Bezug zum heutigen `Info`

Das Kapitel ist eine wichtige Grundlage für die Frage, **was beim
wiederholten `xoxo` noch neu ist**. Unser Algorithmus zählt bei vier solchen
Beats insgesamt 5 Symbole und bei acht Beats 9. Der erste Beat hat Kosten 2,
die folgenden jeweils 1, weil der Playing-Zustand erhalten bleibt, die
Unterteilung aber pro Beat neu eingerichtet wird. London beschreibt dagegen
ausdrücklich, dass wiederkehrende schnelle Artikulationen eine stabile
Erwartungsebene bilden können.

Diese Gegenüberstellung beweist keinen Fehler in der Berechnung. Sie
unterscheidet zwei mögliche Bedeutungen: wiederkehrende **Kodierkosten
innerhalb des gesetzten Verfahrens** und **Neuorganisation beim Hören**.
Soll `Info` Letzteres abbilden, muss erklärt werden, warum stabile
Unterteilung weiterhin pro Beat dieselben Kosten erzeugt. Soll es Ersteres
bleiben, kann genau dieser begrenzte Bezug klar benannt werden.

Auch ein fehlender Beat und ein zusätzliches Ereignis sind nicht ohne
Weiteres Gegenstücke derselben Kodieroperation. Der Hörer kann einen
fehlenden Beat ergänzen oder ein zusätzliches Ereignis in eine vorhandene
Unterteilung einordnen. Ob dies geringe oder hohe Restbeschreibungskosten
ergibt, hängt von der gesetzten Referenz, Vorgeschichte und Kodierung ab.
London liefert keine feste Rangfolge dieser Kosten.

## Bezug zu unseren Hörpräsentationen und VR-12

Das Buch benennt ausdrücklich den Nutzen vorherigen Einzählens: Es
etabliert für die anschließende Darbietung eine zeitliche Referenz.
[Einzählen](IMG_1820.PNG), [Vorausgehende Timeline](IMG_1821.PNG).
Unser `--count-in` verändert somit die Hörbedingung, obwohl es nicht in
die Huffman-Analysewerte eingeht. Eine Präsentation mit Einzählen prüft
**Unterteilung relativ zu einer vorbereiteten Referenz**; sie ist kein
Versuch zur freien Beat-Wahl ohne Vorbereitung.

Eigene Vergleichsfälle aus dem Kapitel:

1. Einmalige zusätzliche Zwischenereignisse und eine längere Folge derselben
   Unterteilung nach gleicher Vorbereitung vergleichen. Vorläufige und
   stabilisierte Erwartung unterscheiden.
2. Dieselbe Folge mit und ohne Einzählen präsentieren und nach Beat-Ebene
   sowie übergeordneter Gruppierung getrennt fragen.
3. Einen Ereignisratenwechsel bei erhaltenem Referenzbeat von einem Wechsel
   des Referenzbeats unterscheiden. Eine Halbierung der Ereignisrate ist
   nicht schon eine Verdopplung des gehörten Beat-Abstands.
4. Für den Hauptfall gleicher Klänge Kick-/Snare-Wechsel und zusätzliche
   Lautstärkeakzente vermeiden. Unser Mapper liefert eine musikalische
   Demonstration, aber keinen akustisch neutralen VR-12-Stimulus.

**Gewicht für VR-12:** sehr hoch für vorbereitete Referenz, Wiederholung und
Unterscheidung von Aufbau und Fortsetzung. Besonders verwertbar ist die
Gegenprobe zu pro Beat erneut gezählten Unterteilungsoperationen.

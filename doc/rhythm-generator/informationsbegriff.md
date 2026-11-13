# Information als Beschreibung zeitlicher Ereignisse

Theoretische Arbeitsskizze zu [VR-1](http://localhost:3456/tasks/9).
Stand: 29. September 2026. Grundlage sind Philipps Gedanken aus der
Projektabstimmung. Die folgenden Annahmen sind ein Untersuchungsansatz,
keine empirisch bestätigte Theorie des Hörens und noch kein neuer Algorithmus.

## Ausgangspunkt: Musik geht vorbei

> Die Haupteigenschaft der Musik sei, dass sie vorbei geht.

Dieser Satz bildet das Paradigma des Ansatzes. Musik wird als flüchtige
Zeitkunst betrachtet. Ereignisse schneiden oder durchbrechen Stille. Sobald
es mehrere Ereignisse gibt, können sie aufeinander bezogen werden; schon
zwei Ereignisse lassen eine zeitliche Spanne bestimmen. Ein einzelner Abstand
legt damit noch keinen eindeutigen Beat oder ein Metrum fest.

Die Ereignis- und Aufmerksamkeitspragmatik fragt, was im zeitlichen Verlauf
als Ereignis bemerkt, behalten, erwartet und erneut organisiert wird.
Aufmerksamkeit soll dabei zunächst eine Bedingung der Organisation
bezeichnen, keinen bereits definierten numerischen Gewichtungsfaktor.

## Arbeitshypothese: Hören organisiert Ereignisse

Die zeitlichen Beziehungen sind das Material, an dem sich ein konstruiertes
Bezugssystem bewähren muss. Ein empfundener Beat wird im Ansatz nicht als
natürliche, eindeutig vorgegebene Eigenschaft der Ereignisse vorausgesetzt.
Das Hören organisiert Ereignisse zu Rhythmen, indem es eine metrische
Ordnung entwickelt. Dieses Modell könnte eine kürzere Beschreibung der
Ereignisfolge ermöglichen.

Vorläufige Begriffe für die Untersuchung:

- **Ereignis:** ein wahrgenommener zeitlicher Einschnitt. Für erste Beispiele
  betrachten wir ausschließlich Onsets; Dauer, Klang und Akzent bleiben offen.
- **Stille:** Ausgangsbegriff für den Raum zwischen Ereignissen. Im Code
  bedeutet `o` nur „kein neuer Onset“, nicht notwendig akustische Stille.
- **Beat:** eine angenommene periodische zeitliche Referenz mit Abstand und
  Phase; seine Schätzung ist vom beobachteten Ereignisabstand zu unterscheiden.
- **Metrum:** eine Organisation zeitlicher Referenzen und Gruppierungen.
  Wie viel Hierarchie über einen periodischen Puls hinaus benötigt wird,
  ist zu bestimmen. Es werden keine metrischen Gewichte vorausgesetzt.
- **Wahrnehmungsgenauigkeit:** eine festzulegende zeitliche Auflösung oder
  Toleranz. Sie bestimmt, welche zeitlichen Unterschiede im Modell erhalten
  werden müssen. Konkrete psychologische Schwellen sind nicht festgelegt.

## Festlegung: kürzeste verbleibende Beschreibung

Information bezeichnet die **Länge der kürzesten verbleibenden Beschreibung**.
Die durch Kompression eingesparte Länge ist eine andere Größe.

Als Vorschlag zur Präzisierung dient:

```text
I(E | ε, C) = min über zulässige M von [ L_C(M) + L_C(E | M, ε) ]
```

`E` bezeichnet Ereignisse in einem festgelegten Beobachtungsfenster, `ε` die
zeitliche Genauigkeit, `M` ein zulässiges zeitliches Modell und `C` eine
gemeinsame Beschreibungssprache. Die Beschreibung muss die Ereignisse bis
zur vereinbarten Genauigkeit rekonstruieren können. Länge und Grenzen des
Beobachtungsfensters müssen entweder mitbeschrieben oder für alle Vergleiche
als bekannte Rahmenbedingungen festgelegt werden.

Die Modellbeschreibung zählt mit: Ein eigenes Raster für jede Folge darf
Unregelmäßigkeit nicht kostenlos verstecken. Bevor Zahlen berechenbar sind,
müssen zulässige Modelle, Operationen, Kodierung und Einheit der Länge
feststehen. „Kürzeste“ meint zunächst das Minimum innerhalb dieser
festgelegten Möglichkeiten. Eine allgemeine oder wahrnehmungspsychologische
Optimalität wird damit nicht behauptet.

Zu prüfen ist auch eine direkte Ereignisbeschreibung als Vergleichsmodell,
damit metrische Organisation sich durch geringeren Aufwand bewähren muss.
Gleich kurze Beschreibungen können unterschiedliche Metren zulassen.

## Lokaler Aufwand und Veränderung

Philipps These lautet, dass Verdopplung und Halbierung relativ zu einem
konstruierten Beat beide Information tragen können. Weniger Ereignisse
können ebenfalls eine Abweichung von einer bestehenden Erwartung bedeuten.
Die Begriffe müssen dabei präzisiert werden: Doppelte Ereignisrate bedeutet
halben Ereignisabstand; ein doppelt so langer Beat-Abstand ist etwas anderes.

Für die Beispiele unten wird ausdrücklich die Ereignisrate verändert,
während die anfängliche Referenz zunächst bestehen bleibt. Diese Konvention
entscheidet noch nicht, ob das Hören anschließend einen neuen Beat annimmt.

Eine rückblickend kürzeste Beschreibung einer gesamten Folge und der lokale
Aufwand beim fortlaufenden Hören sind getrennte Untersuchungsgrößen. Das
lokale Modell darf nur den bereits gehörten Kontext verwenden. Zu klären ist,
ob eine neue Unterteilung bei jeder Wiederholung Kosten verursacht oder ob
sie nach einem Übergang zur günstig beschreibbaren Erwartung wird. Eine
globale Minimierung allein definiert noch keine eindeutige Verteilung ihrer
Kosten auf Beats oder Takte.

## Erste Prüffälle

Die Zeitpunkte sind künstliche Beispiele in derselben Zeiteinheit, keine
Vorgabe eines wahrgenommenen Metrums. Alle Beispiele werden im Fenster
`[0, 8)` bei gleicher, noch festzulegender Genauigkeit betrachtet.

| Fall | Ereigniszeitpunkte | Zu prüfende Erwartung / offene Frage |
| --- | --- | --- |
| Regelmäßigkeit | 0, 1, 2, 3, 4, 5, 6, 7 | Eine periodische Beschreibung könnte günstiger als acht Einzelangaben sein; ihre Einrichtung muss mitzählen. |
| Ein ausgelassenes Ereignis | 0, 1, 2, 3, 5, 6, 7 | Gegenüber der aufgebauten Referenz entsteht bei 4 eine Ausnahme. Ob die globale Länge steigt, ist mit der Kodierung zu prüfen. |
| Doppelte Rate ab 4 | 0, 1, 2, 3, 4, 4.5, 5, 5.5, 6, 6.5, 7, 7.5 | Übergang zu halben Abständen: Modellwechsel oder wiederholte lokale Abweichung? |
| Halbe Rate ab 4 | 0, 1, 2, 3, 4, 6 | Übergang zu doppelten Abständen kann trotz weniger Ereignissen lokalen Aufwand verursachen. |
| Doppelte Rate von Beginn an | 0, 0.5, 1, 1.5, …, 7.5 | Vergleich mit dem Ratenwechsel: Aufbau einer Referenz gegenüber Änderung einer vorhandenen Referenz. |
| Stille / einzelnes Ereignis | keine / nur 4 | Grenzfälle ohne aus Ereignisabständen bestimmbare Periodizität. |

Die Skizze liefert Fragen und qualitative Hypothesen. Zahlen und Rangfolgen
werden erst nach Festlegung einer Beschreibungssprache berechnet. Auch ein
Scheitern der erwarteten Ordnung ist als Ergebnis zu dokumentieren.

## Verhältnis zum bestehenden Code

[`Rhythm`](../../src/main/java/syrincs/a_domain/rhythm/Rhythm.java) zerlegt
Onsets in vorgegebene Vierergruppen.
[`HuffmanRhythm`](../../src/main/java/syrincs/a_domain/rhythm/HuffmanRhythm.java)
zählt erzeugte Codesymbole und summiert diese je Eingabe. Die Zahl ist eine
Symbolanzahl; der Code weist sie nicht als Bitlänge einer nachweislich
kürzesten vollständigen Beschreibung aus.

Der Playing-Zustand läuft über Beats hinweg. Der Unterteilungszustand wird
für jeden Beat wieder als Quarter initialisiert. Tempo und eine
Wahrnehmungstoleranz gehen nicht in die Informationsberechnung ein. Eine
Suche über mögliche Metren oder konkurrierende Beschreibungen findet nicht
statt. Damit ist der bestehende Algorithmus ein konkreter Vergleichsfall,
aber bislang kein Nachweis der hier vorgeschlagenen Minimierung.

Die vorhandenen [Tests](../../src/test/java/syrincs/a_domain/rhythm/HuffmanRhythmTest.java)
halten unter anderem Information 1 für `xooo xooo xooo xooo`, 3 für
`xooo oooo xooo xooo` und 5 für `xoxo xoxo xoxo xoxo` fest.
Diese Codeverträge sind von theoretisch gewünschten Werten zu unterscheiden.
Für den Vergleich müssen Zeitpunkte mit explizit gewähltem Raster auf
Onsets abgebildet werden; diese Wahl darf nicht als vom Code erschlossen gelten.

## Ergebnisauftrag für VR-1

VR-1 soll die Begriffe präzisieren, eine begrenzte Beschreibungssprache
vorschlagen, die Prüffälle auswerten und den Unterschied zur heutigen
Berechnung belegen. Anschließend werden die Folgen für Taktinformation
abgeleitet: Was kann einem Takt zugerechnet werden, welcher Kontext ist
erforderlich und ist die Zurechnung eindeutig?

Ein dokumentierter Gegenbefund oder begründeter Bedarf an weiterer Forschung
ist ein zulässiges Ergebnis. VR-2 und VR-8 benötigen für die Umsetzung dennoch
einen ausdrücklich akzeptierten Taktvertrag. Ein Abschluss der Untersuchung
gibt diese Folgeaufgaben nicht automatisch frei. Eine neue Modellimplementierung
und eine Änderung bestehender Kennzahlen sind gesondert zu entscheiden.

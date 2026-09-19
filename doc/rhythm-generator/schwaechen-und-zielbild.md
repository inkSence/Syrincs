# Rhythmusmodul: Schwächen und musikalisches Zielbild

Analyse zu [PV-1](http://localhost:3456/tasks/1), zweites To-do.
Stand: 19. September 2026, Codebasis `572fb4d`.

## Ergebnis und Umfang

Syrincs besitzt bereits ein musikalisches Suchkriterium: das projektinterne
Informationsmaß. Der DB-Weg wählt anhand gewünschter Informationsgrade und
einer festen Deviation-Schwelle aus. Dieses Maß ist der Kern des Moduls.

Die Weiterentwicklung soll Information auf Beat-, Takt- und Phrasenebene
gestaltbar machen. Dafür fehlen taktweise Analyseergebnisse, zugängliche
Informationsprofile und eine einstellbare Deviation. Reproduzierbarkeit
unterstützt diese Arbeit: Eine nach Information gefundene Idee soll sich
wieder aufrufen, vergleichen und weiterentwickeln lassen.

Die wichtigste Weiterentwicklung ist deshalb ein zusammenhängender Ablauf:

```text
Informationsgrad oder Informationsverlauf festlegen
           ↓
Deviation einstellen → nach Informationsmaß suchen
           ↓
Information je Beat, je Takt und für die Phrase prüfen
           ↓
Kandidaten hören und ihre Informationsverläufe vergleichen
```

Das Zitat im Ticket trifft den Verlust der zeitlichen Gestalt. Die Aufgabe
ist, das vorhandene Maß über seine Gesamtzahl hinaus nutzbar zu machen:
mit Beat-Profilen, eigenständigen Taktbewertungen und Informationsverläufen
über mehrere Takte.

Dieses Dokument ist ein Arbeitsentwurf und bleibt während der Abstimmung
eigenständig, ohne Verweise aus den Bestands-READMEs.

Dieses Dokument trennt belegtes Verhalten von Vorschlägen. Alle neuen
Befehle sind Entwürfe, keine vorhandenen APIs. Mit „Endpunkten“
sind hier CLI-Befehle und die dahinterliegenden Application-Use-Cases gemeint.
Ein HTTP-Dienst ist für diese Aufgaben nicht erforderlich. Es werden weder
fachliche Defaults geändert noch neue Vikunja-Projekte oder Tasks angelegt.

## 1. Was erhalten bleiben sollte

- Das Informationsmaß besitzt konkrete, getestete Erwartungswerte. Es sollte
  zunächst sichtbar und nachvollziehbar werden, nicht nebenbei ersetzt werden.
- Bestehende Analyse, Katalogsuche und MIDI-Wiedergabe bilden die Grundlage
  für den Ausbau. Ihre bisherigen Aufrufmöglichkeiten bleiben erhalten.

## 2. Belegte Schwächen und ihre musikalische Bedeutung

Die Nummerierung dient der Referenz, nicht als Rangfolge. Die vorgeschlagene
Bearbeitungsreihenfolge steht in Abschnitt 5.

### S1 — Informationen sind berechnet, aber nicht als Arbeitsmaterial zugänglich

HuffmanRhythm berechnet für jede Zählzeit einen eigenen Informationswert. Dieser
entspricht der Anzahl der Codesymbole (00, 01, 10 & 11), die die Zustandsmaschine für die vier
Positionen der Zählzeit erzeugt. Die Informationswerte der einzelnen Zählzeiten
werden anschließend nur zur Berechnung der Gesamtinformation und der
Standardabweichung verwendet; sie werden weder gespeichert noch über die
öffentliche Schnittstelle zugänglich gemacht. Auch die zugrunde liegenden
Codesymbolfolgen bleiben intern. Die CLI zeigt unter Beats lediglich die Onset-
Gruppen der einzelnen Zählzeiten, nicht deren Informationswerte.

Damit lässt sich anhand der öffentlichen Analyse nicht prüfen, auf welche
Zählzeiten sich die Information verteilt. Gesamtinformation und
Standardabweichung allein zeigen den zeitlichen Verlauf nicht.

**Vorschlag:** Ein unveränderliches Analyseergebnis mit Beat-Werten,
Gesamtwert, Mittelwert, Streuung und Kontext. Taktbewertungen werden nach
Klärung des Taktinformationsbegriffs in S2 ergänzt.

Beleg: [HuffmanRhythm](../../src/main/java/syrincs/a_domain/rhythm/HuffmanRhythm.java),
[Analyse-CLI](../../src/main/java/syrincs/c_adapters/cli/RootCmd.java),
[HuffmanRhythmTest](../../src/test/java/syrincs/a_domain/rhythm/HuffmanRhythmTest.java).

### S2 — Eine eigenständige taktweise Auswertung mehrtaktiger Rhythmen fehlt

Ein einzelner Takt als eigene Eingabe ist heute analysierbar und wird vom
Generator bewertet. Innerhalb einer mehrtaktigen Eingabe berechnet der Code
Beat-Werte, aber keine strukturierten Ergebnisse je Takt. Öffentlich liefert
er nur die Aggregate der gesamten Eingabe. Es fehlt eine ausdrückliche
Analyseebene zwischen Beat und Gesamtphrase. Beat bedeutet hier eine
Zählzeit mit vier Rasterpositionen, nicht einen einzelnen Onset.

Folgende Werte wurden mit dem aktuellen Produktionscode geprüft; für die
Profile wurde die private Berechnung ausschließlich diagnostisch aufgerufen:

| Onsets | Beat-Information | Summe | Standardabweichung |
| --- | --- | ---: | ---: |
| `xooo xooo xooo xooo` | `[1,0,0,0]` | 1 | 0,433013 |
| derselbe Takt zweimal als eine Eingabe | `[1,0,0,0,0,0,0,0]` | 1 | 0,330719 |
| `xooo xoxo xooo xoxo` | `[1,1,0,1]` | 3 | 0,433013 |
| `xoxo xooo xoxo xooo` | `[2,0,1,0]` | 3 | 0,829156 |
| `oooo oooo oooo oooo` | `[0,0,0,0]` | 0 | 0 |

**Vorschlag mit hoher Priorität:** Den Taktinformationsbegriff fachlich
ausarbeiten und darauf eine taktweise Analyse aufbauen. Zwei mögliche
Betrachtungsweisen dienen dabei als Ausgangspunkt:

| Auswertung | Berechnung | Verwendung |
| --- | --- | --- |
| Takt isoliert | Jeden Takt ab Ruhe mit dem bestehenden Maß analysieren | Takte unabhängig vergleichen und über die vorhandenen Katalogwerte suchen. |
| Takt im Phrasenkontext | Zustand durch die Phrase führen, Beat-Werte nach Takt gruppieren | Informationsverlauf der zusammengesetzten Phrase untersuchen. |

Ein mögliches Ergebnis je Takt umfasst Taktnummer, Onsets,
Beat-Informationsprofil, Summe und Deviation seiner Beat-Werte. Für die beiden
Vierteltakte oben liefert die isolierte Betrachtung die Werte `[1,1]`, die
Gruppierung der durchgehenden Analyse nach Takten dagegen `[1,0]`.
Ob beide Betrachtungsweisen öffentlich angeboten werden und welche davon
als Taktinformation bezeichnet werden soll, ist Gegenstand der fachlichen
Klärung. Der Gegenstand des Taktinformationsbegriffs ist noch nicht klar und
kann auch nicht so schnell geklärt werden.

Die kontextuellen Taktsummen ergeben zusammen die Phraseninformation; die
isolierten Taktsummen müssen das nicht. Phrasen-Deviation wird aus allen
kontextuellen Beat-Werten berechnet, nicht als Mittel der Takt-Deviations.
Eine Streuung der Taktinformationen wäre eine weitere, separat zu benennende
Kennzahl.

Die Anforderung „Takt 1 Information 3, Takt 2 Information 5“ lässt sich erst
nach dieser Klärung eindeutig umsetzen. Bei isolierter Bewertung könnten
zwei Katalogabfragen genügen; bei kontextueller Bewertung wäre die Verbindung
der Takte einzubeziehen. Welche Such- und Auswertungsmöglichkeiten daraus
folgen, soll aus dem Taktinformationsbegriff abgeleitet werden. Eine Änderung
des bestehenden Informationsmaßes ist damit noch nicht beschlossen.

Beleg: `calculateInformationForEachBeat`, `playingAfterBeat` und
`calculateInformation` in [HuffmanRhythm](../../src/main/java/syrincs/a_domain/rhythm/HuffmanRhythm.java).

### S3 — Der feste Streuungsfilter trifft eine ungewählte musikalische Entscheidung

Der `UseCaseInteractor` fragt für jeden exakten Informationsgrad ausschließlich
Kandidaten mit `deviation > 0.7` ab. Es gibt keine CLI-Einstellung dafür.
Die DB-Abfrage ohne diesen Filter existiert, ist aber hier nicht zugänglich.

Der gleichmäßige Vierteltakt und das dritte Beispiel oben fallen aus der
Auswahl heraus, das vierte Beispiel bleibt zugelassen. Höhere Streuung wird
damit implizit bevorzugt, obwohl ein stabiles Ostinato ebenso ein legitimes
musikalisches Ziel ist. Streuung ist weder Dichte noch eine Qualitätswertung.

**Vorschlag mit hoher Priorität:** Deviation wird ein Parameter der Suche
nach Informationsmaß, auch beim vorhandenen `play rhythm info`-Befehl.
Entwurf: `play rhythm info 3 5 --deviation-min 0.2 --deviation-max 0.8`.
Damit bestimmt der Anwender die zulässige Streuung innerhalb der gewünschten
Informationsgrade.

Der gemeinsame Suchvertrag bietet optionale inklusive Grenzen; negative
Werte und `min > max` sind Eingabefehler. Ohne Filter soll eine neue Suche
alle Deviations zulassen. Alte Aufrufe ohne Optionen können zunächst den
strikten Filter `> 0.7` als dokumentierten Kompatibilitätsdefault behalten;
explizite Grenzen müssen ihn vollständig ersetzen. Die Default-Migration
bleibt beim Task-Schnitt zu entscheiden, die Einstellbarkeit steht fest.

Die heutige Deviation bezieht sich auf die Beat-Informationswerte der
analysierten Eingabe. Sollte nach S2 zusätzlich eine Deviation je Takt
angeboten werden, muss deren Bezugsmenge ausdrücklich benannt werden.

Beleg: [UseCaseInteractor](../../src/main/java/syrincs/b_application/UseCaseInteractor.java),
[RhythmRepository](../../src/main/java/syrincs/b_application/ports/RhythmRepository.java).

### S4 — Suche, Auswahl und Wiedergabe lassen sich nicht getrennt bedienen

Die CLI besitzt keine Kandidatenliste, kein Sortieren, keine Trefferzahl und
keine Auswahl eines bestimmten DB-Treffers. `ThreadLocalRandom` entscheidet
direkt vor dem Playback. Ein erneut ausgeführter Befehl kann anderes Material
liefern. Fehlen nur einzelne Grade, werden sie still übersprungen; die Phrase
kann kürzer werden als beabsichtigt.

Die DB-Auswahl bezieht sich auf einzeln bewertete Takte. Ein Aufruf wie
`play rhythm info 3 5` wählt bei vorhandenen Kandidaten je einen solchen
Treffer und spielt die beiden verbunden ab;
eine Analyse der entstehenden Phrase wird dabei nicht ausgegeben. Eine
Trennung der Arbeitsschritte würde erlauben, die ausgewählten Inhalte vor
der Wiedergabe im Zusammenhang zu untersuchen. Wie ihre Taktinformationen
dabei zu bestimmen sind, bleibt die in S2 beschriebene fachliche Frage.

**Vorschlag:** Suche liefert Daten, Auswahl liefert ein konkretes Ergebnis,
Playback konsumiert dieses Ergebnis. Fehlende Positionen werden benannt;
ein neuer Phrasenablauf schlägt standardmäßig fehl, wenn ein angeforderter
Baustein fehlt. Überspringen oder Auffüllen mit Stille wird ausdrücklich
gewählt. Der historische Befehl kann als Komfortfunktion bestehen bleiben.

Ein Seed allein reicht nicht: Kandidatenmenge, Reihenfolge, Duplikate,
Zufallsalgorithmus und Analyseversion müssen feststehen. Die Ausgabe sollte
die konkret ausgewählten Onsets zum direkten Wiederaufruf enthalten.
Dauerhaft gespeicherte Auswahlen oder Presets wären eine separate Erweiterung,
keine Voraussetzung für die Trennung von Suche, Auswahl und Wiedergabe.

Beleg: [UseCaseInteractor](../../src/main/java/syrincs/b_application/UseCaseInteractor.java),
[RootCmdRhythmCliTest](../../src/test/java/syrincs/c_adapters/cli/RootCmdRhythmCliTest.java).

### S5 — Nach der zeitlichen Verteilung von Information kann nicht gesucht werden

Die vorhandene Suche behandelt den Informationsgrad als einzelne Zahl für
einen Rhythmus. Damit lassen sich Rhythmen mit gleicher Gesamtinformation
nicht gezielt nach dem zeitlichen Verlauf dieser Information auswählen.
Der vorhandene Deviation-Filter beschreibt zusätzlich eine skalare Streuung,
aber weder Reihenfolge noch Position der Informationswerte. Dessen
Parametrisierung bleibt das getrennte Thema von S3.

Nicht ausdrückbar sind beispielsweise:

- gleiche Gesamtinformation bei unterschiedlichem Beat-Profil;
- ein Informationsmaximum auf einer bestimmten Zählzeit;
- ein vorgegebenes Informationsprofil innerhalb eines Takts;
- ein Informationsverlauf über mehrere Takte.

**Vorschlag:** Die Informationssuche um Kriterien ergänzen, die sich auf die
zeitliche Verteilung des bestehenden Informationsmaßes beziehen. Dazu gehören
insbesondere Beat-Profile und – nach Klärung des Taktinformationsbegriffs –
Informationsverläufe über mehrere Takte.

Beleg: Abfrageumfang von [RhythmRepository](../../src/main/java/syrincs/b_application/ports/RhythmRepository.java)
und Befehle in [RootCmd](../../src/main/java/syrincs/c_adapters/cli/RootCmd.java).

### S6 — Der Weg vom eigenen Pattern zum Hören ist unnötig lang

Ein Onset-String lässt sich direkt analysieren, aber nicht direkt per CLI
abspielen. Man muss ihn manuell in RDL übertragen oder über die DB-Zufallssuche
anderes Material abrufen. DB-Playback verwendet 120 BPM und feste Voices.
Eine Tempooption und endliche Wiederholungen fehlen.

**Vorschlag:** Dieselben Onsets bzw. dieselbe Auswahl analysieren und hören
können. Tempo und Wiederholungsanzahl sind einstellbar. Die Wiedergabe nutzt
das vorhandene Kick-/Snare-Mapping. Nachlauf gehört ans Ende der Vorschau,
nicht zwischen die Wiederholungen.

Beleg: [RootCmd](../../src/main/java/syrincs/c_adapters/cli/RootCmd.java),
[PlayHuffmanRhythmsUseCase](../../src/main/java/syrincs/b_application/PlayHuffmanRhythmsUseCase.java),
[JdkSequencePlayer](../../src/main/java/syrincs/c_adapters/midi/JdkSequencePlayer.java).

### S7 — Katalog und gespeicherte Analysewerte sind nicht hinreichend stabil

Jeder vollständige Generatorlauf erzeugt erneut alle 65.536 Pattern und fügt
sie als neue Datensätze ein. Weder das Repository noch das Datenbankschema
verhindern doppelte Pattern. Die Zufallsauswahl arbeitet anschließend auf den
gefundenen Datensätzen. Mehrfach gespeicherte Pattern gehen daher
entsprechend mehrfach in die Auswahl ein. Bei einer gleichmäßigen
Vervielfachung des gesamten Katalogs bleibt die relative Verteilung
unverändert; ungleichmäßige Duplikate verändern dagegen die
Auswahlwahrscheinlichkeiten einzelner Pattern.

Die Datenbank speichert `info` und `deviation` und verwendet diese Werte auch für
Suchabfragen. Beim Laden werden sie jedoch nicht übernommen. Stattdessen erzeugt
das Repository aus `rhythmstring`, `numerator` und `denominator` ein neues
`HuffmanRhythm`, das Information und Deviation mit dem aktuellen Algorithmus
erneut berechnet. Nach einer späteren Änderung des Informationsmaßes können
deshalb die gespeicherten Suchwerte und die Werte des rekonstruierten
Domänenobjekts auseinanderfallen. Eine Version des verwendeten Analyseverfahrens
wird nicht gespeichert.

**Vorschlag:** Die Katalogerzeugung sollte idempotent werden, sodass ein erneuter
vollständiger Generatorlauf keine weiteren Exemplare bereits vorhandener Pattern erzeugt.
Außerdem sollten persistierte Analysewerte einer definierten Analyseversion zugeordnet
werden. Ändert sich das Informationsmaß, müssen die davon abhängigen Katalogwerte
gezielt neu berechnet werden.


Beleg: [PostgresRhythmRepository](../../src/main/java/syrincs/c_adapters/postgres/PostgresRhythmRepository.java),
[GenerateAndPersistRhythmUseCase](../../src/main/java/syrincs/b_application/GenerateAndPersistRhythmUseCase.java).

### S8 — Die Bindung an 4/4 und vier Positionen pro Beat begrenzt das Modell

Der Generator erzeugt ausschließlich 16 Positionen im 4/4-Takt. Die
Huffman-Berechnung verarbeitet Vierergruppen mit den Unterteilungen
Quarter, Eighth und Sixteenth. Eine beliebig lange Eingabe bedeutet heute
nur mehr vollständige Takte im vorgegebenen Raster, keine frei bestimmbare
Anzahl von Positionen oder aus den Onsets abgeleitete Taktbildung.

**Langfristiges Ziel:** Jede positive ganzzahlige Anzahl von Positionen
soll modellierbar sein. Die vermutete praktische Konzentration auf Zahlen
mit den Primfaktoren 2, 3 und 5 ist eine Arbeitshypothese. Andere Primfaktoren
gelten in diesem Ansatz als außergewöhnlich, bleiben aber zulässig. Daraus
folgt keine feste Beschränkung auf 2, 3 und 5.

**Anlehnung an Kognition als Bildungsgesetz für Takte:** Das Bedürfnis nach
Vereinfachung könnte zur Unterteilung zeitlicher Folgen führen. Die
Vereinfachung minimiert Kosten. Unterteilungen sind / bilden Gruppierungen.
Takte lassen sich auch als Hierarchische Gruppierungen beschreiben, deren
Teile sich aufeinander beziehen. Dies ist ein möglicher Erklärungsansatz für
die Taktbildung.

**Erste Anwendung: Bestimmung statt Erzeugung.** Ein Algorithmus nimmt einen
Onset-String entgegen und liefert eine Wahrscheinlichkeitsverteilung über
Primfaktoren als mögliche Unterteilungsfaktoren. Alternativ könnte eine
festgelegte Entscheidungsregel daraus einen im Modell bestimmten
„objektiven Takt“ ableiten. Verteilung und ausgewählte Taktstruktur sind
unterschiedliche Ergebnisse; welches davon benötigt wird, bleibt zu klären.
Ein denkbarer Weg zur Verteilung ist:

1. Zulässige hierarchische Unterteilungen für die Positionszahl bestimmen.
2. Jede Unterteilung anhand der tatsächlichen Onset-Anordnung und ihrer
   Kostenersparnis bewerten.
3. Die Bewertungen nach einer festgelegten Regel in Wahrscheinlichkeiten
   überführen und zu einer Verteilung über Primfaktoren zusammenfassen.

Die Primfaktorzerlegung der Länge allein genügt nicht: Zwei Onset-Strings
gleicher Länge können unterschiedliche Gruppierungen nahelegen. Bei zwölf
Positionen wären etwa wiederholte Zweier- und Dreiergruppen zu unterscheiden.
Auch die Reihenfolge der Unterteilungen kann relevant sein; eine bloße
Verteilung über 2 und 3 bildet diese Hierarchie noch nicht vollständig ab.

„Genau eine Verteilung“ bedeutet einen reproduzierbaren Algorithmusvertrag:
Für denselben normalisierten String und dasselbe Modell mit denselben
Parametern entsteht dieselbe normierte Verteilung. Diese kann mehrere
plausible Unterteilungen ausdrücken; sie behauptet keine eindeutig bestimmte
wahrgenommene Taktart. Ein Onset-String allein enthält etwa keine Angaben zu
Akzenten oder zum Tempo.

Vor einer Umsetzung sind insbesondere festzulegen:

- Worauf bezieht sich die Wahrscheinlichkeit eines Primfaktors: auf die
  nächste Unterteilung, die oberste Gruppierung oder die Verwendung innerhalb
  der ganzen Hierarchie? Diese Ereignisse sind nicht identisch.
- Sind nur gleich große Teilgruppen erlaubt? Dann sind die Kandidaten durch
  die Teiler der Positionszahl eingeschränkt. Additive oder ungleich große
  Gruppen benötigen ein erweitertes Modell.
- Wie werden Einfachheit, Onset-Anordnung und mögliche Präferenzen für
  2, 3 und 5 bewertet, ohne das Ergebnis bereits fest vorzuschreiben?
- Wie werden gleich gute Erklärungen, reine Stille und der Grenzfall einer
  einzigen Position behandelt? Eine Position besitzt keinen Primfaktor;
  dafür ist beispielsweise ein eigenes Ergebnis „keine Unterteilung“ nötig.

Diese Untersuchung folgt nach der Verbesserung der vorhandenen
Informationsanalyse. Die Zulässigkeit beliebiger Positionszahlen verlangt
keine vollständige Erzeugung aller `2^N` Pattern. Zunächst werden einzelne
vorgegebene Strings bestimmt und die Resultate fachlich geprüft.

Beleg für die heutige Grenze:
[Generator](../../src/main/java/syrincs/b_application/GenerateAndPersistRhythmUseCase.java),
[Rhythm](../../src/main/java/syrincs/a_domain/rhythm/Rhythm.java) und
[HuffmanRhythm](../../src/main/java/syrincs/a_domain/rhythm/HuffmanRhythm.java).
Der kognitive Ansatz und die Verteilungsbestimmung sind Forschungsvorschläge
aus der laufenden Abstimmung, keine Ergebnisse dieser Codeprüfung.

## 3. Musikalische Eigenschaften konkret definieren

### Informationsmaß als Kern

Die zentrale Frage lautet: Wie viel Information trägt der Rhythmus und wie
verteilt sie sich über Beats und Takte in einem benannten Kontext? Zusätzliche
Onset-Kriterien können die Suche später eingrenzen. Eine höhere
Informationssumme bedeutet nicht automatisch mehr Einsätze oder höhere
Spielschwierigkeit.

### Kernkriterien für den Ausbau der Informationssuche

| Eigenschaft | Vorgeschlagener Vertrag | Musikalische Arbeit |
| --- | --- | --- |
| Informationsgrad | Vorhandener exakter Wert; ergänzend inklusive Bereiche | Material mit gewünschter Information suchen. |
| Deviation | Einstellbarer Bereich der Beat-Informationsstreuung | Bei gleichem Informationsgrad unterschiedliche Verteilungen zulassen. |
| Beat-Profil | Geordnete Information innerhalb eines Takts | Information zeitlich anders verteilen. |
| Taktverlauf | Nach Klärung von S2; geordnete Taktsummen sind eine mögliche Definition | Mehrtaktige Informationsfolgen planen und prüfen. |
| Informationsmaximum | Alle Beat- oder Taktpositionen des Maximums | Rechnerischen Schwerpunkt gezielt platzieren. |

Der Taktverlauf soll das bestehende Maß auf einer weiteren zeitlichen Ebene
nutzbar machen; seine genaue Definition ist offen. Analyse und Suche müssen
die jeweils verwendete Ebene und Berechnungsweise benennen.

### Mögliche spätere Zusatzkriterien

| Eigenschaft | Vorgeschlagener Vertrag | Musikalische Frage |
| --- | --- | --- |
| Onset-Anzahl | Anzahl `x`; inklusiver Ganzzahlbereich | Wie dicht soll der Takt sein? |
| Dichte | `Anzahl x / Schrittanzahl`; zusätzlich absolute Anzahl ausgeben | Wie dicht sind verschieden lange Pattern relativ? |
| Einsätze je Beat | Geordneter Vektor von vier Anzahlen für einen 4/4-Takt | Wo verdichtet sich die Bewegung? |
| Pflicht-/Verbotspositionen | Maske aus `x`, `o`, `?`: Einsatz, frei, beliebig | Muss die Eins frei bleiben? |

CLI-Beatnummern sollten einbasiert sein, Indexfelder für Programme eindeutig
benannt. Eine Maske vermeidet zunächst zusätzliche Positionsnotationen.
Widersprüchliche Filter sind Eingabefehler; eine gültige Suche ohne Treffer
liefert eine leere Liste mit Trefferzahl null und startet kein Playback.

### Profilformen ohne unklare Etiketten

Für `b = [b1, ..., bn]` aus Beat-Informationswerten oder Taktinformationen
folgende Definitionen vorschlagen. Ebene und Kontext werden explizit gewählt;
die Definitionsdetails sind vor Implementierung fachlich zu bestätigen:

- konstant: alle Werte gleich;
- steigend: alle Differenzen `b(i+1)-b(i) >= 0`, mindestens eine positiv;
- fallend: entsprechend `<= 0`, mindestens eine negativ;
- alternierend: mindestens drei Werte; jede Differenz ungleich null und
  aufeinanderfolgende Differenzen mit wechselndem Vorzeichen;
- Gipfel: zunächst ein eindeutiges inneres Maximum, davor nicht fallend,
  danach nicht steigend; Plateaus vorerst kein Treffer dieser Klasse.

Leere Profile sind ungültig; ein einzelner Wert gilt nur als konstant.
Diese Definitionen auf Taktwerte anzuwenden setzt die Klärung von S2 voraus.
Das sind erklärbare Konventionen, keine allgemeingültigen musikalischen
Kategorien. Ein konstantes Profil soll nicht gleichzeitig „steigend“ heißen.
Ein Maximum am Rand zählt als Peak, aber nicht als innerer Gipfel. Bei
mehreren Takten muss die Abfrage sagen, ob die Form für jeden Takt oder die
gesamte Phrase gelten soll. Anfangsaufwand des Huffman-Maßes bleibt sichtbar.

Später könnte ein Zielprofil mit Distanz zusätzliche Formlabels ergänzen:
etwa mittlerer absoluter Abstand zu `[0,1,2,3]`, nur bei gleicher Vektorlänge.
Rohwerte und normalisierte Formen nicht vermischen: Normalisierung würde
absolute Informationshöhe ausblenden und benötigt einen benannten Modus.

## 4. Fehlende Endpunkte und ihre Verträge

Die folgende Syntax ist ein Vorschlag. Bestehende Befehle und Aliasse bleiben
erhalten; neue Hauptbefehle erfordern später gemeinsame Änderungen an
`RootCmd`, Hilfe, Completion, Tests und README.

Die Entwürfe mit `--per-bar`, `--info-per-bar` und `--context` illustrieren
mögliche Folgen der Begriffsarbeit aus S2. Insbesondere die beiden
Kontextmodi sind keine beschlossene Anforderung; ihre Ausgestaltung hängt
von der Definition der Taktinformation ab.

P1 bezeichnet den ersten Ausbau, P2 eine darauf aufbauende Sucherweiterung.
Auch ein P1-Entwurf zur Taktanalyse setzt die fachliche Klärung voraus.
Dateibasierte Auswahlen sind gesondert als optionale Erweiterung markiert;
Presets für gewünschte Informationsverläufe sind hier nicht spezifiziert.

| Priorität | Entwurf | Eingabe und Ergebnis | Verhalten |
| --- | --- | --- | --- |
| P1 | `analyze rhythm ONSETS --details --format json` | Ein Pattern → normalisierte Daten, Profil, Aggregate, Kontext, Version | Ohne DB und Hardware; Text bleibt Standard. |
| P1 | `analyze rhythm ONSETS --per-bar --context isolated` | Mehrtaktiges Pattern → Taktanalysen mit Beat-Profil, Information und Deviation | Jeden Takt ab Ruhe bewerten; `--context continuous` wertet im Phrasenkontext aus. |
| P1 | `play rhythm info 3 5 --deviation-min 0.2 --deviation-max 0.8` | Bestehende Informationsauswahl mit einstellbarer Deviation | Explizite Grenzen ersetzen den festen Filter. |
| P1 | `search rhythms --info 3 --deviation-min 0.2 --deviation-max 0.8 --limit 20` | Informationskriterium → Kandidaten mit Profil und Deviation | Vorhandenes Maß als Suchgrundlage; kein Playback. |
| P2 | `search rhythms --info 3 --beat-profile 1,1,0,1` | Gesamtinformation und exaktes Beat-Profil → Kandidaten | Konkretisiert die neue Suchdimension aus S5. |
| P2 | `search rhythms --info-per-bar 3,5,3,7 --context isolated` | Gewünschter Taktverlauf → Kandidaten je Position | Katalogwerte isoliert verwenden; ein späterer Modus `continuous` muss kontextabhängig suchen. |
| P1 | `play rhythm onsets ONSETS --tempo 90 --repeat 8` | Konkretes Pattern → Vorschau mit vorhandenem Kick-/Snare-Mapping | Ohne DB; endliche Wiederholung ohne Pausen zwischen Takten. |
| Optional | `select rhythms --in candidates.json --count 4 --seed 42 --out selection.json` | Kandidaten → geordnete, wiederlesbare Auswahl | Standard ohne Zurücklegen; zu wenige Kandidaten sind ein Fehler. |
| Optional | `play rhythm selection --in selection.json --tempo 90` | Gespeicherte Auswahl → exakt diese Inhalte in dieser Reihenfolge | Keine erneute Suche; fehlende Daten sind Fehler. |

`search` baut auf Information und parametrisierter Deviation auf. Es folgen
Profilform, Taktverlauf und stabile Sortierung; Onset-Masken und Dichtefilter
sind spätere Ergänzungen. Erweiterungen sollten dieselbe
Suchschnittstelle nutzen, statt für jede musikalische Eigenschaft einen
eigenen Befehl einzuführen.

### Optionale Erweiterung: konkrete Auswahlen als Datei

Eine solche Datei speichert konkrete Pattern. Ein Preset für Suchkriterien
oder einen gewünschten Informationsverlauf wäre ein anderes, zusätzliches
Feature. Beides gehört nicht zum notwendigen Kernablauf.

Eine Auswahl sollte mindestens Formatversion, normalisierte Onsets, Raster,
Reihenfolge und stabile Inhaltskennung enthalten. Ergänzend: Analyseversion,
Kontext, Filter, Seed, Auswahlalgorithmus und optional DB-IDs. Tempo und
Wiederholungsanzahl können als Wiedergabevorschlag gespeichert werden;
explizite CLI-Overrides werden im Wiedergabeprotokoll angezeigt.

Wird eine paginierte Kandidatenliste exportiert, muss sie als Ausschnitt
erkennbar sein. `select --in candidates.json` wählt nur aus diesem Inhalt,
nicht vermeintlich aus allen Treffern. Ein komfortabler direkter
Such-und-Auswahl-Aufruf kann später denselben Use Case kombinieren.

### Musikalische Anwendungen und ein weiterführendes Szenario

**Material eines Informationsgrades untersuchen:** Information 3 vorgeben,
erst kleine, dann größere Deviation zulassen. Beat-Profile der Treffer
vergleichen und die Pattern bei gleichem Tempo hören. Erfolg: Das Maß steuert
die Auswahl; die Streuung ist veränderbar und Treffer bleiben wiederaufrufbar.

**Informationsverteilungen auswählen:** Bei gleicher Gesamtinformation Takte
mit frühem und spätem Beat-Informationsmaximum suchen. Profile ansehen und
die ausgewählten Onsets direkt hören. Erfolg: Der Unterschied ist am Informationsprofil
nachvollziehbar. Ob die Variante als Fill funktioniert, wird musikalisch
erprobt; ein Informationsmaximum ist nicht automatisch höhere Onset-Dichte.

**Eine Phrase entwickeln – vorbehaltlich S2:** Falls isolierte Taktinformation
und eine kontextuelle Auswertung angeboten werden, vier Takte mit Werten 3, 5, 3,
7 auswählen. Je Takt Beat-Profil und Deviation anzeigen, anschließend den
Informationsverlauf im durchgehenden Kontext prüfen. Abweichungen benennen
und bei Bedarf andere Bausteine wählen. Erfolg: Information ist auf Beat-,
Takt- und Phrasenebene gestaltbar; die konkrete Phrase bleibt reproduzierbar.

## 5. Empfohlene Reihenfolge und Grenzen

1. **Informationsmaß auf allen Ebenen zugänglich machen:** Beat-Profile
   veröffentlichen, den Taktinformationsbegriff klären und darauf die
   taktweise Analyse aufbauen. Deviation in der
   bestehenden Informationsauswahl parametrieren.
2. **Nach Information gestalten und Ergebnisse wiederaufrufen:** Kandidaten
   nach Informationsgrad, Deviation und Profil vergleichen; Taktverläufe
   nach Klärung von S2 planen und prüfen. Stabile Sortierung, Ausgabe der
   ausgewählten Onsets und direkten Wiederaufruf ergänzen. Playback mit Tempo und Wiederholungen
   ermöglicht das Hören derselben analysierten Inhalte.
3. **Katalog verlässlich halten:** Duplikate verhindern und gespeicherte
   Analysewerte versionieren. Diese Grundlage ist
   für wiederholbare Auswahl und dauerhaft nachvollziehbare Suchwerte nötig.
   Sie sollte mit dem Ausbau der Auswahl abgestimmt werden, nicht erst
   nachträglich erfolgen.
4. **Nach Anwendungserfahrung:** Zusätzliche Onset-Filter und
   Zielprofil-Distanzen beurteilen.
5. **Positionszahlen und Taktbildung verallgemeinern:** Den kognitiven
   Unterteilungsansatz untersuchen und zuerst einen Bestimmungsalgorithmus
   für Primfaktor-Wahrscheinlichkeiten entwickeln. Das Ziel umfasst jede
   positive ganzzahlige Positionszahl, mit 2, 3 und 5 als vermuteten
   praktischen Schwerpunkten.

Ein erstes sinnvolles Produktinkrement ist erreicht, wenn man nach dem
vorhandenen Informationsmaß mit selbst gewählter Deviation auswählt, die
Information je Beat und je Takt untersuchen kann und die konkrete Auswahl
gezielt hört und wiederaufruft. Reproduzierbarkeit allein erfüllt dieses
Ziel nicht; die taktweise Informationsanalyse gehört ausdrücklich dazu.
Die Klärung ihres Begriffs kann separat erfolgen; sie muss die zugänglichen
Beat-Profile und die parametrisierte Deviation nicht blockieren.
4/4 im 16tel-Raster ist eine vorläufige Arbeitsgrenze, keine dauerhafte
Zielbeschränkung. Bei 32 binären
Schritten hätte vollständige Enumeration
bereits `2^32 = 4.294.967.296` Kombinationen; mehrtaktige Arbeit sollte zunächst
bereits bewertete Takte nach ihrem Informationsverlauf auswählen, statt den
ganzen Raum aufzubauen.

Vor dem späteren Task-Schnitt sind fachlich zu entscheiden:

- Was soll Taktinformation bezeichnen, und welche Rolle spielen isolierte
  und kontextuelle Bewertung? Erst daraus ergeben sich die nötigen
  Analysemodi und ihre öffentliche Schnittstelle.
- Wie wird der bisherige Deviation-Default beim bestehenden Befehl migriert?
- Welche Profilformen und welche kontextabhängigen Taktabfragen werden
  zuerst benötigt?
- Profilformen beziehen sich zunächst auf das Informationsmaß. Ob später
  zusätzliche Onset-Profile benötigt werden, bleibt offen.

Diese Entscheidungen konkretisieren die nächsten To-dos aus PV-1. Sie sind
noch kein angelegtes Projekt und keine verbindlich geschnittenen Tickets.

## 6. Quellen und Aussagegrenzen

Die Analyse basiert auf Produktionscode, den verlinkten Tests, der tatsächlichen
Root-Hilfe und gezielten Diagnoseaufrufen des Huffman-Modells. Ausgeführt:

```bash
mvn -q -Dtest='HuffmanRhythmTest,RhythmTest,RhythmE2ETest,GenerateAndPersistRhythmUseCaseTest,RootCmdRhythmCliTest,SequenceBuilderTest' test
mvn -q exec:java -Dexec.args='--help'
```

Ergebnis der ursprünglichen Codeprüfung: 24 Tests, keine Fehler, keine übersprungenen Tests in diesem
Ausschnitt. Das ist kein realer DB-Integrationstest oder Hörtest. Konkrete
Huffman-Beispiele wurden über JShell mit dem durch Maven gebauten Code
nachgerechnet. Keine Produktionsdateien oder musikalischen Defaults wurden
für diese Analyse verändert. Die vorgeschlagenen Definitionen der Profilformen
bedürfen fachlicher Bestätigung; es wurden keine Performance-Benchmarks oder
empirischen Aussagen über musikalische Wahrnehmung erhoben.

Die anschließende inhaltliche Überarbeitung stellt gemäß Abstimmung das
Informationsmaß, taktweise Analyse und parametrisierte Deviation in den
Vordergrund. Sie verändert nur diesen Arbeitsentwurf; die obigen Testläufe
beziehen sich weiterhin auf die unveränderte Produktionscodebasis.

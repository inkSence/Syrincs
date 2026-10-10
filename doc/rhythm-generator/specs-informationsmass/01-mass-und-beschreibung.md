# 01 – Maß, Beschreibungssprache und zeitliche Referenz

Status und Quellen: [gemeinsamer Rahmen](README.md).
Die Kennungen dienen der Rückverfolgbarkeit in Implementierungsplan und Tests.

## M01 – Was Information bezeichnet

Für eine Ereignisfolge `E`, einen bekannten Rahmen `F`, bekannten Kontext `K`
und eine versionierte Beschreibungssprache `C` gilt:

```text
I_C(E | F, K) = min { Länge(d) : Decoder_C(d, F, K) = E }
```

`Länge` zählt Bits der vereinbarten Beschreibung. Das Minimum gilt
innerhalb der angegebenen Sprache und ihrer Grenzen. Neu benötigte
Regeln, Muster, Unterteilungen, Grenzen und Ausnahmen sind mitzubeschreiben.
Was als bekannter Rahmen oder Kontext ausgeklammert wird, MUSS angegeben
sein und auf beiden Seiten der Rekonstruktion tatsächlich bekannt sein.

Im kontextfreien Modus kann dies als `L(M) + L(E | M)` gelesen werden.
Das direkte Beschreiben von Ereignissen MUSS als Alternative verfügbar sein;
das Maß darf Periodizität nicht erzwingen. Die Restlänge allein ist kein
Gesamtinformationswert. Ebenso ist die gegenüber einer direkten
Beschreibung eingesparte Länge eine andere, zusätzlich ausweisbare Größe.

Eine probabilistische Überraschung `-log₂ P(E | Vergangenheit)` wird hier
nicht definiert. Eine Grammatikregel, ein Codesymbol, eine wahrgenommene
Überraschung und verbrauchte Aufmerksamkeit sind verschiedene Gegenstände.

## M02 – Eingabe, Genauigkeit und Rahmen

Der erste neue Modus beschreibt **Einsatzpositionen** gleichen Ereignistyps.
Auf dem Raster wird `x` auf `1`, `o` auf `0` abgebildet. Ein Nullbit bedeutet
keinen neuen Einsatz; es behauptet keine akustische Stille.

```text
Frame F:
  origin = 0
  slots N = positive Ganzzahl, zunächst 1..128
  window = [0, N) in Rasterpositionen
  slotDurationMs = positive rationale Zahl oder ausdrücklich UNKNOWN
  observation = GRID_EXACT
  notation / segmentBoundaries / metricReferences = optionale Angaben
```

Die binäre Ereignisfolge hat genau `N` Positionen. Whitespace und
Groß-/Kleinschreibung werden normalisiert; andere Zeichen sind Fehler.
Die leere Eingabe ist ungültig. Ein positives Fenster ohne Ereignisse ist
dagegen eine gültige Folge ausschließlich aus Nullbits.

Ein alternativer Eingabeadapter darf explizite Zeitpunkte in dieses Raster
überführen, zunächst jedoch nur bei exakter Darstellbarkeit. Keine implizite
Rundung, Fusion oder Entfernung doppelter Ereignisse. Nicht darstellbare
Zeitpunkte und mehrere Ereignisse in derselben Position sind zu melden.
Mehrstimmige Ereignistypen und eine tolerante Quantisierung sind später
gesondert zu spezifizieren.

`GRID_EXACT` ist eine technische Beobachtungsdefinition, keine Behauptung
einer Wahrnehmungsschwelle. Bei einer späteren toleranten Analyse müssen
Quantisierung, Rekonstruktionsfehler und Ereigniszuordnung angegeben werden.
Ein gröberes Raster darf nicht kostenlos im Minimierungsverfahren gewählt
werden, um Unterschiede verschwinden zu lassen.

### Bekannte und mitzubeschreibende Angaben

Der primäre neue Wert ist **bedingt auf F**. Dadurch darf der Decoder `N`
verwenden. Dies ist legitim, weil der Nutzer das Fenster vorgibt; das
Verfahren schätzt es nicht aus den Einsätzen. Vergleiche müssen diesen
Rahmen nennen. Unterschiedliche Fensterlängen dürfen nicht als reine
Unterschiede des Tempos oder der Komplexität ausgegeben werden.

Ein selbsttragendes Dateiformat muss zusätzlich den Rahmen übertragen.
Dessen Bytezahl ist nicht unser primärer Informationswert; ein neues
numerisches Maß einschließlich Rahmenkodierung benötigt einen eigenen
Vertrag. Unbekannte, vom Verfahren gewählte Musterperioden und
Abschnittsgrenzen gehören dagegen bereits im ersten Modus in die Kosten.

## M03 – Kompatibler Modus `legacy-fsm-v1`

Quelle ist
[HuffmanRhythm](../../../src/main/java/syrincs/a_domain/rhythm/HuffmanRhythm.java),
zusammen mit
[Rhythm](../../../src/main/java/syrincs/a_domain/rhythm/Rhythm.java) und
[den Tests](../../../src/test/java/syrincs/a_domain/rhythm/HuffmanRhythmTest.java).

- Vier Positionen pro vorgegebener Analyse-Zählzeit, öffentliche Analyse 4/4.
- Start in Ruhe; Playing-Zustand über Beat- und Taktgrenzen fortführen.
- Unterteilungszustand pro Beat als Quarter initialisieren.
- Information als erzeugte Codesymbolanzahl, nicht als neu spezifizierte
  Bitlänge einer vollständigen Nachricht.
- `Deviation` als Populationsstandardabweichung dieser Beat-Zahlen.
- Tempo, Klang und tatsächliche Hörvorgeschichte verändern den Wert nicht.

Dieser Modus MUSS unverändert bleiben. Die Zählung wird nicht mit Faktor 2
in einen Wert von `description-v1` umgerechnet. Dass die internen
Symbolnamen je zwei Zeichen haben, beweist keinen gleichwertigen
vollständigen Bitcode. Ein neuer Modus wird durch eigene Typen und
Ergebnisse ergänzt, nicht durch fachlich veränderte Methoden in der
bestehenden Klasse versteckt.

## M04 – Endliche Sprache `description-v1`

Die folgende Sprache ist die konkrete erste Vergleichsgrundlage. Sie
beschreibt zeitliche Regelmäßigkeit auf beliebigen positiven Rasterlängen
innerhalb M02. Sie bezeichnet ein wiederholtes Motiv **nicht automatisch
als Beat oder Metrum**.

Für jeden nichtleeren Abschnitt ist seine Länge `n` dem Decoder bekannt:
am Anfang aus F, innerhalb einer Teilung aus der beschriebenen Grenze.

```text
w(a) = ceil(log₂ a), für a >= 1; insbesondere w(1) = 0
u_a(v) = v als genau w(a) Bits, 0 <= v < a
```

Überzählige Bitkombinationen, falls `a` keine Zweierpotenz ist, sind
ungültig. Bitfelder werden höchstwertiges Bit zuerst gelesen.

| Tag | Beschreibung | Weitere Felder / Rekonstruktion | Länge |
| --- | --- | --- | --- |
| `000` | EMPTY | Liefert `n` Nullbits. | `3` |
| `001` | FULL | Liefert `n` Einsbits. | `3` |
| `010` | LITERAL | Die folgenden `n` Bits sind das Ereignismuster. | `3 + n` |
| `011` | REPEAT | `p` in `1..floor(n/2)` als `u_floor(n/2)(p−1)`; danach eine rekursive Beschreibung der ersten `p` Bits. Dieses Motiv wiederholen und auf `n` Positionen abschneiden. | `3 + w(floor(n/2)) + D(Motiv)` |
| `100` | CONCAT | Grenze `k` in `1..n−1` als `u_(n−1)(k−1)`; danach Beschreibung von links mit Länge `k` und rechts mit Länge `n−k`. | `3 + w(n−1) + D(links) + D(rechts)` |
| `101` | PATCH | Periode `p` wie bei REPEAT; `p` rohe Motivbits; Anzahl `q` in `0..n` als `u_(n+1)(q)`; dann `q` strikt aufsteigende Positionsindizes, jeweils `u_n(i)`. Das periodische Motiv auf Länge `n` ausgeben und an diesen Positionen das Bit umkehren. | `3 + w(floor(n/2)) + p + w(n+1) + q·w(n)` |
| `110`, `111` | reserviert | Fehler; kein stilles Interpretieren. | — |

REPEAT und PATCH sind für `n=1` unzulässig; CONCAT ebenfalls. EMPTY und
FULL sind nur Beschreibungen der jeweils passenden tatsächlichen Folge.
REPEAT verlangt mindestens zwei vollständige Motivlängen. Das ist eine
Regel der Beschreibungssprache, kein Befund über minimale Hörzeit.
Eine abgeschnittene weitere Wiederholung wird aus dem bekannten Fenster
bestimmt und nicht als weiterer freier Parameter behandelt.

PATCH erlaubt Hinzufügen und Entfernen von Einsätzen gleichermaßen.
Jeder Index zählt mit; die direkte Beschreibung bleibt eine Alternative.
Faktoren 5 und 7 sind ebenso zulässig wie 2 und 3, wenn sie in die Grenze
passen. Keine kostenfreie Stilvorlage, metrische Gewichtung oder Belohnung
bestimmter Primfaktoren wird hinzugefügt.

### Kosten und Rekonstruktion

Alle Tags haben absichtlich gleiche feste Länge. Dies erleichtert die
erste überprüfbare Definition, ist aber keine universell optimale Wahl
der Beschreibungssprache. Änderung von Tags, Feldern, Preisen oder
zulässigen Konstruktoren erzeugt eine neue Maßversion.

Für bekanntes n ist die Sprache selbstabgrenzend: Tags und bekannte
Feldlängen bestimmen das Ende einer Beschreibung, auch bei rekursiven
Kindern. Ein vollständiger Decoder MUSS fehlende Bits, ungültige Feldwerte,
unzulässige Konstruktoren, unsortierte oder doppelte PATCH-Indizes und
zusätzliche Bits nach der Wurzel als Fehler behandeln. Ein abgebrochener
Bitstring darf nicht durch Nullen ergänzt werden.

Der vollständige Syntaxbaum und der daraus erzeugte Bitstring sind
Rekonstruktionsbelege. Der Decoder darf weder die ursprünglichen Onsets
noch das Ergebnis einer externen Analyse benötigen. Seine Rekursion
terminiert: REPEAT verkleinert auf `p <= n/2`, CONCAT auf zwei kleinere
Abschnitte, PATCH besitzt keine rekursive Basisbeschreibung.

### Modell- und Restkosten erklären

Der Gesamtlänge untergeordnet werden folgende Kosten ausgewiesen:

| Form | Modellkosten | Restkosten |
| --- | --- | --- |
| EMPTY / FULL | Tag `3` | `0` |
| LITERAL | Tag `3` | die `n` Datenbits |
| REPEAT | Tag, Periodenfeld und **gesamte** Motivbeschreibung | `0`; das Motiv ist die eingerichtete Regel |
| CONCAT | Tag, Grenze und Modellkosten beider Kinder | Restkosten beider Kinder |
| PATCH | Tag, Periode und Motivbits | Ausnahmeanzahl und Positionsliste |

Modellkosten plus Restkosten MUSS die ausgegebene Gesamtlänge ergeben.
Ein Motiv darf nicht zugleich als kostenlos bekannt und als vom System
neu aus der Zielphrase gewählt behandelt werden.

## M05 – Minimum, Gleichstände und Nachweis

Für eine Folge `S` ist `D(S)` das Minimum aller gültigen Beschreibungen
aus M04. Der erste Analyzer MUSS innerhalb seiner Längengrenze exakt
sein. Eine schnelle Heuristik darf denselben Namen nicht mit bloßem
Wunsch nach Minimalität verwenden.

Eine überprüfbare Berechnung betrachtet:

1. EMPTY/FULL, soweit passend, sowie immer LITERAL;
2. alle erlaubten exakten REPEAT-Perioden und deren minimale Motivkosten;
3. alle CONCAT-Grenzen mit den minimalen Kinderkosten;
4. jede PATCH-Periode und die günstigsten Motivbits samt allen Ausnahmen.

Bei PATCH kann die beste Vorlage für eine Periode durch Mehrheitswahl
je Restklasse `i mod p` gefunden werden: Die Länge der Vorlage und des
Anzahlfelds ist konstant, jede Ausnahme kostet dieselbe Anzahl Bits.
Gleichstände einer Restklasse bleiben alternative minimale Vorlagen.
Dieser Schritt darf nicht zur Wahl des gehörten Beats umgedeutet werden.

Dynamische Programmierung über Abschnitte liefert ein endliches Minimum.
Der Nachweis beruht auf vollständiger Betrachtung der sechs Konstruktoren
und Induktion über kleinere Kinderlängen. Optimierungen dürfen nur
nachweislich dominierte oder unmögliche Kandidaten entfernen.

Bei gleichen Kosten wird als kanonischer Beleg der lexikografisch kleinste
vollständige Bitstring gewählt (`0 < 1`). Weitere minimale Beschreibungen
werden als Entscheidungsgraph, Zahl und begrenzte aufklappbare Liste
darstellbar. Ein Anzeigelimit ändert weder das Minimum noch den Nachweis;
es darf keine Behauptung einer einzigen Lösung erzeugen. Unterschiedliche
Codes für dieselbe Folge sind außerdem nicht automatisch unterschiedliche
gehörte Metren.

Für jedes Ergebnis gilt `3 <= D(S) <= n+3`. Eine Teilberechnung mit Budget
liefert höchstens eine obere Schranke mit `optimality=UPPER_BOUND`.
Sie darf nicht in einen Generatorfilter eingehen, der einen exakten
Informationsgrad verspricht. Ein Ressourcenabbruch ist kein leerer Rhythmus.

## M06 – Bekannter Kontext und kausale Fortsetzung

Für lokale Gestaltung wird zusätzlich eine **Fortsetzungsbeschreibung**
definiert. Sie hat einen eigenen Geltungsbereich und ist kein
nachträgliches Stück der globalen Minimierung.

Vor jedem Analysefenster sind die vorangegangenen, tatsächlich bekannten
Onsets `H` verfügbar. Die Politik `suffix-periods-16-v1` bildet daraus K:

H liegt auf demselben Raster und endet unmittelbar vor dem Zielabschnitt.
Bekannte Rasterdauern müssen übereinstimmen; eine geänderte Zeitbasis
verlangt eine ausdrückliche exakte Transformation oder einen Kontextreset.
Für K reichen die letzten 32 Positionen von H. Längere Vorgeschichte darf
dokumentiert bleiben; sie verändert diese konkrete Politik nicht.

- Für `p=1..min(16, floor(length(H)/2))` die letzten zwei Abschnitte der
  Länge `p` vergleichen.
- Nur identische Abschnitte als Kandidat nehmen; das letzte Motiv `W`
  sagt die unmittelbar folgende Fortsetzung voraus.
- Ein Motiv mit kleinerer exakter Teilerperiode entfernen. Dafür alle
  echten Teiler `r` von `p` prüfen, ob `W[i]=W[i mod r]` gilt.
- Nach Motivlänge und binärem Inhalt sortieren und identische Motive
  deduplizieren. Das ergibt `m` für Decoder und Encoder bekannte Vorlagen.

Die Vorhersage der Vorlage j ist `P_j[i] = W_j[i mod p_j]`, beginnend
direkt nach H. Es gibt keinen aus dem Zielabschnitt erschlossenen
kostenlosen Phasenparameter. Bei einer zeitlichen Lücke muss diese als
Nullpositionen beobachtet worden sein oder Kontext ausdrücklich verworfen
werden; das System darf unbekannte Zeit nicht still als Stille behandeln.

```text
DIRECT:        0  | Beschreibung_M04(S)
REUSE_CONFIRM: 10 | u_m(j), falls m > 0 und S = P_j
REUSE_CHANGE:  11 | u_m(j) | Beschreibung_M04(S XOR P_j), falls m > 0

J(S | K) = min(1 + D(S),
               min_j mit S=P_j [2 + w(m)],
               min_j [2 + w(m) + D(S XOR P_j)])
```

Die Kontextauswahl zählt mit. Der Decoder bildet dieselbe Vorhersage und
gibt sie bei REUSE_CONFIRM unverändert aus; bei REUSE_CHANGE wendet er
die dekodierten XOR-Abweichungen an. Ein leerer Kontext lässt nur DIRECT
zu. Dessen Header bleibt auch dann Teil des Vertrags. Die drei Tags
`0`, `10`, `11` sind eindeutig lesbar; nach REUSE_CONFIRM ist die Nachricht
bereits vollständig. Gleichstände folgen erneut dem kanonischen Bitstring.

Für die Kostenerklärung zählt bei DIRECT der Header zu den Modellkosten;
die Aufteilung des Kindes bleibt wie M04. Bei den beiden REUSE-Formen
zählen Header und Vorlagenindex zu den Modellkosten. REUSE_CONFIRM hat
Restkosten null; bei REUSE_CHANGE zählt die gesamte Beschreibung der
XOR-Abweichung zu den Restkosten. In jedem Fall ergibt ihre Summe J.
Der Beleg enthält den Kontextschlüssel und die verwendete Vorlage;
die Liste bekannter Vorlagen wird nicht nochmals als neue Nachricht
berechnet. Ohne denselben bekannten Kontext ist der Code nicht decodierbar.

Eine neue Unterteilung wird nicht pro Beat auf Quarter zurückgesetzt.
Nach ausreichender tatsächlicher Wiederholung kann sie im folgenden
Fenster als bekannte Vorlage verfügbar werden. Die konkrete Erkennungsregel
oben ist eine technische Politik und keine Schätzung, nach wie vielen
Schlägen Menschen diese Referenz etablieren.

Die Bestätigung einer bekannten Fortsetzung benötigt keinen neuen
vollständigen Musteraufbau. Diese Unterscheidung setzt den Aufbau–
Fortführungs-Gedanken konkret um. Auch die Bestätigung hat ein Codewort:
ein bedingter, selbstabgrenzender Code darf für eine mögliche Alternative
nicht still die leere Nachricht benutzen. Seine zwei Tagbits sind unsere
technische Entscheidung, kein gemessener Aufmerksamkeitsverbrauch.

**Wichtig:** J ist kausal hinsichtlich der Kontextwahl, aber analysiert den
vollständigen aktuellen Abschnitt als Block. Es ist keine Messung der
Überraschung jedes gerade eintreffenden Onsets. Bei DIRECT darf ein neuer,
einfacherer Abschnitt gewonnen werden; eine Auslassung muss daher nicht
immer einen höheren J-Wert erzeugen. Echte probabilistische
Überraschungswerte oder psychologisch begründete Wechselkosten gehören
zu einer späteren, gesondert spezifizierten Version.

Für bekannte Vorläufe wird deren eigene Beschreibung getrennt ausgewiesen,
falls sie Teil der zu analysierenden Komposition sind. Ein ausdrücklich
vorbereitetes Hörbeispiel kann K als gemeinsame Bedingung setzen; sein
Vorbereitungsaufwand wird dadurch nicht als null behauptet.

## M07 – Tempo, Metrum und Hierarchie

Die strukturelle Beschreibung und die metrische Interpretation werden
zusammen ausgegeben, aber nicht zu einer erfundenen skalaren Mischgröße
addiert. `description-v1` bleibt bei gleicher Bitfolge und gleichem Kontext
tempo-invariant. Tempo verändert jedoch die absoluten Zeitabstände und
gegebenenfalls die Plausibilität einer benannten Referenz.

Notation kann den Rahmen aus folgenden rationalen Angaben bestimmen:

```text
quarterBpm = positive rationale Zahl
ticksPerQuarter r = positive Ganzzahl
numerator, denominator = positive Ganzzahlen
slotDurationMs = 60000 / (quarterBpm * r)
barSlots = numerator * 4 * r / denominator, muss ganzzahlig sein
```

Notierter Nenner, tatsächlich vorgeschlagener Tactus und technische
Rasterdichte sind verschiedene Angaben. Beispiel 6/8 mit `r=4`:
ein Takt hat 12 Positionen; zwei punktierte Viertel hätten je sechs
Positionen. Vier Positionen pro Beat sind keine allgemeine Modellregel.

Eine benannte metrische Referenz enthält:

- positive Zykluslänge und Phase in Rasterpositionen;
- geordnete Positionen der Beat-Ebene, mindestens zwei pro Zyklus;
- geordnete aktive Ebenen und deren Positionen im gemeinsamen Zyklus;
- Kennzeichnung `SUPPLIED`, `CANDIDATE` oder `ABSENT`;
- Quelle beziehungsweise Begründung des Kandidaten und Zeitangaben.

Die technische Rasterauflösung ist nicht automatisch die schnellste
aktive Ebene. Beat-Abstände können gleich oder ungleich sein. Übergeordnete
Gruppen werden nicht als Anzahl von Unterteilungen gespeichert.
Ohne benannte Referenz spricht die Ausgabe von Positionen und Fenstern;
sie behauptet keine gehörten Beats.

Ein Referenzpunkt an relativer Position z erscheint absolut bei
`phase + z + k·cycleSlots`, k ganzzahlig, soweit er im Beobachtungsfenster
liegt. Phase darf nicht kostenlos verschoben werden, um Onsets zu treffen.
Vorgegebene Referenzen können über das beobachtete Fenster hinausreichen;
dadurch wird ihre tatsächliche Etablierung nicht als beobachtet behauptet.

Der erste Stand MUSS vorgegebene und explizit angefragte Referenzen
darstellen und vergleichen können. Eine automatische freie Beat-Wahl
ist kein versteckter Default. Werden später Kandidaten automatisch erzeugt,
müssen Kandidatenraum, Budget und Mehrdeutigkeit angegeben werden. Das
kürzeste Wiederholungsmotiv aus M04 genügt dafür nicht.

Eine bereitgestellte Referenz ist eine Interpretationsbedingung, kein
kostenlos aus der Zielphrase gewonnenes Kompressionsmodell. M04 minimiert
bereits über bezahlte Motive und Grenzen; ein Referenzvergleich daneben
ändert D nicht. Soll später ein automatisch gewähltes Metrum zur
Rekonstruktion verwendet werden, müssen dessen Kandidatenindex, Phase,
Ebenen und Ausnahmen in einer erweiterten Sprache mitbeschrieben werden.
Unterschiedliche kostenlos vorausgesetzte Referenzen dürfen nicht als
solche neue Modellkosten miteinander verrechnet werden.

### Benanntes zeitliches London-Profil

Jede Referenz hat im ersten Stand folgende strukturelle Gültigkeitsregeln:
Zykluslänge positive ganze Zahl; Phase in `0..cycleSlots−1`; Positionen
je Ebene eindeutig, aufsteigend und im Zyklus; Beat-Ebene vorhanden;
Positionen jeder gröberen aktiven Ebene Teilmenge der unmittelbar feineren.
Eine explizite einmalige Zyklusmarke darf eine Position haben, die Beat-
und Unterteilungsebenen mindestens zwei. Das stellt eine deklarierte
verschachtelte Hierarchie dar; es beweist nicht ihre Hörbarkeit.

`london-time-window-v1` prüft optional **nur zeitliche Bedingungen** dieser
vorgegebenen Referenz. Aus Kapitel 5/8 werden die ungefähren Modellbereiche
für diese technische, versionierte Prüfung wie folgt operationalisiert:

- jeder zyklische Abstand der schnellsten aktiven Ebene ist `>= 100 ms`;
- jeder zyklische Beat-Abstand liegt einschließlich der Grenzen in
  `[400,1200] ms`;
- die vollständige Zyklusdauer ist `<= 5000 ms`.

Zum zyklischen Abstand gehört auch der Weg vom letzten Punkt bis zum
ersten Punkt des nächsten Zyklus. Rationale Dauern werden exakt verglichen.
Die geschlossenen Grenzen sind unsere Festlegung für reproduzierbare Tests,
keine universellen menschlichen Hörgrenzen. Gleichmäßigkeit der Beats wird
hier nicht vorausgesetzt. Konkrete Zeitabstände und verletzte Bedingungen
werden mit `PASS` oder `FAIL` ausgegeben. Fehlende Referenz oder Zeitangabe
ergibt `NOT_EVALUABLE` mit Grund, keine bestandene Prüfung.

Die 5-s-Angabe begrenzt einen Referenzzyklus, nicht die Länge einer ganzen
Phrase. Ein Nutzer darf `PASS` als Generierungsbedingung wählen;
ohne diese Wahl werden beschreibbare Folgen außerhalb des Profils nicht
abgewiesen. Das Profil ist ausdrücklich **keine vollständige Prüfung aller
London-WFCs**. Die weiteren Beziehungen für nichtisochrone Hierarchien,
einschließlich maximal evenness, benötigen später einen eigenen formalen
Vertrag. Aus ihnen werden hier keine unbestimmten Pflichtfilter abgeleitet.

Erfahrungsabhängigkeit, Ausdruck, Klangdauer und Bewegung bleiben als
Hörbedingungen dokumentierbar. Sie erzeugen im ersten Maß keine
ungeprüften zusätzlichen Gewichte. Der Kontextmechanismus aus M06 ist
eine reproduzierbare strukturelle Annäherung, kein implementierter
Nachweis der Many Meters Hypothesis.

# Fachlicher Vertrag und Entscheidungsvorschläge

Status und Herkunft: [README](README.md). Kennungen in Klammern verweisen
auf den übergebenen Entwurf. Sämtliche nachfolgend als Vorschlag benannten
Regeln sind bis zur ausdrücklichen Entscheidung unverbindlich.

## 1. Ziel und bekannte Bedingungen

### Leitsätze

**Festgelegt, Präzisierung des Projektinhabers vom 13. November 2026:**

1. **Der Informationswert gilt für ein endliches Beispiel als Ganzes.**
   Er ist das Minimum der Kosten über alle zulässigen Anweisungsfolgen
   dieses Beispiels unter seiner benannten Klangbedingung. Nur endliche
   Beispiele besitzen einen berechenbaren Wert.
2. **Eine Fortsetzung ergibt ein neues Beispiel mit eigenem Wert.** Hinter
   jedem Beispiel kann ein weiterer Takt stehen, der den Wert und die
   günstigste Deutung der früheren Takte ändert. Der Wert eines Beispiels
   ist deshalb keine Summe feststehender Taktwerte.
3. **Vagheit innerhalb eines Takts ist gewollt.** Solange mehrere
   Deutungen zulässig sind, bleiben sie nebeneinander bestehen. Erst
   weitere Information „aus der Zukunft“ erlaubt es, den Wert klar zu
   benennen. Die offene Deutung ist Teil des Ergebnisses, kein Fehler.
4. **Die Klangbedingung hängt von der Ausführung ab.** Auf einer Orgel ist
   ein Tonende gut hörbar, auf einer Trommel nur der Impuls des Anfangs.
   Derselbe notierte Rhythmus kann daher unter I und G verschiedene Werte
   haben; ohne benannte Klangbedingung gibt es keinen Wert.

**Rechenbeispiel nach der Übergangstabelle in Abschnitt 2:** F05.2 unter I
kostet allein 4 Bit (`10 11`). Folgt ein Takt mit nur einem Einsatz bei 4,
kostet das zweitaktige Beispiel 6 Bit über `10 01 01`; der Pausenpfad
`10 11 10 11` kostet 8. Der zweite Takt ändert also rückblickend die
günstigste Deutung des ersten von „Pausen spielen“ zu zweimaligem
Verringern der Teilung.

**Folgerung unter Profil K, kein Beschluss:** Eine Verlängerung kann den
Wert nicht senken, weil jede zulässige Anweisungsfolge des längeren
Beispiels eine zulässige Folge des kürzeren enthält.

Die folgenden Abschnitte sind im Sinn dieser Leitsätze zu lesen. Aussagen
über das Erhalten bereits entstandener Information gelten **innerhalb
einer Anweisungsfolge**, nicht zwischen einem Beispiel und seiner
Verlängerung.

**Festgelegt (Z01–Z03):** Beschrieben werden soll rhythmisches Hören;
praktisches Hauptziel ist Musikgenerierung. Ein bereits etablierter Beat
bzw. ein Metrum ist gegeben, seine Erkennung ist nicht Teil des Kerns.
Regeln wirken fort; Änderungen und ausbleibende erwartete Einsätze tragen
Information. Spätere Ereignisse dürfen bereits entstandene Information
nicht einfach löschen. Praktische Erprobung benötigt keine vorangehende
eigene empirische Studie.

**Folgerung:** Der feste metrische Bezug und der veränderliche Spielabstand
sind zu unterscheiden. Eine Änderung der Ereignisrate darf nicht unbemerkt
den vorausgesetzten Beat oder die Taktgrenzen ändern. Das entspricht auch
der Unterscheidung von Oberfläche und metrischer Fortsetzung bei London.

**Festgelegt (E01–E04, H01–H04):** Symbolische Musik ist zulässig. Unterschiedliche
Notation allein begründet keine unterschiedlichen Werte. Hörbar gleiche
Realisierungen erhalten die sparsamere Interpretation. Ein tatsächlich
hörbares Tonende darf dabei nicht entfernt werden. Für ein kurz ausklingendes
Instrument können Halbe und Viertel plus Viertelpause äquivalent sein;
für einen gehaltenen Orgelton mit hörbarer Unterbrechung sind sie es nicht.
**Festgelegt – verschiedene Verläufe dürfen gleiche Summen haben:** Halbe
und Viertel mit hörbarer Pause bleiben durch ihre Ereignisse und durch
`10 01` bzw. `10 11` unterscheidbar. Beide können je 4 Bit kosten.
Hörbare Verschiedenheit verlangt nicht automatisch verschiedene Summen.
Diese Entscheidung ersetzt die frühere Forderung nach strikt höherem
Informationswert der hörbaren Unterbrechung; deren Erhaltung bleibt Pflicht.

**Erforderliche Ausdrucksfähigkeit (E02 als Folgerung):** Einsätze, Dauern,
Pausen, Bindungen ohne Neuanschlag, Taktgrenzen, Bezugspuls und Phase,
binäre und ternäre Teilung sowie relevante Tempo- und Klangbedingungen.
Notation, klingende Realisierung und Modellinterpretation werden getrennt
aufbewahrt. Ein `o` allein kann diese Unterschiede nicht repräsentieren.

**Festgelegt – Vorrang und erste Klangfälle:** Die handschriftlichen
Bitwerte sind fachliche Ausgangsbeispiele. Bei einem Konflikt hat die
informationsgünstigste hörbar gleichwertige Interpretation Vorrang. Die
Fotozahl darf weder zusätzliche Anweisungen erzwingen noch die Entfernung
eines hörbaren Tonendes rechtfertigen. Notierte Pausengliederungen begründen
für sich **keine Teilungsanweisungen**, auch keine spätere Rücknahme einer
nur aufgrund der Pausennotation eingeführten Teilung.

Jeder Prüffall muss seine Klangbedingungen benennen. Für die erste Fassung
werden ausschließlich die beiden idealisierten Fälle in der folgenden
Tabelle ausgearbeitet; weitere Artikulationsfälle folgen später.

**Vorschlag V01, verbleibender Umfang:** Erste Versuche sind einstimmig,
mit gleichem Ereignistyp. Mehrstimmigkeit und eine Zusammenfassung mehrerer
Stimmen benötigen eine eigene Entscheidung.

| Annahme | Was als Evidenz verwendet werden darf |
| --- | --- |
| **I – Kurzer Impulsklang** | Jeder Anschlag erzeugt einen gleichen kurzen Impuls. Notierte Dauer und Pausengliederung verändern seine Hüllkurve nicht. Natürliches Ausklingen erzeugt keinen automatischen Pausenbefehl; die für die Bewertung variablen Ereignisse sind die Onsets. |
| **G – Gehaltener Klang mit hörbarer Abschaltung** | Der Klang hält bis zur notierten Abschaltung an; das Tonende ist hörbar und muss erhalten bleiben. Bindung verlängert ohne neuen Einsatz. Eine rein grafische Aufteilung anschließender Stille erzeugt kein weiteres Klangende. |

Dies sind Prüfbedingungen, keine akustisch universellen Instrumentklassen.
Ein gedämpfter Schlag kann ein wirksames Tonende besitzen und liegt dann
außerhalb des ersten Impulsklangfalls. Variabler Release, Hall, Dämpfung
und andere Artikulationen werden hier nicht mit zusätzlichen Schwellen
modelliert. Beim idealisierten Impulsklang darf das Fehlen einer notierten
Abschaltung nicht wie das Weiterhalten eines Orgeltons ausgewertet werden.
Bei fehlender Annahme: vorgeschlagen ist ein unvollständiger Auftrag ohne
eindeutigen Zahlenwert oder ausdrücklich getrennte Szenarien. Kein Default
„Bongo“ oder „Orgel“ und keine erfundene allgemeine Abschaltschwelle.

**Offen:** Ob Note-Off einen Pausenzustand immer sofort auslöst; welche
Tonlänge beim Noten-Spielzustand erwartet wird; wie ein erneuter Anschlag
bei weiterhin klingendem Ton behandelt wird. Diese Regeln sind für das
Orgelbeispiel entscheidend, auch wenn ein Format Dauern vollständig liefert.

Die [F05-Rechnungen](02-bildpruefung-und-ereignisablaeufe.md#3-schrittweise-rechnung-f051f057-unter-i-und-g)
konkretisieren dies als **Rechenprofil K mit noch unbestätigten Teilen**:
P erhält darin Teilung/Phase, erwartet aber keine neuen Töne; G
erwartet zunächst ein Ende nach Δ und erlaubt reguläre Reartikulation
ohne `11 10`. Die Ankerregel ist seit dem 13. November 2026 festgelegt
(Abschnitt 2), ebenso der Pausenzustand (K-P); die offenen Teile von K-G
bleiben Rechenannahmen.
Anfangspause und Analysegrenze sind inzwischen entschieden: `11` am
Anfang bezahlen; keine zusätzliche Anweisung allein an der Fenstergrenze.

## 2. Wörterbuch und Zustand

**Festgelegt (W01–W05):** Zwei musikalische Zustandskomponenten wirken fort:
Noten/Pausen spielen und zeitlicher Abstand Δ. Anfangsabstand ist relativ
zum vorausgesetzten Beat bestimmt, in den Beispielen eine Viertel.
Andere Beat-Werte müssen möglich sein. Erster Einsatz kostet immer `10`.

| Code | Einheitliche Bezeichnung | Wirkung | Kosten |
| --- | --- | --- | ---: |
| `00` | Teilung verstärken | Δ → Δ/2 | 2 Bit |
| `01` | Teilung verringern | Δ → 2Δ | 2 Bit |
| `10` | Noten spielen | Noten-Spielzustand aktivieren | 2 Bit |
| `11` | Pausen spielen | Pausen-Spielzustand aktivieren | 2 Bit |

Vier Viertel benötigen unter Viertelabstand nur einmal `10`. Zwei
Verstärkungen sind zwei Anweisungen: Halbe → Viertel → Achtel kostet 4 Bit.
Der Playing-Zustand des Bestands ist keine fertige Definition dieses Zustands.

**Vorschlag V02:** Für die fachliche Erklärung wird ein Zustand als
`(Spielmodus, Δ, Anker A, nächste Erwartung e, Wörterbuch W)` dargestellt.
Der konstante metrische Rahmen steht daneben. Ein noch nicht initialisierter
Spielmodus ist von „Pausen spielen“ zu unterscheiden. Dies legt keine
Java-Typen oder dritte musikalische Zustandsebene fest.

**Vorschlag V03, nur für die Beispielrechnung:** Δ ist eine positive rationale
Anzahl gegebener Beats. Erwartungen liegen auf `A + k·Δ`. Nach einer
Änderung wird nur die gegenwärtige/neue Zukunft geprüft. Eine Verfeinerung
erfindet keine nachträglich ausgebliebenen Einsätze an neu eingeführten
früheren Rasterpunkten. Eine Erkennung bei Zeit r darf sich auf eine frühere
musikalische Stelle b beziehen, verändert aber nicht das damals verfügbare
Wissen. **Inzwischen bestätigt für den Punktierungsfall:** Der erste
Einsatz bei 0 bleibt Anker. Nach `01` gilt Δ=2 und die nächste Erwartung
liegt bei 2. Der Einsatz bei 3/2 führt durch `00 00` zu Δ=1/2, ohne den
Anker zu verschieben, solange dieser Einsatz eingeordnet wird.

**Festgelegt – Ankerregel, Entscheidung des Projektinhabers vom
13. November 2026:**

1. Das Raster der Erwartungen zählt immer vom **zuletzt gespielten
   Einsatz**: Erwartet wird bei `A + k·Δ` mit `k ≥ 1`. Vor dem ersten
   Einsatz eines Takts ist A der Taktanfang.
2. Ein **Taktwechsel setzt den Anker auf den Taktanfang** zurück. Abstand Δ
   und Spielmodus laufen unverändert weiter; es entstehen dadurch keine
   Anweisung und keine Kosten.
3. Gilt am Taktanfang „Noten spielen“, ist die **Eins selbst ein
   erwarteter Einsatz**. Ein über den Taktstrich fortgesetzter verschobener
   Puls bezahlt den ausbleibenden Einsatz auf der Eins daher in jedem Takt
   neu.
4. Ein Einsatz wird gegen das Raster des bis dahin geltenden Ankers
   eingeordnet; erst danach wird er selbst zum Anker. Der bestätigte
   Punktierungsfall ist ein Spezialfall dieser Regel: Bis zum zweiten
   Einsatz ist der erste zugleich der zuletzt gespielte.

**Folge:** Innerhalb eines Takts folgt das Raster dem tatsächlich
Gespielten; am Taktstrich setzt sich der bekannte metrische Bezug wieder
durch. Der frühere Vorschlag, das erste Gitter eines Takts beizubehalten
(A=0), ist abgelöst. Die günstigsten Werte der bisher gerechneten
Beispiele ändern sich dadurch nicht; einzelne teurere Alternativpfade
schon (F05.6.I, Pfad `10 01`).

**Festgelegt – Schrittweite der Teilungswechsel, 13. November 2026:**
Mehrfaches Verstärken am selben Ereignis ist erlaubt und wird je Schritt
bezahlt (Viertel → Sechzehntel: `00 00`, 4 Bit). Ein einzelnes Ausbleiben
verringert die Teilung dagegen höchstens um **eine** Stufe (`01`, Δ → 2Δ).
Weiteres Verringern braucht ein weiteres Ausbleiben. Die Zuordnung der
Codes bleibt wie in der Tabelle oben; verlangt ist nur ihre Eindeutigkeit.

**Festgelegt – Pausenzustand, Entscheidung des Projektinhabers vom
13. November 2026:**

1. Der Abstand Δ bleibt während „Pausen spielen“ erhalten.
2. Der Anker bleibt der letzte Einsatz vor der Pause; das Raster läuft
   stumm weiter.
3. Stille kostet nichts, unabhängig von ihrer Länge. Es gibt kein
   wiederholtes `11`.
4. Ein Einsatz auf dem stummen Raster kostet nur `10`. Liegt er daneben,
   wird zuerst so oft wie nötig verstärkt (`00`), dann folgt `10`.

### Übergangstabelle für den Kern (Impulsklang, exaktes Raster, binäre Teilung)

**Festgelegt als Zusammenfassung der Entscheidungen vom 13. November 2026.**
Zustand ist Spielmodus (N = Noten, P = Pausen spielen), Abstand Δ und
Anker A. Gehaltene Klänge (G) und Triolen sind hier nicht erfasst.

| Nr. | Zustand | Ereignis | Anweisung | Folgezustand | Kosten |
| --- | --- | --- | --- | --- | ---: |
| 1 | Beginn des Beispiels | Einsatz auf der Eins | `10` | N, Δ = Beat, A = Taktanfang | 2 |
| 2 | Beginn des Beispiels | kein Einsatz auf der Eins | `11` | P, Δ = Beat, A = Taktanfang | 2 |
| 3 | N | Einsatz am erwarteten Punkt | keine | A = dieser Einsatz | 0 |
| 4 | N | Einsatz vor dem erwarteten Punkt | `00`, so oft, bis der Einsatz auf `A + k·Δ` liegt | Δ entsprechend kleiner, A = dieser Einsatz | 2 je Schritt |
| 5a | N | erwarteter Einsatz bleibt aus | `11` | P, Δ und A unverändert | 2 |
| 5b | N | erwarteter Einsatz bleibt aus | `01` | N, Δ verdoppelt, nächste Erwartung `A + Δ` mit dem neuen Δ | 2 |
| 6 | P | kein Einsatz | keine | unverändert | 0 |
| 7 | P | Einsatz auf dem stummen Raster | `10` | N, A = dieser Einsatz | 2 |
| 8 | P | Einsatz neben dem stummen Raster | `00` so oft wie nötig, dann `10` | N, Δ kleiner, A = dieser Einsatz | 2 je Schritt + 2 |
| 9 | N oder P | Taktwechsel | keine | A = Taktanfang; Δ und Spielmodus bleiben | 0 |
| 10 | beliebig | Ende des Beispiels | keine | Wert = Minimum über alle Wege (Leitsatz 1) | 0 |

Bei einem Ausbleiben werden 5a und 5b beide weiterverfolgt; das ist die
gewollte Vagheit nach Leitsatz 3. Außerhalb dieser Zeilen entsteht keine
Anweisung: kein Verstärken ohne Einsatz, kein Verringern ohne Ausbleiben,
kein `11` in P und kein `10` in N.

**Folgerungen aus Zeile 9 und der bestätigten Ankerregel:**

- Ein Einsatz auf der Eins ist bei N stets erwartet. Wer im Takt auf
  verschobenen Positionen gespielt hat, kehrt auf der Eins des nächsten
  Takts ohne Kosten zum Metrum zurück.
- Eine Erwartung, die über den Taktstrich reicht, verfällt. Liegt der
  letzte Einsatz bei 3½ mit Viertelabstand, gilt nach dem Taktwechsel die
  Eins bei 4 und danach 5, nicht 4½.

**Stand der Werte:** Mit dieser Tabelle ist der Kern für beliebige
Rasterbeispiele unter I bestimmt. Die bisherigen I-Werte sind von Hand
gerechnet; ihr Nachweis als Minimum steht bis zu einer maschinellen
Rechnung über alle Wege aus.

### Gehaltener Klang: das Tonende als Pauseneinsatz

**Festgelegt, Entscheidung des Projektinhabers vom 13. November 2026:**
Endet ein gehaltener Ton, ohne dass zugleich ein neuer Ton einsetzt, hört
man die Pause als Einsatz. Das ist die einzige Möglichkeit, eine Pause als
Einsatz zu hören. Unter I gibt es deshalb keine Pauseneinsätze; dort
bleibt „Pausen spielen“ eine Deutung des Ausbleibens.

**Festgelegt – Schreibweise, 13. November 2026:** Rasterbeispiele unter G
werden mit `x` (Einsatz), `=` (Ton hält) und `o` (Stille) notiert, etwa
Halbe plus halbe Pause als `x=== ==== oooo oooo`. Der Pauseneinsatz ist
die erste Stille nach `x` oder `=`. Unter I sind nur `x` und `o` zulässig.
Die Schreibweise ist von RDL getrennt.

**Festgelegt – Übergangstabelle unter G, bestätigt am 13. November 2026:** G
verwendet dieselbe Tabelle mit einer zweiten Ereignisart, dem
Pauseneinsatz. Es ändern sich nur die Zeilen zum erwarteten Punkt in N:

| Nr. | Zustand | Ereignis unter G | Anweisung | Folgezustand | Kosten |
| --- | --- | --- | --- | --- | ---: |
| 5a-G | N | Pauseneinsatz am erwarteten Punkt | `11` | P, Δ unverändert, A = dieser Pauseneinsatz | 2 |
| 5b-G | N | am erwarteten Punkt weder neuer Ton noch Pauseneinsatz (Ton hält) | `01` | N, Δ verdoppelt | 2 |
| 4-G | N | Pauseneinsatz vor dem erwarteten Punkt | `00` so oft wie nötig, dann `11` | P, Δ kleiner, A = dieser Pauseneinsatz | 2 je Schritt + 2 |

Ein nahtloser Neuanschlag am erwarteten Punkt bleibt Zeile 3 ohne Kosten.
Unter G entfällt damit die Wahl zwischen 5a und 5b: Der hörbare Verlauf
entscheidet. Die Vagheit nach Leitsatz 3 entsteht an dieser Stelle nur
unter I.

**Abgleich mit den gerechneten Beispielen (Handrechnung):** Die Regel
ergibt die bisherigen G-Werte F05.1–F05.7 (`2, 6, 6, 4, 6, 4, 4`), Halbe
`10 01` gegen Viertel plus Pause `10 11` und den Punktierungsfall
`10 01 00 00`. Für Foto 1, Rhythmus 2 ergibt sie, wenn der letzte Ton als
Achtel gehalten wird, `10 11 00 10 00 01` = 12 Bit: dieselben Anweisungen
und dieselbe Summe wie die korrigierte Fotofolge `10 00 11 10 00 01`, in
anderer Reihenfolge.

**Festgelegt, bestätigt am 13. November 2026:**

- Der Pauseneinsatz wird wie ein gespielter Einsatz zum Anker; das stumme
  Raster zählt danach von ihm.
- Ein über den Taktstrich gehaltener Ton ist auf der Eins ein Ausbleiben
  und kostet `01`, solange der Abstand die Eins noch erwartet. Eine
  Überbindung bezahlt also am Taktstrich.

**Festgelegt – Anfangspause:** Der betrachtete Takt und das Metrum stehen
bereits fest. Dass an seinem Anfang nichts erklingt, gehört zum rhythmischen
Verlauf und kostet `11` (2 Bit). Die Analyse darf diese Information nicht
durch einen erst beim ersten Ton beginnenden Kostenauftrag ausklammern.
Der erste spätere Einsatz kostet weiterhin `10`; die Pausengliederung
rechtfertigt keine zusätzliche Teilung. Ein noch uninitialisierter
Spielmodus beschreibt höchstens den Zustand vor der initialen Anweisung,
nicht eine kostenlose Anfangspause.

**Offen (W04/W06):** Vorgeschichte und neuer Analysebeginn. Ankerauswahl
und Phase sind durch die Ankerregel entschieden.
Insbesondere darf eine vorhandene Vorgeschichte
die bestätigte Zahlung des ersten Einsatzes nicht still aufheben.

### Präzisierung des Projektinhabers: Halbe und Analysebeispiele

**Festgelegt, Nachtrag vom 2. November 2026:** Das geteilte Metrum ist als
gemeinsamer Bezugsrahmen vorausgesetzt. Nach dem ersten Ton/Schlag stehen
die Viertelebene (bei entsprechendem Takt) und „Noten spielen“ fest.
Eine Halbe wird daran erkannt, dass auf ihren Onset mit Beginn der nächsten
Viertel kein Offset folgt. In diesem Augenblick muss die Höranalyse die
Teilung mit `01` verringern. Dafür ist kein Wissen um die vollständige
Notendauer beim ersten Einsatz nötig.

Die notierten Bitwerte sind **fachliche Ausgangsbeispiele der Analyse**,
nicht bloß Erzeugungskosten und keine gegen das Äquivalenzminimum
geschützten Sollzahlen. Die zuvor vorgeschlagene Trennung als mögliche Erklärung
abweichender Hörwerte beantwortet diesen Fall nicht und wird dafür
zurückgenommen. Die räumliche Codeanordnung auf dem Foto legt weiterhin
nicht automatisch den Erkennungszeitpunkt jeder Anweisung fest.

**Folgerung:** Beim gehaltenen Ton darf die Auswertung nicht ausschließlich
auf einen fehlenden neuen Onset prüfen. Im Halbenbeispiel ist ausdrücklich
das ausbleibende Offset am erwarteten Viertelende entscheidend. Die
Übertragung auf I verwendet ausschließlich die dort relevanten Ereignisse:
Das Ausbleiben einer wirksamen Abschaltung ist bei I kein Beleg für eine
Halbe. Die konkrete Onset-Ausbleiberegel sowie eine mögliche
Toleranz bei realem Spiel bleiben gesondert zu klären; die Festlegung selbst
wird nicht durch eine frei angenommene Frist ersetzt.

## 3. Ereignis, Erwartung und Erkennung

**Festgelegt (T01–T06):** Ereigniszeit, erwartete Zeit, Erkennungszeit,
musikalischer Bezugspunkt und technischer Anweisungszeitpunkt unterscheiden.
Ein zusätzlicher Einsatz liefert mit Auftreten Evidenz für feinere Teilung.
Ausbleiben benötigt eine Toleranzpolitik. Hörbares Tonende ist ein eigenes
Ereignis. Eine symbolische Datei verleiht dem Hörmodell kein Zukunftswissen.
Technischer Vorlauf des Generators verursacht keine zusätzlichen Bitkosten.

**Festgelegt – Ausbleiben als Evidenz, auch unter I:** Das Ausbleiben eines
aufgrund des geltenden Zustands erwarteten Ereignisses ist selbst relevante
Evidenz. Es kann insbesondere eine Interpretation als „Pausen spielen“
(`11`) oder als Verringerung der Teilung (`01`) begründen. Ein hörbares
Tonende ist **keine notwendige Voraussetzung für `11`**. Diese Regel gilt
ausdrücklich auch für den idealisierten kurzen Impulsklang: Sein natürliches
Ausklingen allein löst keinen Pausenbefehl aus, ein ausbleibender erwarteter
neuer Einsatz kann ihn jedoch begründen.

Unter den mit dem hörbaren Verlauf und den bisherigen Festlegungen
vereinbaren Interpretationen wird die informationsgünstigste gewählt.
Weder wird aus jedem Ausbleiben zwingend `11`, noch muss vor `11` stets
ein `01` ausgeführt werden. Welche Interpretation zulässig und günstiger
ist, muss anhand des Zustands, der Erwartung und des weiteren zulässigen
Erkenntnisverlaufs geprüft werden. Eine bloße notierte Pausengliederung
bleibt als Begründung für zusätzliche Teilung ausgeschlossen.

**Verbindlich zu berücksichtigender Kandidat – F05.6.I:** Bei Viertelbezug
und Einsätzen bei 0 und 1 aktiviert `10` bei 0 „Noten spielen“; der Einsatz
bei 1 setzt diesen Zustand fort. Der dadurch bei 2 erwartete Einsatz
bleibt aus. Diese Evidenz begründet den Kandidaten `11`, ohne dass ein
hörbares Tonende nötig wäre. Die Folge `10 11` kostet im Grundwörterbuch
4 Bit und muss im Interpretationsvergleich berücksichtigt werden. Der
Erkennungszeitpunkt des Ausbleibens folgt der noch zu bestimmenden
Erkennungspolitik; sein erwarteter Zeitpunkt bleibt 2. Dies ist kein
vollständiger Minimalitätsnachweis und keine allgemeine Festlegung,
dass jeder ausbleibende Einsatz als Pause interpretiert werden muss.

**Vorschlag V04:** Jede Änderung dokumentiert Anlass, diese Zeiten,
Vorher-/Nachherzustand, Wörterbuch, Kosten und Begründung. Für Ausbleiben
zusätzlich erwarteten Ereignistyp und Zuordnungsfenster. Erwartet werden
muss ein bestimmtes Ereignis: ein neuer Anschlag, ein Tonende oder eine
Pausenfortführung sind nicht austauschbar.

**Festgelegt – Erkennung auf dem exakten Raster, 13. November 2026:**
Bei rastergenauen symbolischen Beispielen gilt ein Ausbleiben **am
erwarteten Rasterpunkt** als erkannt; Erkennungszeit und erwartete Zeit
fallen zusammen. Toleranzen und Fristen betreffen erst gespielte Musik mit
Timingabweichungen und sind für den symbolischen Kern keine Voraussetzung.

**Vorschlag V05, nur für Darbietungen mit Timingabweichung:** Trennung von Ereignistoleranz und Ausbleibefrist,
zunächst konfigurierbar mit offener Herkunft. Die
[Literaturauswertung](03-literatur-und-erkennung.md) liefert keine allgemeine
Zahl. Die Beispieltabellen setzen symbolisch
kleine Fristen voraus, damit relevante Entscheidungen nacheinander
liegen. Das sind bedingte Rechnungen, keine empirischen Schwellen.

**Offen:** Wenn Tonende und eine Ausbleibefrist zusammentreffen, dürfen sie
nicht durch zwei unabhängig implementierte Regeln unbeabsichtigt doppelt
bezahlt werden. Zu entscheiden ist, ob `11` die fehlende Reartikulation
bereits erklärt oder daneben ein `01` erforderlich ist. Gleichzeitige
Anweisungen brauchen eine definierte Reihenfolge, weil sie andere Zustände
und Erwartungen erzeugen können.

**Festgelegt – Analysegrenze:** Kein zusätzliches `11` allein wegen der
Grenze eines betrachteten Ausschnitts. Sie ist keine musikalische
Zustandsänderung. Bei fortlaufender Analyse wird der Zustand weitergeführt;
tatsächliche Ereignisse an der Grenze werden dort regulär ausgewertet.
Ein hörbares Tonende an der Grenze bleibt also erhalten und kann nach den
normalen Übergangsregeln eine Anweisung begründen, aber nicht aufgrund
der bloßen Grenze. Es gibt weder einen kostenlosen Reset noch eine
pauschale Abschlussgebühr. Die F05-Kosten für `[0,4)` enthalten keinen
künstlichen Abschluss; Ereignisse bei 4 gehören zur Auswertung an 4.

**Präzisierung vom 13. November 2026 (Leitsätze 1 und 2):** „Zustand
weiterführen“ bedeutet, dass ein verlängertes Beispiel ohne Reset über die
frühere Grenze hinweg ausgewertet wird. Es bedeutet nicht, dass der Wert
und die Deutung des kürzeren Beispiels unverändert übernommen und nur um
einen Folgebetrag ergänzt werden. Das verlängerte Beispiel wird als Ganzes
bewertet.

**Weiter offen:** Die konkrete Erkennung und Zuordnung einer vor der Grenze
entstandenen, erst danach entscheidbaren Ausbleibeerwartung. In einer
fortlaufenden Analyse darf sie nicht mit dem Ausschnitt verworfen werden.

## 4. Minimum ohne Löschung der Vergangenheit

**Festgelegt (M01–M05):** Sparsamkeit gilt zwischen hörbar gleichwertigen
Interpretationen. Hörbare Ereignisse bleiben erhalten; erkannte
Ausbleibeinformation ebenfalls. Spätere Korrektur ist möglich.
Mit dem Beginn des nächsten Takts wird der Wert des bis dahin vorliegenden
Beispiels benannt. Das ist eine Modellentscheidung, kein nachgewiesenes
Hörgesetz.

**Präzisierung vom 13. November 2026 (Leitsätze 2 und 3):** Dieser
Abschluss gilt für das Beispiel, das an der Taktgrenze endet. Folgt ein
weiterer Takt, liegt ein neues Beispiel vor; dessen Minimum darf die
Deutung der früheren Takte neu bestimmen. Die frühere Lesart, nach der
jede Mehrdeutigkeit am Taktbeginn endgültig entschieden ist, ist damit
abgelöst.

**Festgelegt – Rangfolge im Konflikt mit dem Foto:** Zulässigkeit wird an
den hörbaren Ereignissen unter I bzw. G und am zeitlichen Informationsstand
geprüft. Unter den so zulässigen Interpretationen hat die günstigste Vorrang
vor einem handschriftlichen Beispielwert. Die Minimierung muss daher auch
Varianten ohne rein notationsbedingte Pausenteilung berücksichtigen.
Bereits hörseitig entstandene Information bleibt nach dem bisherigen
Vertrag erhalten; der neue Vorrang ist keine Freigabe für beliebige
rückblickende Kompression.

**Vorschlag V07 – festgeschriebene Erkenntnisse, offene Interpretation:**

**Abgrenzung durch den Fallvergleich:** Ausbleibeevidenz festzuhalten bedeutet
nicht, ohne Auswahlregel immer sofort `01` festzuschreiben. In F05.3.I
erklären `10 01 00` und `10 11 10` denselben hörbaren Verlauf mit je 6 Bit;
beide bezahlen die Änderung bei 1. Die Wahl zwischen solchen noch offenen
Pfaden muss sichtbar bleiben. Innerhalb eines bereits gewählten Pfads
werden frühere Kosten nicht durch spätere Evidenz gelöscht. Unter G kann
dagegen das tatsächliche Weiterklingen den P-Pfad konkret ausschließen.

1. Zu jedem Zeitpunkt werden ausschließlich bis dahin verfügbare Evidenz
   und bekannte Bedingungen verwendet. Spekulative Erklärungen werden
   ausdrücklich als solche geführt; sie erzeugen noch keine festen Kosten.
2. Sobald die festgelegte Erkennungspolitik eine Änderung entscheidet,
   werden Anlass, gewählte Anweisungen und deren Kosten festgeschrieben.
   Zulässige Fortsetzungen müssen diese Entscheidungshistorie übernehmen.
3. Innerhalb einer Anweisungsfolge kann spätere Evidenz neue
   Änderungsanweisungen hinzufügen; sie löscht dort keinen bereits
   bezahlten Befehl. Welche Anweisungsfolge für ein Beispiel gilt,
   entscheidet dagegen das Minimum über das ganze Beispiel (Leitsatz 1).
   Bei einem verlängerten Beispiel kann deshalb eine andere Folge gelten
   als beim kürzeren.
4. Innerhalb der noch unentschiedenen Interpretationen dürfen nur Varianten
   verglichen werden, die dieselben festgeschriebenen Erkenntnisse und alle
   hörbaren Ereignisse erhalten. Gewählt wird die günstigste zulässige
   Fortsetzung. Wann genau etwas festgeschrieben wird, braucht D03.
5. Am Ende des Beispiels wird unter diesen Bedingungen entschieden; bei
   Gleichstand bleiben Gleichwertigkeit und Alternativen sichtbar. Ein
   deterministischer Vertreter kann technisch gewählt werden. Abschluss
   bedeutet weder psychologische Eindeutigkeit noch Reset von Spielmodus,
   Δ oder Phase, und er bindet ein verlängertes Beispiel nicht.

**Grenze dieses Vorschlags:** Das Minimum nach Leitsatz 1 vergleicht nur
Anweisungsfolgen, die vom selben festgelegten Anfangszustand ausgehen
(bekannter Bezug, Anfangsabstand, erster Einsatz `10`) und jede Abweichung
von ihren eigenen Erwartungen bezahlen. Unzulässig bleibt ein Vergleich,
der rückblickend ein anderes Anfangsgitter wählt und dadurch den
ausgebliebenen Viertel-Einsatz nie erwartet hätte. Eine nur lokal
billigste Operation ist ebenso wenig bereits festgelegt. V07 ist ein
Entscheidungsrahmen, noch kein vollständiger Auswahlalgorithmus.

**Offen:** Was geschieht, wenn am Taktende keine Variante alle Bedingungen
erfüllt? Vorgeschlagen ist ein erklärtes nicht bewertbares Ergebnis statt
erfundener Anweisungen. Eine erst nach der Grenze ablaufende Frist kann
nicht zugleich kausal vollständig und bereits endgültig abgeschlossen sein.
Daher muss „Taktabschluss“ zwischen musikalischer Grenze und späterem
Auswertungsabschluss unterscheiden. Auch das erfordert eine Entscheidung.

## 5. Triolen und Kosten

**Festgelegt (R01/R02/R06):** Ab Erkennen einer Dreiteilung gilt vorübergehend
ein erweitertes Wörterbuch mit 3 Bit je Anweisung. Frühere Kosten bleiben
bei 2 Bit. Im folgenden Takt gilt wieder das 2-Bit-Grundwörterbuch;
ein Reset des ganzen musikalischen Zustands folgt daraus nicht.

**Vorschläge (R03/R04):** Zwei weitere Operationen Δ/3 und 3Δ; sechs
gleich lange 3-Bit-Wörter, konkrete Codes noch offen. Aktivierende Anweisung
bereits 3 Bit, ohne gesonderte Wechselkosten. Beide Vorschläge bedürfen
der Bestätigung. Noch keine tatsächliche adaptive Huffman-Codierung.

**Offen:** Hörseitige Erkennung, Dauer des erweiterten Wörterbuchs,
ternärer Abstand im nächsten Takt, Rückkehr vor Taktende. Insbesondere
folgt aus einem früheren Einsatz nicht eindeutig „Triole“: Bei Viertelbezug
könnte ein Ereignis bei 1/3 auch eine feinere binäre Teilung mit zulässiger
Timingabweichung sein. Die Kandidaten-/Toleranzregel muss diesen Fall
entscheiden. Die gedruckte Triolenklammer genügt dem Hörmodell nicht.
Weitere Teilungen bleiben zunächst ausdrücklich nicht spezifiziert.

**Kosten (B01–B04):** Für eine bestimmte Interpretation gilt
`I = Summe c(a_i, W_i)` mit 2 bzw. 3 Bit nach aktivem Wörterbuch.
Die Kosten bezeichnen musikalische Anweisungen unter bekannten Bedingungen.
Zeitzuordnung und Wörterbuchwechsel sind noch kein selbsttragender Bitstrom;
alle 2-Bit-Wörter sind bereits vergeben. Eine serialisierte Datei und ihre
Bytezahl sind ein anderer Vertrag. Die vorläufig maßgebliche Darstellung
ist die erklärte Anweisungsfolge mit Kosten und Zuständen.

## 6. Analyzer, Generator und fachliche Abnahme

**Vorschlag (A01–A05):** Analyse liefert normalisierte Notation, hörbare
Realisierung unter Klangannahme, Rahmen, Startzustand, Toleranzpolitik,
Ereignis-/Erwartungsverlauf, Anweisungsprotokoll und kumulierte Kosten.
Vorläufige Varianten und endgültige Entscheidungen unterscheiden.
Lokale Profile sind erst nach Wahl ihrer Zeitbasis definiert: Erkennung
oder musikalischer Bezugspunkt; beide dürfen separat ausgegeben werden.
Die technische Vorbereitung ist keine solche Profilzeitbasis.

**Folgerung (G01–G04):** Generator verwendet denselben Analyzer. Ein
Konstruktionspfad mit Zielkosten ist noch kein Erfolg: Die ausgegebene
Notation und ihre Realisierung müssen unter identischen Bedingungen die
Zielbewertung erreichen. Konstruktionskosten und Hörwert bleiben bis zum
Nachweis ihrer Gleichheit getrennt. Notations-/Austauschausgabe ist Ziel,
Echtzeit perspektivisch; MIDI/OSC sind Ausführungswege. Formatpriorität,
Suchstrategien, Ziele, Profile, Zufall, Budget und Datenbanknutzung sind offen.
Keine Übernahme der alten Codesuche oder des alten 128-Slot-Limits.

**Abnahme:** Eine andere Person kann aus Eingabe und benannten Bedingungen
dieselben Anweisungen, Zustände, Wörterbuchwechsel und Kosten herleiten.
Softwaretests belegen diese Reproduzierbarkeit, keine psychologische
Validierung. Jeder Prüffall benennt I oder G (bei einem Vergleich beide),
Ereignisfolge, Fenster und bekannte Bedingungen. Notierter Ausgangswert,
Kosten einer vorgeschlagenen Interpretation und nachgewiesenes Minimum
werden unterschieden. Abweichungen vom Foto werden mit der günstigeren
Interpretation und deren hörbarer Gleichwertigkeit erklärt; wo Regeln
noch fehlen, wird kein exaktes Minimum behauptet.

## 7. Entscheidungen für den nächsten fachlichen Durchgang

| ID | Entscheidung und konkreter Prüfpunkt | Vorschlag / Status |
| --- | --- | --- |
| D01 | Bedeutung der Bildwerte und Erkenntnis der Anfangshalben. | Geklärt: fachliche Ausgangsbeispiele; Äquivalenzminimum hat Vorrang. In G nach erstem Einsatz N und Viertelabstand; `01` zu Beginn der zweiten Viertel bei ausbleibendem Offset. |
| D02 | Anker und Phase nach Teilungswechseln; keine rückwirkenden neuen Erwartungen. | Entschieden am 13. November 2026: Raster zählt vom zuletzt gespielten Einsatz; Taktwechsel setzt nur den Anker auf den Taktanfang; die Eins ist bei „Noten spielen“ erwartet. Mehrfaches Verstärken am selben Ereignis erlaubt, Verringern nur eine Stufe je Ausbleiben. Punktierungsfall bleibt als Spezialfall gültig. |
| D03 | Welche Interpretation wird wann festgeschrieben? Welche Korrektur bleibt kostenpflichtig? | V07 und F05.3.I: konkurrierende 6-Bit-Pfade bezahlen dieselbe Ausbleibeevidenz, ohne zwingend dieselben Befehle zu verwenden. Punktierungsfall als Gegenprobe erhalten. |
| D04 | Tonende, Dauererwartung und Pause: wann `11`, wann `01`, wann beide? | I und G festgelegt; hörbare Enden erhalten. Ausbleiben ist auch unter I Evidenz für `11` oder `01`; Tonende für `11` nicht notwendig. F05.6.I muss `10 11` berücksichtigen. Allgemeine Auswahl-/Übergangsregeln noch offen. |
| D05 | Ausbleibefrist, konkurrierende Ereignisse, wiederholtes Ausbleiben und Analyseende. | Für exakte Rasterbeispiele entschieden: Erkennung am erwarteten Rasterpunkt. Fristen und Toleranzen für gespielte Musik bleiben offen (V05/V06). |
| D06 | Anfangspause und Analysegrenze einschließlich Fristen jenseits der Grenze. | Entschieden: Anfangspause mit `11` bezahlen; keine Zusatzanweisung allein wegen der Analysegrenze, Zustand fortführen und Grenzereignisse regulär auswerten. Noch offen ist die Zuordnung grenzüberschreitender Erkennung. |
| D07 | Zusätzliche Triolenoperationen, aktivierende Kosten, Erkennung, Taktwechsel. | R03/R04 bleiben Vorschläge. |
| D08 | Korrekturen auf Foto 1 und komplexer 22-Bit-Fall. | Beide ersten `01` auf Foto 1 ausdrücklich zu `00` korrigiert. Nur der komplexe 22-Bit-Fall bleibt sicher zu transkribieren. |
| D09 | Profilzeitbasis, erstes Format, Mehrstimmigkeit, weitere Teilungen, Generatorziele. | Erst nach belastbaren einfachen Ereignisabläufen entscheiden. |

Die noch offenen Teile von D02–D06 sind Voraussetzungen für einen belastbaren Kernplan. D07 kann nur
bei ausdrücklich begrenzter erster Lieferung nachgelagert werden; Triolen
bleiben dann als geforderte, noch nicht unterstützte Erweiterung sichtbar.

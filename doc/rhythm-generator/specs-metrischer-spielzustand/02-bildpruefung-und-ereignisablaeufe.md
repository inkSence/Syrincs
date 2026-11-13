# Bildprüfung und Ereignisabläufe

Status: [Rahmen](README.md), Regeln und Entscheidungskennungen:
[fachlicher Vertrag](01-fachlicher-vertrag.md). Zeiten sind Viertelpositionen
ab 0, Taktfenster zunächst `[0,4)`, Ausgangsabstand Δ=1. Keine Millisekunden-
oder Wahrnehmungsschwelle wird damit festgelegt. `N`/`P` bezeichnen Noten/
Pausen spielen, `U` den vorgeschlagenen uninitialisierten Zustand.

**Klangbedingungen:** I = idealisierter kurzer Impulsklang; G = idealisierter
gehaltener Klang mit hörbarer Abschaltung, gemäß
[Vertrag, Abschnitt 1](01-fachlicher-vertrag.md#1-ziel-und-bekannte-bedingungen).
Die Foto-Tabelle ist Quelleninventar, kein klangunabhängiger Zahlenvertrag.
Die sieben einfachen Quellenfiguren werden in Abschnitt 3 jeweils unter I
und G durchgerechnet; weitere Klangvergleiche stehen in Abschnitt 7.
Keine Dämpfung, variable Hüllkurve oder weitere Artikulation wird unterstellt.

## 1. Prüfung der beiden Fotos

Originale: [IMG_2214.JPEG](<../Rhythmus Codierung Huffman Mitschrift/IMG_2214.JPEG>)
und [IMG_2215.JPEG](<../Rhythmus Codierung Huffman Mitschrift/IMG_2215.JPEG>).
Beide wurden am Bild betrachtet. Datum auf Foto 1 lesbar als 17. Okt. 25;
der durchgestrichene obere Wörterbuchentwurf wird nicht verwendet.
Das untere Wörterbuch passt zur bestätigten Fassung im Entwurf, trotz
überschriebener Zeichen bei den Spielbefehlen.

| Quelle | Lesung / Status |
| --- | --- |
| Foto 1, Rhythmus 1 | Notengruppen und ergänzende Onsets `xooo xoxo xoxx xxxo` passen. Sichtbar `10 01 00 01`, 8 Bit. Vom Projektinhaber ausdrücklich korrigiert: `10 00 00 01`. |
| Foto 1, Rhythmus 2 | Viertel, Achtelpause/Achtel, danach gleiche Gruppen; `xooo ooxo xoxx xxxo`. Sichtbar `10 01 11 10 00 01`, 12 Bit. Vom Projektinhaber ebenfalls ausdrücklich korrigiert: `10 00 11 10 00 01`. |
| Foto 2, sieben einfache Fälle | Codefolgen und Werte stimmen mit der übergebenen Tabelle überein. Die darunter wiederholte Notation unterstützt die Lesung der teils uneindeutigen Köpfe. |
| Foto 2, oben rechts | 22 Bit erkennbar; Bögen, überschriebenes Material und Zuordnung der Codes bleiben unsicher. Kein exakter Testfall. |

**Präzisierung des Projektinhabers:** Nur diese beiden Fotos sind musikalische
Referenzen; andere Bildentwürfe dokumentieren einen verworfenen Pfad. Die
notierten Werte sind fachliche Ausgangsbeispiele der Analyse. Bei einem
Konflikt hat die informationsgünstigste hörbar gleichwertige Interpretation
Vorrang. Eine davorstehende Codegruppe
belegt nicht, dass die Höranalyse die Notendauer schon am Onset kennt.
Insbesondere wird die Halbe zu Beginn der zweiten Viertel am ausbleibenden
Offset erkannt. Die frühere Einordnung der Fotos als bloße Erzeugungswerte
wird zurückgenommen.

## 2. Sieben einfache Analysebeispiele: Notation und Foto-Code

Bei gehaltenem Klang sind neben den Einsätzen die Tonenden zu erhalten;
ein Übergang zu einem neuen Ton kann mit Ende des vorherigen zusammenfallen.
Bei Impulsklang sind die genannten Notenenden nicht automatisch Abschaltungen.
Der gemeinsame Endpunkt 4 wird hier separat als Rahmenende geführt.
Notation `a:b` bedeutet Einsatz bei a, notierte Dauer b.

| Fall | Noten / ausdrückliche Pause | Gelesene Codefolge auf dem Foto, ohne zugewiesene Erkennungszeiten | Notierter Analysewert |
| --- | --- | --- | ---: |
| F05.1 Vier Viertel | `0:1, 1:1, 2:1, 3:1` | `10` | 2 Bit |
| F05.2 Halbe, halbe Pause | `0:2`; Pause `[2,4)` | `01 10 11` | 6 Bit |
| F05.3 Halbe, zwei Viertel | `0:2, 2:1, 3:1` | `01 10 00` | 6 Bit |
| F05.4 Zwei Viertel, Halbe | `0:1, 1:1, 2:2` | `10 01` | 4 Bit |
| F05.5 Halbe Pause, Halbe | Pause `[0,2)`; `2:2` | `01 11 10` | 6 Bit |
| F05.6 Zwei Viertel, halbe Pause | `0:1, 1:1`; Pause `[2,4)` | `10 01 11` | 6 Bit |
| F05.7 Halbe Pause, zwei Viertel | Pause `[0,2)`; `2:1, 3:1` | `01 11 00 10` | 8 Bit |

Die Summe ist jeweils die Anzahl gezeigter Codes mal 2. Der Erkenntnisverlauf
wird davon getrennt zeitlich erläutert. Unter G erfordert die Anfangshalbe kein
Zukunftswissen: Nach `10` gilt Viertelabstand; bei ausbleibendem Offset zu
Beginn der zweiten Viertel folgt `01`. Unter I ist hingegen die
Onset-Ausbleibeerkennung zu prüfen.

## 3. Schrittweise Rechnung F05.1–F05.7 unter I und G

Bearbeitungsbasis dieses Durchgangs: `835655ee26c40f54b3c7934c5f9e6cf153b559a0`.
`IMG_2215.JPEG` wurde erneut am Bild geprüft. Die Quellenlesung in Abschnitt 2
bleibt erhalten; sie bestimmt weder Erkenntnisreihenfolge noch Minimum.

### 3.1 Gemeinsamer Rahmen, Festlegungen und verbleibende Rechenannahmen

Alle vierzehn Fälle haben Beobachtungsfenster `[0,4)`, bekannten 4/4-Bezug,
Viertelabstand Δ=1 und keine mitgelieferte Vorgeschichte. I besitzt nur die
angegebenen Impulse als variable Evidenz. G besitzt zusätzlich alle genannten
hörbaren Abschaltungen. Keine menschlichen Timingabweichungen werden
hinzuerfunden. `r_e` bezeichnet die noch festzulegende Erkennung einer bei e
unerfüllten Erwartung; e und r_e sind verschiedene Angaben. Für die
hier dargestellten aufeinanderfolgenden Entscheidungen wird ausdrücklich
angenommen, dass r_e vor dem nächsten relevanten Ereignis liegt. Liegt die
Erkennung später, ist der jeweilige Pfad nur ein bedingtes Ergebnis.
Keine Millisekundenfrist wird festgelegt. Fehlende G-Offsets im bestätigten
Halbenfall werden gemäß Projektinhaber am musikalischen Viertelende erkannt.

**Festgelegt am 13. November 2026:** Für diese rastergenauen Beispiele
gilt `r_e = e`; ein Ausbleiben ist am erwarteten Rasterpunkt erkannt. Die
in den Tabellen noch genannten Bedingungen der Form `r_e < …` sind damit
erfüllt und keine offenen Annahmen mehr.

**Rechenprofil K:** K-G bleibt in Teilen eine unbestätigte
Konkretisierung. K-A, K-P, K-E und K-S sind inzwischen durch die folgenden
Entscheidungen festgelegt; der Gesamtvertrag ist damit noch nicht vollständig.

- **K-A (Phase), festgelegt am 13. November 2026:** A ist der zuletzt
  gespielte Einsatz; vor dem ersten Einsatz eines Takts und nach jedem
  Taktwechsel ist A der Taktanfang. Erwartet wird bei `A + k·Δ`, `k ≥ 1`;
  am Taktanfang ist bei N zusätzlich die Eins selbst erwartet. Δ und
  Spielmodus überdauern den Taktwechsel. Nach einem durch Ausbleiben
  begründeten `01` liegt die nächste Erwartung bei `A + 2Δ` und damit
  stets nach dem unerfüllten Zeitpunkt: Der Ausfall ist bereits bezahlt.
  Bei F05.6.I liegt sie nach dem Ausbleiben bei 2 daher bei 3 (A=1), nicht
  mehr bei 4. Ein einzelnes Ausbleiben verringert nur um eine Stufe;
  mehrfaches Verstärken am selben Einsatz ist erlaubt. Die frühere
  Rechenannahme A=0 ist abgelöst; der bestätigte Punktierungsfall bleibt
  als Spezialfall gültig.
- **K-P (Pausenzustand), festgelegt am 13. November 2026:** P unterdrückt erwartete neue Töne, erhält aber Δ
  und A. Es gibt Pausenrasterpunkte, jedoch keinen Klang an ihnen und keine
  kostenpflichtige Wiederholung von `11`. Ein passender neuer Einsatz kann
  mit `10` auf demselben Gitter zurückkehren; ein Einsatz neben dem Gitter
  braucht zuvor `00`. Die vollständigen Übergänge stehen in der
  [Übergangstabelle des Vertrags](01-fachlicher-vertrag.md#2-wörterbuch-und-zustand).
- **K-G (Dauer), Grundsatz festgelegt am 13. November 2026:** Ein Tonende
  ohne gleichzeitigen Neuanschlag wird als Pauseneinsatz gehört; siehe
  [Vertrag, Abschnitt 2](01-fachlicher-vertrag.md#gehaltener-klang-das-tonende-als-pauseneinsatz).
  Die folgende Rechenfassung stimmt damit überein: In N ist bei G neben dem nächsten Einsatz ein Tonende
  nach Δ erwartet. Nahtloses Ende mit regulärem Neuanschlag ist Fortsetzung,
  kein `11 10`. Ein isoliertes hörbares Ende kann durch `11` erklärt werden.
  P bei weiter hörbar gehaltenem Ton ist unzulässig. Dieser konkrete
  Übergangsvorschlag ist zu bestätigen; er ergänzt die bereits festgelegte
  Erhaltung hörbarer Enden und Halben-Erkennung.
- **K-E (Analysegrenze), festgelegt:** Für `[0,4)` entsteht kein `11`
  allein wegen der Grenze bei 4. Endzustand und offene Erwartungen werden
  in einer fortlaufenden Analyse übernommen. Tatsächliche G-Enden und
  andere Ereignisse bei 4 werden dort regulär ausgewertet; sie bleiben
  als Randereignisse der vorliegenden Figuren dokumentiert. Kein Reset.
- **K-S (Beginn), festgelegt:** Anfangsstille gehört bei bekanntem Takt
  und Metrum zum rhythmischen Verlauf. Der Anfangszustand P kostet `11`
  (2 Bit), in den Tabellen weiterhin mit **S+** bezeichnet. Auch der erste
  spätere Einsatz kostet `10`. Die zuvor gerechnete Variante SU (U bis
  zum ersten Ton ohne Anfangskosten) ist für diesen Auftrag ausgeschlossen.

Die Tabellen verwenden `M/Δ/A/e`. e ist bei N die nächste Onset-Erwartung,
bei G zugleich das erwartete nächste Tonende nach K-G. `P:3` bedeutet nur
Pausenrasterpunkt 3, **keinen erwarteten Onset oder erneuten Abschaltvorgang**.
`U/1/0/–` hat noch keine Spielerwartung. Bei G stehen gleichzeitige
Abschaltung und Neuanschlag zusammen in der Evidenzspalte. Alle Befehle
kosten hier 2 Bit. Zustand und Kosten werden pro Variante getrennt geführt;
keine Variante erbt die bereits bezahlten Befehle eines anderen Pfads.
Innerhalb eines Pfads bleiben erkannte Änderungen erhalten.

**Wertstatus:** Fotozahl = Ausgangswert; K = Kosten eines unter den
benannten Annahmen dargestellten Kandidaten; günstigster dargestellter
K-Wert = Vergleich dieser Pfade. Ein nachgewiesenes Minimum unter vollständig
festgelegtem Gesamtvertrag liegt noch nicht vor. Insbesondere sind Teile von K-G nicht bestätigt, und der maschinelle
Minimalitätsnachweis steht aus. K-A, K-P, K-E und K-S sind entschieden. Gleiche Summen bei verschiedenen erhaltenen Klangverläufen
sind zulässig; die ältere strikte Ungleichung ist abgelöst.

### 3.2 F05.1 – vier Viertel (Foto: 2 Bit)

Notation `0:1, 1:1, 2:1, 3:1`. I: Onsets `{0,1,2,3}`. G: dieselben
Onsets, Tonenden `{1,2,3,4}`; das Ende 4 bleibt Randereignis.

| Fall / Pfad | Zeitpunkt | Verfügbare Evidenz | Erkennung | Vorher | Anweisung | Nachher Δ/Anker/Erwartung enthalten | Kosten / Summe |
| --- | --- | --- | --- | --- | --- | --- | --- |
| I/G | 0 | Erster Einsatz | 0 | U/1/0/– | `10` | N/1/0/1 | 2 / 2 |
| I/G | 1 | Einsatz; G zugleich reguläres Ende des vorherigen Tons | 1 | N/1/0/1 | keine | N/1/1/2 | 0 / 2 |
| I/G | 2 | Einsatz; G zugleich reguläres Ende | 2 | N/1/1/2 | keine | N/1/2/3 | 0 / 2 |
| I/G | 3 | Einsatz; G zugleich reguläres Ende | 3 | N/1/2/3 | keine | N/1/3/4 | 0 / 2 |

K: I=G=2. `11` an einem der inneren Punkte würde ohne gleichzeitiges `10`
den tatsächlichen Einsatz nicht erklären. Das Paar `11 10` kostet je 4 Bit
zusätzlich, obwohl reguläre Fortsetzung bereits alle dortigen Ereignisse
erhält: nach K-G unnötig, nicht informationsgünstiger. `01` ohne Evidenz
würde eine erfüllte Viertelerwartung verändern und ist keine durch Ausbleiben
begründete Alternative. Unter K wird die Untergrenze von einmal `10`
erreicht. Ein zusätzliches `11` allein wegen der Analysegrenze ist
ausgeschlossen; tatsächliche Ereignisse bei 4 werden dort regulär behandelt.

### 3.3 F05.2 – Halbe, halbe Pause (Foto: 6 Bit)

Notation `0:2`, Pause `[2,4)`. I: Onset `{0}`. G: Onset `{0}` und
hörbares Ende `{2}`. Die Halbenlänge ist unter I keine eigene Evidenz.

| Fall / Pfad | Zeitpunkt | Verfügbare Evidenz | Erkennung | Vorher | Anweisung | Nachher | Kosten / Summe |
| --- | --- | --- | --- | --- | --- | --- | --- |
| I-P / I-D / G-D | 0 | Erster Einsatz | 0 | U/1/0/– | `10` | N/1/0/1 | 2 / 2 |
| I-P | 1 | Erwarteter Einsatz fehlt | r_1 | N/1/0/1 | `11` | P/1/0/P:2 | 2 / 4 |
| I-P | 2 | Kein neuer Einsatz; P setzt sich fort | 2 | P/1/0/P:2 | keine | P/1/0/P:3 | 0 / 4 |
| I-P | 3 | Kein neuer Einsatz; P setzt sich fort | 3 | P/1/0/P:3 | keine | P/1/0/P:4 | 0 / 4 |
| I-D | 1 | Erwarteter Einsatz fehlt | r_1 | N/1/0/1 | `01` | N/2/0/2 | 2 / 4 |
| I-D | 2 | Auch dieser Einsatz fehlt | r_2 | N/2/0/2 | `01` | N/4/0/4 | 2 / 6 |
| G-D | 1 | Erwartetes Offset fehlt; Ton klingt weiter | 1 | N/1/0/1 | `01` | N/2/0/2 | 2 / 4 |
| G-D | 2 | Hörbares Ende, kein neuer Ton | 2 | N/2/0/2 | `11` | P/2/0/P:4 | 2 / 6 |

I: `10 11` kostet 4; `10 01 01` kostet 6. Ebenfalls darstellbar:
`10 01 11` mit P ab r_2, 6 Bit; der dritte Tabellenzweig zeigt genau den
bis 2 gemeinsamen N/2-Zustand. Für I gewinnt unter den dargestellten Pfaden
4 statt der Foto-6. In G ist `11` bei 1 unzulässig: Es würde den tatsächlich
weiterklingenden Ton von 1 bis 2 entfernen. `01` statt `11` bei 2 erhält
unter K-G fälschlich Klang über das hörbare Ende hinaus; ein zusätzliches
`11` könnte das berichtigen, kostet aber 8 statt 6. Keine Teilung aus der
Pausennotation wird benötigt. G-Kandidat: `10 01 11`, 6.

### 3.4 F05.3 – Halbe, zwei Viertel (Foto: 6 Bit)

Notation `0:2, 2:1, 3:1`. I: Onsets `{0,2,3}`. G: dieselben Onsets,
Tonenden `{2,3,4}`; Ende 4 am Rand.

| Fall / Pfad | Zeitpunkt | Verfügbare Evidenz | Erkennung | Vorher | Anweisung | Nachher | Kosten / Summe |
| --- | --- | --- | --- | --- | --- | --- | --- |
| I-D / I-P / G-D | 0 | Erster Einsatz | 0 | U/1/0/– | `10` | N/1/0/1 | 2 / 2 |
| I-D | 1 | Erwarteter Einsatz fehlt | r_1 < 2 | N/1/0/1 | `01` | N/2/0/2 | 2 / 4 |
| G-D | 1 | Erwartetes Ende fehlt, Ton hält | 1 | N/1/0/1 | `01` | N/2/0/2 | 2 / 4 |
| I-D / G-D | 2 | Einsatz, in G zugleich Ende der Halben | 2 | N/2/0/2 | keine | N/2/2/4 | 0 / 4 |
| I-D / G-D | 3 | Früherer Einsatz; G zugleich hörbares Ende des Tons ab 2 | 3 | N/2/2/4 | `00` | N/1/3/4 | 2 / 6 |
| I-P | 1 | Erwarteter Einsatz fehlt | r_1 < 2 | N/1/0/1 | `11` | P/1/0/P:2 | 2 / 4 |
| I-P | 2 | Einsatz auf dem erhaltenen Viertelgitter | 2 | P/1/0/P:2 | `10` | N/1/2/3 | 2 / 6 |
| I-P | 3 | Regulärer Einsatz | 3 | N/1/2/3 | keine | N/1/3/4 | 0 / 6 |

I: `10 01 00` und `10 11 10` kosten je 6 und sind unter K hörbar gleichwertig.
G: P bei 1 würde den gehaltenen Ton vor 2 entfernen und ist daher unzulässig;
`10 01 00` kostet 6. Eine Viertel-Verfeinerung schon bei 2 hätte noch keine
hörbare Begründung aus der künftig notierten Vierteldauer. Ohne `00` bei 3
blieben der zusätzliche Onset und unter G das frühe Tonende unerklärt.
Die räumliche Fotofolge `01 10 00` wird nicht zeitlich vor den ersten Ton
übernommen. Bei später r_1 ist die I-Rechnung neu zu prüfen, kein Betrag
wird allein aufgrund der Partitur vorzeitig festgeschrieben.

### 3.5 F05.4 – zwei Viertel, Halbe (Foto: 4 Bit)

Notation `0:1, 1:1, 2:2`. I: Onsets `{0,1,2}`. G: zusätzlich Enden
`{1,2,4}`; insbesondere **kein** Tonende bei 3.

| Fall / Pfad | Zeitpunkt | Verfügbare Evidenz | Erkennung | Vorher | Anweisung | Nachher | Kosten / Summe |
| --- | --- | --- | --- | --- | --- | --- | --- |
| I/G | 0 | Erster Einsatz | 0 | U/1/0/– | `10` | N/1/0/1 | 2 / 2 |
| I/G | 1 | Regulärer Einsatz; G auch Ende | 1 | N/1/0/1 | keine | N/1/1/2 | 0 / 2 |
| I/G | 2 | Regulärer Einsatz; G auch Ende | 2 | N/1/1/2 | keine | N/1/2/3 | 0 / 2 |
| I-P | 3 | Erwarteter Einsatz fehlt | r_3 < 4 | N/1/2/3 | `11` | P/1/2/P:4 | 2 / 4 |
| I-D | 3 | Erwarteter Einsatz fehlt | r_3 < 4 | N/1/2/3 | `01` | N/2/2/4 | 2 / 4 |
| G-D | 3 | Erwartetes Ende fehlt, Ton klingt weiter | 3 | N/1/2/3 | `01` | N/2/2/4 | 2 / 4 |

I: gleich teure Kandidaten `10 11` und `10 01`, je 4. Sie sind im Fenster
gleichwertig, aber besitzen verschiedene Endzustände für eine spätere
Fortsetzung. G: `10 01`, 4; `11` bei 3 würde hörbaren Klang von 3 bis 4
entfernen und ist unzulässig. I muss denselben Wert wie die Schreibweise
„drei Viertel, Viertelpause“ erhalten; G muss beide unterscheiden.

### 3.6 F05.5 – halbe Pause, Halbe (Foto: 6 Bit)

Notation Pause `[0,2)`, Note `2:2`. I: Onset `{2}`; G: zusätzlich hörbares
Ende `{4}`. Es gibt vor 2 weder einen Ton noch ein kontrolliertes Tonende.

| Fall / Pfad | Zeitpunkt | Verfügbare Evidenz | Erkennung | Vorher | Anweisung | Nachher | Kosten / Summe |
| --- | --- | --- | --- | --- | --- | --- | --- |
| I/G, S+ | 0 | Beobachteter Anfang ohne Einsatz; bezahlte Initialisierung festgelegt | r_0 < 2, Politik offen | U/1/0/– | `11` | P/1/0/P:1 | 2 / 2 |
| I/G, S+ | 1 | Stille setzt sich fort | 1 | P/1/0/P:1 | keine | P/1/0/P:2 | 0 / 2 |
| I/G, S+ | 2 | Erster Einsatz auf Viertelgitter | 2 | P/1/0/P:2 | `10` | N/1/2/3 | 2 / 4 |
| I-S+P | 3 | Erwarteter Einsatz fehlt | r_3 < 4 | N/1/2/3 | `11` | P/1/2/P:4 | 2 / 6 |
| I-S+D | 3 | Erwarteter Einsatz fehlt | r_3 < 4 | N/1/2/3 | `01` | N/2/2/4 | 2 / 6 |
| G-S+D | 3 | Erwartetes Ende fehlt, Klang hält | 3 | N/1/2/3 | `01` | N/2/2/4 | 2 / 6 |

S+: I `11 10 11` oder `11 10 01`, je 6; G `11 10 01`, 6.
Die frühere SU-Rechnung (4 Bit ohne bezahlte Anfangspause) ist
für diesen Auftrag ausgeschlossen: Sie klammert die Anfangsinformation aus.
G-`11` bei 3 wäre wegen des weiterklingenden Tons unzulässig. Ein `01` nur
zur Beschreibung der Anfangshalbpause ist nicht begründet: Die Stille bei 1
ist bereits eine erwartbare P-Fortsetzung, kein neuer Anlass. Mit Δ=1
kann der erste Ton bei 2 ohne Teilungswechsel eingeordnet werden. Die
Foto-Summe 6 ist mit der nun festgelegten bezahlten Anfangspause
erreichbar, aber mit anderer Erkenntnisfolge und anderer Begründung
des Teilungswechsels. Die Grundsatzentscheidung über die Anfangskosten
ist geschlossen; die Erkennungspolitik bleibt gesondert zu bestimmen.

### 3.7 F05.6 – zwei Viertel, halbe Pause (Foto: 6 Bit)

Notation `0:1, 1:1`, Pause `[2,4)`. I: Onsets `{0,1}`. G: zusätzlich
hörbare Enden `{1,2}`. `10 11` unter I ist seit `835655e` ausdrücklich
als Kandidat festgelegt; dies wird hier nicht erneut zur Grundsatzfrage.

| Fall / Pfad | Zeitpunkt | Verfügbare Evidenz | Erkennung | Vorher | Anweisung | Nachher | Kosten / Summe |
| --- | --- | --- | --- | --- | --- | --- | --- |
| I/G | 0 | Erster Einsatz | 0 | U/1/0/– | `10` | N/1/0/1 | 2 / 2 |
| I/G | 1 | Regulärer Einsatz; G zugleich Ende | 1 | N/1/0/1 | keine | N/1/1/2 | 0 / 2 |
| I-P | 2 | Erwarteter Einsatz fehlt | 2 | N/1/1/2 | `11` | P/1/1/P:3 | 2 / 4 |
| I-P | 3 | Kein Einsatz, erwartbare P-Fortsetzung | 3 | P/1/1/P:3 | keine | P/1/1/P:4 | 0 / 4 |
| I-D | 2 | Erwarteter Einsatz fehlt | 2 | N/1/1/2 | `01` | N/2/1/3 | 2 / 4 |
| I-D | 3 | Erwarteter Einsatz fehlt erneut | 3 | N/2/1/3 | `11` oder `01` | P/2/1/– bzw. N/4/1/– (nächste Erwartung erst nach dem Taktwechsel) | 2 / 6 |
| G-P | 2 | Hörbares Ende ohne neuen Ton | 2 | N/1/1/2 | `11` | P/1/1/P:3 | 2 / 4 |
| G-P | 3 | Stille setzt sich fort | 3 | P/1/1/P:3 | keine | P/1/1/P:4 | 0 / 4 |

I: `10 11` kostet 4. Der D-Pfad kostet mit dem Anker am letzten Einsatz
6 (`10 01 11` bzw. `10 01 01`) und ist nicht mehr gleich günstig.
G: `10 11`, 4;
`01` allein bei 2 lässt unter K-G den Ton weiterklingen und verletzt das
hörbare Ende. `10 01 11` kostet 6; wenn beide Anweisungen am Ende bei 2
zugelassen werden, ist die Folge im Fenster darstellbar, unter K aber
teurer als die bereits vollständige Erklärung `10 11`. Die halbe Pause
liefert keine eigenständige Evidenz für das zusätzliche `01`.

**Abgelöste Variante A=0:** Mit festem Anker am Taktanfang läge die
nächste Erwartung nach `01` bei 4, und `10 01` hätte 4 Bit gekostet. Seit
der Ankerentscheidung vom 13. November 2026 gilt die Tabelle oben. Die
Ankerregel verändert hier eine Alternative, nicht den günstigsten Wert.
Im Punktierungsfall führen beide Regeln zum selben Ablauf, weil der erste
Einsatz bis zum zweiten auch der zuletzt gespielte ist.

### 3.8 F05.7 – halbe Pause, zwei Viertel (Foto: 8 Bit)

Notation Pause `[0,2)`, Noten `2:1, 3:1`. I: Onsets `{2,3}`.
G: zusätzlich hörbare Enden `{3,4}`; Ende 4 am Rand.

| Fall / Pfad | Zeitpunkt | Verfügbare Evidenz | Erkennung | Vorher | Anweisung | Nachher | Kosten / Summe |
| --- | --- | --- | --- | --- | --- | --- | --- |
| I/G, S+ | 0 | Anfang ohne Einsatz; bezahlte Initialisierung festgelegt | r_0 < 2, Politik offen | U/1/0/– | `11` | P/1/0/P:1 | 2 / 2 |
| I/G, S+ | 1 | Stille setzt sich fort | 1 | P/1/0/P:1 | keine | P/1/0/P:2 | 0 / 2 |
| I/G, S+ | 2 | Erster Einsatz auf erhaltenem Viertelgitter | 2 | P/1/0/P:2 | `10` | N/1/2/3 | 2 / 4 |
| I/G, S+ | 3 | Regulärer Einsatz; G auch Ende | 3 | N/1/2/3 | keine | N/1/3/4 | 0 / 4 |

S+: I=G `11 10`, 4. Der frühere SU-Pfad `10` mit 2 Bit ist
ausgeschlossen, weil er die Anfangspause nicht bezahlt. Die Fotofolge
`01 11 00 10` enthält zwei Teilungswechsel, deren bloße Begründung durch
die notierte Anfangshalbpause unzulässig ist. Eine tatsächlich beobachtete
zusätzliche Evidenz für diese Wechsel liegt hier nicht vor. Würde man
den Zustand zunächst aus anderem verbindlichem Kontext mit Δ=2 vorgeben,
müsste die Rückkehr bei 3 neu geprüft werden; ein solcher Kontext gehört
aber nicht zu diesem Auftrag. Weglassen von `10` bei 2 verletzt die feste
Zahlung des ersten Einsatzes; kostenlose Reaktivierung ist keine Alternative.

### 3.9 Kompakte Ergebnisse und Geltungsbereich

| Fall | Foto | Günstigste dargestellte I-Pfade unter K | Günstigster dargestellter G-Pfad unter K |
| --- | ---: | --- | --- |
| F05.1 | 2 | `10` = 2 | `10` = 2 |
| F05.2 | 6 | `10 11` = 4 | `10 01 11` = 6 |
| F05.3 | 6 | `10 01 00` / `10 11 10` = 6 | `10 01 00` = 6 |
| F05.4 | 4 | `10 11` / `10 01` = 4 | `10 01` = 4 |
| F05.5 | 6 | `11 10 11` / `11 10 01` = 6 | `11 10 01` = 6 |
| F05.6 | 6 | `10 11` = 4 | `10 11` = 4 |
| F05.7 | 8 | `11 10` = 4 | `11 10` = 4 |

Alle Zahlen sind Kandidatenkosten bzw. günstigste Werte **im dargestellten
Pfadvergleich unter K**. Die Spalte „nachgewiesenes Minimum unter vollständig
festgelegten Regeln“ wäre bei allen sieben Figuren derzeit **offen**.
Nicht nur eine Menge von Bitkosten, sondern die zulässigen Erwartungen,
Erkennungsentscheidungen und Endbedingungen müssen dafür feststehen.

Fotoabweichungen: F05.2.I 4 statt 6, weil der Impuls keine gehaltene Halbe
vorschreibt; F05.6.I/G 4 statt 6 ohne notationsbedingtes `01`;
F05.7.I/G 4 statt 8 ohne beide Pausenteilungswechsel. Anfangspausen
werden ausdrücklich bezahlt; die früher betrachtete SU-Verbilligung
ist kein zulässiges Ergebnis dieses Auftrags.
Gleiche Summen wie auf dem Foto belegen keine gleiche Erkennungsreihenfolge.


## 4. Punktierte Viertel plus Achtel unter G

Noten `0:3/2, 3/2:1/2`, hörbares Tonende und Neuanschlag bei 3/2;
das Tonende bei 2 ist als Randereignis zu erhalten. Bekannte weitere
Fortsetzung hier **nicht** festgelegt.
Nur den Abschnitt bis zum zweiten Einsatz betrachten. Der folgende Ablauf
nimmt `0 ≤ τ < 1/2` an und ordnet den Einsatz bei 3/2 nicht dem verspäteten
Viertel bei 1 zu. Beide Bedingungen sind zu wählende Modellparameter.

| Musikalischer Bezugspunkt | Tatsächlich / erwartet | Erkennung | Vorher → nachher | Neue Erwartung / Phase | Code | Kosten / kumuliert |
| --- | --- | --- | --- | --- | --- | --- |
| 0 | Erster Einsatz | 0 | `(U,1)→(N,1)` | A=0; nächster Einsatz 1 | `10` | 2 / 2 |
| 1 | Erwartetes Viertel-Offset bleibt aus (gehaltener Klang) | 1 gemäß Präzisierung; zuvor vorgeschlagen war `1+τ` für einen fehlenden Onset | `(N,1)→(N,2)` | A=0; nächster Einsatz 2 | `01` | 2 / 4 |
| 3/2 | Tatsächlicher Einsatz vor 2 | 3/2 | `(N,2)→(N,1)` | A=0; 3/2 passt noch nicht auf das Viertelraster | erstes `00` | 2 / 6 |
| 3/2 | Dieselbe Evidenz, zweite nötige Verstärkung | 3/2 | `(N,1)→(N,1/2)` | A=0; 3/2 passt, nächste Erwartung 2 | zweites `00` | 2 / 8 |

Die Übertragung auf kurz ausklingende Schläge und das Verhältnis von
Offset-Erkennung und Onset-Frist bleiben zu klären. Für den gehaltenen
Ton gilt die präzisierte Offset-Erkennung bei 1.

Die beiden `00` sind ein durch dasselbe Ereignis begründeter Änderungsblock;
kein erfundener Zeitraum zwischen ihnen. Es bleiben **6 Bit Änderung** plus
**2 Bit erster Einsatz**. Die 6 sind aus der Diskussion abgeleitet, nicht
auf dem Foto notiert. Die alte Viertelerwartung und ihr bezahltes `01`
bleiben erhalten. Verfeinerung erzeugt nicht nachträglich Auslassungen
bei 1/2 oder 1. Keine automatische Rücksetzung an einer Beatgrenze.

**Bestätigt:** Der erste Einsatz bleibt Anker; nach `01` wird ein Einsatz
bei 2 erwartet. Der Einsatz bei 3/2 führt durch `00 00` zum Achtelabstand,
ohne den Anker zu verschieben. Die entsprechende Ankerführung in der
Tabelle ist damit für diesen Fall festgelegt.

**Einordnung in die Ankerregel vom 13. November 2026:** Der Einsatz bei
3/2 wird gegen das Raster des Ankers 0 eingeordnet; danach ist er selbst
der Anker. Die nächste Erwartung bleibt 2. Die Spalte „A=0“ beschreibt
also den Anker während der Einordnung.

**Noch offen:** Zuordnungsfenster und allgemeine Festschreiberegel.
Bei einer stattdessen verwendeten Onset-Ausbleibepolitik mit `τ ≥ 1/2`
kommt der zweite Einsatz vor oder zugleich mit deren Ausbleibeentscheidung. Dann ist dieselbe Anweisungsfolge nicht herleitbar.
Sie darf nicht trotzdem als universeller Wert ausgegeben werden. Über
Kosten nach 3/2 sagt diese Tabelle nichts: Das Ende bei 2 oder eine
Fortsetzung benötigen einen separaten Auftrag.

## 5. Foto 1: erster Rhythmus unter I

Nach korrigierter Lesung Einsätze bei
`0,1,3/2,2,5/2,11/4,3,13/4,7/2`.
Eine mögliche **Hörrechnung für Impulsklang** mit kleinen, passend
gewählten Zuordnungsfenstern und `τ < 1/4` lautet:

| Evidenz | Erkennung | Δ vorher → nachher | Erwartung / Erklärung | Kosten / Summe |
| --- | --- | --- | --- | --- |
| Erster Einsatz 0 | 0 | 1→1 | N aktivieren; erwartet 1 | `10`: 2 / 2 |
| Regulärer Einsatz 1 | 1 | 1→1 | erwartet 2 | 0 / 2 |
| Zusätzlicher Einsatz 3/2 | 3/2 | 1→1/2 | mit Anker 1 passend; erwartet 2 | `00`: 2 / 4 |
| Einsätze 2 und 5/2 | jeweils beim Auftreten | 1/2→1/2 | regulär; danach erwartet 3 | 0 / 4 |
| Zusätzlicher Einsatz 11/4 | 11/4 | 1/2→1/4 | Anker 5/2; danach erwartet 3 | `00`: 2 / 6 |
| Einsätze 3,13/4,7/2 | jeweils beim Auftreten | 1/4→1/4 | danach erwartet 15/4 | 0 / 6 |
| Erwarteter Einsatz 15/4 fehlt | `15/4+τ < 4` | 1/4→1/2 | Anker 7/2; nächster Einsatz 4 liegt außerhalb | `01`: 2 / 8 |

Die Übereinstimmung der Summe mit dem Foto gilt nur unter diesen
Zusatzannahmen. Ein Analyseende direkt nach 7/2 oder eine bis über 4
reichende Frist liefert nicht denselben abgeschlossenen Nachweis.
Die Rechnung zeigt einen Kandidatenpfad, noch keinen Nachweis seiner
Minimalität unter allen zulässigen Interpretationen.

Beim zweiten Rhythmus fehlt unter I der Einsatz bei 1; aus seiner notierten
Achtelpause folgt kein zusätzliches hörbares Tonende. Unter G kommt dagegen
das hörbare Ende der ersten Viertel bei 1 hinzu. Die bestätigte Foto-Codefolge
lautet `10 00 11 10 00 01` = 12 Bit. Beide Klangfälle sind separat zu
analysieren; insbesondere darf die Pausengliederung in I keinen `00`-Befehl
erzwingen. Die genaue günstigste Hörrechnung ist noch zu bestimmen.

## 6. Weitere notwendige Gegenproben

| Prüffall | Klangbedingung | Zu entscheidende bzw. zu bewahrende Eigenschaft |
| --- | --- | --- |
| Längeres Ausbleiben | I; in G als eigener Fall mit explizitem Tonende | Nach einem Einsatz bei A und Start Δ=1 liegen die Erwartungen nach je einem `01` bei A+2, A+4, A+8…; je Ausbleiben nur eine Stufe. Ein Taktwechsel setzt den Anker auf den Taktanfang. Nicht bei jedem ursprünglichen Viertelschlag automatisch `01` zählen. |
| Halbe vs. Viertel plus Viertelpause | G | Gleiches Onset, hörbares Ende bei 1 statt 2; darf nicht äquivalent normalisiert werden. Verschiedene Tonenden und Erklärungen erhalten; gleiche Summen sind zulässig. |
| Halbe vs. Viertel plus Viertelpause | I | Bei gleichen Impulsen gleiche Bewertung; keine allein grafisch begründete Pause. |
| Bindung über die Taktgrenze | G | Klang hält über die Grenze; kein neuer Einsatz aus bloßem Taktstrich. Zustand nicht automatisch zurücksetzen. |
| Neue Taktgrenze kurz vor einer Ausbleibefrist | I | Abschluss und zeitliche Erkennung dürfen sich nicht widersprechen. |
| Triole bei 0,1/3,2/3 | I | Zeitpunkt der Erkennung offen; erster Einsatz allein belegt keine Dreiteilung. Aktivierungsanweisung und 3-Bit-Wert noch Vorschläge. |
| Fortgesetzte Triole im nächsten Takt | I | Wörterbuch soll wieder 2 Bit sein, Abstand kann noch ternär sein. Welche Operation ist dann verfügbar und wann erneut aktivieren? |

Keiner dieser offenen Fälle wird durch einen Sollwert aus `description-v1`
oder eine Multiplikation des alten Symbolmaßes geschlossen.

## 7. Klangbezogene Gegenproben außerhalb der sieben Hauptrechnungen

Die Hauptfälle stehen jetzt vollständig in Abschnitt 3. Für Foto 1 bleiben
zusätzlich die folgenden Klangunterschiede zu erhalten:

| Fall | I: Onsets | G: dieselben Onsets plus Enden | Konsequenz |
| --- | --- | --- | --- |
| F03, Foto 1 / 1 | 0,1,3/2,2,5/2,11/4,3,13/4,7/2 | 1,3/2,2,5/2,11/4,3,13/4,7/2,4 | I-Kandidatenrechnung in Abschnitt 5; G-Enden zusätzlich erhalten. |
| F04, Foto 1 / 2 | 0,3/2,2,5/2,11/4,3,13/4,7/2 | 1,2,5/2,11/4,3,13/4,7/2,4 | G hat eine hörbare Lücke von 1 bis 3/2; I hat keine kontrollierte Abschaltung bei 1. |

### 7.1 Gleiche Stille, andere Pausenschreibweise

F05.6.I und F05.6.G werden je mit halber Pause bzw. zwei Viertelpausen in
`[2,4)` verglichen. Die Evidenz bei 0,1,2 ist gleich; keine neue Evidenz
entsteht durch die grafische Grenze bei 3. P setzt sich unverändert fort:
`10 11`, Kandidatenkosten 4 in beiden Schreibweisen. Gleiches gilt für
F05.7.I/G mit halber Pause bzw. zwei Viertelpausen in `[0,2)`: unter S+
`11 10`, 4. SU ist wegen der unbezahlten Anfangsinformation ausgeschlossen.
Ein `01` und dessen spätere Rücknahme `00`
nur zur Darstellung der halben Pause sind hier ausgeschlossen.

### 7.2 Halbe gegen Viertel plus Viertelpause

Gemeinsames Fenster `[0,2)`; Variante H: `0:2`; Variante QP: `0:1`, dann
Pause `[1,2)`. Gleicher bekannter Viertelbezug, erster Einsatz bei 0.

- **I:** H und QP erzeugen denselben kurzen Impuls bei 0. Bei 1 fehlt in
  beiden Fällen der erwartete nächste Einsatz. Kandidaten `10 11` und
  `10 01` kosten je 4 für den Verlauf im Fenster. Die Notation allein darf
  keinen anderen Wert erzeugen.
- **G-H:** Ton hält bis 2. Bei 1 fehlt das erwartete Offset, `01` ergibt
  `10 01` = 4. `11` bei 1 wäre wegen des klingenden Tons unzulässig.
- **G-QP:** Ton endet hörbar bei 1. `11` erklärt dies und die folgende Pause:
  `10 11` = 4. `01` allein würde das Ende nicht realisieren und ist unzulässig.

**Entschieden – gleiche Summen zulässig:** Die frühere Forderung
`I(G-QP) > I(G-H)` ist durch die Entscheidung des Projektinhabers abgelöst.
H und QP bleiben durch das hörbare Ende und `10 01` bzw. `10 11`
unterscheidbar, obwohl beide hier zwei Anweisungen und damit 4 Bit benötigen.
Hörbare Verschiedenheit verlangt keine unterschiedliche Anweisungsanzahl.
Es werden keine Zusatzkosten zur Herstellung einer strikten Rangfolge
angesetzt. Der noch ausstehende allgemeine Minimalitätsnachweis ist davon
unabhängig; die verschiedenen Tonenden dürfen weiterhin nicht verschwinden.

### 7.3 Anker und kausale Vergangenheit

Nach K-A zählt das Raster vom zuletzt gespielten Einsatz. In F05.3 wird
bei 3 von Δ=2 auf Δ=1 verfeinert. Das neue Viertelgitter wird nicht
rückwirkend bei 1 geprüft; weder `11` noch ein neuer Auslassungsbetrag
wird dort nachgefordert. Die tatsächlich gewählte Information bei 1 bleibt
im jeweiligen Pfad erhalten. Es gibt keinen Wechsel zwischen bereits
bezahlten Pfaden, der frühere Kosten löscht.

F05.6 zeigt, dass A=0 und ein zuletzt artikulierter Anker A=1 die nächste
Erwartung nach `01` verschieden setzen (4 gegenüber 3). **Entschieden am
13. November 2026:** Es gilt der zuletzt gespielte Einsatz. Der bekannte
metrische Bezug bleibt dadurch erhalten, dass jeder Taktwechsel den Anker
auf den Taktanfang zurücksetzt und bei N die Eins erwartet wird. Ein
verschobener Puls ist innerhalb des Takts nach der ersten Verschiebung
regulär und bezahlt die ausbleibende Eins in jedem Takt neu. Der frühere
Vorschlag, das erste Gitter beizubehalten, ist abgelöst. Keine neue
Ankerpolitik wird in den Bestandsalgorithmus übertragen.

## 8. Gegenprüfung am bestätigten Punktierungsfall und am Analyseende

### 8.1 Punktierung: zulässige Konkurrenz und unzulässige Billigregeln

Klang G, Onsets 0 und 3/2, Tonende bei 3/2, Randende bei 2. Bestätigt ist
A=0, `10` bei 0, fehlendes erwartetes Offset bei 1 → `01`, danach Erwartung
2; bei 3/2 `00 00` bis Δ=1/2. Summe bis zum zweiten Einsatz: 8 Bit.

- K-A: Bis zum zweiten Einsatz ist der erste der zuletzt gespielte, also
  A=0; kein Konflikt mit dem bestätigten Anker.
- K-G verbietet `11` bei 1, weil der Ton bis 3/2 weiterklingt. Eine neue
  Regel „bei Ausbleiben immer `11`“ würde hier die hörbare Fortdauer
  verletzen und ist daher abzulehnen. Dass `11` kein Tonende voraussetzt,
  erlaubt nicht die Entfernung eines tatsächlich gehaltenen Klangs.
- Für **I mit denselben Onsets** ist dagegen unter K-P eine Konkurrenz
  möglich: `10` bei 0, `11` bei r_1, bei 3/2 auf dem Viertelgitter A=0
  zunächst `00` (Δ=1/2), dann `10` zur Rückkehr zu N. Das kostet ebenfalls
  8 Bit. Ohne `00` wäre 3/2 kein Punkt des erhaltenen Viertelgitters.
  Die Gleichheit der Summen legt keinen gemeinsamen Erkenntnispfad fest.
  Der ausdrücklich bestätigte `01 00 00`-Pfad bleibt als Referenz erhalten;
  die alternative I-Auswahl wird nicht als neuer bestätigter Pfad ausgegeben.
- Bei `01` nach 1 ist das sofortige Zurückspringen von Δ=2 auf Δ=1/2 mit
  nur einem `00` unzulässig: Jede Operation halbiert genau einmal.
- Ein globaler Rückblick, der von Anfang an Δ=1/2 setzt und die Änderung
  bei 1 löscht, widerspricht dem festgelegten Viertelbeginn und der bereits
  entstandenen Information. V07 darf deshalb nicht nur das billigste
  rückblickend passende Gitter auswählen.

Damit besteht der K-Vorschlag die konkrete Anker-/Schrittprüfung. Eine
allgemeine kausale Auswahlregel ist dadurch noch nicht bewiesen.

### 8.2 Analysegrenze: kein musikalischer Änderungsanlass

**Festgelegt:** Kein zusätzliches `11` allein wegen des Fensterendes.
Der Zustand wird in einer fortlaufenden Analyse weitergeführt. Tatsächliche
Ereignisse an der Grenze werden dort regulär ausgewertet. Für F05.1
bleibt die Rechnung für `[0,4)` daher `10` = 2 Bit; aus der bloßen Grenze
wird kein Pfad `10 11` = 4 Bit.

Ein tatsächliches G-Tonende bei 4 darf dennoch einen regulären Zustandswechsel
begründen, falls der Klang dort endet und kein nahtloser Neuanschlag folgt.
Ob und welche Anweisung erforderlich ist, folgt der Ereignis- und
Übergangsregel bei 4, nicht der Wahl des Analyseausschnitts. Bei `[0,4)`
und anschließender Fortsetzung ab 4 wird dieses Ereignis an 4 ausgewertet,
mit dem vorherigen Zustand als Eingang. Ohne Fortsetzungsdaten wird weder
Stille noch ein weiterer Einsatz erfunden.

Die zuvor gerechnete Variante E-P (am Ende stets P herstellen und bezahlen)
ist damit als allgemeine Fensterabschlussregel ausgeschlossen. Sie kann
nur einen ausdrücklich zusätzlich verlangten musikalischen Stopp beschreiben.
Auch SU ist ausgeschlossen: Anfangsstille bei bekanntem Takt und Metrum
gehört zum Auftrag und wird mit `11` bezahlt. Beide ausgeschlossenen
Varianten werden nicht mehr in den aktiven Ergebnistabellen geführt.

## 9. Entschiedene Fragen und verbleibender Regelbedarf

| Frage | Entscheidung des Projektinhabers | Folge für F05 |
| --- | --- | --- |
| Anfangspause | `11` bezahlen; Takt und Metrum stehen fest. | F05.5 beginnt mit 2 Bit Anfangsinformation; F05.7 ebenso. SU entfällt. |
| Verschiedene Klangverläufe, gleiche Summe | Gleiche Summen zulassen; Ereignisse und Befehle bleiben unterscheidbar. | G-H `10 01` und G-QP `10 11` dürfen beide 4 Bit kosten. Frühere strikte Ungleichung abgelöst. |
| Abschluss am Fensterende | Kein `11` allein wegen der Grenze; Zustand weiterführen, tatsächliche Grenzereignisse regulär auswerten. | Kein pauschaler Aufschlag auf die Ergebnisse für `[0,4)`. E-P als allgemeine Abschlussregel entfällt. |

Diese Fragen werden nicht erneut als offene Grundsatzfragen gestellt.
Am 13. November 2026 kamen hinzu: Ankerregel (K-A), Erkennung am
erwarteten Rasterpunkt und die Schrittweite der Teilungswechsel.
Ebenfalls am 13. November 2026 wurde der Pausenzustand (K-P) bestätigt.
Weiter offen bleiben die noch unbestätigten Teile von K-G,
die Erkennungspolitik für gespielte Musik und die Übergangsregeln im Einzelnen. Insbesondere bestimmt das Bezahlen der Anfangspause
noch keine Wahrnehmungslatenz. Diese Punkte werden durch die Entscheidungen
nicht unbemerkt ergänzt; ein Implementierungsplan folgt hieraus noch nicht.

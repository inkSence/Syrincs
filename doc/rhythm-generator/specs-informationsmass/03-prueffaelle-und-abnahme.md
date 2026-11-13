# 03 – Prüffälle, Gegenproben und Abnahme

Grundlagen: [Rahmen](README.md), [M01–M07](01-mass-und-beschreibung.md)
und [A01–S03](02-analyse-und-generierung.md).
Die Zahlen gelten für den dort definierten Vorschlag, nicht für einen
bereits implementierten neuen Analyzer. Die
[Fixtures](fixtures-description-v1.json) enthalten die vollständigen
Onsets, Bedingungen und kanonischen Codewörter.

## P01 – Nachvollziehbare Grundwerte und Codevergleich

Die ersten neun Folgen haben jeweils N=16. Vierergruppen dienen in dieser
Tabelle der Lesbarkeit. Für J werden ausdrücklich Viererfenster, leerer
Anfangskontext und `suffix-periods-16-v1` gewählt. Tempo ist UNKNOWN;
die strukturelle Rechnung benötigt es nicht.

| Onsets | Legacy: Codesymbole | Neues globales D: Bits | Kausales J-Profil: Bits |
| --- | ---: | ---: | --- |
| `oooo oooo oooo oooo` | 0 | 3 | `[4,2,2,2]` |
| `xooo xooo xooo xooo` | 1 | 13 | `[8,8,3,3]` |
| `xoxo xoxo xoxo xoxo` | 5 | 11 | `[8,2,2,2]` |
| `xxxx xxxx xxxx xxxx` | 9 | 3 | `[4,2,2,2]` |
| `xxox xxox xxox xxox` | 17 | 13 | `[8,8,2,2]` |
| `xooo oooo xooo xooo` | 3 | 19 | `[8,2,8,8]` |
| `xooo oooo oooo oooo` | 2 | 13 | `[8,2,2,2]` |
| `xooo xoxo xooo xoxo` | 3 | 17 | `[8,8,8,8]` |
| `xoxo xooo xoxo xooo` | 3 | 17 | `[8,8,8,8]` |

Die Legacy-Spalte stammt aus den bestehenden
[Vertragstests](../../../src/test/java/syrincs/a_domain/rhythm/HuffmanRhythmTest.java).
Die beiden neuen Spalten sind berechnete Vorhersagen unserer eigenen
Sprache. Ihre Gleichheit oder Rangfolge ist kein empirisches Hörresultat.

**Erklärung für regelmäßige Viertel:** Der kanonische Code lautet
`011 | 011 | 010 | 1000`: REPEAT, Periode p=4 (p−1=3, drei Bits),
LITERAL des vierstelligen Motivs. Das ergibt `3+3+3+4=13` Bits.
REPEAT zählt die vollständige neue Motivbeschreibung zu den Modellkosten;
hier sind deshalb Modellkosten 13 und Restkosten 0.

Bei regelmäßigen Achteln reicht p=2: `011 | 001 | 010 | 10`, also 11 Bits.
FULL beschreibt Einsätze auf jeder bekannten Rasterposition mit drei Bits.
EMPTY beschreibt ebenso kurz das bekannte Fenster ohne Einsatz. Damit
ist die neue Größe weder eine Onset-Zählung noch eine Umbenennung der
Legacy-Zahlen. Hohe Dichte erzwingt keine hohe Beschreibungslänge.

**Erklärung für das Viertel-J-Profil:** Die ersten beiden Fenster benötigen
DIRECT mit `1+D(1000)=8` Bits. Danach liefert H=`10001000` die Vorlagen
`[0,1000]`: Die letzten beiden Nullpositionen stützen p=1, zwei vollständige
Motive p=4. Die Fortsetzung 1000 lässt sich durch `10 | 1` bestätigen,
also drei Bits. Die zusätzliche Nullvorlage verteuert die Auswahl um
ein Indexbit; daraus folgt keine größere Hörschwierigkeit von Vierteln.

Weitere Grenzen in den Fixtures: N=1 mit EMPTY/FULL, N=2/3 ohne Takt,
periodische Motive mit p=5 und p=7 sowie N=128 mit 32 Viertelmotiven.
Der letzte Fall hat D=16; die technische Grenze ist kein Acht-Takt-
Wahrnehmungsgesetz. Alle Konstruktoren, einschließlich PATCH und CONCAT,
werden durch die Beispiele und die vollständige N=16-Prüfung erfasst.

**Gleichstand:** Bei `xoxoxox` (N=7) sind LITERAL, REPEAT mit p=2 und
PATCH mit p=2 ohne Ausnahme je zehn Bits lang. Die drei minimalen Codes
sind `0101010101`, `0110101010` und `1010110000`; der erste ist kanonisch.
Der Analyzer muss drei Lösungen erkennen, auch wenn seine Anzeige nur
einen Beleg zeigt. Daraus folgen keine drei gehörten Metren.

## P02 – Philipps ursprüngliche Hypothesen berechnen

Die Fälle aus [informationsbegriff.md](../informationsbegriff.md#erste-prüffälle)
behalten ihr Fenster `[0,8)` in ursprünglichen Zeiteinheiten. Das gemeinsam
festgelegte Raster hat Abstand 1/2, folglich N=16. Eine ursprüngliche
Einheit entspricht zwei Positionen; es wird kein absoluter BPM-Wert
unterstellt. Lokale Fenster haben vier Positionen.

| Ursprünglicher Fall | D | Kausales Viererprofil |
| --- | ---: | --- |
| Regelmäßig alle ganzen Einheiten | 11 | `[8,2,2,2]` |
| Dasselbe ohne Ereignis bei 4 | 17 | `[8,2,8,8]` |
| Doppelte Rate ab 4 | 19 | `[8,2,4,2]` |
| Halbe Rate ab 4 | 19 | `[8,2,8,8]` |
| Doppelte Rate von Beginn an | 3 | `[4,2,2,2]` |
| Keine Ereignisse | 3 | `[4,2,2,2]` |
| Ein Ereignis bei 4 | 16 | `[4,2,8,2]` |

Diese Beispiele machen den gewünschten Gedanken ausführbar: Eine
Verdopplung und eine Halbierung können relativ zu einer aufgebauten
Regel zusätzliche Beschreibung benötigen. Die Verdopplung wird anschließend
wieder kurz bestätigt. Beide Änderungen müssen weder gleich teuer sein
noch in jeder denkbaren Vorgeschichte höhere Kosten haben.

Kein Test darf die Kosten so verändern, dass eine erwünschte Hörthese
automatisch wahr wird. Eine alternative Erklärung oder Kostenordnung wird
als Gegenbefund dokumentiert und verlangt eine neue Modellversion.

## P03 – Taktvertrag und Zukunftswissen

Für zwei Vierteltakte, also `xooo` achtmal, ergeben sich:

```text
globalDescription.totalBits = 14
isolatedBars = [13,13]; isolatedTotal = 26
causalFourSlotWindows = [8,8,3,3,3,3,3,3]
causalBarContributions = [22,12]; continuationTotal = 34
```

Der globalen Beschreibung reicht eine einmalige Motivbeschreibung mit
einem nun vierstelligen Periodenfeld. Die isolierten Takte kaufen das
Motiv zweimal; der kausale Verlauf bestätigt es je Fenster. Dass die
Blockbeschreibung länger ist, folgt aus dem gewählten Nachrichtenvertrag.
Keiner dieser Werte ersetzt still einen anderen.

Für zwei Achteltakte gilt global D=12, isoliert `[11,11]`, kausaler Verlauf
`[8,2,2,2,2,2,2,2]` und Taktbeiträge `[14,8]`. Für zwei Nulltakte sind es
global D=3, isoliert `[3,3]`, kausal `[4,2,2,2,2,2,2,2]` und Beiträge
`[10,8]`. Im Legacy-Viertelfall bleiben die isolierten Takte `[1,1]`
gegenüber kontinuierlich gruppierten `[1,0]` bestehen.

**Kein Zukunftswissen:** Die erste Hälfte von `xooo` viermal, gefolgt von
`xoxo` viermal, hat dasselbe kausale Profil wie ein isolierter Vierteltakt:
`[8,8,3,3]`. Änderungen der späteren Hälfte dürfen diese Werte nicht
ändern. Der gesamte neue Verlauf lautet `[8,8,3,3,8,2,2,2]`; das globale
D der ganzen Folge ist 32 und braucht diese Summe nicht zu ergeben.

**Kein positives Profil aus Präfixdifferenzen:** Für
`xoooxoxoxoooxox` (N=15) ist D=18. Das ergänzte Nullbit ergibt
`xoooxoxoxoooxoxo` (N=16) mit D=17. Die Differenz ist −1.
Die neue vollständige Wiederholung ermöglicht eine günstigere Regel;
es gibt keine negative lokale J-Nachricht.

**Streuung:** Das Viertelprofil `[8,8,3,3]` besitzt Mittelwert 5,5,
Populationsstandardabweichung 2,5 und Peaks `[1,2]`. Bei zwei Vierteltakten
ist die Beat-Streuung `sqrt(4,6875)`, nicht der Mittelwert der beiden
Taktstreuungen 2,5 und 0. Streuungen sind nicht additiv.

Neue Filter müssen das Viertelprofil sowohl bei Deviation-Minimum 2,5
als auch bei Maximum 2,5 einschließen. Maximum 2,499 schließt es aus.
Die Entscheidung erfolgt mit exakter Varianz, unabhängig von Anzeige-
rundung. NaN, unendliche und negative Grenzen sind Eingabefehler.

## P04 – Die acht Forschungsprüffälle abdecken

Die [vorhandenen Modellprüffälle](../modellprueffaelle-beat-ebene-und-unterteilung.md)
sind keine neuen empirischen Daten. Ihre Zeitpunkte werden ohne Rundung
überführt. Für Vergleiche in einem festen 6000-ms-Fenster wird gemeinsam
ein 100-ms-Raster gewählt. Die Richtung variabler IOIs braucht ein
50-ms-Raster, N=80; deshalb umfasst der erste Analyzer bis zu 128 Positionen.

| Forschungsfall | Ausführbare Gegenprobe und Erwartung |
| --- | --- |
| Rate und konkurrierende Ebenen | N=60, Abstand 100/200/300/600 ms: D=`3/13/14/17`. Kein Wert wählt einen gehörten Beat. Bei Abstand 200 ms sind T=200/400/600/800 ms explizite konkurrierende Referenzen. Die zeitliche Profilprüfung kann sie unterscheiden; D bleibt gleich. |
| Ereigniszahl gegen Dauer | Bei zwölf Ereignissen und ausdrücklich je einem Rasterplatz pro Ereignis sind beide Folgen FULL: N=12 und D=3, jedoch Rasterdauer 200 bzw. 600 ms. Im gemeinsamen 6000-ms-Fenster liegen dagegen 30 bzw. 10 Ereignisse; Werte und Bedingungen dürfen nicht vermischt werden. |
| Kurze Gruppe gegen Wiederholung | `{0,200}` und `{0,200,400}` in `[0,1000)` ergeben im 100-ms-Raster jeweils D=13. Beide dürfen ohne behauptetes Metrum ausgegeben werden; gleiches D beweist keine gleiche Organisation. |
| Gleiche Fortsetzung, anderer Vorlauf | Vorlauf A alle 400 ms, B alle 600 ms; je 3600 ms. Die gemeinsame Fortsetzung alle 200 ms wird als N=36-Block mit ihrem jeweiligen H geprüft. Hier ergeben beide D=13 und J=14. Der strukturelle Code unterscheidet diesen gesamten Fortsetzungsblock numerisch nicht; metrische Bezugsmöglichkeiten bleiben verschieden. |
| Ratewechsel und dauerhafte Unterteilung | Fünf 600-ms-Abstände im Vorlauf, danach doppelte/halbe Rate im 3000-ms-Fenster: D=`13/20`, J für den ganzen neuen Block=`14/21`. Aufbau und spätere Bestätigung werden zusätzlich mit kürzeren benannten Fenstern geprüft; deren Summe ist ein anderer Scope. |
| Auslassung | Im gemeinsamen 6000-ms-Fenster alle 600 ms: D=17. Ohne 3000-ms-Ereignis: D=26. Ein günstiger PATCH-Code muss die Ausnahme vollständig rekonstruieren. |
| Richtung der Tempoänderung | Beide kumulierten Onset-Folgen aus denselben zwölf IOIs in umgekehrter Ordnung erhalten D=83 bei N=80. Kein empirischer Hysterese-Effekt wird daraus errechnet. Lokale Profile dürfen zusätzlich verglichen werden, jedoch ohne vorgegebene Rangfolge. |
| Kein erschließbarer Puls | Leeres 6000-ms-Fenster: D=3. Einzelner Onset bei 3000 ms: D=21. Kein Zwang zur Ausgabe eines erschlossenen Beats. |

Im Ereigniszahlvergleich wird die Rasterdauer ausdrücklich verändert.
Dies zeigt Tempo-Invarianz bei identischem Rasterwort, keinen kostenlosen
automatischen Rasterwechsel in einer Optimierung. Für einen direkten
Kostenvergleich verschiedener Ereignisraten ist der erste Fall mit
gemeinsamem Raster maßgeblich.

Der Vorschlag behauptet nicht, alle Kontextunterschiede der Literatur zu
erklären. Insbesondere sind gleiche D/J-Werte im vierten und siebten Fall
beabsichtigt offengelegte Modellgrenzen. Eine Erweiterung darf Unterschiede
nur mit einer expliziten, geprüften zusätzlichen Politik erzeugen.
Der bisherige Codevorrat besitzt auch keine Regel für arithmetisch
veränderte IOIs: Ein Accelerando kann deshalb hier direkt beschrieben
werden, obwohl eine andere Sprache seine regelmäßige Veränderung kürzer
ausdrücken könnte. D ist ein Minimum dieser Sprache, kein Urteil über
jede zeitliche Regelmäßigkeit.

## P05 – Tempo und metrische Bedingungen

Für einen 4/4-Rahmen mit r=4 und 120 Viertel-BPM gilt Rasterdauer 125 ms.
Bei 108 BPM sind es exakt `1250/9 ms`. Derselbe Onset-String und dieselbe
rasterbezogene Vorgeschichte MUSS dasselbe D und J liefern. Die in ms
ausgedrückte Vorgeschichte ist dazu ausdrücklich passend umzuskalieren;
unterschiedliche tatsächlich gehörte Vorläufe werden nicht gleichgesetzt.

Die vorgegebene Referenz mit cycleSlots=16, Phase 0, Beats `[0,4,8,12]`
und aktiver Unterteilung an allen 16 Positionen besteht in beiden Fällen
`london-time-window-v1`. UNKNOWN-Tempo ergibt `NOT_EVALUABLE`.
Keine daraus abgeleitete Behauptung, ein Hörer nehme diese Referenz an.

Weitere verbindliche Prüfungen:

- 6/8 mit r=4 ergibt 12 Positionen, Tactus `[0,6]`; nicht vier Viertelbeats.
- 150 Viertel-BPM im obigen 4/4-Rahmen: kleinster Abstand 100 ms und
  Beat-Abstand 400 ms erfüllen die einschließlich definierten Grenzen.
- Referenz mit cycleSlots=20, Rasterdauer 50 ms und Beats `[0,10]`:
  Beats 500 ms und Zyklus 1000 ms, aber aktive Unterteilung aller Slots
  verletzt mit 50 ms den gewählten Mindestabstand. Wird das technische
  Raster nicht als aktive Ebene angegeben, ist diese Verletzung nicht
  automatisch zu unterstellen.
- cycleSlots=20, Rasterdauer 250 ms, Beats `[0,4,8,12,16]`:
  Zyklusdauer exakt 5000 ms besteht; bei 250,5 ms scheitert nur die
  Zyklusgrenze, nicht der Beat-Abstand von 1002 ms.
- Nichtisochrone Ebenen sind darstellbar; zyklische Randabstände werden
  geprüft. Das Zeitprofil behauptet keine Prüfung von maximal evenness.
- Dauer einer langen Phrase wird nicht gegen die Zyklusgrenze geprüft.
- Eine zusätzliche gröbere Ebene, deren Punkte keine Teilmenge der
  feineren sind, ist strukturell ungültig und kein Hörgrenzfall.

## P06 – Generierung mit gemeinsamen Zielwerten

Ein vollständig überprüfbarer erster Auftrag lautet:

```text
measure = description-v1; scope = GLOBAL_DESCRIPTION
N = 16; initialContext = leer
informationRange = [13,13]
onsetCountRange = [4,4]       # äquivalent densityRange=[1/4,1/4]
requiredPositions = {0,4,8,12}
search = EXHAUSTIVE; selection = FIRST
resultLimit = 8
```

Erwartung: genau `xooo xooo xooo xooo`, D=13, `totalMatches=1`,
`returnedCount=1`, `searchComplete=true`, `stopReason=COMPLETE`.
Die Differenz zur angeforderten Anzahl 8 ist ausdrücklich sichtbar.
Die Positions- und Anzahlbedingungen beweisen die Eindeutigkeit bereits;
eine vollständige Referenzsuche muss denselben Befund liefern.

Weitere Generatorabnahmen:

- Derselbe Auftrag mit Ziel `[12,12]`: logisch zulässiger Grad, aber kein
  Treffer unter diesen Positionsbedingungen; vollständig `NO_SOLUTION`.
- Ohne weitere Filter liefert N=16, D=13 genau **42** verschiedene Folgen.
  Ein Limit von 5 ändert diese Trefferzahl nicht.
- EMPTY und FULL erfüllen beide D=3; eine Dichtebedingung unterscheidet
  sie. Information allein beschreibt nicht ihre musikalische Verwendung.
- Ein Konstruktor darf Viertel als LITERAL mit Länge 19 vorschlagen.
  Für Ziel D=19 ist diese Folge trotzdem zu verwerfen: ihr Minimum ist 13.
- Ein exaktes kausales Profil `[8,8,3,3]` erzwingt die passende Summe 22.
  Mit Gesamtziel 21 ist der Auftrag widersprüchlich, nicht eine leere Suche.
- Eine zweite Phrase startet wieder mit demselben initialContext;
  vorher getestete Kandidaten beeinflussen ihre Werte nicht.
- Bei BOUNDED_RANDOM darf ein Budget ohne Treffer nur die beobachtete
  Teilmenge beschreiben. Jede ausgegebene Folge erfüllt dennoch alle
  Bedingungen bei erneuter exakter Analyse.
- Duplikate verbrauchen Versuche, erscheinen aber nicht doppelt.
  Wiederholte Ausführung mit gleichem Seed und Zählbudget ist identisch.
- RANDOM_WITHOUT_REPLACEMENT liefert keine doppelten Folgen und verwendet
  auch bei nur einem verbleibenden Treffer genau einen `nextInt(1)`-Aufruf.
- Ein geforderter Peak 2 erlaubt Gleichstände einschließlich Peak 1;
  ein niedrigerer Wert an Position 2 verfehlt die Bedingung.
- Minimaler Onset-Abstand gilt linear, größte Nullfolge auch am Rand.
  Keine heimliche zyklische Gleichsetzung von Anfang und Ende.
- CODE_LENGTH_SEARCH wird für kleine globale, isolierte und kausale
  Aufträge gegen eine vollständige Maskensuche verglichen. Bei N=128,
  D=16, genau 32 Onsets und Pflichtpositionen `{0,4,...,124}` rekonstruiert
  die Codesuche genau die Viertelfolge. Die 65.536 Codewörter der Länge 16
  reichen zur vollständigen Abdeckung dieses Ziels; `2^128` Masken müssen
  dafür nicht einzeln besucht werden.

## P07 – Nachweisumfang der Referenzrechnung

Für diese Dokumente wurde eine temporäre, von der Java-Produktion getrennte
Referenzrechnung mit Encoder, Decoder und dynamischem Minimum ausgeführt:

- alle 65.536 Bitfolgen bei N=16 vollständig analysiert und rekonstruiert;
- eindeutige kanonische Codes und keine Präfixbeziehung zwischen Codes
  verschiedener N=16-Folgen geprüft;
- 1708 PATCH-Fälle für N=2..8: Mehrheitsvorlage je Periode gegen alle
  `2^p` möglichen Vorlagen verglichen;
- für alle 510 Folgen mit N=1..8 Minimum und kanonischen Code zusätzlich
  gegen eine zweite Rekurrenz mit vollständiger Vorlagenenumeration geprüft;
- 64.386 bedingte Nachrichten: alle binären Vorläufe von Länge 0..8,
  alle Zielblöcke von Länge 1..6; rekonstruierte Nachricht, Länge und
  Präfixfreiheit unter derselben Bedingung geprüft;
- die gespeicherten längeren Beispiele bis N=128 rekonstruiert.
- die Codesuche für N=4 in allen drei Scopes gegen sämtliche 16 Masken
  sowie das N=128-Ziel aus P06 mit allen 65.536 Codes der Länge 16 geprüft.

Das ist eine Prüfung des Spezifikationsvorschlags, kein bestandener Test
eines neuen Java-Features. Minimalität folgt zusätzlich aus der
vollständigen Konstruktorrekurrenz in M05; Roundtrips allein beweisen sie
nicht. Die spätere Implementierung MUSS ihren eigenen, unabhängigen
Decoder gegen diese Fixtures und die Rekurrenz prüfen.

### Erreichbare globale Grade bei N=16

| D in Bits | Anzahl Folgen |
| ---: | ---: |
| 3 | 2 |
| 11 | 2 |
| 12 | 6 |
| 13 | 42 |
| 14 | 30 |
| 15 | 58 |
| 16 | 158 |
| 17 | 296 |
| 18 | 152 |
| 19 | 64.790 |
| übrige Werte in 3..19 | 0 |

Summe: 65.536. Damit fallen etwa 98,86 Prozent dieser kurzen Folgen auf
den direkten Wert 19. Die neue Skala eignet sich nicht als stufenloser
Ersatz der bisherigen musikalischen Grade. Der Generator muss
Erreichbarkeit erklären; Zielbereiche, Dichte und Profile bleiben nötig.
Eine glattere oder anders gewichtete Sprache wäre eine neue Maßversion,
nicht eine nachträgliche Umdeutung dieses Histogramms.

## P08 – Abnahmematrix für den Implementierungsplan

| Anforderung | Abnahmebeleg | Zuständige Ebene |
| --- | --- | --- |
| M01: gemeinsame Definition | Gleiche Messschlüssel liefern gleiche Werte; Bits, Symbole, Einsparung und Überraschung werden nicht verwechselt. | Domain / Dokumentation |
| M02: Eingabe/Rahmen | Normalisierung, N=1/128, leeres Fensterwort, nichtbinäre Zeichen, ungenaue Zeitpunkte, doppelte Ereignisse und N=129 prüfen; keine Auffüllung. | Domain / Eingabeadapter |
| M03: Kompatibilität | Vorhandene Legacy-Fälle und Ausgabe einschließlich Zweitaktzustand unverändert. | Domain / CLI |
| M04: Codec | Jeden Konstruktor rekonstruieren; abgeschnittene, überzählige, reservierte und unzulässige Bits sowie PATCH-Indizes zurückweisen. | Domain |
| M05: exaktes Minimum | Rekurrenzbeweis, vollständige N=16-Verteilung, kurze unabhängige Referenz, kanonische Gleichstände; kein UPPER_BOUND als exakter Filterwert. | Domain |
| M06: Kontext | Vorlagen, Phase, CONFIRM/CHANGE/DIRECT, fehlender Kontext, gleiche Vorgeschichte und Zukunftsunabhängigkeit prüfen; Bestätigung ≠ neuer Aufbau. | Domain |
| M07: Interpretation | Zeitgrenzen exakt, Referenzstatus und nichtisochrone Darstellung; keine Beat-Erkennung aus der Motivlänge. | Domain / Application |
| A01: gleicher Analyzer | Alle generierten Treffer erneut durch denselben fachlichen Analyzer prüfen; absichtlich zu langen Zeugen verwerfen. | Application |
| A02: Ergebnisse | Messschlüssel, Kostenzerlegung, Rekonstruktion und Mehrdeutigkeit vollständig; Listen unveränderlich. | Domain / Application |
| A03: Takte/Profile | P03 samt abweichender isolierter/globaler/kausaler Werte; vollständige Partition und richtige Populationsstreuung. | Domain / CLI |
| A04: Hörbedingungen | Tempo-Override und Einzählen ändern E nicht implizit; Darbietung und Analysecondition getrennt ausweisen. | Application / Playbackadapter |
| G01: Zielvertrag | Scalar-/Profilbereich inklusive, Scope/Unit benannt, widersprüchliche Summe vor Suche erkennen. | Application |
| G02: Parameter | Kombinationen mit UND; exakte Dichtegrenzen, Ränder, Peak-Gleichstände und fehlende Referenzprüfung abdecken. | Domain / Application |
| G03: Suchstatus | Vollständige kleine Räume, N=16, Budgetabbruch, Shortfall, Limits und Ressourcenabbruch unterscheiden. | Application |
| G04: Zufall/Identität | Gleicher Seed reproduzierbar; Duplikate, einmalige Ziehungen und Zähler bis `2^128` ohne Überlauf. | Application |
| G05: Mehrtaktziele | Kontext aus tatsächlich gewähltem Präfix; kein unsicherer isolierter Vorfilter und kein unbewiesenes globales Präfixpruning. | Application |
| G06: Codesuche | Kleine Räume gegen Maskenvollsuche, vollständiges N=128-Niedrigziel, ungültige und nichtminimale Codes, Budgets und erneute Analyse. | Domain-Decoder / Application |
| S01: Architektur | Neue Bewertung ohne JDBC/Picocli/MIDI; Analyse/Generierung ohne Datenbank oder Audiohardware. | Architektur / Application |
| S02: Bedienung | Alte CLI unverändert; neue Hilfe, Completion und Beispiele stimmen mit implementierter Syntax überein. | CLI / Dokumentation |
| S03: Versionierung | Keine neuen Bits in Legacy-Spalten; gespeicherte Analysen nur mit passendem Messschlüssel als Filter verwenden. | Repositoryadapter / Application |

Die Produktionsabnahme verbindet fachliche Beispiele, unabhängige
Rekonstruktion und tatsächliches CLI-Verhalten. Hörversuche prüfen
Wahrnehmungsaussagen zusätzlich; ein bestandener Unit-Test ersetzt sie
nicht. MIDI, PostgreSQL und SuperCollider sind für die Kernabnahme keine
Voraussetzung.

## P09 – Übergaben und verbleibende Forschungsfragen

Für den nächsten Plan sind folgende Entscheidungen innerhalb dieses
Vorschlags konkretisiert: gemeinsame Einheit und Genauigkeit,
Beschreibungssprache samt Kosten, exaktes Minimum, kausaler Kontext,
isolierte/fortsetzende Taktverträge, Zielparameter, Vollständigkeitsstatus
und kompatibler Übergang. Eine spätere Planung muss sie als gemeinsamen
Vertrag übernehmen oder die betroffenen Definitionen und Fixtures zusammen
ändern. Das Dokument markiert keine Vikunja-Fachentscheidung als akzeptiert.

Die nötigen Übergabeergebnisse bauen aufeinander auf:

1. Codec und unabhängiger Decoder mit Kosten- und Rekonstruktionsbelegen.
2. Exakter Analyzer samt Modellgrenzen und unveränderlichen Ergebnissen.
3. Kontext- und Segmentanalyse mit den festgelegten Taktbeispielen.
4. Generator mit demselben Analyzer und überprüfbarem Suchstatus.
5. Öffentliche Bedienung, Dokumentation und gegebenenfalls getrennte
   versionierte Persistenz; keine Voraussetzung für Schritte 1–4.

Noch nicht beantwortet sind die psychologische Wahl des Metrums,
wahrnehmungsbezogene Genauigkeit, lokale probabilistische Überraschung,
Lern- und Umschaltkosten, Ausdruck und Klanggewichte. Eine vollständige
NI-WFC-Prüfung ist ebenfalls eine spätere formale Erweiterung.
Diese Fragen verändern die Interpretation oder einen künftigen Modus;
sie müssen nicht durch erfundene Gewichte in den ersten Codec geraten.

Vor einer Wahrnehmungsbehauptung sind mindestens alternative Sprachen,
Raster, Fenstergrößen und Kontextpolitiken auf Empfindlichkeit zu prüfen.
Die dokumentierten Gleichstände und Gegenbefunde bleiben Vergleichsfälle.
Eine bessere Sprache darf durchaus andere Zahlen liefern; dafür sind
Versionierung und unveränderte Legacy-Vergleichswerte vorgesehen.

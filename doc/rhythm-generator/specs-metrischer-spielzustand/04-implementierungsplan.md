# 04 – Implementierungsplan: Metrischer Spielzustand

Stand: 13. November 2026. Planungsbasis: Specs-Commit `0c776f7` auf
`docs/metrischer-spielzustand-spezifikation`. **Entwurf, vom Projektinhaber
noch nicht freigegeben.** Dieses Dokument plant die Umsetzung; es
implementiert nichts und ändert keine Vikunja-Tickets.

Fachliche Quelle ist der [Vertrag](01-fachlicher-vertrag.md), insbesondere
die Leitsätze, die Übergangstabelle für den Kern und die Regeln zum
gehaltenen Klang. Beispielwerte stammen aus den
[Ereignisabläufen](02-bildpruefung-und-ereignisablaeufe.md). Der Plan legt
keine fachlichen Regeln neu fest. Wo er eine technische Entscheidung
braucht, steht sie in Abschnitt 7.

## 1. Ziel und Umfang

Ergebnis ist eine dritte, eigenständige Maßversion **`metric-state-v1`**
neben `legacy-fsm-v1` (`HuffmanRhythm`) und `description-v1`. Sie berechnet
für ein endliches Beispiel unter benannter Klangbedingung:

- den Informationswert als Minimum über alle Wege der Übergangstabelle,
- alle gleich günstigen Anweisungsfolgen (die offene Deutung),
- das Protokoll je Weg: Zeitpunkt, Ereignis, Zustand vorher und nachher,
  Anweisung, Kosten.

**Im Umfang:** Impulsklang (I) und gehaltener Klang (G), exaktes Raster,
binäre Teilung, einstimmig, ein oder mehrere Takte, Analyse über die CLI
ohne Datenbank und ohne Audio.

**Nicht im Umfang (zurückgestellt):** Triolen und das 3-Bit-Wörterbuch,
Toleranzen für gespielte Musik, Mehrstimmigkeit, Loops, Generator,
Persistenz, Katalogsuche und Playback. Diese Punkte erhalten nach
Lieferstufe L3 eine eigene Entscheidung.

## 2. Schutz des Bestands

| Bestand | Bleibt unverändert |
| --- | --- |
| `HuffmanRhythm`, `HuffmanRhythmTest` | Symbolzahlen, Zustandsfortführung und alle kodifizierten Erwartungswerte. |
| `description-v1` (`a_domain/rhythm/information`) | Grammatik, Kosten, Decoder, Fixtures, JSON-Verträge `*-v1.schema.json`. |
| `analyze rhythm`, `calculate rhythms`, `search rhythms`, `play rhythm …` | Syntax, Ausgabe, Defaults, Bedeutung von `--info` und Deviation. |
| Tabelle `huffmanRhythms` | Keine neuen Werte in bestehende Spalten, kein Schemawechsel. |
| `rhythm-info analyze\|decode\|generate` | Verhalten ohne die neue Maßangabe. |

Es gibt keine Umrechnung zwischen den drei Maßen. Die neue Analyse wird
durch eigene Typen ergänzt, nicht durch geänderte Methoden bestehender
Klassen. Domäne und Anwendung bleiben frei von Picocli-, JDBC-, OSC- und
MIDI-Typen und von `System.out`.

## 3. Fachlicher Kern in technischer Form

### 3.1 Eingabe

Ein Beispiel besteht aus einem Rahmen und einer Ereignisfolge je
Rasterposition.

- **Rahmen:** Positionen pro Beat, Positionen pro Takt, Klangbedingung.
  Der Anfangsabstand ist ein Beat. Positionen pro Beat müssen eine
  Zweierpotenz sein, weil nur binäre Teilung unterstützt wird.
- **Ereignisse unter I:** Einsatz oder kein Einsatz je Position.
- **Ereignisse unter G:** Einsatz, „Ton hält“ oder Stille je Position. Ein
  Pauseneinsatz ist die erste Stille nach einem Einsatz oder gehaltenen
  Ton. Er wird abgeleitet und nicht eigens notiert.

Das Textformat steht in Abschnitt 7 (E1).

### 3.2 Zustand und Suche

Zustand ist `(Spielmodus N/P, Abstand Δ, Anker A)`; Δ und A sind exakte
rationale Vielfache der Rasterposition. Der Analyzer läuft die Positionen
von links nach rechts durch und wendet je Position die zutreffende Zeile
der Übergangstabelle an. Verzweigt wird nur bei einem Ausbleiben unter I
(Zeilen 5a und 5b).

Wege, die zur selben Zeit denselben Zustand erreichen, werden
zusammengeführt. Je Zustand werden die bisher geringsten Kosten und alle
Vorgänger mit diesen Kosten gehalten. Damit bleibt die Zahl der Zustände
klein (höchstens Modus × mögliche Abstände × Ankerpositionen), obwohl die
Zahl der Wege exponentiell wachsen kann. Am Ende des Beispiels ist der
Wert das Minimum über alle Endzustände; die minimalen Wege werden aus den
Vorgängern rekonstruiert.

Es gibt keine Heuristik: Das Ergebnis ist das exakte Minimum innerhalb
der Übergangstabelle.

### 3.3 Ergebnis

- Wert in Bit und Zahl der Anweisungen.
- Anzahl der minimalen Wege und, bis zu einer Anzeigegrenze, ihre
  Anweisungsfolgen mit Protokoll.
- Kostenanteil je Takt und je Beat für jeden minimalen Weg. Unterscheiden
  sich die Wege, wird der Bereich (kleinster bis größter Anteil)
  ausgegeben statt eines einzelnen Werts.
- Verlauf über die Länge: der Wert des Beispiels nach jedem vollen Takt.
  Daran wird sichtbar, wo ein Folgetakt die Deutung ändert (Leitsatz 2).
- Messschlüssel: Maßversion, Klangbedingung, Rahmen.

## 4. Architektur

| Schicht | Neu | Aufgabe |
| --- | --- | --- |
| Domäne | Paket `syrincs.a_domain.rhythm.metricstate` | Klangbedingung, Ereignisfolge, Anweisung, Zustand, Protokollschritt, Deutung, Analyseergebnis, `MetricStateAnalyzer`. Nutzt das vorhandene `Rational`. |
| Anwendung | `AnalyzeMetricStateUseCase`, Port, Ergebnis-DTO | Validierung des Auftrags, Aufruf des Analyzers, Anzeigegrenze. Anbindung an `RhythmInformationFacade` additiv. |
| Adapter | Erweiterung `RhythmInfoCmd`, JSON-Abbildung, Schema `metric-state-result-v1.schema.json` | Parsing des Textformats, Text- und JSON-Ausgabe. |
| Komposition | `Main` | Verdrahtung, keine neue Infrastruktur. |

Die vorhandenen Typen `OnsetSequence` und `AnalysisFrame` werden nicht
erweitert: `OnsetSequence` kennt keine gehaltenen Töne, und der Rahmen von
`description-v1` hat bewusst keinen Beat- oder Taktdefault.

## 5. Arbeitspakete und Lieferstufen

Jede Stufe ist für sich abnehmbar. Eine Stufe beginnt erst, wenn die
vorige abgenommen ist.

### L0 – Sollwerte und offene technische Entscheidungen

| AP | Inhalt | Ergebnis |
| --- | --- | --- |
| 0.1 | Entscheidungen E1–E4 aus Abschnitt 7 treffen. | Erledigt am 13. November 2026; E1 zusätzlich im Vertrag. |
| 0.2 | Fixture-Datei [`fixtures-metric-state-v1.json`](fixtures-metric-state-v1.json) anlegen: je Fall Rahmen, Klangbedingung, Ereignisse, Wert, alle minimalen Anweisungsfolgen, Herkunft. | Angelegt am 13. November 2026 mit 24 Fällen nach Abschnitt 6. |

Abnahme: Jeder Fall der Fixture-Datei ist im Vertrag oder in den
Ereignisabläufen belegt und als „Handrechnung“ gekennzeichnet.

### L1 – Kern unter Impulsklang

| AP | Inhalt | Tests |
| --- | --- | --- |
| 1.1 | Domänentypen und Validierung (Rahmen, Ereignisfolge, Zweierpotenz). | Eingabetests: leer, ungültige Zeichen, unpassende Längen. |
| 1.2 | `MetricStateAnalyzer` für die Zeilen 1–10 der Übergangstabelle. | Je Tabellenzeile mindestens ein gezielter Test. |
| 1.3 | Rekonstruktion aller minimalen Wege mit Protokoll. | Gleichstände F05.3.I, F05.4.I, F05.5.I liefern beide Folgen. |
| 1.4 | Fixture-Test über alle I-Fälle. | Wert und Menge der minimalen Folgen stimmen. |
| 1.5 | Mehrtakt: Taktwechsel, Verlauf über die Länge. | Zweitaktbeispiel 4 → 6; kostenlose Rückkehr auf die Eins; verfallene Erwartung. |

Abnahme: Alle I-Fixtures bestehen. **Weicht ein maschineller Wert von der
Handrechnung ab, wird die Fixture nicht still angepasst.** Die Abweichung
wird dokumentiert und dem Projektinhaber vorgelegt: Entweder war die
Handrechnung falsch, oder die Übergangstabelle hat eine Lücke.

### L2 – Gehaltener Klang

| AP | Inhalt | Tests |
| --- | --- | --- |
| 2.1 | Ereignisfolge mit „Ton hält“; Ableitung des Pauseneinsatzes. | Gültigkeit: Halten nur nach Einsatz oder Halten. |
| 2.2 | Zeilen 5a-G, 5b-G, 4-G; Pauseneinsatz als Anker. | Halbe gegen Viertel plus Pause; Punktierung; Staccato. |
| 2.3 | Überbindung am Taktstrich. | Über die Eins gehaltener Ton kostet `01`. |
| 2.4 | Fixture-Test über alle G-Fälle. | Wert und Folgen stimmen; unter G kein Gleichstand durch 5a/5b. |

Abnahme: Alle G-Fixtures bestehen; derselbe notierte Rhythmus liefert
unter I und G die dokumentierten, teils verschiedenen Werte.

### L3 – Verteilung und Entscheidungsgrundlage

| AP | Inhalt | Ergebnis |
| --- | --- | --- |
| 3.1 | Alle 65.536 Eintaktmuster (4/4, Sechzehntelraster) unter I berechnen. | Häufigkeit je Wert, Anteil mehrdeutiger Muster. |
| 3.2 | Vergleich mit `HuffmanRhythm` (Symbolzahl × 2). | Wo gleich, wo verschieden, typische Beispiele. |
| 3.3 | Bericht `05-verteilung.md`. | Grundlage für die Entscheidung über Suche, Katalog und Generator. |

Die Berechnung läuft als Test bzw. Hilfsprogramm ohne Datenbank. Abnahme:
Der Bericht ist reproduzierbar erzeugt; der Projektinhaber entscheidet
anhand dessen über die zurückgestellten Punkte.

### L4 – CLI und Ausgabe

| AP | Inhalt | Tests |
| --- | --- | --- |
| 4.1 | `rhythm-info analyze --measure metric-state-v1` mit `--sound`, `--beat-slots`, `--bar-slots`. | `RhythmInfoCliTest`: Erfolg, fehlende Klangbedingung, ungültiger Rahmen. |
| 4.2 | Textausgabe: Wert, Deutungen, Protokoll, Taktanteile, Verlauf. | Ausgabetests an zwei Fixtures. |
| 4.3 | JSON-Ausgabe mit eigenem Schema. | Schema-Validierung. |
| 4.4 | Hilfe, Completion, `README.md`, `doc/rhythm-generator/README.md`. | `RootCmdCompletionCliTest`. |

Ohne `--measure` verhält sich `rhythm-info analyze` wie bisher. Die
Klangbedingung ist bei der neuen Maßversion Pflicht; es gibt keinen
Default „Trommel“ oder „Orgel“.

## 6. Prüffälle der Fixture-Datei

Raster: vier Positionen pro Beat, 4/4. Alle Werte sind Handrechnungen
aus Vertrag und Ereignisabläufen.

| Fall | I | G |
| --- | ---: | ---: |
| F05.1 vier Viertel | 2 | 2 |
| F05.2 Halbe, halbe Pause | 4 | 6 |
| F05.3 Halbe, zwei Viertel | 6 (zwei Folgen) | 6 |
| F05.4 zwei Viertel, Halbe | 4 (zwei Folgen) | 4 |
| F05.5 halbe Pause, Halbe | 6 (zwei Folgen) | 6 |
| F05.6 zwei Viertel, halbe Pause | 4 | 4 |
| F05.7 halbe Pause, zwei Viertel | 4 | 4 |
| Halbe gegen Viertel plus Viertelpause, Fenster zwei Beats | 4 / 4 | `10 01` / `10 11`, je 4 |
| Punktierte Viertel plus Achtel, bis zum zweiten Einsatz | 8 (zwei Folgen) | 8 |
| Zwei Halbe | 4 | – |
| Foto 1, Rhythmus 1 | 8 (zwei Folgen) | – |
| Foto 1, Rhythmus 2 | – | 12 |
| F05.2 mit Folgetakt (ein Einsatz auf der Eins) | 6 | – |
| Acht Viertel über zwei Takte | 2 | – |

„–“ bedeutet: bisher nicht von Hand gerechnet. Diese Felder werden in L1
bzw. L2 maschinell ermittelt und als neue, zu bestätigende Werte
ausgewiesen, nicht als Sollwerte.

Zusätzliche Regeltests ohne Fotobezug: lange Stille in P kostet nichts;
Einsatz neben dem stummen Raster (`00`, dann `10`); verschobener
Viertelpuls über zwei Takte; Sechzehntel aus Vierteln (`00 00`).

## 7. Technische Entscheidungen

Alle vier Vorschläge wurden vom Projektinhaber am 13. November 2026
angenommen.

| ID | Frage | Vorschlag |
| --- | --- | --- |
| E1 | Textformat für gehaltene Töne. | **Entschieden am 13. November 2026:** `x` Einsatz, `=` Ton hält, `o` Stille; Beispiel Halbe plus halbe Pause: `x=== ==== oooo oooo`. Unter I sind nur `x`/`o` zulässig. Kein Bezug zu RDL (`-` bleibt dort Pause). |
| E2 | Größte Länge eines Beispiels. | **Entschieden am 13. November 2026:** 128 Positionen wie bei `description-v1`; technische Grenze, kein Wahrnehmungsfenster. |
| E3 | Darstellung des Taktanteils bei mehreren minimalen Wegen. | **Entschieden am 13. November 2026:** Bereich je Takt ausgeben; kein stiller Vertreter. |
| E4 | Andere Taktarten als 4/4. | **Entschieden am 13. November 2026:** Rahmen frei wählbar, solange Positionen pro Beat eine Zweierpotenz und die Taktlänge ein Vielfaches des Beats ist. Fachliche Prüffälle zunächst nur 4/4. |

## 8. Risiken

- **Abweichung von der Handrechnung.** Wahrscheinlichster Befund in L1.
  Der Umgang ist in der Abnahme von L1 festgelegt.
- **Lücken der Übergangstabelle bei beliebigen Mustern.** Die Tabelle ist
  an wenigen Figuren geprüft. L3 läuft über alle Muster; ein Zustand ohne
  zutreffende Zeile muss als Fehler mit Position gemeldet werden, nicht
  durch eine erfundene Regel überbrückt.
- **Wenig Unterscheidung.** Der Wert ist 2 × Anzahl der Änderungen. Ob
  die Skala für Suche und Generator genügt, zeigt erst L3.
- **Ticketbezug.** Für Implementierungsbranches und Commits verlangt
  `AGENTS.md` eine Task-ID. Für dieses Vorhaben gibt es noch kein
  Vikunja-Ticket; es ist vor L1 anzulegen.

## 9. Verifikation

Je Stufe der engste sinnvolle Ausschnitt, am Ende von L4 die ganze Suite:

```bash
mvn -Dtest='MetricState*Test' test
mvn -Dtest='RhythmInfoCliTest,RootCmdCompletionCliTest' test
mvn test
mvn package -Dsyrincs.skipCompletionInstall=true
```

Die Testnamen sind geplant. Kein Test benötigt PostgreSQL, MIDI, UDP oder
SuperCollider.

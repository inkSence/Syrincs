# 05 – Verteilung: `metric-state-v1` über alle Eintaktmuster

Stand: 13. November 2026. Lieferstufe L3 des
[Implementierungsplans](04-implementierungsplan.md). Basis ist der
Arbeitsbaum auf Branch `feat/metric-state-v1` (Stand nach Commit `8d68025`,
Domänenkern noch nicht committet). **Maschinell erzeugt, nicht von Hand
gerechnet.** Alle Zahlen stammen aus einem Programmlauf; es wurde nichts
geschätzt oder gerundet außer den Prozentangaben auf zwei Nachkommastellen.

## Was gerechnet wurde

Für alle 2^16 = 65.536 Eintaktmuster (4/4, 16 Positionen, `beatSlots = 4`,
`barSlots = 16`, Klangbedingung I) wurde der Wert nach `metric-state-v1`
(`MetricStateAnalyzer`) berechnet. Als Vergleichswert diente das alte Maß:
`HuffmanRhythm(4, 4, 120, muster).getInformation()` (Symbolzahl) mal 2.
Die Muster laufen als Maske 0..65535; das höchstwertige Bit ist Position 0,
Bit gesetzt = `x`, sonst `o`. Muster sind in Vierergruppen (je ein Beat)
geschrieben.

Reproduktion (ohne Maven, aus der Repository-Wurzel):

```bash
OUT=/pfad/zu/einem/leeren/verzeichnis
mkdir -p "$OUT"
javac -d "$OUT" src/main/java/syrincs/a_domain/rhythm/metricstate/*.java \
  src/main/java/syrincs/a_domain/rhythm/Rhythm.java \
  src/main/java/syrincs/a_domain/rhythm/HuffmanRhythm.java \
  src/main/java/syrincs/a_domain/statistics/StandardDeviation.java \
  src/test/java/syrincs/a_domain/rhythm/metricstate/MetricStateDistributionReport.java
java -cp "$OUT" syrincs.a_domain.rhythm.metricstate.MetricStateDistributionReport
```

Das Programm schreibt die Tabellen als Markdown auf stdout und die
Kennzahlen für den Test auf stderr. Laufzeit des Referenzlaufs: rund 3 s.
Die Häufigkeiten je Wert und die Zahl mehrdeutiger Muster sind zusätzlich
in `MetricStateDistributionTest` als Konstanten festgehalten.

Eine „minimale Folge“ in den Beispieltabellen ist die erste der
minimalen Anweisungsfolgen in der Reihenfolge des Analysators; bei
mehrdeutigen Mustern gibt es weitere (siehe Mehrdeutigkeit).

## Ausnahmen (Regellücken)

Bei keinem der 65.536 Muster trat eine Exception auf. Es gibt keine
Regellücke in der Übergangstabelle unter I für diese Eingaben.

## Häufigkeit je Wert

| Wert (Bit) | Muster neu | Anteil neu | Muster alt (Symbole × 2) | Anteil alt |
| ---: | ---: | ---: | ---: | ---: |
| 0 | 0 | 0.00 % | 1 | 0.00 % |
| 2 | 2 | 0.00 % | 4 | 0.01 % |
| 4 | 12 | 0.02 % | 20 | 0.03 % |
| 6 | 61 | 0.09 % | 78 | 0.12 % |
| 8 | 353 | 0.54 % | 215 | 0.33 % |
| 10 | 1459 | 2.23 % | 517 | 0.79 % |
| 12 | 4493 | 6.86 % | 1075 | 1.64 % |
| 14 | 9055 | 13.82 % | 2037 | 3.11 % |
| 16 | 13932 | 21.26 % | 3311 | 5.05 % |
| 18 | 14840 | 22.64 % | 4874 | 7.44 % |
| 20 | 11590 | 17.68 % | 6417 | 9.79 % |
| 22 | 6668 | 10.17 % | 7735 | 11.80 % |
| 24 | 2321 | 3.54 % | 8370 | 12.77 % |
| 26 | 677 | 1.03 % | 8229 | 12.56 % |
| 28 | 67 | 0.10 % | 7288 | 11.12 % |
| 30 | 6 | 0.01 % | 5869 | 8.96 % |
| 32 | 0 | 0.00 % | 4231 | 6.46 % |
| 34 | 0 | 0.00 % | 2710 | 4.14 % |
| 36 | 0 | 0.00 % | 1496 | 2.28 % |
| 38 | 0 | 0.00 % | 699 | 1.07 % |
| 40 | 0 | 0.00 % | 264 | 0.40 % |
| 42 | 0 | 0.00 % | 78 | 0.12 % |
| 44 | 0 | 0.00 % | 16 | 0.02 % |
| 46 | 0 | 0.00 % | 2 | 0.00 % |

- Maß neu: kleinster Wert 2, größter Wert 30, häufigster Wert 18 (14840 Muster), Median 18, Mittelwert 17.432, verschiedene Werte 15
- Maß alt: kleinster Wert 0, größter Wert 46, häufigster Wert 24 (8370 Muster), Median 24, Mittelwert 24.500, verschiedene Werte 24

Der Wert unter `metric-state-v1` ist bei allen Mustern gerade (kein Muster
mit ungeradem Wert), da jede Anweisung 2 Bit kostet. Werte 0 sind nur beim
alten Maß möglich (Muster ohne Einsatz), nicht beim neuen.

## Mehrdeutigkeit

- Muster mit mehr als einem minimalen Weg: 49341 (75.29 %)
- Muster mit mehr als einer verschiedenen minimalen Anweisungsfolge: 49341 (75.29 %)
- Größte Zahl minimaler Wege: 36 bei `oxox ooxo xoox oxxo` (Wert 30)
- Größte Zahl verschiedener minimaler Folgen: 36 bei `oxox ooxo xoox oxxo`

Mehrdeutige Muster je Wert:

| Wert | Muster | mehrdeutig (Wege) | Anteil | mehrdeutig (Folgen) |
| ---: | ---: | ---: | ---: | ---: |
| 2 | 2 | 0 | 0.00 % | 0 |
| 4 | 12 | 1 | 8.33 % | 1 |
| 6 | 61 | 6 | 9.84 % | 6 |
| 8 | 353 | 43 | 12.18 % | 43 |
| 10 | 1459 | 320 | 21.93 % | 320 |
| 12 | 4493 | 1747 | 38.88 % | 1747 |
| 14 | 9055 | 4939 | 54.54 % | 4939 |
| 16 | 13932 | 9890 | 70.99 % | 9890 |
| 18 | 14840 | 12278 | 82.74 % | 12278 |
| 20 | 11590 | 10642 | 91.82 % | 10642 |
| 22 | 6668 | 6438 | 96.55 % | 6438 |
| 24 | 2321 | 2289 | 98.62 % | 2289 |
| 26 | 677 | 675 | 99.70 % | 675 |
| 28 | 67 | 67 | 100.00 % | 67 |
| 30 | 6 | 6 | 100.00 % | 6 |

Bei diesem Lauf ist die Zahl der Muster mit mehreren minimalen Wegen gleich
der Zahl der Muster mit mehreren verschiedenen minimalen Anweisungsfolgen.

## Vergleich mit dem alten Maß (Symbolzahl × 2)

| Beziehung | Muster | Anteil |
| --- | ---: | ---: |
| gleich | 4647 | 7.09 % |
| neues Maß kleiner | 56777 | 86.63 % |
| neues Maß größer | 4112 | 6.27 % |

Verteilung der Differenz (neu minus alt):

| Differenz | Muster | Anteil |
| ---: | ---: | ---: |
| -38 | 1 | 0.00 % |
| -36 | 1 | 0.00 % |
| -34 | 4 | 0.01 % |
| -32 | 12 | 0.02 % |
| -30 | 24 | 0.04 % |
| -28 | 58 | 0.09 % |
| -26 | 116 | 0.18 % |
| -24 | 216 | 0.33 % |
| -22 | 421 | 0.64 % |
| -20 | 775 | 1.18 % |
| -18 | 1339 | 2.04 % |
| -16 | 2387 | 3.64 % |
| -14 | 3960 | 6.04 % |
| -12 | 5751 | 8.78 % |
| -10 | 7525 | 11.48 % |
| -8 | 9016 | 13.76 % |
| -6 | 9436 | 14.40 % |
| -4 | 8719 | 13.30 % |
| -2 | 7016 | 10.71 % |
| 0 | 4647 | 7.09 % |
| 2 | 2566 | 3.92 % |
| 4 | 1118 | 1.71 % |
| 6 | 356 | 0.54 % |
| 8 | 68 | 0.10 % |
| 10 | 4 | 0.01 % |

Größte Differenz, neues Maß kleiner (Auswahl: Differenz, dann Maske aufsteigend):

| Muster | neu | alt | Differenz | minimale Folge |
| --- | ---: | ---: | ---: | --- |
| `xxox oxox oxox oxox` | 8 | 46 | -38 | `10 00 00 01` |
| `oxox oxox oxox oxox` | 10 | 46 | -36 | `11 00 00 10 01` |
| `xoox oxox oxox oxox` | 8 | 42 | -34 | `10 00 00 01` |
| `xoxx oxox oxox oxox` | 8 | 42 | -34 | `10 00 00 01` |
| `xxox oxox oxox oxoo` | 10 | 44 | -34 | `10 00 00 01 01` |

Größte Differenz, neues Maß größer (Auswahl: Differenz, dann Maske aufsteigend):

| Muster | neu | alt | Differenz | minimale Folge |
| --- | ---: | ---: | ---: | --- |
| `oxoo xooo xoox xooo` | 22 | 12 | 10 | `11 00 00 10 11 10 01 01 00 00 11` |
| `oxoo xooo xoxo xooo` | 20 | 10 | 10 | `11 00 00 10 11 10 01 01 00 01` |
| `oxoo xooo xoxo xoox` | 24 | 14 | 10 | `11 00 00 10 11 10 01 01 00 01 00 00` |
| `oxoo xooo xoxx xooo` | 22 | 12 | 10 | `11 00 00 10 11 10 01 01 00 00 11` |
| `oooo oxoo xooo xooo` | 16 | 8 | 8 | `11 00 00 10 11 10 01 01` |

## Zusammenhang mit der Zahl der Einsätze

| Einsätze | Muster | Mittel neu | Mittel alt | kleinster neu | größter neu |
| ---: | ---: | ---: | ---: | ---: | ---: |
| 0 | 1 | 2.000 | 0.000 | 2 | 2 |
| 1 | 16 | 8.000 | 6.500 | 4 | 10 |
| 2 | 120 | 11.550 | 11.700 | 4 | 14 |
| 3 | 560 | 13.950 | 15.814 | 4 | 18 |
| 4 | 1820 | 15.647 | 19.023 | 2 | 22 |
| 5 | 4368 | 16.843 | 21.475 | 4 | 26 |
| 6 | 8008 | 17.627 | 23.291 | 4 | 30 |
| 7 | 11440 | 18.037 | 24.565 | 4 | 30 |
| 8 | 12870 | 18.095 | 25.369 | 4 | 28 |
| 9 | 11440 | 17.823 | 25.754 | 6 | 28 |
| 10 | 8008 | 17.247 | 25.753 | 6 | 26 |
| 11 | 4368 | 16.389 | 25.385 | 6 | 24 |
| 12 | 1820 | 15.249 | 24.656 | 6 | 22 |
| 13 | 560 | 13.782 | 23.564 | 6 | 18 |
| 14 | 120 | 11.883 | 22.100 | 6 | 14 |
| 15 | 16 | 9.375 | 20.250 | 6 | 10 |
| 16 | 1 | 6.000 | 18.000 | 6 | 6 |

## Auffälligkeiten

- `oooo oooo oooo oooo`: neu 2, alt 0, Folge `11`, minimale Wege 1
- `xxxx xxxx xxxx xxxx`: neu 6, alt 18, Folge `10 00 00`, minimale Wege 1
- Muster mit Wert 2 (2): [`oooo oooo oooo oooo` (Folge `11`), `xooo xooo xooo xooo` (Folge `10`)]
- Kleinster Wert 2 bei 2 Mustern.
- Größter Wert 30 bei 6 Mustern, z. B. [`oxoo xoxo oxox ooxo`, `oxox ooxo oxox ooxo`, `oxox ooxo xoox ooxo`, `oxox ooxo xoox oxxo`, `oxox ooxo xxox ooxo`]
- Muster mit ungeradem Wert: 0

Die Wertspanne des neuen Maßes ist 2 bis 30 Bit in 15 Stufen; das alte Maß
(Symbolzahl × 2) belegt 24 Stufen von 0 bis 46.

## Was der Bericht nicht entscheidet

Der Bericht liefert Zahlen als Grundlage. Über Suche, Katalog und
Generator, über die Behandlung mehrdeutiger Muster und über die Eignung
der Wertskala entscheidet der Projektinhaber. Der Bericht spricht keine
Empfehlung aus und ändert weder Regeln noch Defaults. Er betrifft nur die
Klangbedingung I und einen einzelnen 4/4-Takt im Sechzehntelraster; für G
und für mehrtaktige Beispiele liegen keine Zahlen vor.

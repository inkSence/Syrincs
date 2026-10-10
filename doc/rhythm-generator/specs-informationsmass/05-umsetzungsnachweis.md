# 05 – Umsetzungsnachweis des Informationsmaßes

Stand: 10. Oktober 2026. Umsetzung auf `feat/rhythm-information-implementation`,
Basis `f5f86f5` mit dem [Implementierungsplan](04-implementierungsplan.md).
Für diesen Implementierungsauftrag existiert laut Auftraggeber kein Ticket;
Vikunja-Daten wurden nicht verändert. Die Arbeitspakete AP00–AP11 sind bis
Lieferstufe **L3** umgesetzt.

Die neuen öffentlichen Endpunkte sind `rhythm-info analyze`,
`rhythm-info decode` und `rhythm-info generate`. Die
[Feature-Präsentation](../README.md#neues-beschreibungsmaß-ausprobieren)
enthält ausführbare Beispiele. Das bestehende Huffman-Maß, seine Endpunkte,
Datenbankspalten und Playback-Verträge bleiben erhalten.

## Arbeitspakete und Abnahme

| Paket | Umsetzung und ausgeführter Beleg |
| --- | --- |
| AP00 | Drei versionierte JSON-Schemas, vollständige Anfragen und aufgelöste Ergebnisbedingungen. Beispieldateien und echte CLI-Ergebnisse gegen Draft 2020-12 validiert. |
| AP01 | Unveränderliche Onsets, rationale Zahlen, Zeitrahmen, Partitionen und Messschlüssel. Längen 1/128/129, fehlende Zeit, 108 BPM, exakte Ereigniszeiten und ungültige Raster geprüft. |
| AP02 | Unabhängiger Decoder aller sechs Konstruktoren; nichtminimale Nachrichten, reservierte Tags, abgeschnittene Codes, Restbits und PATCH-Indizes geprüft. |
| AP03 | Vollständige Minimierungsrekurrenz und kompakter optimaler DAG. Alle 51 positiven und neun negativen Fixture-Fälle; unabhängige Codeenumeration für sämtliche Wörter N=1..8; vollständiges N=16-Histogramm mit 65.536 Rekonstruktionen. |
| AP04 | Primitive Kontextvorlagen, tatsächlicher Vorlauf, drei bedingte Nachrichten und lokale Scopes. Kleine Kontexte unabhängig enumeriert; Viertelprofile, Zweitaktsummen, exakte Varianz und eigene Fenster-Messschlüssel geprüft. |
| AP05 | Explizite, auch nichtisochrone Referenzen mit Phase und Herkunft. Geschlossene 100-/400-/1200-/5000-ms-Grenzen, fehlende Daten und technisch feines Raster ohne erfundene aktive Ebene geprüft. |
| AP06 | Gemeinsame Analyze-/Decode-Use-Cases, exakte JSON-Eingabe und getrennter Legacy-Vergleich. Seed-Präzision, große Zähler, Fehlerobjekte und nicht verfügbare Kennzahlen mit Begründung geprüft. |
| AP07 | Additiver CLI-Baum, unabhängiger Einstieg in `Main`, Root-Hilfe und Completion. Prozesstests mit absichtlich ungültiger DB-Konfiguration und fehlendem MIDI; historische CLI-Verträge geprüft. |
| AP08 | Gemeinsame AND-Kriterien, frühe Widerspruchsprüfung und vollständige Maskensuche bis N=16. Dichte, Abstände, Rand-Nullfolgen, Bereichsprofile, zusätzliche globale Ziele, Peaks, Deviation und Shortfall geprüft. |
| AP09 | Scopebezogene Codesuche in Shortlex-Reihenfolge. Kleine Räume gegen Maskensuche geprüft; vollständiger N=128-/D=16-Lauf sowie Abbruch einen Versuch vor Vollständigkeit. Jeder neue gültige Kandidat wird exakt analysiert. |
| AP10 | Reproduzierbare Zufallssuche und partielle Fisher-Yates-Auswahl aus vollständigen Beständen. Exakte Ziehungsfolge, Duplikate, Seed, Kontext-Neustart und unterlassene Zufallsauswahl bei Teilbeständen geprüft. |
| AP11 | Gesamte normale Maven-Suite, App-Paket, tatsächliche Hilfe-/JSON-/Fehleraufrufe und Bash-Syntax der Completion geprüft; Beispiele und Messbefunde dokumentiert. |

Die Architektur bleibt frameworkfrei in Domain und Application. Gson und
Picocli werden nur in Adaptern verwendet. Neue Befehle konstruieren keine
Datenbank-, MIDI- oder OSC-Adapter. Jeder ausgegebene Generatorhit wird mit
dem gemeinsamen Analyzer erneut geprüft.

## Numerische Ergebnisse der App-Prüfung

| Auftrag | Ergebnis |
| --- | --- |
| Ein Vierteltakt, global | 13 Bits, `0110110101000`. |
| Ein Vierteltakt, kausale Viererfenster | `[8,8,3,3]`, Summe 22, Varianz `25/4`. |
| Zwei Vierteltakte | Global 14; isolierte Taktwerte `[13,13]`; kausale Taktbeiträge `[22,12]`. |
| Viertelauftrag N=16, D=13, vier Pflichtpositionen, Limit acht | Ein Treffer, vollständiger Bestand, Shortfall sieben. Dasselbe mit D=12: `NO_SOLUTION`. |
| N=128, D=16, Codesuche mit Budget 65.536, ohne Ereignisfilter | 270 unterschiedliche Kandidaten analysiert; davon 266 Treffer, `totalMatches=266`, `COMPLETE`. |
| Derselbe Codebereich mit Budget 65.535 und Zufallsauswahl | 266 Treffer beobachtet, aber `totalMatches=null`, `BUDGET_EXHAUSTED`, `selectionPerformed=false`, Exitcode 3. |
| N=128, D=16, zusätzlich genau 32 Einsätze an 0,4,…,124 | Nur die Viertelfolge; vollständiger Bestand. Dieser zusätzliche Filter ist auch im Generator-Vertragstest geprüft. |
| N=16, D=19, Zufallssuche, Seed 42, Limit vier | Vier Treffer nach vier Versuchen; `REQUESTED_COUNT_REACHED`, Gesamtbestand unbekannt. |
| `data/rhythm-info/causal-phrase.json`, Seed 42 | Drei Treffer nach sechs Versuchen, jeweils 64 Fortsetzungsbits und Taktbeiträge `[32,32]`; Gesamtbestand unbekannt. |
| 108 Viertel-BPM, vier Positionen pro Viertel, 4/4 | Rasterdauer `1250/9` ms; Viertelfolge weiterhin 13 Bits; getrenntes Legacy-Ergebnis 1 Codesymbol. |

Die vollständige Codesuche beweist Vollständigkeit für den angefragten
Bitbereich und seine Bedingungen. Sie besucht nicht alle `2^128`
Ereignisfolgen. Dass ein Teilbestand hier schon alle tatsächlich existierenden
Treffer enthält, berechtigt ohne vollständige Codeabdeckung noch nicht zum
Vollständigkeitsnachweis.

Das vollständige N=16-Histogramm lautet:

```text
Bits     3  11  12  13  14  15   16   17   18     19
Anzahl   2   2   6  42  30  58  158  296  152  64790
```

Es enthält Lücken. Ein ganzzahliger Zielwert innerhalb der gültigen Skala
muss daher nicht erreichbar sein. Die Fixture-Abnahme prüft die gesamte
Verteilung und jeden kanonischen Code, nicht nur einzelne Beispiele.

## Laufzeit und Speicher

Einzelne frische App-Prozesse unter OpenJDK 21.0.12.1, mit
`JAVA_OPTS=-Xmx384m`, JSON-Ausgabe und `/usr/bin/time -f '%e %M'`.
Die Zeiten enthalten JVM-/CLI-Start und Serialisierung; RSS bezeichnet das
Maximum des Prozesses einschließlich JVM, keinen reinen Domain-Heapbedarf.
Es handelt sich um Messungen dieses Abnahmelaufs, keine garantierten Limits.

| Fall | Laufzeit | Maximaler RSS |
| --- | ---: | ---: |
| Viertelfolge N=16 | 0,53 s | 77.392 KiB |
| Viertelfolge N=80 | 0,52 s | 83.680 KiB |
| Viertelfolge N=128 | 0,53 s | 89.988 KiB |
| Unregelmäßige Folge N=16 | 0,47 s | 78.116 KiB |
| Unregelmäßige Folge N=80 | 0,59 s | 96.724 KiB |
| Unregelmäßige Folge N=128 | 0,87 s | 148.956 KiB |
| Maskenvollsuche N=16 mit Viertelbedingungen | 0,96 s | 234.160 KiB |
| Codesuche N=128, D=16, vollständig | 14,51 s | 233.640 KiB |
| Codesuche N=128, D=16, Budget 65.535 | 14,66 s | 237.008 KiB |

Die unregelmäßigen Vergleichsfolgen waren:

```text
N=16:  ooxoooooxooooooo
N=80:  xoxxooxxxooxooxoxxxoxoxoxxooooxoooxxxxoxxoxooooxxxoxoooxxxooxoxxxoxooxxxxooxoooo
N=128: oxoxxxxooxxoxxoxoxooxooxooxxooxxoooooxoooxoxxooxxxxxooooxoooooooooxooxxooxoxxoooxxxxxoooxxooooxoxoxxooxooooooxxxoxooxoxxxxxxooxo
```

Reproduktion einer Messung:

```bash
JAVA_OPTS=-Xmx384m /usr/bin/time -f '%e %M' \
  target/app/bin/syrincs rhythm-info analyze "xooo xooo xooo xooo" --format json
JAVA_OPTS=-Xmx384m /usr/bin/time -f '%e %M' \
  target/app/bin/syrincs rhythm-info generate --slots 128 --info 16 \
  --strategy code-length --attempt-budget 65536 --limit 5 --format json
```

Der vollständige Histogrammtest läuft in der normalen Suite. Die
Minimierung hält ihre Memoisierung pro Analyse; Suchläufe speichern
Kandidatenidentitäten und passende Masken, keine Analyse-DAGs sämtlicher
besuchter Kandidaten. Große Codebereiche bleiben exponentiell: Ein
Zählbudget begrenzt Versuche, verspricht aber keine feste Laufzeit oder
Trefferzahl. Kooperative Unterbrechung liefert gültige Teilresultate;
ein erzwungener Prozessabbruch kann kein Ergebnisdokument garantieren.

## Ausgeführte Verifikation und Grenzen

```bash
mvn -o -q test
mvn -o -q package -DskipTests -Dsyrincs.skipCompletionInstall=true
target/app/bin/syrincs --help
target/app/bin/syrincs rhythm-info --help
```

Die Paketierung verwendet `-DskipTests`, weil unmittelbar davor die gesamte
Suite ausgeführt wurde. Zusätzlich wurden die drei Blatthilfen,
versionierte JSON-Ergebnisse aller Ergebnisarten, vollständige und
budgetierte Generierung, Anfragebeispiele und JSON-Fehler mit dem
App-Artefakt geprüft. Die erzeugte Bash-Completion wurde ausschließlich
nach `/tmp` geschrieben und bestand `bash -n`.

Die normale Suite umfasst 259 Tests ohne Fehler, geprüft aus einem frischen
Quellstand ohne alte Buildartefakte. 14 vorhandene Tests
wurden übersprungen: zwei reale Roland-MIDI-Sendetests, ein optionaler
SuperCollider-Hörtest und elf UDP-Pakettests, deren Socketzugriff diese
Sandbox verhindert. Eine reale PostgreSQL-/Audio-Abnahme ist für die neuen
reinen Befehle nicht erforderlich und wurde nicht behauptet.

Das Zeitprofil bleibt eine partielle Prüfung expliziter Referenzen.
Automatische Beat-Erkennung, psychologische Kalibrierung, vollständige
NI-WFC-Prüfung sowie neue Persistenz und Wiedergabe sind die im Plan
benannten Folgearbeiten. Sie werden nicht durch Beschreibungskompression
oder einen `PASS`-Status des Zeitprofils ersetzt.

# Justin London: Hearing in Time — Auswertung für Syrincs und VR-12

Stand: 10. Oktober 2026. Grundlage: die lokal bereitgestellten Screenshots,
zehn nummerierte Kapitel sowie Einleitung, Conclusion und Notes.
[VR-12](http://localhost:3456/tasks/20) wurde für diese Auswertung erneut aus
Vikunja geladen; der gelesene Taskstand wurde am 3. Oktober 2026 aktualisiert.

## Fragestellung und Bezug zum implementierten Maß

VR-12 untersucht, wann Schläge gleichen Klangs jeweils als Grundschlag und
wann mehrere davon als Unterteilungen eines Grundschlags gehört werden.
Bei einem Ereignisabstand Δ sind unter anderem die Beat-Abstände Δ, 2Δ und
3Δ zu unterscheiden. Eine berichtete Zweiergruppe kann aber auch **zwei
Beats pro übergeordneter Gruppe** bedeuten. Gruppengröße, Beat-Ebene und
Unterteilung sind deshalb nicht gleichzusetzen. Vorgeschichte, Tempo,
Schalldauer, Darbietungslänge und Messaufgabe gehören zur Fragestellung.

Diese Lektüre dient ausdrücklich der Klärung des **bereits praktisch
implementierten Informationsbegriffs**. Ausgangspunkt ist nicht ein frei
erfundenes neues Maß, sondern
[`HuffmanRhythm`](../../../src/main/java/syrincs/a_domain/rhythm/HuffmanRhythm.java)
mit den Verträgen aus
[`HuffmanRhythmTest`](../../../src/test/java/syrincs/a_domain/rhythm/HuffmanRhythmTest.java):

- `Rhythm` zerlegt die Eingabe in vorgegebene Gruppen von vier Rasterpositionen.
  Die öffentliche Onset-Analyse verwendet 4/4 und ein 16tel-Raster.
- `Info` zählt erzeugte Codesymbole. Die Gesamtinformation ist die Summe der
  Beat-Werte; `Deviation` ist deren Populationsstandardabweichung.
- Der Playing-Zustand wird über Beat- und Taktgrenzen fortgesetzt. Der
  Unterteilungszustand startet pro Beat erneut bei Quarter.
- `x` bezeichnet einen Einsatz; `o` bedeutet keinen neuen Einsatz und ist
  kein verlässliches Zeichen für akustische Stille.
- Tempo, Klang, Lautstärke, Artikulation, Hörvorgeschichte und eine frei
  gewählte Beat-Ebene gehen nicht in die Informationsberechnung ein.

| Bestehender Codevertrag | `Info` | Für die Lektüre relevante Frage |
| --- | ---: | --- |
| `xooo xooo xooo xooo` | 1 | Unter welchen Bedingungen wird diese vorgegebene Referenz tatsächlich gehört? |
| `xoxo xoxo xoxo xoxo` | 5 | Wann ist die wiederholte zusätzliche Unterteilung eine etablierte Erwartung? |
| `xxxx xxxx xxxx xxxx` | 9 | Ist eine schnellere regelmäßige Folge dichter, anders metrisiert oder schwieriger? |
| `xxox xxox xxox xxox` | 17 | Welche Anteile stammen aus dem Kodierverfahren, welche könnten Wahrnehmungsorganisation beschreiben? |
| `xooo xoxo xooo xoxo` | 3 | Profil `[1,1,0,1]`: Was sagt dessen Streuung über lokale Kodierkosten? |
| `xoxo xooo xoxo xooo` | 3 | Profil `[2,0,1,0]`: gleiche Summe, andere Streuung; kein unmittelbarer Nachweis stärkerer gehörter Akzente. |

Der Code ist ein konkretes, deterministisches Maß unter einer gesetzten
Referenz. Er sucht keine kürzeste Beschreibung über konkurrierende Metren;
eine empirische Bedeutung seiner Zahlen folgt nicht allein aus ihrem
Bestehen. Die
[theoretische Skizze](../informationsbegriff.md) fragt
zusätzlich nach der kürzesten verbleibenden Beschreibung einschließlich
Modellkosten. London liefert Kriterien und Gegenproben für diese Deutung,
keine Herleitung unserer Symbolzahlen.

## Was die Lektüre zur Klärung beiträgt

| Frage an unser Maß | Verwertbarer Beitrag des Buches | Besonders relevante Abschnitte |
| --- | --- | --- |
| Was wird vorausgesetzt, bevor Einsätze Information erhalten? | Eine metrische Referenz mit Periode, Phase und Beziehungen zwischen Ebenen. Ihre Bildung und ihre Verwendung sind verschiedene Vorgänge. | Kapitel 1, 4 und 5 |
| Welche Rolle spielt fortgeführter Zustand? | Erwartung kann über fehlende Ereignisse hinweg erhalten bleiben; das macht Kontext relevant. Unser Playing-Zustand ist hierfür eine begrenzte formale Analogie. | Kapitel 3, 4 und 6 |
| Warum kostet wiederholte Unterteilung weiter Symbole? | Etablierte Unterteilung kann das Hören stabilisieren. Die Kosten des erneuten Einrichtens im Code sind deshalb von tatsächlicher Neuorganisation zu unterscheiden. | Kapitel 2, 4 und 7 |
| Wann werden gleiche Ereignisse als Unterteilungen gehört? | Absolute Abstände, Vorgeschichte, Klangdauer, Aufgabe und Erfahrung begrenzen die Deutung. Wohlgeformtheit allein wählt keinen Beat. | Kapitel 2 und 5; Notes |
| Bedeutet höherer Codewert größere Schwierigkeit? | Dichte, metrische Mehrdeutigkeit, Ausführungsschwierigkeit und Beschreibungslänge können auseinanderfallen. | Kapitel 6–10 und Conclusion |
| Wo liegt der Anschluss an minimale Beschreibung? | Die Conclusion referiert diesen Komplexitätsbegriff ausdrücklich. Das Buch liefert jedoch weder unsere Kodierung noch ihre Minimalität oder empirische Kalibrierung. | Conclusion, insbesondere IMG_2144 |

Der unmittelbare Nutzen besteht darin, die **Interpretation des vorhandenen
Maßes** genauer zu bestimmen und gezielte Gegenproben zu formulieren.
Die Kapiteltexte benennen jeweils, was dafür verwertbar ist, was offen
bleibt und welche eigenen Prüffragen daraus folgen.

## Lesepfad

| Abschnitt | Zusammenfassung |
| --- | --- |
| Einleitung | [Ausgangspunkt und Begriffe](<0 Introduction/zusammenfassung-VR-12.md>) |
| Kapitel 1 | [Metrum als Aufmerksamkeitsverhalten](<Chapter 1/zusammenfassung-VR-12.md>) |
| Kapitel 2 | [Zeitbereiche und Wahrnehmungsaufgaben](<Chapter 2/zusammenfassung-VR-12.md>) |
| Kapitel 3 | [Neurobiologie, Lernen und Entwicklung](<Chapter 3/zusammenfassung-VR-12.md>) |
| Kapitel 4 | [Metrischer Hintergrund und rhythmische Oberfläche](<Chapter 4/zusammenfassung-VR-12.md>) |
| Kapitel 5 | [Darstellung und Wohlgeformtheit](<Chapter 5/zusammenfassung-VR-12.md>) |
| Kapitel 6 | [Mehrdeutigkeit, Synkopierung und konkurrierende Deutungen](<Chapter 6/zusammenfassung-VR-12.md>) |
| Kapitel 7 | [Fallstudie: Beethovens Fünfte](<Chapter 7/zusammenfassung-VR-12.md>) |
| Kapitel 8 | [Nichtisochrone Metren](<Chapter 8/zusammenfassung-VR-12.md>) |
| Kapitel 9 | [Nichtisochrone Metren in Theorie und Praxis](<Chapter 9/zusammenfassung-VR-12.md>) |
| Kapitel 10 | [Many Meters Hypothesis und expressive Zeitgestaltung](<Chapter 10/zusammenfassung-VR-12.md>) |
| Conclusion | [Zusammenführung und Komplexitätsfrage](Conclusion/zusammenfassung-VR-12.md) |
| Notes | [Anmerkungen und Grenzen der Belege](Notes/zusammenfassung-VR-12.md) |

## Quellenstatus

Die Texte sind eigenständige deutsche Zusammenfassungen mit einer gezielten
Auswertung für Syrincs. Fundstellen verweisen auf Original-Screenshots im
jeweiligen Ordner; die Bildschirmbilder liefern keine verlässliche gedruckte
Paginierung. OCR unterstützt die vollständige Lektüre; zentrale
Definitionen, Zahlen und für die Argumentation benötigte Abbildungen werden
am Bild überprüft. Fehlerhafte OCR von Noten und Taktzeichen wird nicht als
musikalischer Befund übernommen.

**London referiert**, **der Code implementiert** und **wir leiten als
Prüffrage ab** bleiben getrennte Aussagearten. Im Buch referierte Studien
sind hier nicht zusätzlich als Primärstudien ausgewertet. Diese Kapiteltexte
ersetzen deshalb weder die Befundtabelle noch die Prüfung der Primärquellen
aus VR-12. Sie erweitern die Grundlage der
[bestehenden Literaturauswertung](../hoerpsychologie-beat-ebene-und-unterteilung.md)
und der
[Modellprüffälle](../modellprueffaelle-beat-ebene-und-unterteilung.md).
Vikunja, Produktionscode und vorhandene Informationswerte werden durch diese
Dokumentation nicht geändert.
Bibliography und Index sind im bereitgestellten Screenshotbestand nicht
enthalten. Alle 550 vorhandenen Original-Screenshots wurden unverändert
belassen; die zehn Kapitel umfassen davon 479 Bilder.

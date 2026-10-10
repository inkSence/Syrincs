# Spezifikationen: Information von Rhythmen analysieren und gestalten

Stand: 10. Oktober 2026. Status: zusammenhängender, ausführbarer
Spezifikationsvorschlag für einen anschließenden Implementierungsplan.
Codebasis: `a22e024`, einschließlich der Literaturauswertung und der
kapitelweisen Lektüre von *Hearing in Time*.

Die folgende Darstellung dokumentiert den Entwurf vor seiner Umsetzung.
Der [Implementierungsplan](04-implementierungsplan.md) ist inzwischen bis
Lieferstufe L3 realisiert. Aktuelle Bedienbeispiele stehen in der
[Feature-Präsentation](../README.md#neues-beschreibungsmaß-ausprobieren),
Abnahme und technische Grenzen im
[Umsetzungsnachweis](05-umsetzungsnachweis.md).

Diese Spezifikationen führen Philipps Ziel, den bestehenden Code und den
Stand der Literatur zusammen. **Information bezeichnet verbleibende
Beschreibungslänge einschließlich neu benötigter zeitlicher Regeln.**
Das System soll nachvollziehbar analysieren und Rhythmen erzeugen können,
die diese Größe und weitere ausdrücklich gewählte Parameter erfüllen.
Die mathematische Definition ist implementierbar, ohne eine vollständige
Theorie menschlichen Hörens vorauszusetzen.

## 1. Fachlicher Entscheidungsrahmen

Der Vorschlag verbindet drei bisher getrennte Ebenen:

1. **Das vorhandene Maß weiterführen.** `HuffmanRhythm` bleibt als
   `legacy-fsm-v1` mit seinen bisherigen Symbolzahlen erhalten. Es liefert
   den reproduzierbaren Vergleich zum bisherigen musikalischen Arbeitsmittel.
2. **Die kürzeste Beschreibung ausführbar machen.** `description-v1`
   berechnet das exakte Minimum einer festgelegten, endlichen Sprache aus
   Wiederholung, Abschnittsbildung, Ausnahmen und direkter Beschreibung.
   Ein Decoder und konkrete Bitkosten machen dieses Minimum prüfbar.
3. **Wahrnehmungsorganisation explizit behandeln.** Grundschlag,
   Unterteilung, Kontext, Tempo und Darbietung werden als benannte Bedingungen
   geführt. Der kleinste Codewert wird nicht automatisch zum gehörten Metrum.

Die Namen dieser Modi sind neue Spezifikationsnamen, keine vorhandenen
CLI-Optionen. Die Kosten von `description-v1` sind **eigene technische
Festlegungen**. Weder London noch die Primärstudien liefern diese Zahlen.
Der ausführbare Vorschlag legt keine alten Informationsgrade neu aus.

### Warum dieser Weg

Der vorhandene Automat zeigt die produktive Idee: Einsätze werden relativ
zu einer zeitlichen Regel beschrieben, Zustand wird fortgeführt und neue
Unterteilung kann zusätzliche Beschreibung benötigen. Für ein Minimum
fehlen jedoch ein vollständiger Beschreibungsvertrag, konkurrierende Regeln
und ein Rekonstruktionsnachweis. Die neue Sprache übernimmt die Idee
zeitlicher Regeln, statt die alten Zahlen mit neuen Bedeutungen zu versehen.

London erklärt, warum Referenzen, Unterteilungen, Zeitbereiche und
Vorgeschichte relevant sind. Er liefert keine allgemeine Auswahlregel für
das Metrum und keine Kalibrierung eines Informationsmaßes. Daraus folgt:
Wir können ein überprüfbares strukturelles Maß definieren; seine Beziehung
zu Aufmerksamkeit, Überraschung oder Schwierigkeit muss separat untersucht
werden. Der Generator benötigt für seine korrekte Funktion zunächst die
mathematische Definition, keinen solchen Wahrnehmungsnachweis.

## 2. Dokumente und Verbindlichkeit

| Dokument | Ergebnis für die spätere Planung |
| --- | --- |
| [01 – Maß und Beschreibung](01-mass-und-beschreibung.md) | Eingabevertrag, gemeinsame Einheit, endliche Sprache, Decoder, Minimum, Kontext und metrische Interpretation. |
| [02 – Analyse und Generierung](02-analyse-und-generierung.md) | Analyseergebnisse, Taktvertrag, Parameter, Suchstatus, Reproduzierbarkeit, Architektur und Kompatibilität. |
| [03 – Prüffälle und Abnahme](03-prueffaelle-und-abnahme.md) | Durchgerechnete Beispiele, Gegenproben, Invarianten und Anforderungen mit überprüfbarer Abnahme. |
| [04 – Implementierungsplan](04-implementierungsplan.md) | Neue CLI-Endpunkte, Datenverträge, Arbeitspakete, Abhängigkeiten und überprüfbare Lieferstufen; bestehende Endpunkte bleiben erhalten. |
| [05 – Umsetzungsnachweis](05-umsetzungsnachweis.md) | Realisierte Arbeitspakete, ausgeführte Abnahme, reproduzierbare Ergebnisse und Laufzeit-/Speicherbefunde. |
| [Numerische Fixtures](fixtures-description-v1.json) | Maschinenlesbare Beispielwerte und kanonische Beschreibungen für spätere Vertragstests. |

**MUSS** bezeichnet eine Anforderung innerhalb dieses Vorschlags.
**SOLL** erlaubt eine begründete technische Alternative mit dokumentierten
Folgen. **Später** bezeichnet eine Erweiterung, die für den ersten
vertragstreuen Stand nicht notwendig ist. Die vorhandenen Tickets werden
hier weder geändert noch als erledigt oder fachlich abgenommen markiert.

## 3. Literatur in technische Anforderungen überführen

Die folgende Matrix trennt Befund, Interpretation und eigene Entscheidung.
Die Buchverweise führen zur lokalen Auswertung mit Screenshot-Fundstellen.

| Grundlage | Verwertbare Aussage | Konsequenz für die Specs |
| --- | --- | --- |
| [London Introduction](<../Justin-London--Hearing-in-Time/0 Introduction/zusammenfassung-VR-12.md>) | Metrum betrifft Wahrnehmung und Handlung, nicht allein Notationsregeln. | Ein vorgegebener Takt ist ein Analyseparameter, keine Feststellung des gehörten Metrums. |
| [London 1](<../Justin-London--Hearing-in-Time/Chapter 1/zusammenfassung-VR-12.md>) | Metrische Aufmerksamkeit organisiert Ereignisse; subjektive Gruppen sind keine eindeutige Beat-Messung. | Ereignisraster, metrische Referenz und Gruppierungsangabe sind verschiedene Daten. |
| [London 2](<../Justin-London--Hearing-in-Time/Chapter 2/zusammenfassung-VR-12.md>) | Zeitbereiche und Unterteilungsnutzen hängen von der Aufgabe ab. | Tempo wird dokumentiert; keine universelle Grenzfunktion oder Gleichsetzung Dichte = Aufwand. |
| [London 3](<../Justin-London--Hearing-in-Time/Chapter 3/zusammenfassung-VR-12.md>) | Instruktion, Bewegung und Erwartung verändern Organisation bei gleicher Oberfläche. | Derselbe Onset-String darf verschiedene Kontexte haben; neuronale Aktivität ist keine Codeeinheit. |
| [London 4](<../Justin-London--Hearing-in-Time/Chapter 4/zusammenfassung-VR-12.md>) | Aufbau und Fortführung sind verschieden; wiederkehrende Unterteilung kann stabil werden. | Kontextbeschreibung erhält bekannte Muster; keine pauschale Rücksetzung pro Beat. |
| [London 5](<../Justin-London--Hearing-in-Time/Chapter 5/zusammenfassung-VR-12.md>) | Hierarchie und absolute Zeitbereiche begrenzen formale Kandidaten. | Eine technische Rasterposition ist keine aktive Aufmerksamkeitsebene; Kandidaten benötigen explizite Ebenen. |
| [London 6](<../Justin-London--Hearing-in-Time/Chapter 6/zusammenfassung-VR-12.md>) | Formbarkeit, tatsächliche Mehrdeutigkeit und metrische Vagheit sind verschieden. | Mehrere Beschreibungen, mehrere Referenzkandidaten und fehlende Referenz bleiben unterscheidbar. |
| [London 7](<../Justin-London--Hearing-in-Time/Chapter 7/zusammenfassung-VR-12.md>) | Ebenen können entstehen und verschwinden; dünnere Oberfläche kann weniger stabil sein. | Rückblickende Gesamtbeschreibung und kausale Fortsetzung sind getrennte Ergebnisse. |
| [London 8](<../Justin-London--Hearing-in-Time/Chapter 8/zusammenfassung-VR-12.md>) | Regelmäßige Zyklen brauchen keine gleich langen Beats; maximale Gleichmäßigkeit ist hierarchisch. | Nichtisochrone Beat-Abstände bleiben darstellbar; `Deviation` ist keine maximal evenness. |
| [London 9](<../Justin-London--Hearing-in-Time/Chapter 9/zusammenfassung-VR-12.md>) | Oberfläche, Timeline und Beat-Zyklus können auseinanderfallen. | Wiederholungsmotiv wird nicht als Beat ausgegeben; keine Beschränkung auf Primfaktoren 2 und 3. |
| [London 10](<../Justin-London--Hearing-in-Time/Chapter 10/zusammenfassung-VR-12.md>) | Erlernte, stil- und tempobezogene Muster sind eine konkurrierende Erklärung. | Bekannter Kontext wird benannt; Kompressionsoptimalität wird nicht als Hörmechanismus behauptet. |
| [London Conclusion](../Justin-London--Hearing-in-Time/Conclusion/zusammenfassung-VR-12.md) | Kürzeste Beschreibung ist ein Komplexitätsbegriff; Schwierigkeit betrifft auch Oberfläche–Referenz-Beziehungen. | Beschreibungslänge, Einsparung, Überraschung und Schwierigkeit erhalten eigene Begriffe. |
| [London Notes](../Justin-London--Hearing-in-Time/Notes/zusammenfassung-VR-12.md) | Zahlreiche Aussagen sind Vermutungen, illustrative Fälle oder eingeschränkte Belege. | Keine Kosten oder Schwellen allein aus einem Hörbeispiel ableiten. |
| [Primärstudienauswertung](../hoerpsychologie-beat-ebene-und-unterteilung.md), insbesondere McAuley/Semple und Bååth | Freie Beat-Wahl und berichtete Gruppen sind mehrdeutig und aufgabenabhängig. | Kein automatischer Schluss von Gruppengröße auf Ereignisse pro Beat. |

Als zusätzliche methodische Quelle wurde
[Grünwald: A Tutorial Introduction to the Minimum Description Length Principle](https://homepages.cwi.nl/~pdg/ftp/mdlintro.pdf)
geprüft, insbesondere §§ 1.4 und 2.2.1. Sie begründet den Anschluss über
Modell- und Datenbeschreibung sowie eindeutig lesbare Codes. Zugleich
warnt sie vor der Abhängigkeit einfacher zweiteiliger Verfahren von der
gewählten Kodierung. Unser endlicher Code ist deshalb ein bewusst
begrenztes Beschreibungsmaß, kein Nachweis eines verfeinerten statistischen
MDL-Verfahrens oder eines optimalen menschlichen Hörmodells.

Der lokale Forschungsstand bleibt maßgeblich: Londons referierte Studien
wurden durch die Buchlektüre nicht zu neu geprüften Primärstudien. Die
vollständige Martens-Auswertung steht in der bestehenden Recherche weiterhin
aus. Das betrifft spätere Wahrnehmungsaussagen, nicht die ausführbare
Definition unserer eigenen Beschreibungssprache.

## 4. Erster vollständiger Produktumfang

Der erste neue Stand MUSS Folgendes gemeinsam leisten:

- bestehende Analyse, Katalogsuche und Playback weiterhin kompatibel bedienen;
- mit `description-v1` Onset-Folgen auf einem ausdrücklich gegebenen Raster
  bis 128 Positionen exakt analysieren, auch ohne Taktangabe;
- Beschreibungen rekonstruieren und Modell-, Rest- und Gesamtkosten erklären;
- isolierte Beschreibungen und kausale Fortsetzungen getrennt ausgeben;
- bei vorhandenen Segmentgrenzen Beat-/Taktverläufe mit benanntem Vertrag
  liefern und deren Streuung korrekt berechnen;
- Rhythmen ohne DB oder Audiozugriff nach Maß, Zielbereich, Dichte,
  Positionen, Länge, Zeitrahmen und optionalen Verläufen erzeugen;
- vollständige und budgetbegrenzte Erzeugung sowie unerreichbare und noch
  nicht gefundene Ziele unterscheiden;
- niedrige Beschreibungsziele auch auf längeren Fenstern durch begrenzte
  Enumeration von Codewörtern statt bloßer Maskenzufälle ansteuern;
- jedes erzeugte Ergebnis mit demselben Analyzer nachprüfen.

128 Positionen sind eine technische erste Grenze, kein Wahrnehmungsfenster.
Sie decken auch den 80-Positionen-Fall mit 50-ms-Raster aus der Recherche
ab, ohne dessen Ereigniszeiten durch gröbere Quantisierung zu verändern.
Die vollständige Generatorabnahme beginnt beim vorhandenen Raum von
16 Positionen. Längere Räume werden mit offenem Vollständigkeitsstatus
bedient. Es ist kein Ziel, alle `2^128` Folgen vollständig zu erzeugen.

Beliebige Audiosignale, automatische Wahrnehmungstoleranzen, Klanggewichte,
ein gelerntes Hörermodell und eine abschließende freie Beat-Erkennung gehören
zu späteren Erweiterungen. Die Kernverträge enthalten dafür die nötigen
Zeit-, Kontext- und Versionsangaben. Diese Erweiterungen erhalten bei
geänderter Semantik eine neue Maßversion.

## 5. Übergabe an die vorhandene Taskstruktur

Am 10. Oktober 2026 wurden VR-1, VR-9, VR-10, VR-11, VR-12, VR-2 und VR-8 erneut
aus Vikunja gelesen. Die Spezifikationen liefern Material für Begriffe,
Beispielmodell, Codevergleich und Taktentscheidung. Sie erweitern den
Auftrag um einen neuen Analysemodus und einen parameterisierten Generator;
das ist mehr als der derzeitige Umfang von VR-2 oder VR-8.

Die spätere Planung MUSS die neuen Implementierungsarbeiten entsprechend
schneiden. Insbesondere darf ein Wechsel des Maßes nicht als bloße
Implementierung des bisherigen VR-2-Auftrags behandelt werden. Es werden
keine Ticketinhalte erfunden und keine bestehenden Abhängigkeiten geändert.

Die Anforderungen und Abnahmekriterien in den Folgedokumenten sind so
geordnet, dass Decoder/Definition, Analyse, Kontext, Generator und
Kompatibilität jeweils konkrete Übergabeergebnisse besitzen. Produktionscode,
Katalogdaten und öffentliche Befehle werden mit diesem Dokument noch nicht
verändert.

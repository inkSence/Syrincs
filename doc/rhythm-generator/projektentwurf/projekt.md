# Projektübersicht: Rhythmisches Informationsmaß musikalisch nutzbar machen

Status: Arbeitsplanung zum bestehenden Vikunja-Projekt VR.
Herkunft: [PV-1](http://localhost:3456/tasks/1).
Fachliche Grundlage: [Schwächen und Zielbild](../schwaechen-und-zielbild.md).
Theoretische Grundlage: [Informationsbegriff](../informationsbegriff.md).
Stand der Planung: 29. September 2026.

Die Aufgaben sind als VR-1 bis VR-8 in Vikunja angelegt. Titel,
Anforderungen und Akzeptanzkriterien werden dort gepflegt; die lokalen
Taskentwürfe wurden entfernt. Diese Übersicht hält Ziel und Abhängigkeiten fest.

## Projektziel

Das vorhandene Informationsmaß wird als musikalisches Arbeitsmittel
zugänglich: Informationsgrad wählen, Deviation einstellen, Verteilung auf
Zählzeiten und nach fachlicher Klärung auf Takte untersuchen, Kandidaten
ansehen und eine nachvollziehbare Auswahl an die bestehende Wiedergabe übergeben.

Reproduzierbarkeit unterstützt diesen Ablauf. Speichern von
Informationsverläufen als Presets ist kein Bestandteil des Kernziels.
Das bestehende Informationsmaß wird nicht nebenbei verändert.

## Ergebnisse und Etappen

**Erster nutzbarer Stand:** Beat-Information sichtbar, Deviation einstellbar,
Kandidaten ohne Playback auffindbar und die Auswahl reproduzierbar.
Die ausgewählten Onsets werden ausgegeben. Die noch offene
Taktdefinition blockiert diesen Stand nicht.

**Fachlicher Kernabschluss:** Zusätzlich ist Taktinformation definiert,
die beschlossene taktweise Analyse implementiert und die Suche nach
zeitlicher Informationsverteilung nutzbar.

## Taskübersicht

| Ticket | Titel | Quelle | Abhängigkeiten |
| --- | --- | --- | --- |
| [VR-1](http://localhost:3456/tasks/9) | Den Informationsbegriff aus Ereigniswahrnehmung und zeitlicher Organisation präzisieren | S2, Bezug zu S8 | keine |
| [VR-2](http://localhost:3456/tasks/10) | Taktweise Analyse gemäß fachlicher Definition umsetzen | S2 | 1,3 |
| [VR-3](http://localhost:3456/tasks/11) | Beat-Informationswerte öffentlich zugänglich machen | S1 | keine |
| [VR-4](http://localhost:3456/tasks/12) | Deviation als Parameter der Informationssuche anbieten | S3 | keine |
| [VR-5](http://localhost:3456/tasks/13) | Informationssuche ohne Wiedergabe bereitstellen | S4, S3 | 3,4 |
| [VR-6](http://localhost:3456/tasks/14) | Zufallsauswahl reproduzierbar und sichtbar machen | S4 | 5 |
| [VR-7](http://localhost:3456/tasks/15) | Nach Beat-Profilen und Informationsmaxima suchen | S5 | 5 |
| [VR-8](http://localhost:3456/tasks/16) | Nach mehrtaktigen Informationsverläufen suchen | S5, S2 | 2,5 |

Die Nummerierung ist keine Rangfolge. Aufgeführt sind nur unmittelbare
Abhängigkeiten: Die Umsetzung benötigt ein konkretes Ergebnis des Vorgängers.
Mittelbare Voraussetzungen werden nicht ein zweites Mal aufgeführt. Reine
Koordination wird separat benannt, damit keine unnötigen Blockaden entstehen.

## Abhängigkeiten

Pfeile zeigen von einer Voraussetzung zum Folgetask. Indirekte
Abhängigkeiten sind im Diagramm nicht zusätzlich eingezeichnet.
Task 2 benötigt sowohl 1 als auch 3; Task 5 sowohl 3 als auch 4;
Task 8 sowohl 2 als auch 5.

```mermaid
flowchart TD
    A["1 · Informationsbegriff präzisieren"] --> B["2 · Taktweise Analyse"]
    C["3 · Beat-Werte zugänglich machen"] --> B
    C --> E["5 · Suche ohne Playback"]
    D["4 · Deviation parametrieren"] --> E
    E --> F["6 · Reproduzierbare Auswahl"]
    E --> G["7 · Nach Beat-Profilen suchen"]
    B --> H["8 · Nach Taktverläufen suchen"]
    E --> H
```

## Empfohlene Bearbeitung

1. Tasks 3 und 4 sind unabhängig umsetzbar. Task 1 beginnt daneben
   als fachliche Klärung.
2. Task 5 folgt auf 3 und 4. Danach folgen 6 für reproduzierbare
   Auswahl und 7 für die zeitliche Suchdimension innerhalb eines Takts.
3. Erst nach abgestimmtem Ergebnis von 1 folgt 2; darauf baut 8 auf.
   Eine Entscheidung für zwei Taktkontexte wird nicht vorausgesetzt.
   Der Abschluss von 1 allein genügt nicht: Ergibt die Untersuchung noch
   keinen akzeptierten Taktvertrag, bleiben 2 und damit 8 nicht startbereit.

Die allgemeinen Qualitätsregeln stehen nicht in einem zusätzlichen
„Tests verbessern“-Task. Jede Umsetzung trägt ihre eigenen fachlichen
Beispiele und geeigneten Prüfungen.

## Abdeckung der Schwächen

| Schwäche | Zuständiger Task |
| --- | --- |
| S1 – Beat-Werte nicht zugänglich | 3 |
| S2 – Taktinformation und taktweise Analyse | 1 (Begriff), 2 (Umsetzung) |
| S3 – Deviation fest verdrahtet | 4 |
| S4 – Suche, Auswahl und Playback gekoppelt | 5, 6 |
| S5 – Keine zeitliche Informationssuche | 7 (Beat), 8 (Taktverlauf) |
| S6 – Direkte Wiedergabe fehlt | Außerhalb dieses Projektumfangs |
| S7 – Katalog und Analysewerte instabil | Außerhalb dieses Projektumfangs |
| S8 – Beschränkte Positionszahlen und Taktbildung | Außerhalb dieses Projektumfangs |

S3 bleibt die Parametrisierung eines bereits vorhandenen skalaren Kriteriums.
S5 führt eine andere Suchdimension ein: die zeitliche Verteilung derselben
Information. Weder Onset-Dichte noch metrische Gewichtung ersetzt das Maß.

## Projekt-Akzeptanzkriterien für den fachlichen Kern

- [ ] Die bestehenden Kennzahlen bleiben kompatibel und die Werte je
  Zählzeit sind öffentlich zugänglich.
- [ ] Deviation ist explizit einstellbar; Such- und Kompatibilitätsdefaults
  sind dokumentiert.
- [ ] Die Kandidatensuche ist ohne Auswahl und Playback bedienbar; eine
  Auswahl kann per Dry-run geprüft werden, und vor dem Playback wird genau
  die an die Wiedergabe übergebene Auswahl angezeigt.
- [ ] Ausgewählte Onsets werden ausgegeben; ein Seed hat einen klar
  begrenzten Reproduzierbarkeitsvertrag.
- [ ] Der Taktinformationsbegriff ist fachlich abgestimmt; die daraus
  abgeleitete Taktanalyse und Taktverlaufsuche entsprechen den Beispielen.
- [ ] Beat-Profile unterscheiden Rhythmen gleicher Gesamtinformation.
- [ ] Normale Tests benötigen keine reale MIDI-Hardware. DB-Verhalten wird
  in einer getrennten Testdatenbank verifiziert.
- [ ] Die öffentlichen Änderungen sind in CLI-Hilfe, Completion und
  Benutzerdokumentation gemeinsam nachvollzogen.

## Festlegungen, offene Entscheidungen und zurückgestellte Ideen

- **Informationsbegriff:** Task 1 untersucht die kürzeste verbleibende
  Beschreibung einschließlich der Kosten des zeitlichen Modells. Globale
  Länge und lokale Kostenzurechnung sind getrennt zu prüfen; die Bedeutung
  von Taktinformation wird daraus abgeleitet. Tasks 2 und 8 benötigen
  anschließend einen ausdrücklich akzeptierten Taktvertrag.
- **Deviation-Defaults:** Task 4 legt als Kompatibilitätsvertrag fest,
  dass alte Playback-Aufrufe weiterhin strikt `> 0.7` verwenden und
  explizite inklusive Grenzen diesen Default vollständig ersetzen.
- **Profilformen:** konstant, steigend, fallend, alternierend und gipfelförmig
  bleiben spätere Erweiterungen. Task 7 beschränkt sich auf exakte
  Beat-Profile und die Position eines Informationsmaximums.
- **Weitere Onset-Filter und Zielprofil-Distanzen:** mögliche spätere
  Ergänzungen, noch kein konkreter Implementierungsauftrag.
- **Dateiablage und Presets:** kein Bestandteil dieser Tasks.
- **Direkte Wiedergabe und Katalogpflege:** S6 und S7 bleiben außerhalb des
  Umfangs. S8 berührt die theoretische Frage von VR-1; die Implementierung
  beliebiger Positionszahlen oder einer Primfaktor-Verteilung bleibt vertagt.

Die zuvor gestrichenen Themen werden nicht als eigenständige Tasks
wiedereingeführt: keine Mapping-Neuentwicklung, allgemeine
Validierungsbereinigung, Architektur-Neufassung, Variationswerkzeuge oder
Exportfunktionen. Notwendige Eingabeprüfung und Tests bleiben Bestandteil
der jeweils angefassten Funktion.

## Verhältnis von Tasksn und Tickets

Jede Datei enthält Titel, Ziel, Umfang, Akzeptanzkriterien, Abgrenzung und
Verifikation. Die Tickets VR-1 bis VR-8 sind angelegt; ihre Beschreibungen
enthalten Ticketverweise für Abhängigkeiten. Die fachliche Begriffsaufgabe bleibt
von den darauf aufbauenden Implementierungsaufgaben unterscheidbar.
Die genaue Befehlsnotation ist ein Task und wird nicht allein durch
Übernahme eines Tasks zur implementierten CLI.

Die Neuausrichtung von VR-1 verändert keine Checkboxen von PV-1.
Die theoretische Skizze bleibt ein eigenständiges Arbeitsdokument.

# Syrincs – fachliche Roadmap

Stand: 10. Juli 2026

Syrincs soll sich vom Materialkatalog zu einem musikalischen
Entscheidungswerkzeug entwickeln. Der größte fachliche Gewinn entsteht durch
erklärbare Hindemith-Analyse, kontrollierte Auswahl sowie Beziehungen zwischen
Akkorden und Rhythmen über die Zeit.

## Prioritäten

| Priorität | Aufgabe | Fachlicher Gewinn | Aufwand |
| --- | --- | --- | --- |
| 1 | Hindemith-Analyse absichern und erklären | Vertrauen in das fachliche Alleinstellungsmerkmal | Mittel |
| 2 | Akkordfolgen mit Stimmführung erzeugen | Aus Einzelakkorden wird musikalische Harmonik | Groß |
| 3 | Rhythmen gezielt suchen statt zufällig ziehen | Reproduzierbare, gestaltbare Rhythmik | Mittel |
| 4 | RDL für beliebige Stimmen und Taktarten vervollständigen | Aus der Drum-Demo wird ein brauchbarer Sequencer | Mittel |
| 5 | MIDI-Dateien exportieren | Ergebnisse werden dauerhaft und DAW-tauglich | Klein bis mittel |
| 6 | Persistenz idempotent und skalierbar machen | Verlässlicher Katalog statt wachsender Duplikatmenge | Mittel |

## 1. Hindemith-Analyse quellengestützt und erklärbar machen

Einige Gruppenspezifikationen sind im Code noch mit `//check` markiert. Ein
Test hält außerdem ausdrücklich fest, dass eine Gruppenzuordnung noch
diskutiert wird. Da die Hindemith-Klassifikation das fachliche Zentrum von
Syrincs ist, sollte ihre Absicherung vor neuen darauf aufbauenden Features
erfolgen.

### Aufgabe: Hindemith-Gruppen vollständig verifizieren

- [ ] Die beiden fachlichen PDFs unter `doc/` systematisch mit den 18
      Spezifikationen in `ChordSpecificationRepository` abgleichen.
- [ ] Für jede der 18 Gruppen mindestens ein eindeutig belegtes Positivbeispiel
      als Domain-Test festhalten.
- [ ] Sinnvolle Gegenbeispiele für überlappende Spezifikationen ergänzen.
- [ ] Alle verbliebenen `//check`-Markierungen fachlich klären und anschließend
      entfernen oder durch eine präzise Quellenangabe ersetzen.
- [ ] `doc/Gruppen.md` vom älteren 14-Gruppen-Stand auf die verifizierten 18
      Gruppen aktualisieren.

### Aufgabe: Invarianten der Akkordanalyse testen

- [ ] Permutationen derselben MIDI-Noten liefern dasselbe Ergebnis.
- [ ] Transpositionen erhalten die Gruppe und verschieben den Grundton
      entsprechend.
- [ ] Unterschiedliche Oktavlagen mit denselben relevanten Intervallstrukturen
      werden bewusst geprüft.
- [ ] Alle zulässigen drei- bis fünfstimmigen Pitch-Class-Kombinationen erhalten
      genau eine Gruppe oder einen fachlich erklärten Fehler.
- [ ] Die Eingabeliste bleibt unverändert und das Ergebnis veröffentlicht nur
      unveränderliche Daten.

### Feature: `analyze chord --explain`

- [ ] Ein frameworkfreies, strukturiertes Analyseergebnis für die Begründung
      entwerfen.
- [ ] Tritonus-/Spaltenentscheidung ausgeben.
- [ ] Intervallqualitäten nach Reihe 2 ausgeben.
- [ ] Maßgebliches Intervall und Grundtonentscheidung erklären.
- [ ] Grundton-/Bass-Beziehung darstellen.
- [ ] Die erfüllten Kriterien der gewählten Gruppe darstellen.
- [ ] Darstellung in der CLI implementieren, ohne Ausgaben in Domain oder Use
      Cases einzuführen.
- [ ] CLI-Tests, Root-Hilfe und `README.md` aktualisieren.

Beispiel:

```bash
syrincs analyze chord --explain 60 64 67
```

## 2. Akkordfolgen mit Stimmführung

`play chords` lädt derzeit alle Datenbanktreffer für genau einen Grundton und
spielt sie in Datenbankreihenfolge. Eine musikalische Auswahl oder Bewertung
der Übergänge gibt es noch nicht.

### Feature: Stimmgeführte Hindemith-Akkordfolgen erzeugen

- [ ] Ein Domain-Modell für Akkordfolge und Akkordübergang einführen.
- [ ] Pro Ziel genau einen Akkord aus Grundton, erlaubten Gruppen,
      Stimmenanzahl und Ambitus wählen.
- [ ] Gemeinsame Töne bevorzugen.
- [ ] Gesamte Halbtonbewegung der Stimmen bewerten.
- [ ] Große Basssprünge, Stimmkreuzungen und Bereichsverletzungen bestrafen.
- [ ] Die einzelnen Kostenbestandteile im Ergebnis sichtbar machen.
- [ ] Die Suche mit `--seed` reproduzierbar machen, sofern mehrere gleich gute
      Lösungen existieren.
- [ ] Zunächst eine feste Stimmenanzahl innerhalb einer Folge verlangen;
      wechselnde Stimmenzahlen erst später ergänzen.
- [ ] Kandidaten über einen Application-Port beziehen, damit Domain und Use
      Case nicht von PostgreSQL abhängen.
- [ ] Folge über SuperCollider oder MIDI abspielen.
- [ ] Domain-, Use-Case- und CLI-Tests ergänzen.

Mögliche Syntax:

```bash
syrincs compose chords 48 53 55 48 \
  --voices 4 --groups 1 2 3 --range 48:76 --seed 42
```

## 3. Rhythmusauswahl kontrollierbar machen

`play rhythm info` lädt aktuell alle Kandidaten eines Informationsgrades mit
ausreichender Deviation und wählt nicht reproduzierbar einen davon. Das
gewählte Onset-Pattern wird vor dem Playback nicht angezeigt.

### Feature: Rhythmen suchen und beschreiben

- [ ] Einen separaten Such-Use-Case einführen; Auswahl und Playback nicht in
      derselben Methode vermischen.
- [ ] Informationsgrad und minimale/maximale Deviation filtern.
- [ ] Anzahl beziehungsweise Dichte der Onsets als verständliches Merkmal
      ergänzen.
- [ ] Verteilung auf metrisch starke und schwache Rasterpositionen als weiteres
      Suchmerkmal prüfen.
- [ ] `--limit` und einen Modus ohne Playback anbieten.
- [ ] Kandidaten einschließlich normalisiertem Onset-String ausgeben.
- [ ] Auswahl mit `--seed` reproduzierbar machen.
- [ ] Fehlende Informationsgrade weiterhin einzeln überspringen, aber sichtbar
      melden.
- [ ] Den vorhandenen Mapping-Stil `FOUR_ON_FLOOR` über die Anwendung erreichbar
      machen, falls er fachlich beibehalten wird.

Mögliche Syntax:

```bash
syrincs search rhythms \
  --info 5 --hits 5..8 --min-deviation 0.7 --limit 10 --seed 42
```

### Technische Verbesserung der Auswahl

- [ ] Nicht mehr sämtliche Kandidaten aus PostgreSQL in den Speicher laden,
      wenn nur ein oder wenige Treffer benötigt werden.
- [ ] Repository-Port mit fachlichen Suchkriterien statt immer längeren
      Einzelmethoden modellieren.
- [ ] Deterministische Zufallsquelle im Test injizieren können.

## 4. RDL für beliebige Stimmen und korrekte Taktarten

Der Parser kann bereits beliebig benannte Stimmen lesen. Die Validierung
verlangt anschließend jedoch exakt `kick` und `snare`. Außerdem verwendet der
MIDI-Sequenzbau für die Schrittdauer bislang nur PPQ und `res-per-beat`; der
Taktnenner beeinflusst das Timing nicht.

### Aufgabe: Voice-Validierung verallgemeinern

- [ ] Jede deklarierte Voice benötigt genau ein gleichnamiges Pattern.
- [ ] Jedes Pattern benötigt eine deklarierte Voice.
- [ ] Beliebige Drum- und MIDI-Stimmen wie Hi-Hat und Toms erlauben.
- [ ] Doppelte Voice- und Patternnamen mit verständlicher Zeilenangabe ablehnen.
- [ ] `gate` vollständig auf `0..100` validieren.
- [ ] Tempo, Taktzähler, Taktnenner, Raster und Taktzahl vollständig validieren.
- [ ] Bestehende Kick-/Snare-Dateien kompatibel halten.

### Aufgabe: Metrisches MIDI-Timing korrigieren

- [ ] Die Bedeutung von BPM und `res-per-beat` für verschiedene Taktnenner
      eindeutig dokumentieren.
- [ ] Tickberechnung unter Berücksichtigung des Taktnenners implementieren.
- [ ] Exakte MIDI-Ereignisse für 3/4, 6/8 und mehrere Takte testen.
- [ ] Gate-Zeitpunkte und Nachlaufzeit für diese Taktarten prüfen.
- [ ] RDL-Dokumentation und Beispielmaterial erweitern.

## 5. MIDI-Dateien exportieren

Syrincs baut bereits `javax.sound.midi.Sequence`-Objekte, kann sie aber nur
unmittelbar an ein MIDI-Gerät senden. Ein Datei-Export schafft einen
reproduzierbaren Workflow ohne angeschlossene Hardware.

### Feature: RDL als Standard-MIDI-Datei exportieren

- [ ] Einen Application-Port für das Schreiben eines musikalischen Ergebnisses
      vorsehen; `javax.sound.midi` bleibt im Adapter.
- [ ] Zunächst vorhandene RDL-Pattern exportieren.
- [ ] Überschreiben vorhandener Dateien nur mit bewusstem Opt-in erlauben.
- [ ] Fehler für nicht beschreibbare Ziele verständlich ausgeben.
- [ ] Exportierte Datei in einem Test wieder einlesen und Tempo, Kanäle, Noten
      und Ticks prüfen.
- [ ] Danach denselben Exportpfad für Akkordfolgen verwenden.

Beispiel:

```bash
syrincs export midi --in data/beat.rdl --out beat.mid
```

## 6. Persistenz idempotent und skalierbar machen

Beide Generatoren hängen derzeit neue Datensätze an. Die Akkorderzeugung legt
außerdem zunächst alle Kombinationen im Speicher ab.

### Aufgabe: Datenqualität sichern

- [ ] Fachlich passende Unique Constraints für Akkorde und Rhythmen definieren.
- [ ] `calculate chords` und `calculate rhythms` mit `ON CONFLICT` idempotent
      machen.
- [ ] CLI-Ausgabe zwischen neu eingefügten und bereits vorhandenen Datensätzen
      unterscheiden lassen.
- [ ] Migration bestehender Tabellen ohne unbeabsichtigten Datenverlust planen.
- [ ] PostgreSQL-Integrationstests für Schema, Migration, Inserts und Filter
      ergänzen.

### Aufgabe: Generierung und Abfragen skalieren

- [ ] Akkorde während der Erzeugung analysieren und batchweise persistieren,
      statt alle Ergebnisse gleichzeitig zu halten.
- [ ] Rhythmusgenerierung ebenfalls inkrementell an das Repository übergeben.
- [ ] Indizes für die tatsächlichen Akkord- und Rhythmussuchen ergänzen.
- [ ] Große Eingabebereiche vorab abschätzen und mit einer verständlichen
      Warnung beziehungsweise einem bewussten Opt-in schützen.

## Später: Scale und Counterpoint fachlich neu aufbauen

Die vorhandenen Klassen sollten nicht unverändert an die CLI gebunden werden.
`Scale` ist noch mutable und stark auf den Klavierbereich zugeschnitten.
`CounterpointInterval` unterscheidet bislang nur statisch zwischen Konsonanz
und Dissonanz; Bewegungsarten und Stimmführungsregeln fehlen.

- [ ] Unveränderliches Modell aus Pitch Class, Tonika und Skalen-/Modusdefinition
      entwerfen.
- [ ] Schreibweisen und enharmonische Gleichheit bewusst behandeln.
- [ ] Fachliche Quellen und Tests für die angebotenen Skalen festlegen.
- [ ] Im Kontrapunkt melodische Bewegung, Gegen-/Parallel-/Seitenbewegung,
      Stimmkreuzungen und parallele Quinten/Oktaven modellieren.
- [ ] Dissonanzbehandlung nicht nur als Intervallklasse, sondern im zeitlichen
      Kontext beschreiben.
- [ ] Erst danach Scale- oder Counterpoint-Befehle öffentlich anbieten.

## Derzeit nicht priorisieren

- [ ] Keine GUI oder DAW-artige Automation beginnen, bevor ein tragfähiges
      Score-/Folgenmodell existiert.
- [ ] Keine große Sample-Library oder weitere Presetfamilien als Hauptfeature
      behandeln; sie vertiefen die algorithmisch-musikalische Domäne kaum.
- [ ] Scale und Counterpoint nicht nur deshalb veröffentlichen, weil bereits
      Klassen dafür existieren.
- [ ] CLI-Kosmetik, Versionsnummern und bekannte historische Syntax nur als
      kleine Begleitarbeiten einplanen, nicht als fachlichen Meilenstein.

## Empfohlener erster Meilenstein

**Explainable Hindemith + erste stimmgeführte Akkordfolge**

1. Die 18 Gruppen anhand der Quellen verifizieren.
2. Strukturierte Analysebegründung und `--explain` implementieren.
3. Ein Übergangsmodell mit nachvollziehbaren Stimmführungskosten entwickeln.
4. Für eine vorgegebene Grundtonfolge jeweils einen Akkord auswählen.
5. Ergebnis zunächst ausgeben und anschließend über vorhandene Ausgänge
   abspielen.

## Ausgangslage der Verifikation

Zum Zeitpunkt der Erstellung dieser Roadmap war `mvn test` erfolgreich:

- 84 Tests ausgeführt
- 0 Fehler
- 0 Fehlschläge
- 14 umgebungsbedingt übersprungene Hardware-, UDP- oder Hörtests


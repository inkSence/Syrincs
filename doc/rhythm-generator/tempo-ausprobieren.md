# Rhythmen direkt im gewünschten Tempo hören

Stand: 4. Oktober 2026, `feat/rhythm-count-in/VR-13`, auf Basis von
`feat/rhythm-tempo/VR-14`. Diese Präsentation zeigt die gemeinsame Anwendung
der Tempo-Erweiterung und des optionalen Einzählens.

Mit `--tempo` kannst du denselben Rhythmus langsamer oder schneller hören,
ohne eine RDL-Datei zu ändern oder eine Katalogauswahl nach RDL zu exportieren.

## Vorbereitung

Führe die Befehle im Repository-Verzeichnis aus:

```bash
mvn package -Dsyrincs.skipCompletionInstall=true
target/app/bin/syrincs devices
```

Die Hörbeispiele verwenden Gervill. Ersetze `--device Gervill` durch einen
anderen passenden Gerätenamen, wenn du einen anderen Klangerzeuger nutzt.
Für RDL ist keine Datenbank nötig; die Katalogbeispiele brauchen erreichbares
PostgreSQL mit vorhandenen Rhythmusdaten.

## 1. Dieselbe RDL-Datei bei 120 und 108 BPM hören

Die mitgelieferte Datei `data/beat.rdl` enthält 120 BPM. Spiele sie zunächst
ohne Überschreibung und danach bei 108 BPM:

```bash
target/app/bin/syrincs play rhythm --in data/beat.rdl --device Gervill
target/app/bin/syrincs play rhythm --in data/beat.rdl --device Gervill --tempo 108
```

Du hörst dieselben Einsätze und Klangfarben mit mehr Zeit zwischen ihnen.
Zähle die vier Viertel mit. Ein 4/4-Takt dauert bei 120 BPM zwei Sekunden,
bei 108 BPM ungefähr 2,22 Sekunden. Auch die Notenlängen folgen dem Tempo,
weil der Gate-Anteil am Raster erhalten bleibt.

Die Datei bleibt unverändert. Ohne `--tempo` gilt wieder ihr Tempoheader.

## 2. Eine Katalogfolge direkt langsamer spielen

Zeige zuerst die Auswahl und das geplante Tempo, ohne etwas abzuspielen:

```bash
target/app/bin/syrincs play rhythm info 3 5 3 --deviation-min 0 --seed 42 --tempo 108 --dry-run
```

Auf die Auswahlzeilen folgt:

```text
[PLAYBACK] TempoBpm=108
```

Entferne nun nur `--dry-run` und ergänze das Gerät:

```bash
target/app/bin/syrincs play rhythm info 3 5 3 --deviation-min 0 --seed 42 --tempo 108 --device Gervill
```

Alle drei ausgewählten Takte laufen bei 108 BPM. Für den Hörvergleich
wiederhole denselben Befehl mit `--tempo 120`. Bei unverändertem Katalog
und gleichem Seed hörst du dieselben Patterns; Information und Deviation
ändern sich durch das Playback-Tempo nicht.

## 3. Vererbung und Überschreibung ausprobieren

Du kannst das Tempo vor `info` angeben. Eine Angabe nach `info` hat Vorrang:

```bash
target/app/bin/syrincs play rhythm --tempo 96 info 3 --seed 42 --dry-run
target/app/bin/syrincs play rhythm --tempo 96 info 3 --tempo 108 --seed 42 --dry-run
```

Die erste Vorschau zeigt 96 BPM, die zweite 108 BPM. Ohne Tempoangabe
zeigt die Katalogvorschau weiterhin 120 BPM.

## 4. Tempo und Einzählen gemeinsam hören

Lass dir vor der gesamten Wiedergabe einen 4/4-Takt einzählen:

```bash
target/app/bin/syrincs play rhythm --in data/beat.rdl --tempo 108 --count-in --device Gervill
target/app/bin/syrincs play rhythm info 3 5 3 --deviation-min 0 --seed 42 --tempo 108 --count-in --dry-run
target/app/bin/syrincs play rhythm info 3 5 3 --deviation-min 0 --seed 42 --tempo 108 --count-in --device Gervill
```

Die Vorschau ergänzt diese beiden Zeilen, ohne ein MIDI-Gerät zu öffnen:

```text
[PLAYBACK] TempoBpm=108
[COUNT-IN] Bars=1 | Beats=4 | Time=4/4
```

Einzählen und Rhythmus verwenden beide 108 BPM. Vor der gesamten
dreitaktigen Katalogfolge erklingt genau ein Einzähltakt mit vier Schlägen
und betonter Eins. Anfangspausen des Rhythmus bleiben erhalten. Vergleiche
denselben Aufruf mit `--tempo 120`; die Patterns bleiben bei gleichem Seed
und unverändertem Katalog dieselben.

`--count-in` aktiviert das Einzählen vor oder nach `info`. Es läuft nur vor
der Folge; während der Rhythmuswiedergabe gibt es keinen zusätzlichen Klick.

## Grenzen

`--tempo` akzeptiert ganze BPM-Zahlen von 4 bis 60.000.000; dies sind
technische Grenzen des Tempoformats, keine musikalisch empfohlenen Werte.
Null, negative Werte, Dezimalzahlen und Werte außerhalb dieses Bereichs
werden vor DB-Abfrage und Playback abgewiesen.

Einzählen unterstützt derzeit nur 4/4. Mit aktivierter Option werden andere
Taktarten auch bei `--dry-run` abgewiesen. Das gewählte MIDI-Gerät bestimmt
die Klangfarbe; die Geräteoption lädt keine bestimmte Soundbank.

Weitere Details stehen in der [Projekt-README](../../README.md) und der
[Rhythmusmodul-Dokumentation](README.md).

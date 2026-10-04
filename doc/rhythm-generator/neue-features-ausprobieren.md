# Die neuen Rhythmusfeatures ausprobieren

Stand: 4. Oktober 2026, `feat/rhythm-count-in/VR-13`, auf Basis von
`feat/rhythm-tempo/VR-14`. Enthält den PROJEKT-Stand mit VR-3 bis VR-7
(`90e0629`), das optionale Einzählen aus VR-13 und die Tempooption aus VR-14.

Syrincs macht die zeitliche Verteilung seines Informationsmaßes jetzt
sichtbar und gezielt durchsuchbar. Du kannst Rhythmen untersuchen, passende
Kandidaten vergleichen und eine zufällige Auswahl vor dem Hören prüfen und
anschließend wiederholen.

Diese Hörtour führt dich von zwei selbst eingegebenen Rhythmen bis zu
einer reproduzierbaren Folge aus dem Katalog. Jeder Abschnitt verbindet
**anzeigen → analysieren → hören → vergleichen**. Auch die einzelnen
Suchtreffer kannst du gezielt hören. Mit dem neuen Einzählen findest du
vor dem ersten Rhythmustakt den gemeinsamen Puls. Mit `--tempo` kannst du
denselben Rhythmus direkt langsamer oder schneller hören.

## Was du gewonnen hast

| Feature | Was du damit tun kannst | Neue Optionen / Befehle |
| --- | --- | --- |
| Beat-Information (VR-3) | Erkennen, wie sich Information auf die einzelnen Beats verteilt. | `analyze rhythm --details` |
| Deviation-Grenzen (VR-4) | Rhythmen mit gleichmäßigerer oder stärker schwankender Beat-Information auswählen. | `--deviation-min`, `--deviation-max` |
| Suche ohne Playback (VR-5) | Kandidaten ansehen, bevor du eine Auswahl triffst. | `search rhythms`, `--limit` |
| Wiederholbare Auswahl (VR-6) | Eine konkrete Zufallsauswahl vorab sehen und mit demselben Seed erneut erzeugen. | `--seed`, `--dry-run` |
| Profil- und Peak-Suche (VR-7) | Genau vier Beat-Werte oder ein Informationsmaximum auf einer bestimmten Zählzeit verlangen. | `--beat-profile`, `--peak-beat` |
| Einzählen (VR-13) | Vor der ganzen Folge einen Takt mit vier hohen Klicks hören und im Tempo mitgehen. | `play rhythm … --count-in`, auch bei `info` |
| Tempo wählen (VR-14) | Dieselbe RDL-Datei oder Katalogfolge direkt im gewünschten Tempo hören. | `play rhythm … --tempo BPM`, auch bei `info` |

## Vorbereitung

Führe die Befehle im Repository-Verzeichnis aus. Baue zuerst das aktuelle
Startartefakt auf dem oben genannten Branch, damit Tempooption und Einzählen verfügbar sind:

```bash
mvn package -Dsyrincs.skipCompletionInstall=true
target/app/bin/syrincs --help
```

Die Beispiele verwenden bewusst `target/app/bin/syrincs`, damit ein eventuell
älterer, separat installierter `syrincs`-Befehl nicht dazwischenkommt.

| Teil der Tour | Voraussetzung |
| --- | --- |
| Eigene Rhythmen analysieren | JDK 21, Maven und der Paketbau |
| Katalog durchsuchen und Auswahl als Vorschau zeigen | zusätzlich erreichbares PostgreSQL mit Rhythmusdaten |
| Auswahl hören | zusätzlich ein MIDI-Ausgang mit angeschlossenem oder eingerichtetem Klangerzeuger |

SuperCollider wird für diese Rhythmusfeatures nicht benötigt.

### Den Hörweg einmal vorbereiten

Prüfe deinen MIDI-Ausgang, bevor du die Hörbeispiele startest:

```bash
target/app/bin/syrincs devices
```

Syrincs unterstützt die automatische Geräteauswahl. Bei einem DP603 verwende
`--device DP603`, sofern es in der Geräteliste steht. Verwende sonst einen
passenden Teil des Gerätenamens.
Auch ein Software-Klangerzeuger mit eingerichtetem MIDI-Ausgang ist möglich.
Für den in unserer Hörtour verwendeten Java-Software-Synthesizer ergänze
`--device Gervill --soundbank /usr/share/sounds/sf2/FluidR3_GM.sf2`, sofern
Gervill in der Geräteliste steht und die Soundbankdatei vorhanden ist.
Die folgenden Hörbefehle verwenden diesen Weg für unsere lokale Präsentation.
Bei einem Hardware-Klangerzeuger ersetze `--device Gervill` durch dessen Namen
und entferne `--soundbank` samt Pfad.

Die [Hördateien für A](hoertour/vergleich-a.rdl),
[B](hoertour/vergleich-b.rdl) und [A/B](hoertour/vergleich-a-b.rdl)
liegen bereits neben dieser Anleitung. Eigene Patterns und Suchausgaben
kannst du mit dem lokalen [RDL-Exporthelfer](hoertour/onsets-zu-rdl.sh)
in Hördateien umwandeln. Dafür ist keine neue Syrincs-CLI-Option nötig.
Der Helfer benötigt Bash und das JDK aus dem Paketbau und öffnet selbst
weder MIDI noch eine Datenbank.

So exportierst du ein selbst eingegebenes Pattern für vier Wiederholungen:

```bash
bash doc/rhythm-generator/hoertour/onsets-zu-rdl.sh /tmp/syrincs-eigenes-pattern.rdl 4 "xooo xoxo xooo xoxo"
target/app/bin/syrincs play rhythm --in /tmp/syrincs-eigenes-pattern.rdl --device Gervill --soundbank /usr/share/sounds/sf2/FluidR3_GM.sf2
```

Die erste Zahl gibt an, wie oft die **gesamte eingegebene Folge** gespielt
wird (1 bis 64). Du kannst weitere Onset-Strings dahinter angeben. Ein
Onset-String darf mehrere vollständige 4/4-Takte enthalten; sie werden
einzeln gemappt und danach verbunden. Die gewählte Ausgabedatei wird erzeugt
oder ersetzt. Die Beispiele speichern solche Arbeitsdateien unter `/tmp`.

Die bisherigen Hördateien nutzen 120 BPM, Kick 36 und Snare 38 auf MIDI-Kanal 10,
Velocity 90 und Gate 50 Prozent. Die Zuordnung entspricht dem aktuellen
DB-Playback. Ein Informationsmaximum erhält keinen zusätzlichen Lautstärkeakzent.
Zähle beim Vergleich die vier Beats mit. Mit `--count-in` kannst du jedem
Rhythmus-Playback einen Takt Einzählen voranstellen; während des Rhythmus
läuft kein Metronom mit. Abschnitt 8 führt dich durch das Einzählen und
unseren A/B-Vergleich bei 108 BPM.

## 1. Zwei Rhythmen mit gleicher Gesamtinformation unterscheiden

Gib zuerst dieses Pattern ein:

```bash
target/app/bin/syrincs analyze rhythm "xooo xoxo xooo xoxo" --details
```

Die tatsächliche Ausgabe lautet:

```text
[ANALYZE] Rhythm=xoooxoxoxoooxoxo | Info=3 | Deviation=0.433013 | Beats=[xooo, xoxo, xooo, xoxo]
[DETAILS] BeatInformation=[1, 1, 0, 1] | Mean=0.750000
```

Vergleiche damit:

```bash
target/app/bin/syrincs analyze rhythm "xoxo xooo xoxo xooo" --details
```

```text
[ANALYZE] Rhythm=xoxoxoooxoxoxooo | Info=3 | Deviation=0.829156 | Beats=[xoxo, xooo, xoxo, xooo]
[DETAILS] BeatInformation=[2, 0, 1, 0] | Mean=0.750000
```

Beide Rhythmen haben Gesamtinformation **3** und Mittelwert **0,75**.
Ihre zeitliche Gestalt unterscheidet sich aber:

| Rhythmus | Beat 1 | Beat 2 | Beat 3 | Beat 4 | Deviation |
| --- | ---: | ---: | ---: | ---: | ---: |
| `xooo xoxo xooo xoxo` | 1 | 1 | 0 | 1 | 0,433013 |
| `xoxo xooo xoxo xooo` | 2 | 0 | 1 | 0 | 0,829156 |

Das zweite Pattern bündelt mehr Information auf dem ersten Beat. Seine
Beat-Werte streuen stärker. Genau diese Unterschiede kannst du jetzt
sichtbar machen und in der Suche verwenden.

**So liest du die Werte:** `x` bedeutet Einsatz, `o` keinen neuen Einsatz.
Jede Vierergruppe entspricht einem Beat mit vier 16tel-Positionen.
`BeatInformation` zeigt die Informationswerte in Beat-Reihenfolge;
`Info` ist deren Summe und `Mean` ihr Mittelwert. `Deviation` beschreibt
ihre Streuung. Die Zahlen zählen Codesymbole im bestehenden Syrincs-Modell,
nicht einfach die Einsätze. Ein Beat-Wert von 0 kann deshalb auch bei einem
Beat mit Einsatz auftreten. Die Werte sind keine Messung der Lautstärke oder
ein empirischer Nachweis musikalischer Komplexität.

Du kannst hier eigene `x`/`o`-Patterns einsetzen. Für die Analyse muss die
Länge nach Entfernen von Leerzeichen ein positives Vielfaches von 16 sein.
Bei mehreren Takten enthält die Detailausgabe alle Beat-Werte nacheinander.
Dieser Schritt benötigt weder Datenbank noch MIDI und speichert nichts.

### Jetzt A und B hören

Höre zuerst A vier Takte lang, danach B vier Takte lang:

```bash
target/app/bin/syrincs play rhythm --in doc/rhythm-generator/hoertour/vergleich-a.rdl --device Gervill --soundbank /usr/share/sounds/sf2/FluidR3_GM.sf2
target/app/bin/syrincs play rhythm --in doc/rhythm-generator/hoertour/vergleich-b.rdl --device Gervill --soundbank /usr/share/sounds/sf2/FluidR3_GM.sf2
```

Höre anschließend den direkten Wechsel: ein Takt A, ein Takt B, viermal
hintereinander, insgesamt acht Takte:

```bash
target/app/bin/syrincs play rhythm --in doc/rhythm-generator/hoertour/vergleich-a-b.rdl --device Gervill --soundbank /usr/share/sounds/sf2/FluidR3_GM.sf2
```

Lies währenddessen die Profilwerte in der Tabelle mit. Kannst du den Wechsel
erkennen? Welche Onsets unterscheiden die Patterns trotz gleicher Summe?
Die Profile beschreiben das bestehende Modell; wie deutlich du ihre
Unterschiede hörst, ist die Frage dieses Vergleichs.

Für einen hörbaren gemeinsamen Start ergänze `--count-in`. Wenn du unseren
Vergleich direkt bei 108 BPM wiederholen möchtest, gehe zu Abschnitt 8.

Die Analysewerte oben gelten jeweils für einen einzeln analysierten Takt.
Die Hördateien wiederholen dessen Einsätze ohne erneute Informationsanalyse.
Analysierst du dagegen einen mehrtaktigen Onset-String, läuft der Zustand
über Taktgrenzen weiter und spätere Beat-Werte können abweichen.

## 2. Den Katalog ansehen, ohne etwas abzuspielen

Prüfe zunächst, ob die konfigurierte Datenbank erreichbar ist:

```bash
target/app/bin/syrincs start db
```

Der Befehl prüft die Verbindung; er startet PostgreSQL nicht selbst.
Die lokale Einrichtung beschreibt die [Projekt-README](../../README.md#postgresql).

Wenn noch kein Rhythmuskatalog vorhanden ist, bereite ihn einmal vor:

```bash
target/app/bin/syrincs init
target/app/bin/syrincs calculate rhythms
```

`init` legt das Schema idempotent an. `calculate rhythms` schreibt alle
65.536 eintaktigen 4/4-Patterns in die Datenbank. **Überspringe die Berechnung,
wenn dein Katalog schon gefüllt ist:** Jeder weitere Aufruf hängt dieselben
Patterns erneut an. Für die folgenden Beispiele reicht der vorhandene Katalog.

Suche nun nach Rhythmen mit Gesamtinformation 3:

```bash
target/app/bin/syrincs search rhythms --info 3 --limit 5
```

Die Ausgabe besteht aus einer `[SEARCH]`-Kopfzeile und
`[CANDIDATE]`-Zeilen. Jede Kandidatenzeile zeigt Onsets, Beat-Profil,
Gesamtinformation und Deviation. In der Kopfzeile bedeutet:

- `Total`: Anzahl aller eindeutigen Treffer nach Anwendung der Filter.
- `Shown`: Anzahl der tatsächlich angezeigten Kandidaten.
- `Limit`: maximale Anzahl der ausgegebenen Kandidaten, hier 5.

Der Befehl wählt nichts zufällig aus und öffnet kein MIDI-Gerät. Die
Kandidaten erscheinen in stabiler Reihenfolge. Doppelte Katalogeinträge
erscheinen in der Ausgabe nur einmal; die Suche löscht sie nicht aus der DB.

### Genau die angezeigten Kandidaten hören

Speichere die Suchausgabe und lies sie vor dem Export:

```bash
target/app/bin/syrincs search rhythms --info 3 --limit 5 > /tmp/syrincs-kandidaten.txt
cat /tmp/syrincs-kandidaten.txt
bash doc/rhythm-generator/hoertour/onsets-zu-rdl.sh /tmp/syrincs-kandidaten.rdl 1 --from-output /tmp/syrincs-kandidaten.txt
target/app/bin/syrincs play rhythm --in /tmp/syrincs-kandidaten.rdl --device Gervill --soundbank /usr/share/sounds/sf2/FluidR3_GM.sf2
```

Du hörst einen Takt je **angezeigtem** Kandidaten, in derselben Reihenfolge.
`Total` kann größer sein, aber nur die `Shown`-Kandidaten werden exportiert.
Verfolge beim Hören die Onset-Strings. Leere Stellen im Pattern bleiben
stille Stellen; jeder Takt dauert bei 120 BPM zwei Sekunden, am Ende folgt
der übliche Nachlauf. Für eine Wiederholung der ganzen Folge ersetze die
`1` im Exportbefehl durch `4`.

Um nur einen bestimmten Treffer zu hören, kopiere seinen `Rhythm=`-Wert:

```bash
bash doc/rhythm-generator/hoertour/onsets-zu-rdl.sh /tmp/syrincs-ein-treffer.rdl 4 "ooooooooooooooox"
target/app/bin/syrincs play rhythm --in /tmp/syrincs-ein-treffer.rdl --device Gervill --soundbank /usr/share/sounds/sf2/FluidR3_GM.sf2
```

Das Beispiel ist der erste Treffer der zuvor geprüften Suche. Du hörst
viermal einen Einsatz auf der letzten 16tel-Position des Takts. Ersetze
den String durch einen beliebigen angezeigten 4/4-Kandidaten. Ein Peak oder
eine hohe Informationszahl kann auch durch lange Stille und einen einzelnen
späten Einsatz entstehen; die Zahl allein sagt nicht, wie dicht es klingt.

## 3. Die Streuung der Beat-Information eingrenzen

Suche zunächst nach eher gleichmäßig verteilter Information:

```bash
target/app/bin/syrincs search rhythms --info 3 --deviation-max 0.5 --limit 8 > /tmp/syrincs-deviation-gering.txt
cat /tmp/syrincs-deviation-gering.txt
```

Das Beispielprofil `[1,1,0,1]` erfüllt diese Grenze, `[2,0,1,0]` nicht.
Beide haben weiterhin Gesamtinformation 3.

Suche anschließend nach stärkerer Streuung:

```bash
target/app/bin/syrincs search rhythms --info 3 --deviation-min 0.8 --limit 8 > /tmp/syrincs-deviation-hoch.txt
cat /tmp/syrincs-deviation-hoch.txt
```

Hier erfüllt `[2,0,1,0]` die Grenze, `[1,1,0,1]` nicht.
Auch ein geschlossenes Intervall ist möglich:

```bash
target/app/bin/syrincs search rhythms --info 3 --deviation-min 0.2 --deviation-max 0.8 --limit 5
```

**Suche und Playback haben unterschiedliche Defaults:**

| Aufruf | Deviation-Bedingung |
| --- | --- |
| `search rhythms` ohne Grenzen | kein Deviation-Filter |
| `play rhythm info` ohne Grenzen | weiterhin strikt `> 0.7` |
| einer der beiden Befehle mit nur Minimum | `>= Minimum` |
| einer der beiden Befehle mit nur Maximum | `<= Maximum`, ohne verstecktes Minimum |
| einer der beiden Befehle mit beiden Grenzen | beide Grenzen inklusive |

Explizite Grenzen ersetzen beim Playback den Default vollständig.
Verwende einen Punkt als Dezimaltrennzeichen. Grenzen müssen endlich und
nichtnegativ sein; das Minimum darf nicht größer als das Maximum sein.

### Zwei Gruppen mit unterschiedlicher Deviation hören

Wir vergleichen diesmal zwei Gruppen statt der beiden festen Patterns aus
Abschnitt 1: zuerst **acht Suchtreffer mit Deviation ≤ 0,5**, direkt danach
**acht Suchtreffer mit Deviation ≥ 0,8**. Alle haben Gesamtinformation 3.
Die Suchausgaben oben enthalten jeweils die ersten acht eindeutigen Treffer
in stabiler Reihenfolge. Prüfe vor dem Export, dass beide Ausgaben `Shown=8`
melden; `--limit 8` ist eine Obergrenze, keine Garantie für genügend Treffer.

Verbinde beide Ausgaben in dieser Reihenfolge und exportiere sie gemeinsam:

```bash
cat /tmp/syrincs-deviation-gering.txt /tmp/syrincs-deviation-hoch.txt > /tmp/syrincs-deviation-gruppen.txt
bash doc/rhythm-generator/hoertour/onsets-zu-rdl.sh /tmp/syrincs-deviation-gruppen.rdl 1 --from-output /tmp/syrincs-deviation-gruppen.txt
target/app/bin/syrincs play rhythm --in /tmp/syrincs-deviation-gruppen.rdl --tempo 108 --count-in --device Gervill --soundbank /usr/share/sounds/sf2/FluidR3_GM.sf2
```

Du hörst **einen Einzähltakt**, dann 16 Rhythmustakte: je einen Takt pro
Treffer. Takte 1–8 gehören zur Gruppe mit geringer Streuung, Takte 9–16 zur
Gruppe mit hoher Streuung. Zwischen den Gruppen gibt es weder eine zusätzliche
Pause noch ein zweites Einzählen; beide liegen in derselben RDL-Datei und
laufen in einem Playback. Der übliche Nachlauf folgt erst nach Takt 16.

Zähle die Takte mit und achte auf den Gruppenwechsel nach acht Takten,
bei 108 BPM etwa 17,78 Sekunden nach Beginn des ersten Rhythmustakts.
Vergleiche die Verteilung der Einsätze innerhalb der Gruppen. Stärkere
Streuung der Informationswerte bedeutet keine höhere Lautstärke und muss
auch nicht deiner subjektiven Einschätzung von Unruhe entsprechen.
Dies sind die ersten acht Suchtreffer je Bereich, keine Zufallsstichprobe;
die Hörfolge zeigt diese konkreten Beispiele, nicht alle möglichen Rhythmen
eines Deviation-Bereichs. Für eine Wiederholung spiele dieselbe Datei erneut.

## 4. Ein exaktes Beat-Profil suchen

Jetzt kannst du die beiden Anfangsbeispiele gezielt auseinanderhalten:

```bash
target/app/bin/syrincs search rhythms --info 3 --beat-profile 1,1,0,1 --limit 5
target/app/bin/syrincs search rhythms --info 3 --beat-profile 2,0,1,0 --limit 5
```

Alle Treffer des ersten Aufrufs tragen `[1,1,0,1]`, alle Treffer des zweiten
`[2,0,1,0]`. Unterschiedliche Onset-Patterns können dasselbe Profil besitzen;
du suchst die zeitliche Informationsverteilung, keinen einzelnen Onset-String.

Bei der lokalen Prüfung am 3. Oktober 2026 lieferte die erste Suche drei
eindeutige Treffer, die zweite vier. Die erste Suche zeigte:

```text
[SEARCH] Info=3 | DeviationMin=none | DeviationMax=none | Limit=5 | Total=3 | Shown=3 | BeatProfile=[1, 1, 0, 1] | PeakBeat=none
[CANDIDATE] Time=4/4 | Rhythm=xoooooooooooxooo | BeatInformation=[1, 1, 0, 1] | Info=3 | Deviation=0.433013
[CANDIDATE] Time=4/4 | Rhythm=xoooxoxoxooooooo | BeatInformation=[1, 1, 0, 1] | Info=3 | Deviation=0.433013
[CANDIDATE] Time=4/4 | Rhythm=xoooxoxoxoooxoxo | BeatInformation=[1, 1, 0, 1] | Info=3 | Deviation=0.433013
```

Diese Trefferzahlen sind eine Momentaufnahme des vorhandenen Katalogs.

Das Profil verlangt exakt vier nichtnegative Ganzzahlen. Die Summe muss zum
weiterhin erforderlichen `--info` passen: Beide Beispiele summieren sich zu 3.
Profilfilter gelten für genau einen 4/4-Takt.

### Unterschiedliche Rhythmen mit demselben Profil hören

Exportiere die tatsächlich gefundenen Patterns des ersten Profils:

```bash
target/app/bin/syrincs search rhythms --info 3 --beat-profile 1,1,0,1 --limit 5 > /tmp/syrincs-profil.txt
cat /tmp/syrincs-profil.txt
bash doc/rhythm-generator/hoertour/onsets-zu-rdl.sh /tmp/syrincs-profil.rdl 4 --from-output /tmp/syrincs-profil.txt
target/app/bin/syrincs play rhythm --in /tmp/syrincs-profil.rdl --device Gervill --soundbank /usr/share/sounds/sf2/FluidR3_GM.sf2
```

Im oben geprüften Katalog hörst du drei unterschiedliche Takte als Folge,
die viermal wiederholt wird. Alle drei haben dasselbe Profil `[1,1,0,1]`.
Vergleiche die Onsets und ihren Klang: Welche Unterschiede bleiben, obwohl
das Profil gleich ist? Wiederhole den Versuch mit `--beat-profile 2,0,1,0`.

## 5. Ein Informationsmaximum auf einer Zählzeit verlangen

Du musst nicht das ganze Profil vorgeben. Suche beispielsweise nach einem
globalen Informationsmaximum auf Beat 2 und begrenze zugleich die Streuung:

```bash
target/app/bin/syrincs search rhythms --info 3 --peak-beat 2 --deviation-max 0.5 --limit 5
```

Die Zählzeiten beginnen bei **1**. Ein Peak muss nicht einzigartig sein:

| Profil | Beats, die als Peak zählen |
| --- | --- |
| `[1,1,0,1]` | 1, 2 und 4 |
| `[2,0,1,0]` | nur 1 |
| `[0,0,0,0]` | 1, 2, 3 und 4 |

Du kannst alle Kriterien kombinieren:

```bash
target/app/bin/syrincs search rhythms --info 3 --beat-profile 1,1,0,1 --peak-beat 4 --deviation-max 0.5 --limit 1
```

Der Kandidat muss **alle** Bedingungen erfüllen. Die Filter werden vor
Trefferzählung und Ausgabe-Limit angewandt: `--limit 1` zeigt einen passenden
Kandidaten, während `Total` weiterhin sämtliche eindeutigen Treffer zählt.

Eine gültige Kombination kann auch bewusst keine Treffer liefern:

```bash
target/app/bin/syrincs search rhythms --info 3 --beat-profile 2,0,1,0 --deviation-max 0.5
```

Dieses Profil hat Deviation von ungefähr 0,829156 und passt deshalb nicht
unter 0,5. `Total=0` und `Shown=0` sind hier ein korrektes Suchergebnis.

### Peaks an verschiedenen Positionen hören

Suche je einen Kandidaten mit Peak auf Beat 1 und auf Beat 4:

```bash
target/app/bin/syrincs search rhythms --info 3 --peak-beat 1 --deviation-min 0.7 --limit 1 > /tmp/syrincs-peak-1.txt
target/app/bin/syrincs search rhythms --info 3 --peak-beat 4 --deviation-min 0.7 --limit 1 > /tmp/syrincs-peak-4.txt
cat /tmp/syrincs-peak-1.txt
cat /tmp/syrincs-peak-4.txt
bash doc/rhythm-generator/hoertour/onsets-zu-rdl.sh /tmp/syrincs-peak-1.rdl 4 --from-output /tmp/syrincs-peak-1.txt
bash doc/rhythm-generator/hoertour/onsets-zu-rdl.sh /tmp/syrincs-peak-4.rdl 4 --from-output /tmp/syrincs-peak-4.txt
target/app/bin/syrincs play rhythm --in /tmp/syrincs-peak-1.rdl --device Gervill --soundbank /usr/share/sounds/sf2/FluidR3_GM.sf2
target/app/bin/syrincs play rhythm --in /tmp/syrincs-peak-4.rdl --device Gervill --soundbank /usr/share/sounds/sf2/FluidR3_GM.sf2
```

Lies zuerst die tatsächlichen Profile und zähle beim Hören mit.
Liegt das Informationsmaximum dort, wo du einen musikalischen Schwerpunkt
empfindest? Kick-/Snare-Klangfarben beeinflussen diesen Eindruck zusätzlich.
Der Export verstärkt die Peak-Position nicht: Du hörst die Einsätze mit
derselben Zuordnung und Velocity wie beim regulären DB-Playback.

Für Gleichstände kannst du A mit `[1,1,0,1]` noch einmal hören: Dass Beat 1,
2 und 4 alle als Peaks zählen, ist eine Eigenschaft des Profils. Das
Nullprofil `[0,0,0,0]` hat ebenfalls vier rechnerische Peaks; beim
vollständig leeren Onset-Pattern hörst du trotzdem nur Stille.

## 6. Eine Folge auswählen und exakt wiederholen

Lass dir eine Folge aus drei Takten zeigen, ohne sie abzuspielen:

```bash
target/app/bin/syrincs play rhythm info 3 5 3 --deviation-min 0 --seed 42 --dry-run
```

Syrincs wählt je angefragter Position einen Katalogrhythmus:
erst einen mit Information 3, dann einen mit 5, dann wieder einen mit 3.
`--deviation-min 0` schließt hier auch gleichmäßige Profile ein und ersetzt
den sonst geltenden Playback-Filter `> 0.7`.

Die `[SELECTION]`-Zeile zeigt Seed, Auswahlverfahren, Filter und Anzahl.
Jede `[SELECTED]`-Zeile zeigt Position, angefragte Information, Onsets,
Beat-Profil und Analysewerte. Der wiederholte Grad 3 bleibt eine eigene
Position; an beiden Positionen darf auch dasselbe Pattern gezogen werden.

Mit dem lokal vorhandenen Katalog ergab Seed 42 bei der Prüfung:

```text
[SELECTION] Seed=42 | Algorithm=java.util.Random | DeviationFilter=DeviationRange[min=0.0, max=null] | Count=3
[SELECTED] Position=1 | RequestedInfo=3 | Time=4/4 | Rhythm=ooooxoooooooxooo | BeatInformation=[0, 1, 1, 1] | Info=3 | Deviation=0.433013
[SELECTED] Position=2 | RequestedInfo=5 | Time=4/4 | Rhythm=ooooxoxxxoxxxooo | BeatInformation=[0, 3, 2, 0] | Info=5 | Deviation=1.299038
[SELECTED] Position=3 | RequestedInfo=3 | Time=4/4 | Rhythm=ooxoxoxoxoooxooo | BeatInformation=[2, 1, 0, 0] | Info=3 | Deviation=0.829156
```

**Führe denselben Befehl ein zweites Mal aus.** Mit demselben Seed,
unveränderten Kandidaten, denselben Kriterien und derselben Programmversion
bekommst du dieselbe Auswahl. Ändere danach `--seed 42` auf `--seed 43`:
Damit erhältst du eine weitere deterministische Ziehung. Einzelne Takte
können zufällig trotzdem übereinstimmen.

Ohne `--seed` wird ein Seed erzeugt und angezeigt. Du kannst ihn für einen
späteren Aufruf übernehmen. Nach Änderungen am Katalog oder Algorithmus
garantiert er die alte Auswahl nicht mehr; Seed und Auswahl werden nicht
automatisch gespeichert.

`--dry-run` benötigt weiterhin PostgreSQL, aber weder einen MIDI-Ausgang
noch Playback. Fehlen Kandidaten für einen angefragten Grad, meldet Syrincs
alle betroffenen Positionen und bricht den gesamten Aufruf ab.

## 7. Die vorgeschlagene Folge hören

Wenn ein MIDI-Ausgang mit Klangerzeuger bereitsteht, lass dir die Geräte zeigen:

```bash
target/app/bin/syrincs devices
```

Entferne dann nur `--dry-run` aus der vorherigen Auswahl:

```bash
target/app/bin/syrincs play rhythm info 3 5 3 --deviation-min 0 --seed 42 --device Gervill --soundbank /usr/share/sounds/sf2/FluidR3_GM.sf2
```

Bei unverändertem Katalog hörst du genau die zuvor gezeigte Auswahl.
Sie wird in der angefragten Reihenfolge als zusammenhängende Kick-/Snare-Folge
über MIDI gespielt, derzeit mit 120 BPM.

Wenn du gezielt dein DP603 verwenden möchtest und es in der Geräteliste
vorhanden ist:

```bash
target/app/bin/syrincs play rhythm info 3 5 3 --deviation-min 0 --seed 42 --device DP603 --device Gervill --soundbank /usr/share/sounds/sf2/FluidR3_GM.sf2
```

Ersetze `DP603` bei Bedarf durch einen passenden Teil des Namens deines
MIDI-Ausgangs. Ohne explizite Geräteangabe greift die bestehende automatische
Geräteauswahl. Ein verfügbarer Ausgang allein garantiert noch keinen hörbaren
Klangerzeuger.

### Die Auswahl behalten und mehrere Seeds hörend vergleichen

Du kannst die Vorschau als konkrete Folge festhalten. Dann hängt das
spätere Hören dieser Datei nicht von einer erneuten DB-Auswahl ab:

```bash
target/app/bin/syrincs play rhythm info 3 5 3 --deviation-min 0 --seed 42 --dry-run > /tmp/syrincs-seed-42.txt
target/app/bin/syrincs play rhythm info 3 5 3 --deviation-min 0 --seed 43 --dry-run > /tmp/syrincs-seed-43.txt
cat /tmp/syrincs-seed-42.txt
cat /tmp/syrincs-seed-43.txt
bash doc/rhythm-generator/hoertour/onsets-zu-rdl.sh /tmp/syrincs-seed-42.rdl 4 --from-output /tmp/syrincs-seed-42.txt
bash doc/rhythm-generator/hoertour/onsets-zu-rdl.sh /tmp/syrincs-seed-43.rdl 4 --from-output /tmp/syrincs-seed-43.txt
target/app/bin/syrincs play rhythm --in /tmp/syrincs-seed-42.rdl --device Gervill --soundbank /usr/share/sounds/sf2/FluidR3_GM.sf2
target/app/bin/syrincs play rhythm --in /tmp/syrincs-seed-43.rdl --device Gervill --soundbank /usr/share/sounds/sf2/FluidR3_GM.sf2
```

Jede dreitaktige Folge wird viermal gespielt, insgesamt zwölf Takte.
Die angefragte Informationsfolge bleibt `3 → 5 → 3`, die gewählten
Onset-Patterns unterscheiden sich bei der hier geprüften Auswahl.
Höre, wie unterschiedlich derselbe Verlauf der Gesamtinformation klingen kann.
Bewahre eine RDL-Datei außerhalb von `/tmp` auf, wenn du diese konkrete
Folge dauerhaft behalten möchtest. Sie bleibt ein Klangbeispiel aus den
festgehaltenen Onsets; ein später geändertes Informationsmaß kann sie neu bewerten.

## 8. Mit Einzählen gemeinsam in den Rhythmus starten

Höre zunächst den bekannten A/B-Wechsel mit einem Takt Einzählen:

```bash
target/app/bin/syrincs play rhythm --in doc/rhythm-generator/hoertour/vergleich-a-b.rdl --device Gervill --count-in --soundbank /usr/share/sounds/sf2/FluidR3_GM.sf2
```

Du hörst **vier kurze Einzählschläge** bei 120 BPM. Syrincs sendet dafür
General-MIDI-Claves; die tatsächliche Klangfarbe hängt von deiner Soundbank
ab, wie unten beschrieben. Die erste Zählzeit ist etwas lauter.
Zähle „eins, zwei, drei, vier“ mit und führe
den Puls mit dem Fuß weiter. Auf der nächsten Eins beginnt A, danach
wechseln A und B wie bisher acht Takte lang. Vor B oder vor einer
Wiederholung wird nicht erneut eingezählt.

### Unseren A/B-Vergleich bei 108 BPM hören

Spiele dieselbe Datei direkt mit Tempoüberschreibung und Einzählen:

```bash
target/app/bin/syrincs play rhythm --in doc/rhythm-generator/hoertour/vergleich-a-b.rdl --tempo 108 --device Gervill --count-in --soundbank /usr/share/sounds/sf2/FluidR3_GM.sf2
```

Einzählen und Rhythmus laufen jetzt beide bei **108 BPM**. Die Vorlage
bleibt unverändert bei 120 BPM; `--tempo 108` überschreibt ihren Header nur
für diesen Aufruf. Ohne `--tempo` hörst du wieder das Tempo aus der Datei.

Vergleiche denselben Aufruf mit `--tempo 120`: Einsätze und Klangfarben
bleiben gleich, bei 108 BPM liegt mehr Zeit zwischen den Einsätzen.
Ein 4/4-Takt dauert bei 120 BPM zwei Sekunden, bei 108 BPM etwa 2,22 Sekunden.
Auch die Notenlängen folgen dem Tempo, weil ihr Gate-Anteil am Raster gleich bleibt.

**Höraufgabe:** Bleibe nach den vier Klicks im Puls und erkenne den Wechsel
von A zu B. Fällt dir der Einstieg mit Einzählen leichter? Wiederhole
denselben Playback-Befehl ohne `--count-in`, um den direkten Start zu
vergleichen.

### Auch die Katalogauswahl einzählen lassen

Du kannst dieselbe Option mit Seed und Vorschau kombinieren:

```bash
target/app/bin/syrincs play rhythm info 3 5 3 --deviation-min 0 --seed 42 --tempo 108 --count-in --dry-run
target/app/bin/syrincs play rhythm info 3 5 3 --deviation-min 0 --seed 42 --tempo 108 --count-in --device Gervill --soundbank /usr/share/sounds/sf2/FluidR3_GM.sf2
```

Die Vorschau zeigt das effektive Tempo und das geplante Einzählen:

```text
[PLAYBACK] TempoBpm=108
[COUNT-IN] Bars=1 | Beats=4 | Time=4/4
```

`--dry-run` bleibt vollständig still. Beim zweiten Befehl erklingt ein
Einzähltakt vor der gesamten dreitaktigen Auswahl bei 108 BPM.
Bei unverändertem Katalog bleiben die mit Seed 42 gewählten Patterns
dieselben wie ohne Einzählen und bei einem anderen Playback-Tempo. Vergleiche
denselben Aufruf mit `--tempo 120`. Ohne Tempooption gilt für Katalogrhythmen
weiterhin 120 BPM.

Du kannst `--tempo` und `--count-in` vor oder nach `info` setzen. Ein Tempo
nach `info` hat Vorrang vor einer Angabe davor. Einzählen ist aktiviert,
wenn die Flag an einer der beiden Stellen gesetzt ist. Der RDL-Export aus
Abschnitt 7 bleibt hilfreich, um konkrete Suchtreffer oder eine Auswahl als
Datei festzuhalten; für die reine Tempoänderung genügt `--tempo`.

Das Einzählen unterstützt zunächst **4/4** und verwendet denselben
MIDI-Ausgang wie der Rhythmus. Sein hoher Klang heißt in General MIDI
„Claves“; die konkrete Klangfarbe hängt vom Klangerzeuger ab.
Auf diesem Rechner verwendet Gervill standardmäßig das „Emergency GM sound
set“, das diese Note als Side Stick wiedergibt. Beim erfolgreichen
Claves-Hörvergleich wurde die installierte FluidR3-GM-Soundbank gezielt
mit einem temporären Java-Helfer geladen. Diesen Schritt übernimmt jetzt die
reguläre Option `--soundbank`: Die Hörbefehle oben wählen FluidR3 GM
explizit. `--device Gervill` allein wählt weiterhin die Standardbank.
Die Soundbank bestimmt auch die Kick-/Snare-Klangfarben; sie gilt für die
ganze Wiedergabe, nicht nur für die Einzählschläge.
Für unsere Präsentationswiedergabe ist der MIDI-Hall-Send jetzt auf
**16 von 127** gesetzt, statt Gervills bisherigem Default 40. Das gilt
für Rhythmus und Einzählen. Die Soundbank kann zusätzlich eigene
Hallanteile und Ausklingzeiten mitbringen.
Einzählen verändert weder Onsets noch Beat-Profil, Information oder
Deviation. Auch Anfangspausen bleiben erhalten: Der Rhythmustakt beginnt
direkt nach dem Einzähltakt, sein erster Einsatz kann aber später liegen.

## Welche Kombinationen heute möglich sind

Profil- und Peak-Filter gehören aktuell zu **`search rhythms`**.
`play rhythm info` unterstützt Informationsgrade, Deviation-Grenzen,
Seed, Vorschau, Tempoüberschreibung und Einzählen, aber keine Profil- oder
Peak-Optionen. Eine vorherige Suche
setzt deshalb keinen Filter für einen späteren Playback-Aufruf.

Der Hörweg über die gespeicherte Suchausgabe schließt diese Lücke für die
Präsentation: Er exportiert genau die passenden, angezeigten Treffer in eine
RDL-Datei. `--from-output` ist eine Option des lokalen Exporthelfers,
keine Option von `syrincs`. Eine direkte Syrincs-Option `play rhythm --onsets`
ist weiterhin eine mögliche spätere Erweiterung.

Die Analyse kann mehrere Takte anzeigen; Profil- und Peak-Suche beziehen
sich dagegen auf einen einzelnen 4/4-Takt. Suche nach ähnlichen Profilen,
Profilformen oder mehrtaktigen Informationsverläufen ist noch offen.

Information und Deviation werden in der DB anhand gespeicherter Aggregate
gefiltert. Die angezeigten Werte und Profilfilter werden aus den Onsets neu
berechnet. Bei älteren Katalogdaten können diese Bewertungen auseinanderfallen.
Ein leeres Ergebnis kann daher auch mit dem Katalogstand zusammenhängen.

## Wenn ein Beispiel nicht wie erwartet funktioniert

| Beobachtung | Was du prüfen kannst |
| --- | --- |
| Eine neue Option ist unbekannt. | Paket neu bauen und `target/app/bin/syrincs` verwenden. |
| Verbindung zur Datenbank scheitert. | `start db` ausführen und DB-Konfiguration sowie PostgreSQL-Dienst prüfen. |
| Die Suche liefert keine Treffer. | Zuerst nur nach `--info` suchen, danach Filter einzeln ergänzen; prüfen, ob Rhythmusdaten vorhanden sind. |
| Playback findet einen Grad nicht. | Mit `search rhythms --info N` prüfen; der Playback-Default `> 0.7` kann Kandidaten ausschließen. |
| Die Auswahl ist sichtbar, aber kein Ton kommt. | MIDI-Gerät, Verbindung und Klangerzeuger prüfen; `--dry-run` spielt absichtlich nichts ab. |
| Einzählen meldet eine unpassende Taktart. | `--count-in` unterstützt zunächst nur 4/4, auch in der Vorschau. |
| Nach dem vierten Klick ist zunächst kein Rhythmusschlag hörbar. | Die nächste Eins kommt einen Viertelschlag nach dem vierten Klick. Eine Anfangspause im Pattern bleibt danach ebenfalls erhalten. |
| Profil oder Peak wird als ungültig gemeldet. | Vier ganzzahlige Werte, passende Summe und Peak zwischen 1 und 4 prüfen; der Peak muss im expliziten Profil maximal sein. |
| Der Exporthelfer findet keine Rhythmen. | Gespeicherte Ausgabe mit `cat` ansehen: Bei null Treffern gibt es nichts zu exportieren. Eine alte RDL-Datei wird bei diesem Fehler nicht ersetzt; spiele sie dann nicht als neues Ergebnis ab. |
| Der Hörhelfer meldet fehlende Klassen oder Java-Fehler. | JDK 21 verwenden und zuerst den Paketbau ausführen; Hörbefehle im Repository-Verzeichnis starten. |

Die aktuelle Optionshilfe erreichst du direkt:

```bash
target/app/bin/syrincs analyze rhythm --help
target/app/bin/syrincs search rhythms --help
target/app/bin/syrincs play rhythm info --help
```

Weitere Details findest du in der [Rhythmusmodul-Dokumentation](README.md)
und der [Projekt-README](../../README.md).

### Prüfung dieser Anleitung

Paketbau ohne Completion-Installation, tatsächliche CLI-Hilfe,
Analysebeispiele sowie die gezeigten Profil-, Peak- und Deviation-Suchen
wurden lokal geprüft. Der Dry-Run mit Seed 42 wurde zweimal ausgeführt und
lieferte identische Ausgabe; Seed 43 lieferte eine andere Auswahl. Die
DB-Suche war lesend. Katalogerzeugung und hörbares MIDI-Playback wurden für
die ursprüngliche Erstellung dieser Anleitung nicht ausgeführt.

Für die ursprüngliche Ergänzung zu VR-13 wurden Paketbau, CLI-Hilfe und die vollständige
Testsuite geprüft: 211 Tests erfolgreich, 14 umgebungsbedingt oder als
manuelle Tests übersprungen. Der A/B-Vergleich bei 108 BPM mit Einzählen
wurde über Gervill abgespielt. Die Timing-Tests prüfen unter anderem vier
Klicks, unveränderte Anfangspausen und Notenlängen sowie das einmalige
Einzählen vor mehreren Takten bei 108 und 120 BPM.

Für den gemeinsamen Stand von VR-13 und VR-14 wurden die gezielten Tempo-,
Einzähl-, CLI-, Completion- und Rhythmus-E2E-Tests erneut geprüft: 122 Tests
erfolgreich, keine übersprungen. Eine zusätzliche Prüfung ohne Audiohardware
bestätigt die Kombination aus Quelle 120 BPM, `--tempo 108` und Einzählen:
vier MIDI-Ein-/Ausschaltpaare für die GM-Claves-Note, unveränderte Anfangspausen und Gates, Hall-Send 16 und
korrekter Nachlauf. Ein neuer Hörtest wurde bei dieser ursprünglichen Rebase-Korrektur nicht ausgeführt.

Für die Soundbank-Korrektur wurden Paketbau und vollständige Testsuite erneut
geprüft: 248 Tests erfolgreich, 14 umgebungsbedingt oder als manuelle Tests
übersprungen. Zusätzliche Tests prüfen die CLI-Weitergabe bis zum MIDI-Adapter,
den Vorrang der Option nach `info`, den Dry-Run ohne Dateizugriff sowie
fehlende, ungültige, nicht unterstützte und nicht ladbare Soundbanks.
Rhythmus A bei 108 BPM mit Einzählen und ausdrücklich ausgewähltem FluidR3 GM
wurde über die reguläre CLI ohne Fehlermeldung abgespielt. Der zuvor gemeinsam
bestätigte Claves-Klang benötigt damit keinen temporären Java-Helfer mehr.

Die ergänzten RDL-Dateien werden mit dem Produktionsparser und Validator
geprüft. Der Export nutzt den vorhandenen Mapper und dessen Voice-Defaults;
er berechnet keine eigene Kick-/Snare-Heuristik. Die Zuordnung und MIDI-Sequenz
werden ohne Hardware gegen den bestehenden Playback-Use-Case verglichen.

# Informationssuche ohne Wiedergabe bereitstellen

Lokaler Entwurf 5; keine Vikunja-ID. Status: zur Abstimmung.
Quelle: S4, S3 in der [Schwächenanalyse](../../schwaechen-und-zielbild.md).
Projekt: [Rhythmisches Informationsmaß musikalisch nutzbar machen](../projekt.md).
Einordnung: Kern / hoch.
Abhängigkeiten: [Entwurf 3](entwurf-3.md), [Entwurf 4](entwurf-4.md).

## Ziel

Katalogkandidaten nach exakter Gesamtinformation und optionaler Deviation
auflisten und untersuchen können, ohne eine Zufallsauswahl zu treffen oder
Playback zu starten.

## Belegter Ausgangszustand

Der Repository-Port kann nach exakter Information lesen. Öffentlich
erreichbar ist dieser Weg aber nur über `play rhythm info`: Dort werden
Treffer sofort zufällig ausgewählt und anschließend als eine MIDI-Folge
gespielt. Es gibt keinen `search`-Befehl, keine Trefferzahl und keine stabile
Kandidatenreihenfolge. PostgreSQL kann dasselbe Onset-Pattern mehrfach
enthalten.

## Umfang

- Einen read-only Application-Use-Case und den CLI-Einstieg `search rhythms`
  mit `--info 3`, optionalem `--deviation-min MIN`,
  `--deviation-max MAX` und `--limit N` einführen. `--info` bleibt in diesem
  Task ein einzelner exakter, nichtnegativer Ganzzahlwert. Ohne `--limit`
  werden höchstens 20 Treffer ausgegeben; `--limit` muss positiv sein.
- Ohne Deviation-Option alle Deviations zulassen. Explizite Grenzen verwenden
  denselben inklusiven Vertrag wie Entwurf 4: Beide Grenzen sind optional,
  endlich und nichtnegativ; bei zwei Grenzen gilt `min <= max`.
- Als Kandidatenidentität normalisierte Onsets, Zähler und Nenner verwenden;
  doppelte DB-Zeilen desselben Inhalts vor Trefferzahl und Ausgabe
  zusammenfassen. Das repariert nicht den Katalog, verhindert aber, dass eine
  Suchliste identische musikalische Kandidaten mehrfach zeigt.
- Nach Filterung und Deduplizierung kanonisch nach Zähler, Nenner und Onsets
  sortieren. Erst danach `--limit` anwenden.
- Je ausgegebenem Kandidaten normalisierte Onsets, Beat-Profil,
  Gesamtinformation und Deviation zeigen; außerdem angewandte Filter,
  Gesamtzahl eindeutiger Treffer und Zahl der ausgegebenen Treffer nennen.

## Akzeptanzkriterien

- [ ] Die Suche öffnet kein MIDI-Gerät und startet kein Playback.
- [ ] Ohne Deviation-Parameter wird kein implizites Minimum angewandt.
- [ ] Zwei DB-Zeilen mit identischem normalisiertem Inhalt ergeben einen
  Kandidaten; inhaltlich verschiedene Patterns bleiben getrennt.
- [ ] Gleiche Kataloginhalte und Filter liefern unabhängig von der
  Repository-Reihenfolge dieselbe Reihenfolge. `--limit` ist positiv und
  begrenzt nur die Ausgabe, nicht Filterung oder gesamte Trefferzahl.
- [ ] Eine gültige Suche ohne Treffer liefert Trefferzahl null und Exit-Code
  0. Ungültige Eingaben und Repository-Fehler liefern einen Fehler und
  werden nicht als leere Treffermenge ausgegeben.
- [ ] Die beiden Patterns `xooo xoxo xooo xoxo` und
  `xoxo xooo xoxo xooo` können trotz gleicher Gesamtinformation 3 mit ihren
  verschiedenen Profilen und Deviations angezeigt werden.
- [ ] Root-Hilfe, Completion-Test und README dokumentieren den neuen
  Hauptbefehl und seine Optionen.

## Abgrenzung und offene Entscheidungen

Keine Informationsbereiche, Pagination, JSON-/Dateiexporte, zeitlichen
Profilfilter, Zufallsauswahl, Wiedergabe oder Presets. Keine Unique Constraint
und keine Bereinigung des PostgreSQL-Katalogs.

## Einstieg und Verifikation

[`RhythmRepository`](../../../../src/main/java/syrincs/b_application/ports/RhythmRepository.java),
[`PostgresRhythmRepository`](../../../../src/main/java/syrincs/c_adapters/postgres/PostgresRhythmRepository.java)
und [`RootCmd`](../../../../src/main/java/syrincs/c_adapters/cli/RootCmd.java).
Application- und CLI-Tests verwenden absichtlich unsortierte und duplizierte
Fixtures; der Repository-Adapter wird für die Filtergrenzen gezielt geprüft.

Bei öffentlichen CLI-Änderungen gehören Hilfe, Completion-Tests und die
passende Benutzerdokumentation zur Umsetzung. Änderungen bleiben in den
vorhandenen Schichten; Tests werden am geänderten Verhalten ergänzt.

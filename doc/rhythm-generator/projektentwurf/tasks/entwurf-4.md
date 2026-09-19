# Deviation als Parameter der Informationssuche anbieten

Lokaler Entwurf 4; keine Vikunja-ID. Status: zur Abstimmung.
Quelle: S3 in der [Schwächenanalyse](../../schwaechen-und-zielbild.md).
Projekt: [Rhythmisches Informationsmaß musikalisch nutzbar machen](../projekt.md).
Einordnung: Kern / hoch.
Abhängigkeiten: Keine.

## Ziel

Beim bestehenden `play rhythm info` die fest verdrahtete
Deviation-Untergrenze durch explizit wählbare Unter- und Obergrenzen
ergänzen, ohne das Verhalten alter Aufrufe unbemerkt zu ändern.

## Belegter Ausgangszustand

[`UseCaseInteractor`](../../../../src/main/java/syrincs/b_application/UseCaseInteractor.java)
ruft für jeden Informationsgrad
`getAllByInformationAndMinDeviation(info, 0.7)` auf. Der
[`PostgresRhythmRepository`](../../../../src/main/java/syrincs/c_adapters/postgres/PostgresRhythmRepository.java)
setzt dies als striktes `deviation > ?` um. Die CLI besitzt keine
Deviation-Option. Der vorhandene CLI-Fake prüft die Filterwerte derzeit
nicht, weil er unabhängig von den Argumenten immer einen Rhythmus liefert.

## Umfang

- Einen frameworkfreien Filtervertrag mit optionaler inklusiver Unter- und
  Obergrenze einführen und durch Application-Port und PostgreSQL-Adapter
  führen. Die Bedeutung der vorhandenen strikt-minimalen Repository-Methode
  darf nicht stillschweigend geändert werden.
- `play rhythm info 3 5 --deviation-min 0.2 --deviation-max 0.8`
  unterstützen. Sobald mindestens eine Grenze explizit gesetzt ist, ersetzt
  sie den historischen Filter vollständig.
- Kompatibilitätsvertrag: Ohne beide Optionen gilt beim bestehenden
  Playback-Befehl weiterhin strikt `deviation > 0.7`. Nur `--deviation-min`
  bedeutet `deviation >= min` ohne Maximum; nur `--deviation-max` bedeutet
  `deviation <= max` ohne verstecktes Minimum.

## Akzeptanzkriterien

- [ ] Treffer exakt auf einer expliziten Grenze sind enthalten; Min allein,
  Max allein und beide Grenzen zusammen werden geprüft.
- [ ] `--deviation-min 0` ermöglicht auch Deviation 0; keine versteckte 0,7-Schwelle bleibt aktiv.
- [ ] Negative Werte, `NaN`, unendliche Werte und Min > Max führen vor dem
  Repository-Aufruf zu verständlichen Eingabefehlern.
- [ ] Ein Aufruf ohne neue Optionen verwendet nachweislich den historischen
  strikten Filter `> 0.7`; Gleichheit mit 0,7 ist dabei kein Treffer.
- [ ] Application- und CLI-Tests verwenden einen Fake, der Grad und Grenzen
  tatsächlich auswertet. Die SQL-Prädikate für alle vier Fälle werden in
  einem Adaptertest gegen eine Testdatenbank oder gleichwertig gezielt
  verifiziert.

## Abgrenzung und offene Entscheidungen

Keine Beat-Profilfilter, keine neue Takt-Deviation, keine Änderung des
Informationsmaßes und keine Bereinigung oder Versionierung bestehender
Katalogdaten. Der Default einer erst in Entwurf 5 eingeführten reinen Suche
gehört nicht zu diesem Task.

## Einstieg und Verifikation

[`AppDefaults`](../../../../src/main/java/syrincs/b_application/AppDefaults.java),
`UseCaseInteractor`, `RhythmRepository`, `PostgresRhythmRepository` und
`RootCmdRhythmCliTest`. Die Validierung gehört in Application bzw. CLI, SQL
bleibt im Adapter.

Bei öffentlichen CLI-Änderungen gehören Hilfe, Completion-Tests und die
passende Benutzerdokumentation zur Umsetzung. Änderungen bleiben in den
vorhandenen Schichten; Tests werden am geänderten Verhalten ergänzt.

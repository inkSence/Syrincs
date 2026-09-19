# Beat-Informationswerte öffentlich zugänglich machen

Lokaler Entwurf 3; keine Vikunja-ID. Status: zur Abstimmung.
Quelle: S1 in der [Schwächenanalyse](../../schwaechen-und-zielbild.md).
Projekt: [Rhythmisches Informationsmaß musikalisch nutzbar machen](../projekt.md).
Einordnung: Kern / hoch.
Abhängigkeiten: Keine.

## Ziel

Die bereits intern berechneten Informationswerte je Zählzeit als
unveränderlichen Bestandteil der Rhythmusanalyse veröffentlichen und über
einen opt-in CLI-Detailmodus sichtbar machen.

## Belegter Ausgangszustand

[`HuffmanRhythm`](../../../../src/main/java/syrincs/a_domain/rhythm/HuffmanRhythm.java)
berechnet eine private `List<Integer>` der Beat-Werte, verwendet sie für
Gesamtinformation und Populationsstandardabweichung und verwirft sie danach.
Die Analyse-CLI bezeichnet unter `Beats` dagegen die Onset-Gruppen und zeigt
nicht deren Informationswerte. Eine JSON- oder sonstige strukturierte
CLI-Ausgabe gibt es derzeit nicht.

## Umfang

- Die geordneten Beat-Werte einmal berechnen und als unveränderliche Liste
  öffentlich zugänglich machen. Gesamtinformation, arithmetischer Mittelwert
  der Beat-Werte und vorhandene Deviation müssen aus derselben Liste stammen.
- Den heutigen Berechnungskontext beibehalten: Start im Ruhezustand, danach
  kontinuierliche Zustandsführung über Beat- und gegebenenfalls Taktgrenzen.
- `analyze rhythm ONSETS --details` als Textausgabe ergänzen. Sie zeigt
  normalisierte Onsets, Beat-Informationswerte, Gesamtinformation, Mittelwert
  und Deviation. Das neue Feld heißt eindeutig `BeatInformation`; die
  vorhandene Bezeichnung `Beats` für Onset-Gruppen darf nicht stillschweigend
  umgedeutet werden.

## Akzeptanzkriterien

- [ ] Für `xooo xoxo xooo xoxo` liefert die Domäne `[1,1,0,1]`, Summe 3,
  Mittelwert 0,75 und Deviation `0.4330127018922193`.
- [ ] Für `xoxo xooo xoxo xooo` liefert sie `[2,0,1,0]`, ebenfalls Summe 3,
  aber Deviation `0.82915619758885`; die beiden Verteilungen bleiben damit
  unterscheidbar.
- [ ] Alle Zählzeiten einer mehrtaktigen Eingabe werden flach in
  Eingabereihenfolge ausgegeben; dieser Task erfindet keine Taktinformation.
- [ ] Die veröffentlichte Liste ist nicht von Aufrufern veränderbar; Summe und Deviation stimmen mit ihren Werten überein.
- [ ] `analyze rhythm ONSETS` behält ohne `--details` die bisherige Ausgabe.
  Der Detailmodus ist in Hilfe, CLI-Test und Benutzerdokumentation enthalten.

## Abgrenzung und offene Entscheidungen

Keine Codesymbolfolgen, kein JSON-Format, keine Änderung der Zustandsmaschine,
keine Taktgruppierung und keine Speicherung der Beat-Werte in PostgreSQL.

## Einstieg und Verifikation

[`HuffmanRhythm`](../../../../src/main/java/syrincs/a_domain/rhythm/HuffmanRhythm.java),
[`AnalyseRhythmUseCase`](../../../../src/main/java/syrincs/b_application/AnalyseRhythmUseCase.java),
[`RootCmd`](../../../../src/main/java/syrincs/c_adapters/cli/RootCmd.java),
`HuffmanRhythmTest` und `RootCmdRhythmCliTest`.

Bei öffentlichen CLI-Änderungen gehören Hilfe, Completion-Tests und die
passende Benutzerdokumentation zur Umsetzung. Änderungen bleiben in den
vorhandenen Schichten; Tests werden am geänderten Verhalten ergänzt.

# Taktweise Analyse gemäß fachlicher Definition umsetzen

Lokaler Entwurf 2; keine Vikunja-ID. Status: zur Abstimmung.
Quelle: S2 in der [Schwächenanalyse](../../schwaechen-und-zielbild.md).
Projekt: [Rhythmisches Informationsmaß musikalisch nutzbar machen](../projekt.md).
Einordnung: Kern / hoch, nach Begriffsentscheidung.
Abhängigkeiten: [Entwurf 1](entwurf-1.md), [Entwurf 3](entwurf-3.md).

## Ziel

Die in Entwurf 1 abgestimmte Taktinformation für jeden Takt einer
mehrtaktigen Eingabe als unveränderliches Analyseergebnis berechnen und in
der Analyse-CLI sichtbar machen.

## Belegter Ausgangszustand

[`Rhythm`](../../../../src/main/java/syrincs/a_domain/rhythm/Rhythm.java)
akzeptiert bereits mehrere vollständige Takte, und `HuffmanRhythm` führt
seine Zustandsmaschine über alle Beats der Eingabe. Öffentlich verfügbar
sind jedoch nur die Aggregate der gesamten Eingabe. Es gibt kein Ergebnis
mit Taktposition, Takt-Onsets oder Beat-Profil je Takt.

## Umfang

- Den akzeptierten Vertrag aus Entwurf 1 ohne zusätzliche semantische
  Entscheidungen in der Domäne umsetzen.
- Je Takt mindestens einbasierte Taktposition, normalisierte Onsets und das
  Beat-Informationsprofil liefern. Taktinformation, Deviation und Kontext
  werden nur aufgenommen, soweit die Spezifikation sie definiert.
- Die Application-Analyse um dieses Ergebnis erweitern und einen ausdrücklich
  taktweisen CLI-Modus ergänzen. Optionsname und mögliche Kontextauswahl
  folgen der akzeptierten Spezifikation; `--per-bar` ist nur ein
  Syntaxvorschlag.
- Den bisherigen Aufruf `analyze rhythm ONSETS` und seine heutige
  Aggregatausgabe unverändert nutzbar halten.

## Akzeptanzkriterien

- [ ] Alle fachlich abgestimmten Beispiele aus Entwurf 1 sind als Domänentests umgesetzt.
- [ ] Bei einer gültigen Eingabe mit `n` vollständigen 4/4-Takten enthält das
  Ergebnis genau `n` geordnete Taktergebnisse mit den jeweils richtigen
  16 Onset-Positionen und vier Beat-Werten.
- [ ] Beziehungen zwischen Takt- und Gesamtwerten entsprechen der gewählten
  Definition; Deviations werden weder addiert noch gemittelt, sofern der
  Vertrag dies nicht ausdrücklich definiert.
- [ ] Der taktweise CLI-Modus benennt die verwendete Berechnungsweise und
  gibt jeden Takt genau einmal aus. Ohne die neue Option bleibt die bisherige
  Ausgabe kompatibel.
- [ ] Das veröffentlichte Taktergebnis und seine enthaltenen Listen können
  von Aufrufern nicht verändert werden.
- [ ] Domain-, Application- und CLI-Tests decken mindestens zwei Takte und
  einen stillen Takt ab; die CLI-Tests benötigen weder DB noch MIDI-Gerät.

## Abgrenzung und offene Entscheidungen

Kein Vorgriff auf Entwurf 1, keine Taktverlaufsuche, keine Datenbank- oder
Katalogmigration und keine Verallgemeinerung über das heutige 4/4-Modell mit
vier Positionen je Beat hinaus. Verlangt die akzeptierte Definition eine
Änderung des bestehenden Informationsmaßes, muss dieser Task vor der
Umsetzung neu geschnitten werden.

## Einstieg und Verifikation

[`HuffmanRhythm`](../../../../src/main/java/syrincs/a_domain/rhythm/HuffmanRhythm.java),
[`AnalyseRhythmUseCase`](../../../../src/main/java/syrincs/b_application/AnalyseRhythmUseCase.java)
und [`RootCmd`](../../../../src/main/java/syrincs/c_adapters/cli/RootCmd.java).
Zusätzlich zu den Domänentests sind die vorhandenen Rhythmus-CLI-Tests zu
erweitern.

Bei öffentlichen CLI-Änderungen gehören Hilfe, Completion-Tests und die
passende Benutzerdokumentation zur Umsetzung. Änderungen bleiben in den
vorhandenen Schichten; Tests werden am geänderten Verhalten ergänzt.

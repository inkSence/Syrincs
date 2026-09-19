# Den Taktinformationsbegriff fachlich bestimmen

Lokaler Entwurf 1; keine Vikunja-ID. Status: zur Abstimmung.
Quelle: S2 in der [Schwächenanalyse](../../schwaechen-und-zielbild.md).
Projekt: [Rhythmisches Informationsmaß musikalisch nutzbar machen](../projekt.md).
Einordnung: Begriffsarbeit / hoch.
Abhängigkeiten: Keine.

## Ziel

Eine abgestimmte fachliche Spezifikation bestimmt eindeutig, was
„Information eines Takts“ bedeutet. Sie ist der verbindliche Rechenvertrag
für taktweise Analyse und spätere Taktverlaufsuche.

## Belegter Ausgangszustand

[`HuffmanRhythm`](../../../../src/main/java/syrincs/a_domain/rhythm/HuffmanRhythm.java)
startet eine Analyse im Ruhezustand, führt den Playing-Zustand über
Beatgrenzen fort und veröffentlicht nur Gesamtinformation und Deviation.
Bei einer mehrtaktigen Eingabe gibt es daher weder ein Taktergebnis noch
eine öffentliche Entscheidung, ob ein Takt isoliert oder in seinem
Phrasenkontext zu bewerten ist. Das zweimal wiederholte Pattern
`xooo xooo xooo xooo` liefert isoliert die Taktsummen `[1,1]`, in der
durchgehenden Analyse dagegen die nach Takten gruppierten Summen `[1,0]`.

## Umfang

- Isolierte Bewertung, Gruppierung einer durchgehenden Analyse und begründete
  weitere Kandidaten vergleichen, ohne zwei öffentliche Modi vorauszusetzen.
- Für die gewählte Definition Startzustand, Zustandsübergang an der
  Taktgrenze, Aggregation der Beat-Werte und den Bezug zur
  Phraseninformation festlegen.
- Festlegen, ob eine Deviation je Takt Teil des Ergebnisses ist und, falls
  ja, über welche Werte sie berechnet wird. Sie darf nicht als Mittelwert
  vorhandener Takt-Deviations definiert werden.
- Die Entscheidung samt Rechenbeispielen, verworfenen Alternativen und
  bekannten Grenzen in `doc/rhythm-generator/taktinformationsbegriff.md`
  dokumentieren und fachlich abstimmen.

## Akzeptanzkriterien

- [ ] Die Spezifikation enthält mindestens vollständige Rechenbeispiele für
  zwei gleiche Takte, zwei verschiedene aufeinanderfolgende Takte und einen
  stillen Takt; Onsets, Beat-Werte und Taktwerte sind angegeben.
- [ ] Die Spezifikation unterscheidet eine mathematische Gruppierung heutiger
  Beat-Werte von einer fachlichen Definition der Taktinformation.
- [ ] Es ist entschieden, ob genau eine Definition oder mehrere ausdrücklich
  benannte Modi öffentlich benötigt werden. Kontext, Startzustand und
  Bedeutung jedes Ergebnisfelds sind eindeutig. Bei mehreren Modi ist
  festgelegt, welcher davon für die Taktverlaufsuche maßgeblich ist.
- [ ] Gesamtinformation, Taktinformation und gegebenenfalls Deviation je
  Takt sind begrifflich getrennt; ihre zulässigen Beziehungen sind benannt.
- [ ] Die Spezifikation ist fachlich akzeptiert und enthält erwartete Werte,
  die unverändert als Tests für Entwurf 2 übernommen werden können.

## Abgrenzung und offene Entscheidungen

Keine Implementierung, keine CLI-Festlegung und keine vorausgesetzte Pflicht,
isolierte und kontextuelle Bewertung beide anzubieten. Eine noch offene
Kernentscheidung erfüllt diesen Task nicht; in diesem Fall bleibt Entwurf 2
blockiert.

## Einstieg und Verifikation

Abschnitt S2 der Schwächenanalyse,
[`HuffmanRhythmTest`](../../../../src/test/java/syrincs/a_domain/rhythm/HuffmanRhythmTest.java)
und die private Beat-Berechnung in `HuffmanRhythm`. Die Beispielwerte sind
mit dem bestehenden Maß nachzurechnen; Produktionscode wird nicht geändert.

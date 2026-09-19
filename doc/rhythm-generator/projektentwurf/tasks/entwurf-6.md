# Zufallsauswahl reproduzierbar und sichtbar machen

Lokaler Entwurf 6; keine Vikunja-ID. Status: zur Abstimmung.
Quelle: S4 in der [Schwächenanalyse](../../schwaechen-und-zielbild.md).
Projekt: [Rhythmisches Informationsmaß musikalisch nutzbar machen](../projekt.md).
Einordnung: Kern / hoch.
Abhängigkeiten: [Entwurf 5](entwurf-5.md).

## Ziel

Die vom bestehenden `play rhythm info` gewählten Pattern vor dem Playback
sichtbar machen und die Auswahl mit einem Seed innerhalb eines klar
begrenzten Vertrags wiederholbar machen.

## Belegter Ausgangszustand

[`UseCaseInteractor`](../../../../src/main/java/syrincs/b_application/UseCaseInteractor.java)
fragt Kandidaten je Informationsgrad ab, wählt unmittelbar mit
`ThreadLocalRandom` und übergibt nur die ausgewählten Domänenobjekte an die
Wiedergabe. Gewählte Onsets und Zufallszustand werden nicht ausgegeben.
Fehlt ein einzelner Grad, wird er still übersprungen; nur wenn überhaupt kein
Grad einen Treffer hat, schlägt der Aufruf fehl.

## Umfang

- Einen eigenständig testbaren Application-Auswahlschritt zwischen Suche und
  Playback einführen. Kandidaten mit gleichen normalisierten Onsets, Zähler
  und Nenner gelten als identisch; eindeutige Kandidaten werden vor der
  Auswahl kanonisch nach Zähler, Nenner und Onsets sortiert.
- `play rhythm info ... --seed LONG` ergänzen. Der Vertrag verwendet einen
  dokumentierten Pseudozufallsalgorithmus, initialisiert ihn einmal pro
  Aufruf und zieht genau einmal für jede angeforderte Position aus deren
  vollständiger Kandidatenliste.
- Informationsgrad und optionale Deviation-Grenzen bestimmen die
  Kandidatenmenge jeder Position. Das Ausgabe-Limit der reinen Suche darf die
  Auswahl nicht verkleinern.
- Wiederholte Informationsgrade werden als getrennte Positionen behandelt;
  dasselbe Pattern darf an mehreren Positionen gewählt werden. Doppelte
  DB-Zeilen erhöhen seine Wahrscheinlichkeit nicht.
- Vor jedem Playback die geordnete Auswahl mit Onsets, Beat-Profil,
  Information und Deviation ausgeben. `--dry-run` führt dieselbe Auswahl und
  Ausgabe durch, ruft aber keinen Playback-Port auf.
- Fehlt für eine angeforderte Position ein Kandidat, alle fehlenden
  Positionen benennen und vor dem Playback fehlschlagen. Damit wird die
  bisherige stille Verkürzung bewusst beendet.

## Akzeptanzkriterien

- [ ] Bei unverändertem normalisiertem Kandidateninhalt, gleicher
  Programmversion, gleicher Kriterienfolge und gleichem Seed entsteht exakt
  dieselbe geordnete Auswahl; die ursprüngliche DB-Reihenfolge ist dafür
  unerheblich.
- [ ] Tests legen Zufallsalgorithmus, Initialisierung, Ziehreihenfolge und
  Verhalten wiederholter Informationsgrade durch konkrete Erwartungswerte
  fest.
- [ ] Die Ausgabe enthält exakt und in derselben Reihenfolge die Inhalte, die
  anschließend einmal gemeinsam an den Playback-Port übergeben werden.
- [ ] `--dry-run` öffnet kein MIDI-Gerät. Ein fehlender Grad führt auch ohne
  MIDI-Aufruf zu einem verständlichen Fehler mit Positions- und Gradangabe.
- [ ] Ohne `--seed` bleibt eine nichtdeterministische Auswahl zulässig, wird
  aber ebenfalls sichtbar ausgegeben. Hilfe und README erklären, dass ein
  Seed nach Änderung von Kandidateninhalt oder Algorithmus keine dauerhafte
  Pattern-Adresse ist.

## Abgrenzung und offene Entscheidungen

Keine Auswahldatei, keine Persistenz von Seed oder Auswahl, kein direkter
Onset-Playback-Befehl und keine Presets. Der Task macht keine Zusage über
Reproduzierbarkeit über Katalog- oder Programmänderungen hinweg.

## Einstieg und Verifikation

`UseCaseInteractor`, der Such-Use-Case aus Entwurf 5,
[`PlayHuffmanRhythmsUseCase`](../../../../src/main/java/syrincs/b_application/PlayHuffmanRhythmsUseCase.java)
und `RootCmdRhythmCliTest`. Tests verwenden kontrollierte Kandidaten und
einen aufrufzählenden Playback-Fake; reale MIDI-Hardware ist nicht beteiligt.

Bei öffentlichen CLI-Änderungen gehören Hilfe, Completion-Tests und die
passende Benutzerdokumentation zur Umsetzung. Änderungen bleiben in den
vorhandenen Schichten; Tests werden am geänderten Verhalten ergänzt.

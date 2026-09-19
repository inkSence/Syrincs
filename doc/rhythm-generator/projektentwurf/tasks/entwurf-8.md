# Nach mehrtaktigen Informationsverläufen suchen

Lokaler Entwurf 8; keine Vikunja-ID. Status: zur Abstimmung.
Quelle: S5, S2 in der [Schwächenanalyse](../../schwaechen-und-zielbild.md).
Projekt: [Rhythmisches Informationsmaß musikalisch nutzbar machen](../projekt.md).
Einordnung: Ausbau / nach Fachentscheidung.
Abhängigkeiten: [Entwurf 2](entwurf-2.md), [Entwurf 5](entwurf-5.md).

## Ziel

Eine endliche Folge gewünschter Taktinformationen als Suchziel ausdrücken
und passende Folgen vorhandener Katalogtakte nach genau derselben
Taktdefinition finden.

## Belegter Ausgangszustand

`play rhythm info 3 5` sucht jeden Informationsgrad unabhängig anhand des in
PostgreSQL gespeicherten Werts eines einzelnen 16-Schritt-Takts, wählt je
einen Treffer und analysiert die zusammengesetzte Phrase nicht. Das ist nur
dann eine korrekte Taktverlaufsuche, wenn Entwurf 1 eine isolierte Semantik
festlegt. Bei einer kontextabhängigen Semantik kann ein Vorfilter auf den
isoliert gespeicherten Informationswert gültige Folgen ausschließen. Der
Repository-Port besitzt derzeit außerdem keine allgemeine Abfrage für den
gesamten Rhythmuskatalog.

## Umfang

- Den CLI-Entwurf `search rhythms --info-per-bar 3,5,3,7
  --max-combinations M [--limit N]` gemäß dem in Entwurf 2 umgesetzten
  Kontextvertrag konkretisieren. Die Zielliste enthält mindestens einen
  nichtnegativen ganzzahligen Wert. `--max-combinations` ist erforderlich
  und positiv; für die Ausgabe gilt derselbe positive Limit-Vertrag mit
  Default 20 wie bei der einfachen Suche.
- `--info-per-bar` und das skalare `--info` sind gegenseitig ausgeschlossen.
  In diesem ersten Schnitt ist die Verlaufsuche auch nicht mit Deviation-,
  Beat-Profil- oder Peak-Filtern kombinierbar; solche Kombinationen benötigen
  einen eigenen fachlichen Vertrag und führen hier zu einem Eingabefehler.
- Nur bereits katalogisierte vollständige 4/4-Takte kombinieren; keine
  Binärstrings der gesamten Phrase erzeugen. Inhaltlich gleiche Katalogzeilen
  anhand von Onsets, Zähler und Nenner zusammenfassen und Kandidaten innerhalb
  jeder Suchstufe nach Zähler, Nenner und Onsets ordnen.
- Bei isolierter Taktsemantik dürfen Kandidaten anhand ihrer isolierten
  Information vorgefiltert werden. Bei kontextabhängiger Semantik jeden
  Kandidaten mit dem erforderlichen Übergangszustand bewerten und den
  Repository-Port so erweitern, dass kein unsicherer Filter auf gespeicherte
  isolierte Information gültige Kandidaten verwirft.
- Ergebnisse deterministisch als vollständige Folgen ausgeben: pro Takt
  Position, Onsets, Beat-Profil und Taktinformation sowie die Analyse der
  gesamten zusammengesetzten Phrase. Erst nach vollständiger Prüfung
  `--limit` anwenden.
- `--max-combinations` begrenzt die Zahl bewerteter Erweiterungen eines
  Suchpräfixes um genau einen Kandidatentakt. „Keine Lösung im vollständig
  untersuchten Kandidatenraum“ und „Suchbudget erschöpft“ sind verschiedene
  Ergebnisse.

## Akzeptanzkriterien

- [ ] Für jede ausgegebene Folge wird der Verlauf durch erneute Analyse der
  zusammengesetzten Onsets bestätigt. Eine nur isoliert passende, im
  beschlossenen Kontext aber falsche Folge ist kein Treffer.
- [ ] Ist bereits die Kandidatenmenge einer isoliert vorfilterbaren Position
  leer, werden ihre einbasierte Nummer und ihr Zielwert benannt. Sind nur die
  Kombinationen unvereinbar, wird stattdessen eindeutig „keine vollständige
  Folge“ gemeldet; es entsteht nie eine verkürzte Phrase.
- [ ] Gleiche Kataloginhalte, Ziele und Suchgrenzen liefern unabhängig von
  der DB-Reihenfolge dieselben geordneten Ergebnisse; Zufall ist nicht Teil
  dieses Tasks.
- [ ] Tests vergleichen kleine künstliche Kataloge mit einer vollständigen
  Referenzsuche und decken mindestens Treffer, unerfüllbares Ziel und
  erschöpftes Suchbudget ab. Bei kontextabhängiger Taktinformation kommt ein
  Übergangsfall hinzu; bei isolierter Definition wird stattdessen die
  Unabhängigkeit aufeinanderfolgender Takte geprüft.
- [ ] `--limit` begrenzt nur ausgegebene Treffer. Ein erreichtes
  `--max-combinations` wird nie als Beweis ausgegeben, dass keine Lösung
  existiert.
- [ ] Suche und Tests öffnen kein MIDI-Gerät und starten kein Playback.
- [ ] Unzulässige Kombinationen mit skalaren Suchoptionen werden vor einem
  Repository-Aufruf verständlich zurückgewiesen.

## Abgrenzung und offene Entscheidungen

Keine neue Definition von Taktinformation, keine Suche über beliebig
generierte mehrtaktige Binärstrings, keine Zufallsauswahl, kein Playback,
keine Presets, kein Arrangement-Editor und keine automatische Variation.
Wenn Entwurf 2 keinen eindeutigen maschinenlesbaren Taktvertrag bereitstellt,
ist dieser Task nicht startbereit und darf die Semantik nicht selbst erfinden.

## Einstieg und Verifikation

Taktvertrag und Analyseergebnis aus Entwurf 2, Such-Use-Case und kanonische
Kandidatenidentität aus Entwurf 5 sowie `RhythmRepository`. Fachliche
Beispielphrasen werden als Application- und CLI-Tests mit kleinen In-Memory-
Katalogen ausgeführt; ein Adaptertest deckt eine gegebenenfalls benötigte
neue Repository-Abfrage ab.

Bei öffentlichen CLI-Änderungen gehören Hilfe, Completion-Tests und die
passende Benutzerdokumentation zur Umsetzung. Änderungen bleiben in den
vorhandenen Schichten; Tests werden am geänderten Verhalten ergänzt.

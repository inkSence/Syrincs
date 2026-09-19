# Nach Beat-Profilen und Informationsmaxima suchen

Lokaler Entwurf 7; keine Vikunja-ID. Status: zur Abstimmung.
Quelle: S5 in der [Schwächenanalyse](../../schwaechen-und-zielbild.md).
Projekt: [Rhythmisches Informationsmaß musikalisch nutzbar machen](../projekt.md).
Einordnung: Ausbau / hoch.
Abhängigkeiten: [Entwurf 5](entwurf-5.md).

## Ziel

Die Suche aus Entwurf 5 um zwei unmittelbar aus dem Beat-Profil ableitbare
Kriterien erweitern: ein exaktes Profil und die Zählzeit, an der ein
Informationsmaximum liegen soll.

## Belegter Ausgangszustand

Die bestehende Repository-Suche kennt nur die skalare Gesamtinformation und
eine Deviation-Untergrenze. Die Patterns `xooo xoxo xooo xoxo` mit Profil
`[1,1,0,1]` und `xoxo xooo xoxo xooo` mit Profil `[2,0,1,0]` haben beide
Gesamtinformation 3, sind damit aber nicht gezielt unterscheidbar. Entwurf 3
macht die für den Filter benötigten Beat-Werte erst öffentlich verfügbar.

## Umfang

- Für die reine Suche die optionale Angabe
  `--beat-profile 1,1,0,1` unterstützen. Sie verlangt im heutigen 4/4-Modell
  exakt vier nichtnegative Ganzzahlen in zeitlicher Reihenfolge.
- Optional `--peak-beat N` unterstützen. Die CLI zählt Beats ab 1; ein
  Kandidat trifft, wenn Beat `N` einen globalen Maximalwert seines Profils
  trägt. Bei einem Gleichstand dürfen deshalb mehrere Positionen Peak sein.
- `--info`, Deviation-Grenzen, exaktes Profil und Peak-Bedingung werden mit
  UND verknüpft. Ein exaktes Profil, dessen Summe nicht `--info` entspricht,
  sowie eine Peak-Bedingung, die dem expliziten Profil widerspricht, sind
  Eingabefehler.
- Profilfilter nach der skalaren Repository-Abfrage, aber vor
  Deduplizierung, Sortierung, Trefferzählung und `--limit` des Such-Use-Cases
  anwenden.

## Akzeptanzkriterien

- [ ] Gleiche Summe bei verschiedenen Profilen führt zu unterscheidbaren Treffermengen.
- [ ] Für `[1,1,0,1]` treffen `--peak-beat 1`, `2` und `4`, aber nicht `3`;
  für `[2,0,1,0]` trifft nur `--peak-beat 1`. Beim Nullprofil
  `[0,0,0,0]` gelten wegen des Gleichstands alle vier Beats als Maximum.
- [ ] Leere Profile, andere Länge als vier, negative oder nichtnumerische
  Werte, Beatpositionen außerhalb 1 bis 4 und widersprüchliche Filter liefern
  vor der Repository-Abfrage verständliche Fehler.
- [ ] Ein gültiges, aber im Katalog nicht vorhandenes Profil liefert
  Trefferzahl null und Erfolg.
- [ ] Die Suche filtert das vorhandene Informationsmaß und ersetzt es weder durch Dichte noch durch metrische Gewichtung.
- [ ] Deviation-Filter bleiben kombinierbar und werden nicht dupliziert;
  Profil- und Peak-Filter wirken nachweislich vor Trefferzahl und Limit.

## Abgrenzung und offene Entscheidungen

Keine Profilformen wie konstant, steigend, fallend, alternierend oder Gipfel;
sie bleiben spätere Erweiterungen. Keine Profilähnlichkeit, Taktverlaufsfilter,
Metrikgewichte oder Onset-Dichtefilter. Die erste Umsetzung bleibt bewusst auf
einen 4/4-Takt mit vier Beat-Werten begrenzt.

## Einstieg und Verifikation

Analyseergebnis aus Entwurf 3 und Such-Use-Case aus Entwurf 5. Die
Prädikate werden frameworkfrei getestet; CLI-Tests belegen Parsing,
einbasierte Beatnummern, UND-Verknüpfung und Filterreihenfolge.

Bei öffentlichen CLI-Änderungen gehören Hilfe, Completion-Tests und die
passende Benutzerdokumentation zur Umsetzung. Änderungen bleiben in den
vorhandenen Schichten; Tests werden am geänderten Verhalten ergänzt.

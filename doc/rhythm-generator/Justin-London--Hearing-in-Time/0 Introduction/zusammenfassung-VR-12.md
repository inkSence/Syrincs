# Introduction: Was ist Metrum, und wozu dient es?

Ergänzende Auswertung für Syrincs und [VR-12](http://localhost:3456/tasks/20),
10. Oktober 2026. Gelesen: IMG_1631–IMG_1649, 19 Screenshots.
[Bezugsrahmen und Codeverträge](../README.md).

## Zusammenfassung

London beginnt mit der funktionalen Frage, wozu Metrum dient. Für
Ausführende organisiert es zeitliche Handlungen; für Hörende richtet es
Wahrnehmung auf erwartete Ereignisse aus. Rhythmus bezeichnet dabei die
zeitliche Struktur des musikalischen Geschehens, Metrum die Weise seiner
Wahrnehmung und Organisation. Die notierte Taktart ist deshalb keine
vollständige Beschreibung des tatsächlichen Hörens.
[Unterscheidung](IMG_1634.PNG), [Metrum als Verhalten](IMG_1635.PNG).

Für die rhythmischen Abstände sind zunächst die Intervalle zwischen
Einsatzzeitpunkten, die IOIs, maßgeblich. Sie sind von der tatsächlichen
Schalldauer zu unterscheiden: Eine Figur kann mit denselben IOIs legato
oder staccato gespielt werden. Das entspricht unserer Trennung zwischen
Onset-Analyse und Playback-Artikulation; ob die Schalldauer das Hören
beeinflusst, ist eine zusätzliche empirische Frage.
[IOI und Schalldauer](IMG_1634.PNG).

Metrisches Hören verbindet allgemeine Fähigkeiten zeitlicher Abstimmung
mit eingeübten musikalischen Gewohnheiten. Es entsteht nicht jedes Mal
ohne Vorgeschichte. Wiederkehrende Muster, Erfahrung mit Stilen und
Vertrautheit mit bestimmten Aufführungsweisen beeinflussen, was erwartet
wird. Metrum wirkt als Mittel zur Vorhersage und zur Koordination; London
behauptet hier kein numerisches Informationsmaß.
[Lernen](IMG_1636.PNG), [Vorhersage](IMG_1638.PNG),
[Eingeübte Fähigkeiten](IMG_1642.PNG).

Das Buch unterscheidet zunehmend feinere metrische Beschreibungen:
metrische Typen, tempoabhängige Varianten und schließlich charakteristische
expressive Zeitmuster. Der begrenzte Zeitbereich menschlicher Wahrnehmung
ist ebenso wichtig wie die formale Ordnung. Die später behandelte Many
Meters Hypothesis soll erklären, wie wiederholbare, erlernte Zeitmuster
eine große Vielfalt konkreter metrischer Erfahrungen ermöglichen.
[Tempoabhängigkeit](IMG_1645.PNG), [Aufbau des Buches](IMG_1646.PNG),
[Expressive Erwartungen](IMG_1647.PNG).

## Nutzen für den implementierten Informationsbegriff

Unsere Zustandsmaschine setzt eine zeitliche Ordnung voraus und zählt
Operationen innerhalb dieser Ordnung. Die Einleitung liefert die
entscheidende Frage für ihre Interpretation: **Beschreibt `Info` das
Onset-Muster unter einer vorgegebenen Referenz, die Bildung dieser Referenz
oder den Aufwand des Hörens?** Der aktuelle Code beantwortet zunächst nur
die erste Frage durch seine festgelegten Regeln.

Dass ein Metrum Vorhersage ermöglicht, ist eine plausible Grundlage für
die Beschreibung wiederholter Ereignisse durch Regeln statt durch isolierte
Einzelangaben. Daraus folgen aber keine Modellkosten, keine optimale
Kodierung und keine Gleichheit zwischen Codesymbolzahl und Aufmerksamkeit.
Diese Verbindung bleibt eine zu prüfende Deutung unseres Maßes.

Bei einem Rhythmus mit unveränderten Onsets und verändertem Playback-Tempo
bleiben unsere Werte gleich. London kündigt dagegen an, dass dieselbe
formale Struktur bei verschiedenen Tempi anders erlebt werden kann. Ein
Hörvergleich bei mehreren Tempi prüft daher die **Reichweite der psychologischen
Interpretation** unseres Maßes; er widerlegt nicht dessen korrekte Berechnung.

**Gewicht für VR-12:** begrifflicher Rahmen. Die Einleitung enthält keine
kontrollierte Zuordnung von Onset-Abständen zu Ereignissen pro Beat. Für
diese Frage sind besonders Kapitel 2 und 5 zu lesen; Kapitel 10 prüft die
Grenze eines ausschließlich rasterbezogenen Informationsbegriffs.

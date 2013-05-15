ProxySwitcher
=========
Der ProxySwitcher ist ein Java Tool zur Ausführung von Webanfragen an
die Suchmaschinen eTools.ch und Google. Dabei ist es in der Lage automatisch die öffentliche IP-Adresse zu verändern, um IP-Blockaden Seitens der Suchmaschine zu umgehen und ein flüssiges Suchen zu ermöglichen.

Benutzung:
---------
	java WebSearchLauncher [Suchmaschine] [Input] [Output] [zeige Suchstring] [zeige Link] [zeige Titel] [zeige Snippet][Proxyliste]

1. Suchmaschine:
Wert = google/etools.

2. Input:
Pfad zur Eingabedatei, welche die Suchstrings beinhaltet. Suchstrings
werden durch Newline Zeichen getrennt.

3. Output:
Pfad zur Ausgabedatei. Die Ergebnisse der Suche werden in die Aus-
gabedatei geschrieben.

4. zeige Suchstring:
Wert = true/false. Suchstrings werden in der Ausgabedatei geschrieben.

5. zeige Link:
Wert = true/false. Links werden in der Ausgabedatei geschrieben.

6. zeige Titel:
Wert = true/false. Titel werden in der Ausgabedatei geschrieben.

7. zeige Snippet:
Wert = true/false. Snippets werden in der Ausgabedatei geschrieben.

8. [optional] Proxyliste:
Pfad zur Proxyliste. Es werden nur die Proxies aus dieser Datei ver-
wendet. Ein Proxy hat die Form [IP]:[Port] und dProxies sind zeilenweise getrennt.





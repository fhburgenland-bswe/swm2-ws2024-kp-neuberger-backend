# BookManager – Business-Konzept

## 1. Ziel und Zweck des Systems
Der BookManager ist ein webbasiertes System zur Verwaltung persönlicher Buchsammlungen. Es richtet sich an Privatnutzer, die ihre gelesenen, geplanten oder empfohlenen Bücher an einem Ort speichern und bewerten möchten. Zusätzlich können Rezensionen erstellt und Bücher über eine externe API (OpenLibrary) automatisch ergänzt werden.

Ziel ist es, eine intuitive Plattform bereitzustellen, mit der Benutzer ihre Leseliste digital organisieren, Buchinformationen nachschlagen und persönliche Bewertungen dokumentieren können.

---

## 2. Zielgruppen

| Rolle        | Beschreibung                                                                 |
|--------------|------------------------------------------------------------------------------|
| Endnutzer    | Private Leser:innen, die ihre Bücher verwalten und Rezensionen schreiben    |
| Entwickler   | Beitragende, die die Plattform weiterentwickeln oder deployen               |
| Admins       | Für zukünftige Erweiterungen (z. B. Verwaltung von Benutzerrechten)         |

---

## 3. Haupt-Use-Cases

1. **Benutzer anlegen**
    - Eingabe von Name und E-Mail-Adresse
    - E-Mail muss eindeutig sein

2. **Buch zur Sammlung hinzufügen**
    - Eingabe einer ISBN
    - Automatischer Import von Buchinformationen via OpenLibrary

3. **Buchdetails manuell ändern**
    - Titel, Beschreibung, Cover-URL oder Autor:innen nachträglich anpassen

4. **Bücher bewerten**
    - Bewertung auf einer Skala von 1–5
    - Optional: Rezensionstext hinzufügen

5. **Rezensionen verwalten**
    - Rezensionen erstellen, ändern oder löschen

6. **Suchen und filtern**
    - Suche nach Titel, Autor, Jahr, Bewertung in der eigenen Sammlung

---

## 4. Nicht-funktionale Anforderungen

| Kriterium            | Beschreibung                                                                 |
|----------------------|------------------------------------------------------------------------------|
| Performance          | Antworten auf API-Aufrufe < 500ms im Normalbetrieb                          |
| Usability            | Klar strukturierte REST-API, verständliche Fehlerausgaben                    |
| Erweiterbarkeit      | Modulbasierter Aufbau, einfache Integration neuer Features                  |
| Sicherheit           | Eingabedaten validieren, eindeutige Benutzeridentifikation über UUIDs       |
| Kompatibilität       | REST-konforme HTTP-Kommunikation, UTF-8, JSON als Datenformat               |
| Dokumentation        | API vollständig via OpenAPI dokumentiert                                    |

---

## 5. Fachliche Regeln

- ISBN ist Pflichtfeld beim Hinzufügen eines Buches
- Bewertungen sind nur im Bereich **1 bis 5** zulässig
- Rezensionen müssen Text und Bewertung enthalten
- E-Mail-Adressen von Benutzern müssen eindeutig sein
- Eine Benutzer-ID (UUID) ist erforderlich für jede Interaktion mit der API
- Buch-ISBNs sind innerhalb eines Benutzers eindeutig

---

## 6. Zukunftsperspektive / Erweiterungsmöglichkeiten

- Benutzerregistrierung & Login (z. B. via OAuth2 / JWT)
- Öffentliche Rezensionen (z. B. auf Social-Plattform)
- Favoriten oder Lesestatus (Gelesen / Möchte ich lesen)
- Statistiken / Dashboard zu gelesenen Büchern

---

**Erstellt von:** Lukas Neuberger  
**Version:** 1.0  
**Stand:** 22.04.2025
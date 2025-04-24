# Buchmanagement API Dokumentation

## Überblick

Diese API ermöglicht die Verwaltung von Benutzern, deren Büchern und Buchrezensionen. Bücher werden anhand ihrer ISBN hinzugefügt, und die Buchdetails werden automatisch über die OpenLibrary API abgerufen.

## Basis-URL

```
http://localhost:8080
```

## Endpunkte

### Benutzerverwaltung

#### `GET /users`
- **Beschreibung**: Gibt eine Liste aller registrierten Benutzer zurück.
- **Antworten**:
  - `200 OK`: Liste der Benutzer
  - `500 Internal Server Error`

#### `POST /users`
- **Beschreibung**: Erstellt einen neuen Benutzer.
- **Body**:
  - `name` (string, erforderlich)
  - `email` (string, erforderlich)
- **Antworten**:
  - `201 Created`: Benutzer wurde erstellt
  - `400 Bad Request`: Ungültige Eingaben
  - `409 Conflict`: Ein Benutzer mit dieser E-Mail existiert bereits
  - `500 Internal Server Error`

#### `GET /users/{userId}`
- **Beschreibung**: Gibt die Details eines bestimmten Benutzers zurück.
- **Antworten**:
  - `200 OK`
  - `404 Not Found`
  - `500 Internal Server Error`

#### `PUT /users/{userId}`
- **Beschreibung**: Aktualisiert die Daten eines Benutzers.
- **Body**:
  - `name` (string)
  - `email` (string)
- **Antworten**:
  - `200 OK`
  - `400 Bad Request`
  - `404 Not Found`
  - `500 Internal Server Error`

#### `DELETE /users/{userId}`
- **Beschreibung**: Löscht einen Benutzer.
- **Antworten**:
  - `204 No Content`
  - `404 Not Found`
  - `500 Internal Server Error`

---

### Buchverwaltung

#### `GET /users/{userId}/books`
- **Beschreibung**: Gibt alle Bücher eines Benutzers zurück, optional gefiltert nach Bewertung.
- **Antworten**:
  - `200 OK`
  - `400 Bad Request` (ungültiger Bewertungs­parameter)
  - `404 Not Found` (Benutzer nicht gefunden)
  - `500 Internal Server Error`

#### `POST /users/{userId}/books`
- **Beschreibung**: Fügt ein neues Buch anhand der ISBN hinzu.
- **Body**:
  - `isbn` (string, erforderlich)
- **Antworten**:
  - `201 Created`
  - `400 Bad Request` (ungültige ISBN oder fehlende Felder)
  - `404 Not Found` (Benutzer nicht gefunden)
  - `500 Internal Server Error` (Fehler beim Abruf von OpenLibrary)

#### `GET /users/{userId}/books/search`
- **Beschreibung**: Sucht und filtert Bücher mit optionalen Parametern (`title`, `author`, `year`).
- **Antworten**:
  - `200 OK`
  - `400 Bad Request` (ungültige Anfrageparameter)
  - `404 Not Found` (Benutzer nicht gefunden)
  - `500 Internal Server Error`

#### `GET /users/{userId}/books/{isbn}`
- **Beschreibung**: Gibt die Details eines bestimmten Buchs zurück.
- **Antworten**:
  - `200 OK`
  - `404 Not Found` (Buch oder Benutzer nicht gefunden)
  - `500 Internal Server Error`

#### `PUT /users/{userId}/books/{isbn}`
- **Beschreibung**: Aktualisiert die Bewertung eines Buchs.
- **Body**:
  - `rating` (integer, 1–5, erforderlich)
- **Antworten**:
  - `200 OK`
  - `400 Bad Request` (ungültiger Bewertungswert)
  - `404 Not Found` (Buch oder Benutzer nicht gefunden)
  - `500 Internal Server Error`

#### `DELETE /users/{userId}/books/{isbn}`
- **Beschreibung**: Löscht ein Buch anhand der ISBN.
- **Antworten**:
  - `204 No Content`
  - `404 Not Found` (Buch oder Benutzer nicht gefunden)
  - `500 Internal Server Error`

#### `PUT /users/{userId}/books/{isbn}/details`
- **Beschreibung**: Manuelles Bearbeiten der Buchinformationen.
- **Body** (optional):
  - `title` (string)
  - `authors` (Liste von string)
  - `description` (string)
  - `coverUrl` (string)
- **Antworten**:
  - `200 OK`
  - `400 Bad Request` (ungültige Felder)
  - `404 Not Found` (Buch oder Benutzer nicht gefunden)
  - `500 Internal Server Error`

---

### Rezensionen

#### `GET /users/{userId}/books/{isbn}/reviews`
- **Beschreibung**: Gibt alle Rezensionen eines Buches zurück.
- **Antworten**:
  - `200 OK`
  - `400 Bad Request` (ungültige Eingabedaten)
  - `404 Not Found` (Benutzer oder Buch nicht gefunden)
  - `500 Internal Server Error`

#### `POST /users/{userId}/books/{isbn}/reviews`
- **Beschreibung**: Fügt eine neue Rezension hinzu.
- **Body**:
  - `rating` (integer, erforderlich)
  - `reviewText` (string, erforderlich)
- **Antworten**:
  - `201 Created`
  - `400 Bad Request` (ungültige Eingabedaten)
  - `404 Not Found` (Benutzer oder Buch nicht gefunden)
  - `500 Internal Server Error`

#### `PUT /users/{userId}/books/{isbn}/reviews/{reviewId}`
- **Beschreibung**: Aktualisiert eine Rezension.
- **Body**:
  - `rating` (integer)
  - `reviewText` (string)
- **Antworten**:
  - `200 OK`
  - `400 Bad Request` (ungültige Eingabedaten)
  - `404 Not Found` (Benutzer, Buch oder Rezension nicht gefunden)
  - `500 Internal Server Error`

#### `DELETE /users/{userId}/books/{isbn}/reviews/{reviewId}`
- **Beschreibung**: Löscht eine Rezension.
- **Antworten**:
  - `204 No Content`
  - `404 Not Found` (Benutzer, Buch oder Rezension nicht gefunden)
  - `500 Internal Server Error`

---

## Datenmodelle (Schemas)

### User
- `id`: uuid
- `name`: string
- `email`: string
- `books`: Liste von Book

### Book
- `isbn`: string
- `title`: string
- `authors`: Liste von string
- `publisher`: string
- `publishedDate`: string (Datum)
- `coverUrl`: string
- `description`: string
- `rating`: integer

### Review
- `id`: uuid
- `rating`: integer
- `reviewText`: string
- `bookId`: uuid

### ProblemDetail / Error
- `type`: string
- `title`: string
- `status`: integer
- `detail`: string
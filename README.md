# Bookmanager – Buchverwaltungs-Backend

## Projektbeschreibung

Dieses Projekt ist eine moderne Webanwendung zur Verwaltung von Büchern für verschiedene Benutzer. Benutzer können Bücher anhand ihrer ISBN hinzufügen, Rezensionen schreiben und Bewertungen abgeben. Die Anwendung ruft Buchinformationen automatisch über die OpenLibrary API ab, sobald eine gültige ISBN eingegeben wird.

Das Projekt wurde im Rahmen einer kommissionellen Prüfung an der Hochschule Burgenland umgesetzt und ist für die Nutzung in einer professionellen DevOps-Umgebung vorbereitet – das bedeutet: Automatisierte Tests, Containerisierung (Docker), Continuous Integration/Deployment (CI/CD) und einfache Installation per Helm in Kubernetes.

---

## Ziele und Funktionen

- **Verwaltung von Benutzern und deren Büchern**
- **Automatisches Laden von Buchinformationen über ISBN**
- **Buchbewertungen & Rezensionen**
- **Filtern und Suchen von Büchern**
- **Manuelles Bearbeiten von Buchtiteln, Beschreibungen, Autoren**
- **Moderne Entwicklungspraktiken (CI/CD, Docker, Helm, API-Dokumentation)**

---

## Technologischer Überblick

- **Backend**: Java 21, Spring Boot 3.2
- **Build-Tool**: Gradle
- **API**: REST, OpenAPI 3.0
- **Datenbanken**: PostgreSQL (Produktion), H2 (Tests/Lokal)
- **Tests & Qualitätssicherung**: JUnit, JaCoCo, Checkstyle, PMD, SpotBugs, OWASP Dependency-Check
- **Containerisierung**: Docker
- **Deployment**: Helm Charts, Kubernetes (z. B. Minikube)
- **CI/CD**: GitHub Actions
- **Optionales Frontend**: Angular 19

---

## Voraussetzungen zur Nutzung

Um dieses Projekt lokal oder in der Cloud betreiben zu können, brauchst du:

- Java 21
- Docker (zum Erstellen und Ausführen von Containern)
- Helm & Minikube (falls du das Projekt mit Kubernetes testen willst)
- Einen PostgreSQL-Datenbankserver (falls nicht lokal H2 genutzt wird)
- Ein GitHub-Konto, wenn du die CI/CD-Pipeline testen willst

---

## Lokale Ausführung (Entwicklung)

So kannst du das Projekt lokal starten (ohne Docker oder Kubernetes):

```bash
./gradlew bootRun
```

Die Anwendung ist danach erreichbar unter:

```
http://localhost:8080
```

---

## Docker-Nutzung (optional)

Um das Projekt als Docker-Container zu starten:

```bash
docker build -t ghcr.io/<dein-benutzer>/bookmanager:latest .
docker push ghcr.io/<dein-benutzer>/bookmanager:latest
```

---

## Deployment mit Helm (z. B. Minikube)

Stelle sicher, dass dein Cluster läuft und Helm installiert ist. Danach kannst du das Projekt installieren mit:

```bash
helm install bookmanager ./helm/bookmanager
```

Zum Aktualisieren:

```bash
helm upgrade bookmanager ./helm/bookmanager
```

Zum Entfernen:

```bash
helm uninstall bookmanager
```

---

## Datenbank-Zugangsdaten sichern (Secrets)

Um sensible Daten wie das Datenbank-Passwort nicht im Klartext zu speichern, wird ein Kubernetes-Secret verwendet:

```bash
kubectl create secret generic db-credentials --from-literal=password='DEIN_PASSWORT'
```

Dieses Secret wird automatisch im Deployment verwendet.

---

## API-Dokumentation (OpenAPI)

Die API ist dokumentiert nach dem OpenAPI-Standard. Du findest die Spezifikation in der Datei:

```
api-spec-yaml.txt
```

Einige Beispiel-Endpunkte:

| Methode | Pfad | Beschreibung |
|--------|------|--------------|
| GET    | `/users` | Alle Benutzer anzeigen |
| POST   | `/users` | Neuen Benutzer erstellen |
| GET    | `/users/{userId}` | Benutzer und Bücher anzeigen |
| POST   | `/users/{userId}/books` | Neues Buch via ISBN hinzufügen |
| GET    | `/users/{userId}/books` | Bücher anzeigen (mit Filtermöglichkeit) |
| PUT    | `/users/{userId}/books/{isbn}` | Buch bewerten oder bearbeiten |
| DELETE | `/users/{userId}/books/{isbn}` | Buch löschen |
| GET    | `/users/{userId}/books/{isbn}/reviews` | Rezensionen anzeigen |
| POST   | `/users/{userId}/books/{isbn}/reviews` | Rezension erstellen |

---

## Fehlerbehandlung

Die Anwendung gibt bei Problemen strukturierte Fehlermeldungen zurück. Diese enthalten:

- **Statuscode** (z. B. 404)
- **Titel** (z. B. "Benutzer nicht gefunden")
- **Detail** (z. B. "Benutzer mit ID XY nicht gefunden")

#### Globale Ausnahmebehandlung (Exception Handling)

Alle Exceptions in den REST-Controllern werden zentral im `GlobalExceptionHandler` abgefangen und in strukturierte HTTP-Fehlerantworten (ProblemDetail) übersetzt:

| Exception                         | HTTP-Status               | Titel                       | Beschreibung                               |
|-----------------------------------|---------------------------|-----------------------------|--------------------------------------------|
| `MethodArgumentNotValidException` | 400 Bad Request           | Validation failed           | Validierungsfehler bei Request-Body       |
| `InvalidBookException`            | 400 Bad Request           | Ungültige ISBN / Bewertung  | Ungültige ISBN oder Bewertung außerhalb 1–5 |
| `UserNotFoundException`           | 404 Not Found             | Benutzer nicht gefunden     | Kein Benutzer mit gegebener ID             |
| `BookNotFoundException`           | 404 Not Found             | Buch nicht gefunden         | Kein Buch mit gegebener ISBN               |
| `ReviewNotFoundException`         | 404 Not Found             | Rezension nicht gefunden    | Keine Rezension mit gegebener ID           |
| `Exception` (alle anderen)        | 500 Internal Server Error | Interner Fehler             | Unerwarteter Serverfehler                  |

Die Clients erhalten jeweils ein JSON-Objekt nach dem RFC 7807-Format (ProblemDetail), z. B.:

```json
{
  "type":   "about:blank",
  "title":  "Buch nicht gefunden",
  "status": 404,
  "detail": "Buch mit ISBN 9780140328721 wurde nicht gefunden."
}
```
---

## Tests & Codequalität

Um alle Tests lokal auszuführen:

```bash
./gradlew test
./gradlew jacocoTestReport
```

Die Codequalität wird automatisch geprüft via:
- **Checkstyle**
- **PMD**
- **SpotBugs**
- **OWASP Dependency-Check**

---

## Git Commit-Konventionen

Wir verwenden das [Conventional Commits](https://www.conventionalcommits.org/) Format, z. B.:

- `feat: add get user list and update api spec`
- `fix: use fully qualified repo name for tag`

Diese Struktur sorgt für eine klare, automatische Versionierung und übersichtliche Change-Logs.

---

## CI/CD Pipeline

Alle wichtigen Prozesse werden automatisiert über GitHub Actions:

- **Commit-Prüfung** (conform)
- **Code-Analyse** (Checkstyle, PMD, SpotBugs)
- **Sicherheitsanalyse** (Dependency-Check)
- **Tests & Coverage-Berichte**
- **Docker-Build & Push bei Änderungen auf `main`**
- **Linter für Dockerfile**

---
## Lizenz

Dieses Projekt wird unter der **Apache License, Version 2.0** veröffentlicht.

Die Lizenz regelt, dass der Quellcode frei genutzt, verändert und weiterverbreitet werden darf – auch für kommerzielle Zwecke. Gleichzeitig wird durch diese Open-Source-Lizenz sichergestellt, dass die ursprünglichen Urheber genannt werden und die Lizenzbedingungen erhalten bleiben. Dies schafft rechtliche Klarheit und Vertrauen sowohl für Entwickler als auch für Nutzer der Software.

Weitere Informationen findest du in der Datei `LICENSE` im Projektverzeichnis oder unter:  
[https://www.apache.org/licenses/LICENSE-2.0](https://www.apache.org/licenses/LICENSE-2.0)

---
## Kontakt

Projektleitung: Lukas Neuberger  
Studiengang: Software Engineering und vernetzte Systeme    
Hochschule: FH Burgenland

---


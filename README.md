# Bookmanager – Buchverwaltungs-Backend

Ein Spring Boot Backend zur Verwaltung von Benutzern und deren persönlichen Buchsammlungen. Das System unterstützt das Hinzufügen von Büchern über die ISBN und ruft dazu automatisch alle relevanten Buchdaten über die OpenLibrary API ab. Die Anwendung ist für den Einsatz in modernen DevOps-Umgebungen mit CI/CD, Containerisierung und Helm-Charts konzipiert.

---

## Projektüberblick

Dieses Projekt ist Teil der Kommissionellen-Prüfung im Studiengang Software Engineering an der Hochschule Burgenland.
Ziel ist die Umsetzung eines Minimum Loveable Products (MLP) mit Fokus auf:

- Mehrbenutzerfähige Buchverwaltung (ohne Authentifizierung)
- Automatisierter Datenabruf über OpenLibrary API
- Bewertungssystem mit Filteroption
- Moderne Softwarepraktiken (CI/CD, Containerisierung, Dokumentation, Tests)

---

## Technologie-Stack

- Backend: Java 21, Spring Boot 3.2.x, Gradle
- API: REST (OpenAPI 3.0)
- Datenbank: H2 (In-Memory), PostgreSQL
- Tools: PMD, Checkstyle, JaCoCo, Docker, Helm
- CI/CD: GitHub Actions
- Optionales Frontend (Bonus): Angular 19

---

## Externe APIs

- OpenLibrary Books API: https://openlibrary.org/swagger/docs
- OpenLibrary Cover API: https://openlibrary.org/dev/docs/api/covers

---

## Setup & Ausführen

### Voraussetzungen

- Java 21
- Docker (optional für Container-Betrieb)
- Helm & Minikube (für Kubernetes-Tests)

### Lokaler Start (Entwicklung)

```bash
./gradlew bootRun
```

Die API ist dann erreichbar unter:  
http://localhost:8080

### Container-Build

```bash
docker build -t ghcr.io/<your-user>/bookmanager:latest .
docker push ghcr.io/<your-user>/bookmanager:latest
```

### Helm-Deployment (Minikube)

```bash
helm install bookmanager ./helm/bookmanager
```

---

## Tests & Qualität

```bash
./gradlew test
./gradlew jacocoTestReport
```

CI/CD-Pipeline:
- Ausführung via GitHub Actions
- Linting & Style-Check: PMD, Checkstyle
- Testabdeckung: ≥ 70 % via JaCoCo
- Commit-Prüfung via Conform

---

## .gitignore

Die `.gitignore`-Datei stellt sicher, dass keine temporären oder unnötigen Dateien (wie IDE-spezifische Konfigurationen, `build/`, `.gradle/`, `*.iml`, `*.log`, etc.) ins Repository gelangen. Dadurch bleibt das Repository übersichtlich und sauber. Nur relevante Quellcode-Dateien, Konfigurationen und Dokumentationen werden versioniert.

---

## Commit-Richtlinien

Im Projekt wird das [Conventional Commits](https://www.conventionalcommits.org/) Format verwendet. Dies ermöglicht eine konsistente, strukturierte und verständliche Commit-Historie.

Beispiele:
- `feat: Buch-Entität hinzugefügt`
- `fix: Fehler beim Laden der OpenLibrary-Daten behoben`

Commit-Nachrichten werden automatisch geprüft (z. B. via Conform in der CI/CD-Pipeline). Commits, die nicht dem Standard entsprechen, werden abgelehnt.

---

## API-Überblick

Die OpenAPI-Spezifikation findest du in `api-spec-yaml.txt`. Hier ein Auszug der Endpunkte:

| Methode | Pfad | Beschreibung |
|--------|------|--------------|
| GET | `/users` | Alle Benutzer anzeigen |
| POST | `/users` | Neuen Benutzer anlegen |
| GET | `/users/{userId}` | Benutzerdetails inkl. Bücher |
| POST | `/users/{userId}/books` | Buch via ISBN hinzufügen |
| GET | `/users/{userId}/books` | Bücher anzeigen (mit Filter `?rating=...`) |
| PUT | `/users/{userId}/books/{isbn}` | Bewertung oder Details aktualisieren |
| DELETE | `/users/{userId}/books/{isbn}` | Buch löschen |
# README – Helm Chart für das Buchmanagement-Backend

## Übersicht

Dieses Helm Chart dient zur Bereitstellung des **Buchmanagement-Backends** in einem **Kubernetes-Cluster**. Es beinhaltet alle notwendigen Konfigurationen, um die Spring Boot Anwendung zu deployen, Umgebungsvariablen zu setzen und Datenbank-Zugangsdaten sicher zu verwalten.

## Voraussetzungen

Vor der Installation solltest du folgende Tools installiert haben:

- [Helm](https://helm.sh/docs/intro/install/)
- [kubectl](https://kubernetes.io/docs/tasks/tools/)
- Ein laufender **Kubernetes-Cluster**
- Ein in ein Container-Repository gepushtes **Docker-Image** deiner App (z. B. Docker Hub)

## Konfiguration

### Datenbank-Zugang (Secrets)

Das Datenbank-Passwort wird **nicht direkt in der `values.yaml`** gespeichert, sondern über ein Kubernetes Secret eingebunden.

#### Erstelle ein Secret für das DB-Passwort:

```bash
kubectl create secret generic db-credentials \
  --from-literal=password='DEIN_DB_PASSWORT'
```

Ersetze `'DEIN_DB_PASSWORT'` mit dem tatsächlichen Passwort deiner PostgreSQL-Datenbank.

### Umgebungsvariablen setzen

Passe vor der Installation die Datei `values.yaml` wie folgt an:

```yaml
env:
  SPRING_PROFILES_ACTIVE: "dev"
  DB_HOST: "walking-skeleton-lukas-5590.c.aivencloud.com"
  DB_PORT: 13267
  DB_NAME: "bookmanager"
  DB_USER: "avnadmin"
  DATABASE_TYPE: "postgresql"
  BOOK_API_URL: "https://openlibrary.org/api/books"
```

Diese Werte werden automatisch von den Helm-Templates in dein Deployment übernommen.

## Kubernetes-Secrets verwalten

Falls sich Zugangsdaten ändern, kannst du das Secret einfach löschen und neu erstellen:

### Secret löschen

```bash
kubectl delete secret db-credentials
```

### Secret anzeigen

```bash
kubectl get secret db-credentials -o yaml
```

## Deployment

### Chart installieren

```bash
helm install bookmanager ./charts/bookmanager
```

### Chart aktualisieren

```bash
helm upgrade bookmanager ./charts/bookmanager
```

### Chart löschen

```bash
helm uninstall bookmanager
```

Vor der Installation stelle sicher, dass das benötigte Secret existiert:

```bash
kubectl get secret db-credentials
```

## Zugriff auf das Backend

### Möglichkeit 1: Port-Forwarding

Falls dein Service als `ClusterIP` konfiguriert ist:

```bash
kubectl port-forward service/bookmanager 8080:80
```

Aufruf dann über:

```
http://localhost:8080
```

### Möglichkeit 2: Ingress (empfohlen)

Falls du Ingress aktiviert hast, prüfe ob in deiner `values.yaml` folgendes konfiguriert ist:

```yaml
ingress:
  enabled: true
  hosts:
    - host: bookmanager.local
      paths:
        - path: /
          pathType: Prefix
```

Dann füge folgendes zu deiner `/etc/hosts` Datei hinzu:

```
127.0.0.1 bookmanager.local
```

Danach kannst du deine App unter [http://bookmanager.local](http://bookmanager.local) aufrufen.

Ingress prüfen:

```bash
kubectl get ingress
kubectl describe ingress bookmanager
```

## Troubleshooting

### Status prüfen:

```bash
kubectl get pods
```

### Logs anzeigen:

```bash
kubectl logs -l app=bookmanager
```

### Helm Chart debuggen:

```bash
helm template bookmanager ./charts/bookmanager --values values.yaml --debug
```

### Secrets prüfen:

```bash
kubectl get secret db-credentials -o yaml
```

## API-Dokumentation

Die OpenAPI-Spezifikation befindet sich im `docs/`-Ordner. Stelle sicher, dass sie aktuell ist, bevor du ein neues Release machst.

## Hinweis

Dieses Helm Chart ist auf eine PostgreSQL-Datenbank und Spring Boot mit externem API-Zugriff (OpenLibrary) ausgelegt.
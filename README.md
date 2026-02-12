# GoSmartLib

Full-stack applicatie met Angular, Spring Boot, MySQL en Traefik.

---

## Snel starten

### 1. Environment file aanmaken
Maak `.env` bestand in root met:
```bash
sqlrootpwd=ROOT
```

### 2. Start de applicatie
```bash
docker compose up -d
```
##### Als de frontend niet correct de backend aanspreekt
```bash
docker compose down
docker compose build --no-cache frontend
docker compose up
```

### 2. URLs
```bash
Frontend: http://localhost/
Backend: http://localhost/api/hello
Traefik Dashboard: http://localhost:8081/dashboard/
```
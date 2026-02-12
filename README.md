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
Frontend: http://app.195.201.32.74.traefik.me/
Backend: http://api.195.201.32.74.traefik.me/hello
Traefik Dashboard: http://195.201.32.74:8081/dashboard/
```

### Voor te runnen op je eigen machine
Verander urls in docker-compose.yml om ip adres eruit te halen (app.traefik.me/api.traefik.me)
Verander url in environment.ts van frontend naar nieuwe url van backend.
Verander url van origin in controller van backend naar nieuwe url van frontend.

De nieuw nieuwe URLs zijn dan het volgende:
```bash
Frontend: http://app.traefik.me/
Backend: http://api.traefik.me/hello
Traefik Dashboard: http://localhost:8081/dashboard/
```
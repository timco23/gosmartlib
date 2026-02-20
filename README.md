# GoSmartLib

Full-stack applicatie met Angular, Spring Boot, MySQL, Traefik en custom domein via get.tech.

---

## Snel starten

### 1. Environment file aanmaken
Maak een `.env` bestand in root met:
```bash
sqlrootpwd=ROOTROOT
```

### 2. Start de applicatie (lokaal)
```bash
docker compose up -d
```
Als de frontend niet correct de backend aanspreekt
```bash
docker compose down
docker compose build --no-cache frontend
docker compose up
```

Als het nodig is om op de server de production versie te restarten gebruik:
```bash
docker compose -f docker-compose.yml -f docker-compose.prod.yml down
docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d --build
```

### 3. URL's
Productie (server)
```bash
Frontend: http://gsmartlib.tech
Backend: http://api.gsmartlib.tech/hello
```

Lokaal:
```bash
Frontend: http://localhost/
Backend: http://localhost/api/hello
Traefik Dashboard: http://localhost:8081/dashboard/
```
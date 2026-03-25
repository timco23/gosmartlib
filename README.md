# GoSmartLib

Full-stack applicatie met Angular, Spring Boot, MySQL, Traefik en custom domein via get.tech.

---

## Snel starten

### 1. Environment file aanmaken

Maak een `.env` bestand in root met:

```bash
sqlrootpwd=passwordhere
email=yourownemail@gmail.com
sqlrootuser=rootusernamehere

# For oauth
SMARTSCHOOL_CLIENT_ID=yourclientid
SMARTSCHOOL_CLIENT_SECRET=yourclientsecret
SMARTSCHOOL_PLATFORM=yourschool.smartschool.be
SMARTSCHOOL_REDIRECT_URI=https://api.yourdomain.tech/oauth/smartschool/callback
APP_FRONTEND_BASE_URL=https://yourdomain.tech
JWT_SECRET=your32characterlongsecrethere1234
JWT_EXPIRES_MINUTES=15
OAUTH_DEFAULT_SCHOOL_ID=1
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
Frontend: https://gsmartlib.tech
Backend: https://api.gsmartlib.tech/
```

Lokaal:

```bash
Frontend: http://localhost/
Backend: http://localhost/api/
Traefik Dashboard: http://localhost:8081/dashboard/
```

# GoSmartLib
Full-stack application using Angular, Spring Boot, MySQL, Traefik and a custom domain via get.tech.

---

## Short description
GoSmartLib is a school-library management platform where users can log in using Smartschool. This application is made to view and manage the lendings and the books in your school library. The application is split into different roles: student, teacher, librarian and admin. These users have different rights on the platform.

## Roles
| Role | Description |
|-----|-------------|
| Student | View books and lendings |
| Teacher | The same as student + make collections and reading lists |
| Librarian | Complete management over the books and lendings + school settings |
| Admin | Can switch schools and manage users |

## Tech stack
| Layer | Technology |
|-------|-----------|
| Frontend | Angular 19 + Nginx |
| Backend | Spring Boot 3 (Java 21) |
| Database | MySQL 8 |
| Reverse proxy | Traefik |
| Authentication | Smartschool OAuth2 + JWT |
| Book metadata | Google Books API + Hardcover API |
| Profanity filter | Mistral AI |
| Containerization | Docker + Docker Compose |

## Requirements
- Docker Desktop
- Git

## Quick setup
### 1. Create environment file
Create a `.env` file in the root with:
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
# For name encryption
ENCRYPTION_SECRET=aLongRandomKey123!@#
# External metadata APIs
HARDCOVER_API_KEY=yourApiKey
GOOGLE_BOOKS_API_KEY=yourApiKey
# For profanity filter (reviews)
MODERATION_API_KEY=your_mistral_key_here
```
Get your Mistral API key (free, no credit card needed):
1. Go to [console.mistral.ai](https://console.mistral.ai) → create a new account
2. Choose "Free experimentation" (Experiment plan)
3. Verify your phone number
4. Go to Organization → API Keys → "Create new key"
5. Choose a key name and leave the other fields as they are
6. Copy the key — you only see it once!

### 2. Start the application (locally)
```bash
docker compose up -d
```
If the frontend does not correctly reach the backend:
```bash
docker compose down
docker compose build --no-cache frontend
docker compose up
```
If it is necessary to restart the production version on the server, use:
```bash
docker compose -f docker-compose.yml -f docker-compose.prod.yml down
docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d --build
```

### 3. URLs
Production (server)
```bash
Frontend: https://gsmartlib.tech
Backend: https://api.gsmartlib.tech/
```
Local:
```bash
Frontend: http://localhost/
Backend: http://localhost/api/
Traefik Dashboard: http://localhost:8081/dashboard/
```

## Running the tests

Navigate to `backend/spring-backend`, then run:

```bash
.\mvnw clean test
```

### SonarQube analysis

**1. Start SonarQube in Docker**
```bash
docker run -d --name sonarqube -p 9000:9000 -p 9092:9092 sonarqube
```

**2. Create an authentication token**
1. Go to http://localhost:9000/
2. Log in with username `admin` and password `admin`
3. Go to your profile (top right) → Security → Tokens
4. Create a **Global Token** and copy it

**3. Run the analysis**
```bash
.\mvnw clean verify sonar:sonar -DskipTests `
  "-Dsonar.host.url=http://localhost:9000" `
  "-Dsonar.token=<YOUR_TOKEN>" `
  "-Dsonar.projectKey=gosmartlib-backend" `
  "-Dsonar.projectName=gosmartlib-backend"
```
Replace `<YOUR_TOKEN>` with the token you created above.

**4. View the report**

Go to http://localhost:9000/ to view the analysis results.
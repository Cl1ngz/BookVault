# BookVault

A book review application built with Spring Boot, Vue.js, and PostgreSQL.

## Tech Stack
- **Backend:** Java 21, Spring Boot, Spring Data JPA
- **Frontend:** Vue 3, TypeScript, Vite
- **Database:** PostgreSQL 15
- **Infrastructure:** Docker, Nginx

---

## Production — Run entire app with Docker

> Requires Docker and Docker Compose installed.

```bash
git clone https://github.com/Cl1ngz/BookVault.git
cd BookVault
docker-compose up --build
```

| Service  | URL |
|----------|-----|
| Frontend | http://localhost |
| Backend API | http://localhost:8080/api/v1 |
| Swagger UI | http://localhost:8080/swagger-ui/index.html |

#### Stop and remove everything
```bash
docker-compose down -v
```

---

##  Development setup

> Requires Docker, Java 21, Maven, and Node.js installed.

### 1. Clone and start the database
```bash
git clone https://github.com/Cl1ngz/BookVault.git
cd BookVault
docker-compose up -d db
```

### 2. Run backend (IntelliJ IDEA or terminal)
```bash
cd backend
./mvnw spring-boot:run
```
Swagger available at: http://localhost:8080

### 3. Run frontend
```bash
cd frontend
npm install
npm run dev
```

Frontend available at: http://localhost:5173

---

##  Troubleshooting

- **Cannot connect to database?** If you have a VPN or custom DNS active, disable it and try again.
- **Port 80 already in use?** Stop any local web server (Apache/Nginx) before running Docker.
```
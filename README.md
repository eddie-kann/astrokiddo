
# AstroKiddo

### Time travel through space imagery - Images & from NASA with AI voiceover 

## Stack:
- **Backend:** Java 17, Spring Boot 3 (WebFlux, R2DBC Postgres, Caffeine, Flyway, OpenAPI, AWS SDK)
- **Frontend:** Angular 20, TailwindCSS, ng-zorro-antd
- **NASA APIs:** APOD, NASA Image & Video Library
- **Cloudflare:** Worker AI, R2
- **Dev:** Docker/Nginx

## Contents
- [Architecture](#architecture)
- [Prerequisites](#prerequisites)
- [Environment variables](#environment-variables)
- [Run the backend locally](#run-the-backend-locally)
- [Run the frontend locally](#run-the-frontend-locally)
- [Build production artifacts](#build-production-artifacts)
- [Docker & Compose](#docker--compose)
- [API docs](#api-docs)

## Architecture
- **Backend:** Java 17 + Spring Boot 3 (WebFlux, Security, Validation, R2DBC/PostgreSQL, Flyway, Caffeine cache, Springdoc OpenAPI). The `dev` profile is active by default.
- **Data:** PostgreSQL 16 with schema migrations in `src/main/resources/db/migration` applied automatically on startup.
    - NASA APOD and NASA Image & Video Library APIs.
    - Cloudflare Workers AI (text & TTS) and R2 storage for generated deck assets.
- **Frontend:** Angular 20 standalone app served via NGINX in production.

## Prerequisites
- Java 17+
- Maven 3.9+
- Node.js 20+ and npm
- Docker & Docker Compose
## Environment variables
The application is fully driven by environment variables

| Variable                                                                                                                                                                                     | Purpose                 | Example (dev)                                  |
|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-------------------------|------------------------------------------------|
| `SERVER_PORT`                                                                                                                                                                                | Backend HTTP port       | `8091`                                         |
| `APP_TIME_ZONE`                                                                                                                                                                              | JVM & app time zone     | `UTC`                                          |
| `SPRING_R2DBC_URL_DEV`                                                                                                                                                                       | R2DBC connection string | `r2dbc:postgresql://localhost:5432/astrokiddo` |
| `SPRING_R2DBC_USERNAME_DEV` / `SPRING_R2DBC_PASSWORD_DEV`                                                                                                                                    | DB credentials          | `astrokiddo` / `astrokiddo`                    |
| `SPRING_R2DBC_FLYWAY_URL_DEV`                                                                                                                                                                | JDBC URL for Flyway     | `jdbc:postgresql://localhost:5432/astrokiddo`  |
| `NASA_API_KEY`                                                                                                                                                                               | NASA API key            | `DEMO_KEY` (rate-limited)                      |
| `APOD_BASE_URL`                                                                                                                                                                              | APOD base URL           | `https://api.nasa.gov/planetary`               |
| `IMAGES_BASE_URL`                                                                                                                                                                            | NASA Images base URL    | `https://images-api.nasa.gov`                  |
| `CF_API_TOKEN`, `CF_ACCOUNT_ID`, `CF_AI_PROVIDER`, `CF_AI_VENDOR`, `CF_AI_MODEL`, `CF_AI_BASE_URL`, `CF_AI_ENABLED`, `CF_AI_MAX_VOCAB`, `CF_AI_TEMPERATURE`, `CF_TTS_VENDOR`, `CF_TTS_MODEL` | Cloudflare AI settings  | see Cloudflare Workers AI dashboard            |
| `R2_ACCOUNT_ID`, `R2_ACCESS_KEY`, `R2_SECRET_KEY`, `R2_BUCKET`, `R2_PUBLIC_BASE_URL`                                                                                                         | Cloudflare R2 storage   | from your R2 bucket                            |

For production, use the `_PROD` variants referenced in `application-prod.yml`.

## Run the backend locally
**Start PostgreSQL (dev convenience):**
   ```bash
   docker run --name astrokiddo-db -e POSTGRES_DB=astrokiddo -e POSTGRES_USER=astrokiddo -e POSTGRES_PASSWORD=astrokiddo -p 5432:5432 -d postgres:16
   ```

**Export environment variables** (matching the table above):
   ```bash
   export SERVER_PORT=8091
   export APP_TIME_ZONE=UTC
   export SPRING_R2DBC_URL_DEV=r2dbc:postgresql://localhost:5432/astrokiddo
   export SPRING_R2DBC_USERNAME_DEV=astrokiddo
   export SPRING_R2DBC_PASSWORD_DEV=astrokiddo
   export SPRING_R2DBC_FLYWAY_URL_DEV=jdbc:postgresql://localhost:5432/astrokiddo
   export NASA_API_KEY=DEMO_KEY
   export APOD_BASE_URL=https://api.nasa.gov/planetary
   export IMAGES_BASE_URL=https://images-api.nasa.gov
   export CF_AI_ENABLED=false
   ```
   (Set the Cloudflare variables to real values when AI features are needed)


**Run the API:**
   ```bash
   mvn spring-boot:run -P dev
   ```
   The app starts with the `dev` profile, applies Flyway migrations, and listens on `http://localhost:8091`

## Run the frontend locally
Install dependencies:
   ```bash
   cd angular-app
   npm install
   ```
Point the proxy to your backend. Update `angular-app/proxy.conf.json` `target` to your local API
Start the dev server:
   ```bash
   npm start
   ```
   Angular default `http://localhost:4200` with API calls proxied to the backend.

## Build production artifacts
- **Backend JAR:**
  ```bash
  mvn clean package -P prod -DskipTests
  ```

- **Frontend static bundle:**
  ```bash
  cd angular-app
  npm run build
  ```
  Builds to `angular-app/dist/astrokiddo-web/browser/`

## Docker & Compose
- **Backend image:** `docker build -t astrokiddo-backend .`
- **Frontend image:** `docker build -t astrokiddo-frontend ./angular-app`
- **All services (PostgreSQL + backend + frontend):**
    1. Create a `.env` file at the repo root with the variables from [Environment variables](#environment-variables).
    2. Run `docker compose up --build` to start everything on a shared bridge network. Containers expose backend port `8091` and frontend port `80`

## API docs
With the `dev` profile, Swagger UI and OpenAPI JSON are enabled via Springdoc.  The `prod` profile disables these endpoints
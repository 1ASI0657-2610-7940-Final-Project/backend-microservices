# Render Services Deployment

This repo is prepared for **4 independent Render Docker Web Services** (no Docker Compose required in production).

All services must run with:
- `SPRING_PROFILES_ACTIVE=render`
- `SPRING_DATASOURCE_URL` + `SPRING_DATASOURCE_USERNAME` + `SPRING_DATASOURCE_PASSWORD` as primary datasource variables in Render
- `SUPABASE_JDBC_URL` + `SUPABASE_DB_USERNAME` + `SUPABASE_DB_PASSWORD` as compatibility fallback (set both families with the same values)
- shared Supabase PostgreSQL
- same `JWT_SECRET` across all 4 services
- same `INTERNAL_SERVICE_TOKEN` across Pulls + Chat Notification
- health check path: `/actuator/health`

## Account Split

### Render Account A
- `gigu-access-profile-service`
- `gigu-gig-marketplace-service`

### Render Account B
- `gigu-pulls-service`
- `gigu-chat-notification-service`

Because services are split across accounts, use **public HTTPS Render URLs** for service-to-service calls. Do not use private networking across accounts.

## Service 1
Name:
`gigu-access-profile-service`

Root Directory:
`services/access-profile-service`

Dockerfile:
`Dockerfile`

Health Check Path:
`/actuator/health`

Required environment variables:
- `SPRING_PROFILES_ACTIVE=render`
- `SPRING_DATASOURCE_URL=jdbc:postgresql://...`
- `SPRING_DATASOURCE_USERNAME=...`
- `SPRING_DATASOURCE_PASSWORD=<SUPABASE_DB_PASSWORD>`
- `SUPABASE_JDBC_URL=jdbc:postgresql://...`
- `SUPABASE_DB_USERNAME=...`
- `SUPABASE_DB_PASSWORD=<SUPABASE_DB_PASSWORD>`
- `SUPABASE_URL=https://...supabase.co`
- `SUPABASE_SERVICE_ROLE_KEY=<SUPABASE_SERVICE_ROLE_KEY>`
- `SUPABASE_STORAGE_BUCKET_PORTFOLIO=portfolio`
- `JWT_SECRET=<JWT_SECRET_SHARED_ACROSS_ALL_SERVICES>`
- `CORS_ALLOWED_ORIGINS=http://localhost:5173,https://YOUR_FRONTEND_DOMAIN.vercel.app`

## Service 2
Name:
`gigu-gig-marketplace-service`

Root Directory:
`services/gig-marketplace-service`

Dockerfile:
`Dockerfile`

Health Check Path:
`/actuator/health`

Required environment variables:
- `SPRING_PROFILES_ACTIVE=render`
- `SPRING_DATASOURCE_URL=jdbc:postgresql://...`
- `SPRING_DATASOURCE_USERNAME=...`
- `SPRING_DATASOURCE_PASSWORD=<SUPABASE_DB_PASSWORD>`
- `SUPABASE_JDBC_URL=jdbc:postgresql://...`
- `SUPABASE_DB_USERNAME=...`
- `SUPABASE_DB_PASSWORD=<SUPABASE_DB_PASSWORD>`
- `SUPABASE_URL=https://...supabase.co`
- `SUPABASE_SERVICE_ROLE_KEY=<SUPABASE_SERVICE_ROLE_KEY>`
- `SUPABASE_STORAGE_BUCKET_GIG_MEDIA=gig-media`
- `JWT_SECRET=<JWT_SECRET_SHARED_ACROSS_ALL_SERVICES>`
- `CORS_ALLOWED_ORIGINS=http://localhost:5173,https://YOUR_FRONTEND_DOMAIN.vercel.app`

## Service 3
Name:
`gigu-pulls-service`

Root Directory:
`services/pulls-service`

Dockerfile:
`Dockerfile`

Health Check Path:
`/actuator/health`

Required environment variables:
- `SPRING_PROFILES_ACTIVE=render`
- `SPRING_DATASOURCE_URL=jdbc:postgresql://...`
- `SPRING_DATASOURCE_USERNAME=...`
- `SPRING_DATASOURCE_PASSWORD=<SUPABASE_DB_PASSWORD>`
- `SUPABASE_JDBC_URL=jdbc:postgresql://...`
- `SUPABASE_DB_USERNAME=...`
- `SUPABASE_DB_PASSWORD=<SUPABASE_DB_PASSWORD>`
- `JWT_SECRET=<JWT_SECRET_SHARED_ACROSS_ALL_SERVICES>`
- `INTERNAL_SERVICE_TOKEN=<INTERNAL_SERVICE_TOKEN_SHARED_WITH_CHAT_NOTIFICATION>`
- `ACCESS_PROFILE_SERVICE_URL=https://gigu-access-profile-service.onrender.com`
- `GIG_MARKETPLACE_SERVICE_URL=https://gigu-gig-marketplace-service.onrender.com`
- `CHAT_NOTIFICATION_SERVICE_URL=https://gigu-chat-notification-service.onrender.com`
- `CORS_ALLOWED_ORIGINS=http://localhost:5173,https://YOUR_FRONTEND_DOMAIN.vercel.app`

## Service 4
Name:
`gigu-chat-notification-service`

Root Directory:
`services/chat-notification-service`

Dockerfile:
`Dockerfile`

Health Check Path:
`/actuator/health`

Required environment variables:
- `SPRING_PROFILES_ACTIVE=render`
- `SPRING_DATASOURCE_URL=jdbc:postgresql://...`
- `SPRING_DATASOURCE_USERNAME=...`
- `SPRING_DATASOURCE_PASSWORD=<SUPABASE_DB_PASSWORD>`
- `SUPABASE_JDBC_URL=jdbc:postgresql://...`
- `SUPABASE_DB_USERNAME=...`
- `SUPABASE_DB_PASSWORD=<SUPABASE_DB_PASSWORD>`
- `JWT_SECRET=<JWT_SECRET_SHARED_ACROSS_ALL_SERVICES>`
- `INTERNAL_SERVICE_TOKEN=<INTERNAL_SERVICE_TOKEN_SHARED_WITH_PULLS>`
- `CORS_ALLOWED_ORIGINS=http://localhost:5173,https://YOUR_FRONTEND_DOMAIN.vercel.app`

`INTERNAL_SERVICE_TOKEN` is mapped to `SERVICE_TOKEN` by `application-render.yml` for compatibility with the current controller.

## Service URL Dependency Note

If final Render service names differ, update these env vars manually:
- `ACCESS_PROFILE_SERVICE_URL`
- `GIG_MARKETPLACE_SERVICE_URL`
- `CHAT_NOTIFICATION_SERVICE_URL`

## Production Safety Checklist

- No render profile points to `localhost:5432`.
- Never use datasource usernames like `postgres` in Render. Use the full Supabase pooled user (e.g. `postgres.<project-ref>`).
- No render profile hardcodes localhost internal service URLs.
- Runtime Docker command uses `PORT` env var, not fixed service ports.
- `/actuator/health` is publicly reachable (security whitelist present).
- Swagger is enabled for testing (`/swagger-ui.html`), but should be restricted/disabled for strict production hardening later.
- Expected storage buckets:
  - `portfolio`
  - `gig-media`

# backend-microservices

## Architecture
- Frontend: Vue + Vite on Vercel
- Gateway: Vercel rewrites (`frontend/gigu-web/vercel.json`)
- Backend: Spring Boot microservices on Render
- Database: Supabase PostgreSQL
- Storage: Supabase Storage (`portfolio`, `gig-media`)
- CI: GitHub Actions per service
- Messaging: No RabbitMQ in initial stage

## Services
- `services/access-profile-service`
- `services/gig-marketplace-service`
- `services/pulls-service`
- `services/chat-notification-service`

## GitFlow Branches
- `feature/access-profile-service`
- `feature/gig-marketplace-service`
- `feature/pull-engagement-service`
- `feature/chat-notification-service`
- `feature/main-app-logic`

## Local Run
Required profile: `local`

```powershell
cd "C:\Users\VR\Desktop\Gigu Code\backend-microservices"
powershell -ExecutionPolicy Bypass -File .\scripts\run-local-all.ps1
```

Smoke test:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\smoke-test-local.ps1
```

Note: Spring Boot Dashboard can fail if it launches with default profile; use the `local` launch configurations or the script above.

## Swagger URLs
- Access: `http://localhost:8081/swagger-ui.html`
- Marketplace: `http://localhost:8082/swagger-ui.html`
- Pulls: `http://localhost:8083/swagger-ui.html`
- Chat: `http://localhost:8084/swagger-ui.html`

Pulls service compatibility note:
- Service name/folder: `pulls-service`
- API base path remains: `/api/v1/engagement/**`

## Render Notes
See `render/render-services.md` for root directories, env vars, build/start commands, health endpoint, and Swagger endpoints.

## Testing
Run per service:
```bash
cd services/<service-name>
mvn clean verify
```
JaCoCo minimum coverage is 85%.

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
- `services/pull-engagement-service`
- `services/chat-notification-service`

## GitFlow Branches
- `feature/access-profile-service`
- `feature/gig-marketplace-service`
- `feature/pull-engagement-service`
- `feature/chat-notification-service`
- `feature/error-handling-swagger-ci`

## Swagger URLs
- Access: `/swagger-ui.html`
- Marketplace: `/swagger-ui.html`
- Engagement: `/swagger-ui.html`
- Chat: `/swagger-ui.html`
- OpenAPI docs: `/v3/api-docs`

## Render Notes
See `render/render-services.md` for root directories, env vars, build/start commands, health endpoint, and Swagger endpoints.

## Testing
Run per service:
```bash
cd services/<service-name>
mvn clean verify
```
JaCoCo minimum coverage is 85%.

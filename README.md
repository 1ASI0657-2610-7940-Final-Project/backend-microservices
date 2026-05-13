# Backend Microservices

## Tecnologias
- Java 21
- Spring Boot 3.3
- Maven
- PostgreSQL (Supabase)
- Google Cloud Run
- GitHub Actions

## Microservicios
- Access Profile: `services/access-profile-service`
- Gig Marketplace: `services/gig-marketplace-service`
- Pulls: `services/pulls-service`
- Chat Notification: `services/chat-notification-service`

## Cloud Run desplegado
- `gigu-access-profile-service`: `https://gigu-access-profile-service-149855215912.us-central1.run.app`
- `gigu-gig-marketplace-service`: `https://gigu-gig-marketplace-service-149855215912.us-central1.run.app`
- `gigu-pulls-service`: `https://gigu-pulls-service-149855215912.us-central1.run.app`
- `gigu-chat-notification-service`: `https://gigu-chat-notification-service-149855215912.us-central1.run.app`

## Swagger URLs
- Access: `https://gigu-access-profile-service-149855215912.us-central1.run.app/swagger-ui/index.html`
- Marketplace: `https://gigu-gig-marketplace-service-149855215912.us-central1.run.app/swagger-ui/index.html`
- Pulls: `https://gigu-pulls-service-149855215912.us-central1.run.app/swagger-ui/index.html`
- Chat: `https://gigu-chat-notification-service-149855215912.us-central1.run.app/swagger-ui/index.html`

## Deploy manual local (PowerShell)
- `./gcloud/deploy-access-profile-service.ps1`
- `./gcloud/deploy-gig-marketplace-service.ps1`
- `./gcloud/deploy-pulls-service.ps1`
- `./gcloud/deploy-chat-notification-service.ps1`
- `./gcloud/deploy-all.ps1`

## Deploy manual en GitHub Actions
Cada microservicio se despliega por separado usando `workflow_dispatch`:
- `deploy-access-profile-service.yml`
- `deploy-gig-marketplace-service.yml`
- `deploy-pulls-service.yml`
- `deploy-chat-notification-service.yml`

Ejecucion manual:
`GitHub -> Actions -> seleccionar workflow -> Run workflow`

Nota: si no aparece el boton `Run workflow`, el archivo del workflow debe existir en la default branch o en la rama desde donde GitHub permita ejecucion manual.

## GitHub Secrets requeridos
- `GCP_SA_KEY`
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `SUPABASE_URL`
- `SUPABASE_SERVICE_ROLE_KEY`
- `JWT_SECRET`
- `INTERNAL_SERVICE_TOKEN`

## GitHub Variable recomendada
- `CORS_ALLOWED_ORIGINS` (ejemplo: `https://TU-FRONTEND.vercel.app,http://localhost:5173`)

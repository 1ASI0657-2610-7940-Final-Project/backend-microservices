# Backend Microservices

## Stack
- Java 21
- Spring Boot 3.3
- Maven
- PostgreSQL (Supabase)
- Google Cloud Run

## Microservices
- Access Profile: `services/access-profile-service`
- Gig Marketplace: `services/gig-marketplace-service`
- Pulls: `services/pulls-service`
- Chat Notification: `services/chat-notification-service`

## Cloud Run Services
- `gigu-access-profile-service`: `https://gigu-access-profile-service-oawg43e6ea-uc.a.run.app`
- `gigu-gig-marketplace-service`: `https://gigu-gig-marketplace-service-oawg43e6ea-uc.a.run.app`
- `gigu-pulls-service`: `https://gigu-pulls-service-149855215912.us-central1.run.app`
- `gigu-chat-notification-service`: `https://gigu-chat-notification-service-oawg43e6ea-uc.a.run.app`

## Swagger
- Access: `https://gigu-access-profile-service-oawg43e6ea-uc.a.run.app/swagger-ui/index.html`
- Marketplace: `https://gigu-gig-marketplace-service-oawg43e6ea-uc.a.run.app/swagger-ui/index.html`
- Pulls: `https://gigu-pulls-service-149855215912.us-central1.run.app/swagger-ui/index.html`
- Chat: `https://gigu-chat-notification-service-oawg43e6ea-uc.a.run.app/swagger-ui/index.html`

## Health
- Access: `https://gigu-access-profile-service-oawg43e6ea-uc.a.run.app/actuator/health`
- Marketplace: `https://gigu-gig-marketplace-service-oawg43e6ea-uc.a.run.app/actuator/health`
- Pulls: `https://gigu-pulls-service-149855215912.us-central1.run.app/actuator/health`
- Chat: `https://gigu-chat-notification-service-oawg43e6ea-uc.a.run.app/actuator/health`

## Deploy
`./gcloud/deploy-all.ps1`

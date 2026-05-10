# Render Services

## access-profile-service

This microservice must be deployed as an independent Render Web Service.

- Root Directory: `services/access-profile-service`
- Runtime: Docker
- Internal Port: `8080`

Required environment variables:
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `JWT_SECRET`
- `SUPABASE_URL`
- `SUPABASE_SERVICE_ROLE_KEY`
- `SUPABASE_STORAGE_BUCKET_PORTFOLIO`

## gig-marketplace-service
- Root Directory: services/gig-marketplace-service
- Runtime: Docker


## pull-engagement-service
- Root Directory: services/pull-engagement-service
- Runtime: Docker


## chat-notification-service
- Root Directory: services/chat-notification-service
- Runtime: Docker


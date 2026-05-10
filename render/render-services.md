# Render Services

## gigu-access-profile-service
- Root Directory: `services/access-profile-service`
- Build Command: `mvn clean package`
- Start Command: `java -jar target/access-profile-service-0.0.1-SNAPSHOT.jar`
- Environment Variables: `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`, `JWT_SECRET`, `SUPABASE_URL`, `SUPABASE_SERVICE_ROLE_KEY`, `SUPABASE_STORAGE_BUCKET_PORTFOLIO`
- Health Endpoint: `/v3/api-docs`
- Swagger URL: `/swagger-ui.html`

## gigu-gig-marketplace-service
- Root Directory: `services/gig-marketplace-service`
- Build Command: `mvn clean package`
- Start Command: `java -jar target/gig-marketplace-service-0.0.1-SNAPSHOT.jar`
- Environment Variables: `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`, `JWT_SECRET`, `SUPABASE_URL`, `SUPABASE_SERVICE_ROLE_KEY`, `SUPABASE_STORAGE_BUCKET_GIG_MEDIA`
- Health Endpoint: `/v3/api-docs`
- Swagger URL: `/swagger-ui.html`

## gigu-pull-engagement-service
- Root Directory: `services/pull-engagement-service`
- Build Command: `mvn clean package`
- Start Command: `java -jar target/pull-engagement-service-0.0.1-SNAPSHOT.jar`
- Environment Variables: `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`, `JWT_SECRET`, `SERVICE_TOKEN`
- Health Endpoint: `/v3/api-docs`
- Swagger URL: `/swagger-ui.html`

## gigu-chat-notification-service
- Root Directory: `services/chat-notification-service`
- Build Command: `mvn clean package`
- Start Command: `java -jar target/chat-notification-service-0.0.1-SNAPSHOT.jar`
- Environment Variables: `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`, `JWT_SECRET`, `SERVICE_TOKEN`
- Health Endpoint: `/v3/api-docs`
- Swagger URL: `/swagger-ui.html`

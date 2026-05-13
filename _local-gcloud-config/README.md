# Local Google Cloud Config

Esta carpeta esta ignorada por Git para guardar configuracion sensible local.

## Plantillas seguras
- `access-profile-service.env.yaml.example`
- `gig-marketplace-service.env.yaml.example`
- `pulls-service.env.yaml.example`
- `chat-notification-service.env.yaml.example`

Crea tus archivos reales sin `.example` antes de desplegar:
- `_local-gcloud-config/access-profile-service.env.yaml`
- `_local-gcloud-config/gig-marketplace-service.env.yaml`
- `_local-gcloud-config/pulls-service.env.yaml`
- `_local-gcloud-config/chat-notification-service.env.yaml`

## Deploy manual local
- `./gcloud/deploy-access-profile-service.ps1`
- `./gcloud/deploy-gig-marketplace-service.ps1`
- `./gcloud/deploy-pulls-service.ps1`
- `./gcloud/deploy-chat-notification-service.ps1`
- `./gcloud/deploy-all.ps1`

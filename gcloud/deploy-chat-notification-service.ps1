param([string]$ProjectId)
if (-not $ProjectId) { $ProjectId = gcloud config get-value project }
if (-not $ProjectId -or $ProjectId -eq "(unset)") { throw "Set PROJECT_ID or pass -ProjectId" }
gcloud config set project $ProjectId | Out-Null
gcloud run deploy gigu-chat-notification-service --source services/chat-notification-service --region us-central1 --allow-unauthenticated --env-vars-file _local-gcloud-config/chat-notification-service.env.yaml

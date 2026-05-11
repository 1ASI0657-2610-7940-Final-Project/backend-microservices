param([string]$ProjectId)
if (-not $ProjectId) { $ProjectId = gcloud config get-value project }
if (-not $ProjectId -or $ProjectId -eq "(unset)") { throw "Set PROJECT_ID or pass -ProjectId" }

& "$PSScriptRoot/deploy-access-profile-service.ps1" -ProjectId $ProjectId
& "$PSScriptRoot/deploy-gig-marketplace-service.ps1" -ProjectId $ProjectId
& "$PSScriptRoot/deploy-chat-notification-service.ps1" -ProjectId $ProjectId

$accessUrl = gcloud run services describe gigu-access-profile-service --region us-central1 --format="value(status.url)"
$marketUrl = gcloud run services describe gigu-gig-marketplace-service --region us-central1 --format="value(status.url)"
$chatUrl = gcloud run services describe gigu-chat-notification-service --region us-central1 --format="value(status.url)"
Write-Host "Update _local-gcloud-config/pulls-service.env.yaml with:"
Write-Host "ACCESS_PROFILE_SERVICE_URL: $accessUrl"
Write-Host "GIG_MARKETPLACE_SERVICE_URL: $marketUrl"
Write-Host "CHAT_NOTIFICATION_SERVICE_URL: $chatUrl"

& "$PSScriptRoot/deploy-pulls-service.ps1" -ProjectId $ProjectId

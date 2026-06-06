param(
    [string]$ProjectId = 'dosys-rest-api',
    [string]$Region = 'us-central1',
    [string]$ServiceName = 'gigu-chat-notification-service',
    [string]$TopicName = 'gigu-chat-events'
)

$ErrorActionPreference = 'Stop'

function Assert-GCloudAuth {
    $account = (gcloud auth list --filter=status:ACTIVE --format="value(account)").Trim()
    if (-not $account) {
        throw 'No hay sesion activa en gcloud. Ejecuta: gcloud auth login'
    }
    Write-Host "Cuenta activa de gcloud: $account"
}

function Get-ProjectNumber([string]$projectId) {
    return (gcloud projects describe $projectId --format="value(projectNumber)").Trim()
}

Assert-GCloudAuth
gcloud config set project $ProjectId | Out-Null
gcloud services enable pubsub.googleapis.com --project $ProjectId | Out-Null

try {
    $existingTopic = (gcloud pubsub topics describe $TopicName --project $ProjectId --format="value(name)").Trim()
} catch {
    $existingTopic = ''
}

if (-not $existingTopic) {
    gcloud pubsub topics create $TopicName --project $ProjectId | Out-Null
    Write-Host "Topic creado: $TopicName"
} else {
    Write-Host "Topic existente: $TopicName"
}

$serviceAccount = (gcloud run services describe $ServiceName --region $Region --project $ProjectId --format="value(spec.template.spec.serviceAccountName)").Trim()
if (-not $serviceAccount) {
    $projectNumber = Get-ProjectNumber $ProjectId
    $serviceAccount = "$projectNumber-compute@developer.gserviceaccount.com"
}

gcloud projects add-iam-policy-binding $ProjectId `
  --member="serviceAccount:$serviceAccount" `
  --role="roles/pubsub.publisher" | Out-Null

Write-Host "Permiso roles/pubsub.publisher aplicado a: $serviceAccount"

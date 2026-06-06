param(
    [string]$ProjectId = 'dosys-rest-api',
    [string]$Region = 'us-central1',
    [string]$ServiceName = 'gigu-chat-notification-service',
    [string]$TopicName = 'gigu-chat-events',
    [string]$SubscriptionName = 'gigu-chat-events-push-chat'
)

$ErrorActionPreference = 'Stop'

$tokenFile = '_local-gcloud-config/pubsub-push-token.txt'

function Assert-GCloudAuth {
    $account = (gcloud auth list --filter=status:ACTIVE --format="value(account)").Trim()
    if (-not $account) {
        throw 'No hay sesion activa en gcloud. Ejecuta: gcloud auth login'
    }
    Write-Host "Cuenta activa de gcloud: $account"
}

Assert-GCloudAuth
gcloud config set project $ProjectId | Out-Null

if (-not (Test-Path $tokenFile)) {
    throw "No existe el token local: $tokenFile"
}

$token = (Get-Content $tokenFile -Raw).Trim()
if (-not $token) {
    throw "El archivo $tokenFile esta vacio"
}

$serviceUrl = (gcloud run services describe $ServiceName --region $Region --project $ProjectId --format="value(status.url)").Trim()
if (-not $serviceUrl) {
    throw "No pude resolver la URL del servicio $ServiceName"
}

$pushEndpoint = "$serviceUrl/internal/pubsub/chat-events?token=$token"

try {
    $existingSubscription = (gcloud pubsub subscriptions describe $SubscriptionName --project $ProjectId --format="value(name)").Trim()
} catch {
    $existingSubscription = ''
}

if (-not $existingSubscription) {
    gcloud pubsub subscriptions create $SubscriptionName `
      --project $ProjectId `
      --topic $TopicName `
      --push-endpoint $pushEndpoint | Out-Null
    Write-Host "Subscription creada: $SubscriptionName"
} else {
    gcloud pubsub subscriptions update $SubscriptionName `
      --project $ProjectId `
      --push-endpoint $pushEndpoint | Out-Null
    Write-Host "Subscription actualizada: $SubscriptionName"
}

$messageId = [guid]::NewGuid().ToString()
$conversationId = [guid]::NewGuid().ToString()
$senderId = [guid]::NewGuid().ToString()
$receiverId = [guid]::NewGuid().ToString()
$occurredAt = (Get-Date).ToUniversalTime().ToString("o")

$payload = @{
    eventType = 'ChatMessageCreated'
    messageId = $messageId
    conversationId = $conversationId
    senderId = $senderId
    receiverId = $receiverId
    content = "Pub/Sub manual test at $occurredAt"
    contentPreview = "Pub/Sub manual test at $occurredAt"
    occurredAt = $occurredAt
    metadata = @{
        resourceType = 'CONVERSATION'
        origin = 'manual-test'
    }
} | ConvertTo-Json -Depth 8 -Compress

gcloud pubsub topics publish $TopicName --project $ProjectId --message $payload | Out-Null

Write-Host "Mensaje de prueba publicado en $TopicName"
Write-Host "Push endpoint: $pushEndpoint"
Write-Host "Revisa logs del chat service y el websocket en el frontend."

param(
    [string]$ProjectId = 'dosys-rest-api',
    [string]$Region = 'us-central1',
    [string]$ServiceName = 'gigu-chat-notification-service'
)

$ErrorActionPreference = 'Stop'

$serviceRoot = 'services/chat-notification-service'
$envFile = '_local-gcloud-config/chat-cloud.env.yaml'

function Assert-GCloudAuth {
    $account = (gcloud auth list --filter=status:ACTIVE --format="value(account)").Trim()
    if (-not $account) {
        throw 'No hay sesion activa en gcloud. Ejecuta: gcloud auth login'
    }
    Write-Host "Cuenta activa de gcloud: $account"
}

if (-not (Test-Path $serviceRoot)) { throw "No existe el directorio del servicio: $serviceRoot" }
if (-not (Test-Path $envFile)) { throw "No existe archivo de variables: $envFile (usa _local-gcloud-config/*.example como plantilla)" }

Assert-GCloudAuth

gcloud config set project $ProjectId | Out-Null

gcloud run deploy $ServiceName `
  --project $ProjectId `
  --region $Region `
  --source $serviceRoot `
  --allow-unauthenticated `
  --env-vars-file $envFile

$url = gcloud run services describe $ServiceName --region $Region --project $ProjectId --format="value(status.url)"
$serviceAccount = gcloud run services describe $ServiceName --region $Region --project $ProjectId --format="value(spec.template.spec.serviceAccountName)"
Write-Host "Cloud Run URL ($ServiceName): $url"
Write-Host "Service account ($ServiceName): $serviceAccount"

param(
    [string]$ProjectId = 'dosys-rest-api',
    [string]$Region = 'us-central1'
)

$ErrorActionPreference = 'Stop'

$serviceName = 'gigu-access-profile-service'
$serviceRoot = 'services/access-profile-service'
$envFile = '_local-gcloud-config/access-profile-service.env.yaml'

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

gcloud run deploy $serviceName `
  --project $ProjectId `
  --region $Region `
  --source $serviceRoot `
  --allow-unauthenticated `
  --env-vars-file $envFile

$url = gcloud run services describe $serviceName --region $Region --project $ProjectId --format="value(status.url)"
Write-Host "Cloud Run URL ($serviceName): $url"

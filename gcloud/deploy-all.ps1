param(
    [string]$ProjectId = 'dosys-rest-api',
    [string]$Region = 'us-central1'
)

$ErrorActionPreference = 'Stop'

& "$PSScriptRoot/deploy-access-profile-service.ps1" -ProjectId $ProjectId -Region $Region
& "$PSScriptRoot/deploy-gig-marketplace-service.ps1" -ProjectId $ProjectId -Region $Region
& "$PSScriptRoot/deploy-chat-notification-service.ps1" -ProjectId $ProjectId -Region $Region
& "$PSScriptRoot/deploy-pulls-service.ps1" -ProjectId $ProjectId -Region $Region

Write-Host 'Deploy manual completado para los 4 microservicios.'

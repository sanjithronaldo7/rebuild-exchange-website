# Run ReBuild Exchange on http://localhost:8080
$ErrorActionPreference = 'Stop'
$backendDir = Join-Path $PSScriptRoot "rebuild-exchange-backend"
Write-Host "Starting ReBuild Exchange on http://localhost:8080..." -ForegroundColor Cyan
Set-Location $backendDir
java -jar target/rebuild-exchange-backend-1.0.0-SNAPSHOT.jar


# Obtener la raíz del repositorio
$repoPath = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
$envFile = Join-Path $repoPath "backend\.env"

if (!(Test-Path $envFile)) {
    throw "No se encuentra backend\.env"
}

# Cargar variables de entorno
Get-Content $envFile | ForEach-Object {
    $line = $_.Trim()

    if ($line -and !$line.StartsWith("#")) {
        $parts = $line -split "=", 2

        if ($parts.Count -eq 2) {
            $key = $parts[0].Trim()
            $value = $parts[1].Trim().Trim('"').Trim("'")

            [Environment]::SetEnvironmentVariable(
                $key,
                $value,
                "Process"
            )
        }
    }
}

if (!$env:SONAR_TOKEN) {
    throw "No se ha encontrado SONAR_TOKEN en backend\.env"
}

# Evitar que los procesos Node hereden el depurador de VS Code
$env:NODE_OPTIONS = $null

# Situarse en frontend
$frontendPath = $PSScriptRoot | Split-Path -Parent
Set-Location $frontendPath

# ==========================================
# 1. TESTS Y COBERTURA
# ==========================================

Write-Host "`n=== FRONTEND: EJECUTANDO TESTS CON COBERTURA ===" -ForegroundColor Cyan

npm run test:coverage -- --coverage.reportOnFailure
$testExitCode = $LASTEXITCODE

if ($testExitCode -eq 0) {
    Write-Host "`nTests superados correctamente." -ForegroundColor Green
}
else {
    Write-Host "`nHay tests fallidos. Se continuará con SonarQube." -ForegroundColor Yellow
}

# Comprobar que existe el informe de cobertura
$coverageFile = Join-Path $frontendPath "coverage\lcov.info"

if (Test-Path $coverageFile) {
    Write-Host "Informe de cobertura encontrado." -ForegroundColor Green
}
else {
    Write-Host "AVISO: No se ha encontrado coverage\lcov.info." -ForegroundColor Yellow
    Write-Host "SonarQube se ejecutará, pero podría no importar la cobertura."
}

# ==========================================
# 2. SONARQUBE
# ==========================================

Write-Host "`n=== FRONTEND: EJECUTANDO SONARQUBE ===" -ForegroundColor Cyan

npx @sonar/scan
$sonarExitCode = $LASTEXITCODE

if ($sonarExitCode -eq 0) {
    Write-Host "`nAnálisis de SonarQube completado." -ForegroundColor Green
}
else {
    Write-Host "`nEl análisis de SonarQube ha fallado." -ForegroundColor Red
}

# ==========================================
# 3. RESULTADO FINAL
# ==========================================

Write-Host "`n========== RESULTADO FINAL ==========" -ForegroundColor Cyan

if ($testExitCode -ne 0) {
    Write-Host "Tests: FALLIDOS" -ForegroundColor Red
}
else {
    Write-Host "Tests: CORRECTOS" -ForegroundColor Green
}

if ($sonarExitCode -ne 0) {
    Write-Host "SonarQube: FALLIDO" -ForegroundColor Red
}
else {
    Write-Host "SonarQube: CORRECTO" -ForegroundColor Green
}

if ($testExitCode -ne 0 -or $sonarExitCode -ne 0) {
    Write-Host "`nEl proceso ha terminado con errores." -ForegroundColor Yellow
    exit 1
}

Write-Host "`nProceso completado correctamente." -ForegroundColor Green
exit 0

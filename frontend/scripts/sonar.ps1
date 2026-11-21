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

# Situarse en frontend
$frontendPath = $PSScriptRoot | Split-Path -Parent
Set-Location $frontendPath

Write-Host "`n=== FRONTEND: EJECUTANDO TESTS CON COBERTURA ===" -ForegroundColor Cyan

npm run test:coverage

if ($LASTEXITCODE -ne 0) {
    throw "Los tests del frontend han fallado."
}

Write-Host "`n=== FRONTEND: EJECUTANDO SONARQUBE ===" -ForegroundColor Cyan

npx @sonar/scan

exit $LASTEXITCODE
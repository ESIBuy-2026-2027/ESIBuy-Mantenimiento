$envFile = Join-Path $PSScriptRoot "..\.env"

if (!(Test-Path $envFile)) {
    throw "No se encuentra el archivo .env"
}

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
    throw "No se ha encontrado SONAR_TOKEN en .env"
}

.\mvnw.cmd org.sonarsource.scanner.maven:sonar-maven-plugin:sonar `
    "-Dsonar.host.url=http://seralu4.esi.uclm.es:1521/" `
    "-Dsonar.projectKey=2026-g02-backend" `
    "-Dsonar.token=$env:SONAR_TOKEN" `
    "-Dsonar.exclusions=**/test/**,**/tests/**,**/*Test.java,**/*Tests.java,**/*TestCase.java" `
    "-Dsonar.test.exclusions=**"
    
exit $LASTEXITCODE
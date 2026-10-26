$ErrorActionPreference = 'Stop'
$repoPath = Split-Path $PSScriptRoot -Parent
Set-Location -LiteralPath $repoPath
if (!(Test-Path -LiteralPath '.local/application-local.properties')) { throw 'Run scripts/setup-sqlserver.ps1 first.' }
if (Get-NetTCPConnection -State Listen -LocalPort 8080 -ErrorAction SilentlyContinue) {
    throw 'Port 8080 is in use. Stop the running StarShop server before starting another instance.'
}
& mvn -q -DskipTests package
if ($LASTEXITCODE -ne 0) { throw 'Build failed.' }
Copy-Item -LiteralPath target/starshop-1.0.war -Destination .local/starshop-runtime.war -Force
& java '-Dspring.devtools.restart.enabled=false' '-Djavax.net.ssl.trustStoreType=Windows-ROOT' '-Djavax.net.ssl.trustStore=NONE' -jar .local/starshop-runtime.war '--spring.config.additional-location=file:./.local/application-local.properties'

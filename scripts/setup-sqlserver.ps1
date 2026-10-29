param([string]$Server = 'tcp:127.0.0.1,1433')
$ErrorActionPreference = 'Stop'
if ($Server -notmatch '^(tcp:)?[A-Za-z0-9_.-]+,[0-9]+$') { throw 'Use SQL Server host,port (for example tcp:127.0.0.1,1433).' }
$jdbcServer = ($Server -replace '^tcp:', '') -replace ',', ':'
$repoPath = Split-Path $PSScriptRoot -Parent
$localPath = Join-Path $repoPath '.local'
New-Item -ItemType Directory -Force -Path $localPath | Out-Null
$settingsPath = Join-Path $localPath 'application-local.properties'
if (Test-Path -LiteralPath $settingsPath) { throw 'Local configuration already exists. Keep existing credentials; do not rerun setup.' }
Add-Type -AssemblyName System.Data
$sqlConnection = New-Object System.Data.SqlClient.SqlConnection "Server=$Server;Integrated Security=true;Encrypt=true;TrustServerCertificate=true"
$sqlConnection.Open()
try {
    $sqlCommand = $sqlConnection.CreateCommand()
    $sqlCommand.CommandText = "IF DB_ID(N'StarShopLocal') IS NULL CREATE DATABASE [StarShopLocal]"
    [void]$sqlCommand.ExecuteNonQuery()
    $sqlCommand.CommandText = "SELECT COUNT(*) FROM sys.server_principals WHERE name=N'starshop_local_app'"
    if ([int]$sqlCommand.ExecuteScalar() -ne 0) { throw 'Application login already exists; preserve its existing credentials.' }
    $randomBytes = New-Object byte[] 36
    [System.Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($randomBytes)
    $databasePassword = 'Ss9!' + [Convert]::ToBase64String($randomBytes)
    $sqlCommand.CommandText = "CREATE LOGIN [starshop_local_app] WITH PASSWORD = N'$databasePassword', CHECK_POLICY=ON; USE [StarShopLocal]; CREATE USER [starshop_local_app] FOR LOGIN [starshop_local_app]; ALTER ROLE db_owner ADD MEMBER [starshop_local_app];"
    [void]$sqlCommand.ExecuteNonQuery()
    [System.Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($randomBytes)
    $jwtKey = [Convert]::ToBase64String($randomBytes)
    $adminPassword = 'Ss9!' + [Guid]::NewGuid().ToString('N')
    @"
spring.datasource.url=jdbc:sqlserver://$jdbcServer;databaseName=StarShopLocal;encrypt=true;trustServerCertificate=true
spring.datasource.username=starshop_local_app
spring.datasource.password=$databasePassword
spring.datasource.driver-class-name=com.microsoft.sqlserver.jdbc.SQLServerDriver
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
server.address=127.0.0.1
JWT_SECRET=$jwtKey
BOOTSTRAP_ADMIN_EMAIL=admin@starshop.local
BOOTSTRAP_ADMIN_PASSWORD=$adminPassword
spring.profiles.active=demo
starshop.demo-catalog.enabled=true
starshop.demo-catalog.owner-email=admin@starshop.local
"@ | Set-Content -LiteralPath $settingsPath -Encoding utf8
    $acl = Get-Acl -LiteralPath $settingsPath
    $acl.SetAccessRuleProtection($true, $false)
    $rule = New-Object System.Security.AccessControl.FileSystemAccessRule([System.Security.Principal.WindowsIdentity]::GetCurrent().Name, 'FullControl', 'Allow')
    $acl.SetAccessRule($rule)
    Set-Acl -LiteralPath $settingsPath -AclObject $acl
    Add-Content -LiteralPath $settingsPath -Value 'spring.config.import=optional:file:./.local/services.properties'
    Write-Output 'Created StarShopLocal and private .local/application-local.properties.'
} finally { $sqlConnection.Dispose() }

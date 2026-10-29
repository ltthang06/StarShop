$ErrorActionPreference='Stop'
$repoPath=Split-Path $PSScriptRoot -Parent
$settingsPath=Join-Path $repoPath '.local/application-local.properties'
$showcasePath=Join-Path $repoPath '.local/showcase.properties'
$accountsPath=Join-Path $repoPath '.local/accounts.md'
if(!(Test-Path -LiteralPath $settingsPath)) { throw 'Run setup-sqlserver.ps1 first.' }
if(Test-Path -LiteralPath $showcasePath) { throw 'Showcase settings already exist; preserve existing account passwords.' }
$passwordBytes=New-Object byte[] 24
[System.Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($passwordBytes)
$accountPassword='Ss9!'+[Convert]::ToBase64String($passwordBytes)
@"
starshop.showcase.enabled=true
starshop.showcase.orders-enabled=true
starshop.showcase.password=$accountPassword
"@ | Set-Content -LiteralPath $showcasePath -Encoding utf8
@"
# Tài khoản StarShop trên máy

Mật khẩu cho các tài khoản dưới đây: $accountPassword

| Vai trò | Email |
|---|---|
| User | customer@starshop.local |
| Vendor Võ Văn Ngân | vendor.vovanngan@starshop.local |
| Vendor Lê Văn Việt | vendor.leuvanviet@starshop.local |
| Vendor Nguyễn Gia Trí | vendor.nguyengiati@starshop.local |
| Vendor Xô Viết Nghệ Tĩnh | vendor.xovietnghetinh@starshop.local |
| Manager 1 | manager1@starshop.local |
| Manager 2 | manager2@starshop.local |
| Shipper | shipper@starshop.local |

Admin: admin@starshop.local, xem mật khẩu trong application-local.properties.
Chỉ dùng các tài khoản này trên môi trường local. Không đưa file này lên Git.
"@ | Set-Content -LiteralPath $accountsPath -Encoding utf8
$settings=Get-Content -LiteralPath $settingsPath
$importValue='spring.config.import=optional:file:./.local/services.properties,optional:file:./.local/showcase.properties'
if($settings | Where-Object { $_.StartsWith('spring.config.import=') }) {
    $settings=$settings | ForEach-Object { if($_.StartsWith('spring.config.import=')) { $importValue } else { $_ } }
} else { $settings+= $importValue }
$settings | Set-Content -LiteralPath $settingsPath -Encoding utf8
foreach($path in @($showcasePath,$accountsPath)) {
    $acl=Get-Acl -LiteralPath $path
    $acl.SetAccessRuleProtection($true,$false)
    $rule=New-Object System.Security.AccessControl.FileSystemAccessRule([System.Security.Principal.WindowsIdentity]::GetCurrent().Name,'FullControl','Allow')
    $acl.SetAccessRule($rule)
    Set-Acl -LiteralPath $path -AclObject $acl
}
Write-Output 'Prepared private showcase settings and .local/accounts.md. Start StarShop to populate SQL Server.'

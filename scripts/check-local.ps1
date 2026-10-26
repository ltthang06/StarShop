param([string]$RootUrl = 'http://127.0.0.1:8080')
$ErrorActionPreference='Stop'
$repoPath=Split-Path $PSScriptRoot -Parent
$settings=@{}
Get-Content -LiteralPath (Join-Path $repoPath '.local/application-local.properties') | ForEach-Object {
    if ($_ -match '^([^#=]+)=(.*)$') { $settings[$matches[1]]=$matches[2] }
}
$login=Invoke-WebRequest "$RootUrl/login" -SessionVariable appSession
$csrf=[regex]::Match($login.Content,'name="_csrf"[^>]*value="([^"]+)"')
if (!$csrf.Success) { throw 'Login CSRF token missing.' }
$signedIn=Invoke-WebRequest "$RootUrl/login" -Method Post -WebSession $appSession -Body @{
    email=$settings['BOOTSTRAP_ADMIN_EMAIL']; password=$settings['BOOTSTRAP_ADMIN_PASSWORD']; _csrf=$csrf.Groups[1].Value
}
if ($signedIn.Content -match 'name="password"') { throw 'Admin login failed.' }
foreach ($route in @('/','/products','/products?sort=price-asc','/products/top','/profile','/cart','/orders','/wishlist','/recently-viewed','/manager','/manager/users','/manager/shops','/manager/products','/manager/orders','/manager/reports','/manager/promotions','/manager/shipping-providers','/vendor/dashboard','/vendor/shops','/shipper/orders','/notifications')) {
    $response=Invoke-WebRequest "$RootUrl$route" -WebSession $appSession
    if ($response.StatusCode -ne 200 -or $response.Content -match 'Whitelabel Error Page|JasperException') { throw "Page failed: $route" }
    Write-Output "OK $route"
}
foreach ($listRoute in @('/manager/shops','/manager/products')) {
    $listing=Invoke-WebRequest "$RootUrl$listRoute" -WebSession $appSession
    $detailLink=[regex]::Match($listing.Content,[regex]::Escape($listRoute)+'/[0-9]+')
    if (!$detailLink.Success) { throw "No detail link found: $listRoute" }
    $detail=Invoke-WebRequest ($RootUrl+$detailLink.Value) -WebSession $appSession
    if ($detail.StatusCode -ne 200) { throw 'Detail page did not render.' }
    Write-Output "OK $($detailLink.Value)"
}
Write-Output 'Local SQL Server app: admin login and JSP routes passed. No orders were created.'

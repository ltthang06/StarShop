param([string]$RootUrl='http://127.0.0.1:8080')
$ErrorActionPreference='Stop'
$repoPath=Split-Path $PSScriptRoot -Parent
$passwordLine=Get-Content -LiteralPath (Join-Path $repoPath '.local/showcase.properties') | Where-Object { $_.StartsWith('starshop.showcase.password=') }
if(!$passwordLine) { throw 'Showcase account configuration missing.' }
$accountPassword=$passwordLine.Substring('starshop.showcase.password='.Length)
$routes=@{
    'customer@starshop.local'=@('/profile','/orders','/cart','/wishlist','/recently-viewed')
    'vendor.thuduc@starshop.local'=@('/vendor/dashboard','/vendor/shops','/orders')
    'vendor.binhthanh@starshop.local'=@('/vendor/dashboard','/vendor/shops')
    'manager.thuduc@starshop.local'=@('/manager','/manager/shops','/manager/products','/manager/orders','/manager/reports')
    'manager.binhthanh@starshop.local'=@('/manager','/manager/shops','/manager/orders')
    'shipper@starshop.local'=@('/shipper/orders','/notifications')
}
foreach($email in $routes.Keys) {
    $login=Invoke-WebRequest "$RootUrl/login" -SessionVariable roleSession
    $csrf=[regex]::Match($login.Content,'name="_csrf"[^>]*value="([^"]+)"')
    if(!$csrf.Success) { throw 'CSRF token missing.' }
    $signedIn=Invoke-WebRequest "$RootUrl/login" -Method Post -WebSession $roleSession -Body @{email=$email;password=$accountPassword;_csrf=$csrf.Groups[1].Value}
    if($signedIn.Content -match 'name="password"') { throw "Login failed for $email" }
    foreach($route in $routes[$email]) {
        $page=Invoke-WebRequest "$RootUrl$route" -WebSession $roleSession
        if($page.StatusCode -ne 200 -or $page.Content -match 'Whitelabel Error Page|JasperException') { throw "Page failed for $email $route" }
        Write-Output "OK $email $route"
    }
    if($email -eq 'customer@starshop.local' -or $email -eq 'shipper@starshop.local') {
        $denied=Invoke-WebRequest "$RootUrl/manager" -WebSession $roleSession -SkipHttpErrorCheck
        if($denied.StatusCode -ne 403) { throw "Role boundary failed for $email" }
    }
    if($email.StartsWith('manager.')) {
        $shopPage=Invoke-WebRequest "$RootUrl/manager/shops" -WebSession $roleSession
        if($email.StartsWith('manager.thuduc') -and $shopPage.Content.Contains('StarShop Bình Thạnh')) { throw 'Manager Thủ Đức sees another branch.' }
        if($email.StartsWith('manager.binhthanh') -and $shopPage.Content.Contains('StarShop Thủ Đức')) { throw 'Manager Bình Thạnh sees another branch.' }
    }
}
Write-Output 'All local role accounts signed in and rendered their pages; basic role and branch boundaries passed.'

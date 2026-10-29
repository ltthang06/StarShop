param([string]$RootUrl='http://127.0.0.1:8080')
$ErrorActionPreference='Stop'
$repoPath=Split-Path $PSScriptRoot -Parent
$passwordLine=Get-Content -LiteralPath (Join-Path $repoPath '.local/showcase.properties') | Where-Object { $_.StartsWith('starshop.showcase.password=') }
if(!$passwordLine) { throw 'Showcase account configuration missing.' }
$accountPassword=$passwordLine.Substring('starshop.showcase.password='.Length)
$routes=@{
    'customer@starshop.local'=@('/profile','/orders','/cart','/wishlist','/recently-viewed')
    'vendor.vovanngan@starshop.local'=@('/vendor/dashboard','/vendor/shops','/orders')
    'vendor.leuvanviet@starshop.local'=@('/vendor/dashboard','/vendor/shops','/orders')
    'vendor.nguyengiati@starshop.local'=@('/vendor/dashboard','/vendor/shops')
    'vendor.xovietnghetinh@starshop.local'=@('/vendor/dashboard','/vendor/shops')
    'manager1@starshop.local'=@('/manager','/manager/shops','/manager/products','/manager/orders','/manager/reports')
    'manager2@starshop.local'=@('/manager','/manager/shops','/manager/products','/manager/orders','/manager/reports')
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
    if($email.StartsWith('manager')) {
        $shopPage=Invoke-WebRequest "$RootUrl/manager/shops" -WebSession $roleSession
        if($email -eq 'manager1@starshop.local') {
            if($shopPage.Content.Contains('StarShop Nguyễn Gia Trí')) { throw 'Manager Thủ Đức sees a Bình Thạnh branch.' }
            if(!$shopPage.Content.Contains('StarShop Võ Văn Ngân')) { throw 'Manager 1 cannot see the Võ Văn Ngân branch.' }
            if(!$shopPage.Content.Contains('StarShop Lê Văn Việt')) { throw 'Manager Thủ Đức cannot see the second Thủ Đức branch.' }
        }
        if($email -eq 'manager2@starshop.local') {
            if($shopPage.Content.Contains('StarShop Võ Văn Ngân')) { throw 'Manager Bình Thạnh sees a Thủ Đức branch.' }
            if(!$shopPage.Content.Contains('StarShop Nguyễn Gia Trí')) { throw 'Manager 2 cannot see the Nguyễn Gia Trí branch.' }
            if(!$shopPage.Content.Contains('StarShop Xô Viết Nghệ Tĩnh')) { throw 'Manager Bình Thạnh cannot see the second Bình Thạnh branch.' }
        }
    }
    if($email.StartsWith('vendor.')) {
        $vendorPage=Invoke-WebRequest "$RootUrl/vendor/shops" -WebSession $roleSession
        $vendorBranch=switch($email) {
            'vendor.vovanngan@starshop.local' { 'StarShop Võ Văn Ngân' }
            'vendor.leuvanviet@starshop.local' { 'StarShop Lê Văn Việt' }
            'vendor.nguyengiati@starshop.local' { 'StarShop Nguyễn Gia Trí' }
            'vendor.xovietnghetinh@starshop.local' { 'StarShop Xô Viết Nghệ Tĩnh' }
        }
        if(!$vendorPage.Content.Contains($vendorBranch)) { throw "$email cannot see its assigned shop." }
    }
}
Write-Output 'All local role accounts signed in and rendered their pages; basic role and branch boundaries passed.'

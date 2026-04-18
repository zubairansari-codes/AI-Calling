$ErrorActionPreference = "Stop"
$loginBody = '{"email":"e2e@gasagency.com","password":"testpassword"}'
$login = Invoke-RestMethod -Uri 'http://localhost:8080/auth/login' -Method POST -ContentType 'application/json' -Body $loginBody
$headers = @{Authorization = "Bearer $($login.token)"}

try {
    $putBody = '{"name":"Testing","city":"City","agentName":"Raju","transferNumber":"9876543210"}'
    $res = Invoke-RestMethod -Uri 'http://localhost:8080/api/agency/profile' -Method PUT -ContentType 'application/json' -Headers $headers -Body $putBody
    Write-Host "Success:" $res
} catch {
    Write-Host "Status Code:" $_.Exception.Response.StatusCode.value__
    $stream = $_.Exception.Response.GetResponseStream()
    $reader = New-Object System.IO.StreamReader($stream)
    Write-Host "Error Body:" $reader.ReadToEnd()
}

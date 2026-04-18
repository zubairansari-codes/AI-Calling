$ErrorActionPreference = "Continue"

$body = '{"agencyName":"Debug Agency","ownerName":"Debug","email":"debug@gas.com","password":"test1234"}'
$r = Invoke-RestMethod -Uri 'http://localhost:8080/auth/signup' -Method POST -ContentType 'application/json' -Body $body
$h = @{Authorization = "Bearer $($r.token)"}

Write-Host "Signup OK. Token: $($r.token.Substring(0,20))..."

# Try update with just name
Write-Host "`nTesting PUT /api/agency/profile..."
try {
    $result = Invoke-WebRequest -Uri 'http://localhost:8080/api/agency/profile' -Method PUT -ContentType 'application/json' -Headers $h -Body '{"name":"Debug Updated"}'
    Write-Host "Status: $($result.StatusCode)"
    Write-Host "Body: $($result.Content)"
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    Write-Host "Error status: $statusCode"
    $stream = $_.Exception.Response.GetResponseStream()
    $reader = New-Object System.IO.StreamReader($stream)
    $errBody = $reader.ReadToEnd()
    Write-Host "Error body: $errBody"
}

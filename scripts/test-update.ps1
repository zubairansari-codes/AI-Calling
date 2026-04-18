$ErrorActionPreference = "Continue"

$body = '{"agencyName":"Log Test","ownerName":"Test","email":"logtest@gas.com","password":"test1234"}'
$r = Invoke-RestMethod -Uri 'http://localhost:8080/auth/signup' -Method POST -ContentType 'application/json' -Body $body
$h = @{Authorization = "Bearer $($r.token)"}

# Try update and capture full error response
try {
    $result = Invoke-WebRequest -Uri 'http://localhost:8080/api/agency/profile' -Method PUT -ContentType 'application/json' -Headers $h -Body '{"name":"Updated"}'
    Write-Host "OK: $($result.Content)"
} catch {
    Write-Host "Status: $($_.Exception.Response.StatusCode.value__)"
    try {
        $stream = $_.Exception.Response.GetResponseStream()
        $reader = New-Object System.IO.StreamReader($stream)
        $errBody = $reader.ReadToEnd()
        Write-Host "Response: $errBody"
    } catch {
        Write-Host "Could not read error body: $($_.Exception.Message)"
    }
}

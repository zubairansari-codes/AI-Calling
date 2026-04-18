$loginBody = '{"email":"test@gasagency.com","password":"test1234"}'
$login = Invoke-RestMethod -Uri 'http://localhost:8080/auth/login' -Method POST -ContentType 'application/json' -Body $loginBody
$token = $login.token
$headers = @{ Authorization = "Bearer $token" }

Write-Host "Testing POST /api/agency/test-call..."
try {
    $result = Invoke-WebRequest -Uri 'http://localhost:8080/api/agency/test-call' -Method POST -Headers $headers -ContentType 'application/json'
    Write-Host "Status: $($result.StatusCode)"
    Write-Host "Body: $($result.Content)"
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    $stream = $_.Exception.Response.GetResponseStream()
    $reader = New-Object System.IO.StreamReader($stream)
    $body = $reader.ReadToEnd()
    Write-Host "Status: $statusCode"
    Write-Host "Body: $body"
}

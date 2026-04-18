$ErrorActionPreference = "Continue"

# Login with existing user
$body = '{"email":"debug@gas.com","password":"test1234"}'
$r = Invoke-RestMethod -Uri 'http://localhost:8080/auth/login' -Method POST -ContentType 'application/json' -Body $body
$h = @{Authorization = "Bearer $($r.token)"}

# Try update - with verbose error
Write-Host "Testing PUT /api/agency/profile with verbose..."
try {
    $result = Invoke-RestMethod -Uri 'http://localhost:8080/api/agency/profile' -Method PUT -ContentType 'application/json' -Headers $h -Body '{"name":"Updated Name","city":"Mumbai"}' -Verbose
    Write-Host "OK: Name=$($result.name)"
} catch {
    Write-Host "Exception: $($_.Exception.Message)"
    if ($_.Exception.Response) {
        Write-Host "Status: $($_.Exception.Response.StatusCode)"
    }
}

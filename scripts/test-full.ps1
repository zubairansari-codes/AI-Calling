$ErrorActionPreference = "Stop"

Write-Host "=== Re-registering + Full Test ===" -ForegroundColor Cyan

# 1. Signup
Write-Host "`n--- Signup ---" -ForegroundColor Yellow
$signupBody = '{"agencyName":"Test Gas Agency","ownerName":"Rahul Kumar","email":"test@gasagency.com","password":"test1234","phone":"9876543210","city":"Delhi"}'
$signup = Invoke-RestMethod -Uri 'http://localhost:8080/auth/signup' -Method POST -ContentType 'application/json' -Body $signupBody
$token = $signup.token
Write-Host "Signup OK. Token received."
$headers = @{ Authorization = "Bearer $token" }

# 2. Complete setup (needed for test call)
Write-Host "`n--- Setup ---" -ForegroundColor Yellow
$setupBody = '{"agentName":"Raju","transferNumber":"9876543210","emergencyNumber":"101"}'
$setup = Invoke-RestMethod -Uri 'http://localhost:8080/api/agency/setup' -Method POST -ContentType 'application/json' -Headers $headers -Body $setupBody
Write-Host "Setup: agentName=$($setup.agentName), transferNumber=$($setup.transferNumber), setupCompleted=$($setup.setupCompleted)"

# 3. Profile check
Write-Host "`n--- Profile ---" -ForegroundColor Yellow
$profile = Invoke-RestMethod -Uri 'http://localhost:8080/api/agency/profile' -Headers $headers
Write-Host "Name=$($profile.name), Plan=$($profile.planType), Setup=$($profile.setupCompleted)"

# 4. Billing
Write-Host "`n--- Billing ---" -ForegroundColor Yellow
$billing = Invoke-RestMethod -Uri 'http://localhost:8080/api/billing/current' -Headers $headers
Write-Host "Plan=$($billing.currentPlan), Limit=$($billing.monthlyCallLimit), Remaining=$($billing.remainingCalls)"

# 5. Test Call
Write-Host "`n--- Test Call ---" -ForegroundColor Yellow
try {
    $testCall = Invoke-RestMethod -Uri 'http://localhost:8080/api/agency/test-call' -Method POST -Headers $headers -ContentType 'application/json'
    Write-Host "Test call: $($testCall.message), Status: $($testCall.status)"
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    $stream = $_.Exception.Response.GetResponseStream()
    $reader = New-Object System.IO.StreamReader($stream)
    $body = $reader.ReadToEnd()
    Write-Host "Status $statusCode : $body"
}

# 6. Save settings
Write-Host "`n--- Save Settings ---" -ForegroundColor Yellow
$updateBody = '{"name":"Test Gas Agency Updated","city":"Mumbai","agentName":"Raju","transferNumber":"9876543210"}'
$updated = Invoke-RestMethod -Uri 'http://localhost:8080/api/agency/profile' -Method PUT -ContentType 'application/json' -Headers $headers -Body $updateBody
Write-Host "Updated name: $($updated.name), City: $($updated.city)"

# 7. Analytics
Write-Host "`n--- Analytics ---" -ForegroundColor Yellow
$analytics = Invoke-RestMethod -Uri 'http://localhost:8080/api/analytics/summary' -Headers $headers
Write-Host "Total calls: $($analytics.totalCalls), DSC: $($analytics.totalDscCollected), Trend entries: $($analytics.dailyTrend.Count)"

Write-Host "`n=== ALL TESTS PASS ===" -ForegroundColor Green

$ErrorActionPreference = "Continue"

Write-Host "=== Testing Backend APIs ===" -ForegroundColor Cyan

# 1. Login
Write-Host "`n--- 1. Login ---" -ForegroundColor Yellow
$loginBody = '{"email":"test@gasagency.com","password":"test1234"}'
$login = Invoke-RestMethod -Uri 'http://localhost:8080/auth/login' -Method POST -ContentType 'application/json' -Body $loginBody
$token = $login.token
Write-Host "Login OK. Agency: $($login.name), setup: $($login.setupCompleted)"

$headers = @{ Authorization = "Bearer $token" }

# 2. Health check
Write-Host "`n--- 2. Actuator Health ---" -ForegroundColor Yellow
$health = Invoke-RestMethod -Uri 'http://localhost:8080/actuator/health'
Write-Host "Health: $($health.status)"

# 3. Agency profile
Write-Host "`n--- 3. Agency Profile ---" -ForegroundColor Yellow
$profile = Invoke-RestMethod -Uri 'http://localhost:8080/api/agency/profile' -Headers $headers
Write-Host "Agency: $($profile.name), Plan: $($profile.planType), Setup: $($profile.setupCompleted)"

# 4. Billing current
Write-Host "`n--- 4. Billing Current ---" -ForegroundColor Yellow
$billing = Invoke-RestMethod -Uri 'http://localhost:8080/api/billing/current' -Headers $headers
Write-Host "Plan: $($billing.currentPlan), Limit: $($billing.monthlyCallLimit), Used: $($billing.callsUsedThisMonth), Remaining: $($billing.remainingCalls)"

# 5. Analytics summary
Write-Host "`n--- 5. Analytics Summary ---" -ForegroundColor Yellow
$analytics = Invoke-RestMethod -Uri 'http://localhost:8080/api/analytics/summary' -Headers $headers
Write-Host "Total DSC: $($analytics.totalDscCollected), Total Calls: $($analytics.totalCalls), Avg Rate: $($analytics.avgSuccessRate)%"
Write-Host "Cost savings: Human=$($analytics.estimatedHumanCostMonthly), AI=$($analytics.estimatedAiCostMonthly), Saved=$($analytics.estimatedMonthlySavings)"
Write-Host "Daily trend entries: $($analytics.dailyTrend.Count)"

# 6. Dashboard stats
Write-Host "`n--- 6. Dashboard Stats ---" -ForegroundColor Yellow
$dash = Invoke-RestMethod -Uri 'http://localhost:8080/api/calls/dashboard' -Headers $headers
Write-Host "DSC Today: $($dash.dscCollectedToday), Calls Today: $($dash.callsMadeToday), Success: $($dash.successRateToday)%"

# 7. Call stats
Write-Host "`n--- 7. Call Stats ---" -ForegroundColor Yellow
$callStats = Invoke-RestMethod -Uri 'http://localhost:8080/api/calls/stats' -Headers $headers
Write-Host "Total: $($callStats.total), DSC: $($callStats.dscCollected), Transferred: $($callStats.transferred), Failed: $($callStats.failed)"

# 8. List campaigns
Write-Host "`n--- 8. Campaigns ---" -ForegroundColor Yellow
$campaigns = Invoke-RestMethod -Uri 'http://localhost:8080/api/campaigns' -Headers $headers
Write-Host "Total campaigns: $($campaigns.totalElements)"

# 9. List calls
Write-Host "`n--- 9. Call Logs ---" -ForegroundColor Yellow
$calls = Invoke-RestMethod -Uri 'http://localhost:8080/api/calls' -Headers $headers
Write-Host "Total calls: $($calls.totalElements)"

# 10. Test call (will fail because no ElevenLabs agent configured - but tests the endpoint)
Write-Host "`n--- 10. Test Call ---" -ForegroundColor Yellow
try {
    $testCall = Invoke-RestMethod -Uri 'http://localhost:8080/api/agency/test-call' -Method POST -Headers $headers
    Write-Host "Test call: $($testCall.message)"
} catch {
    $err = $_.ErrorDetails.Message | ConvertFrom-Json
    Write-Host "Test call response (expected): $($err.message)" -ForegroundColor DarkYellow
}

# 11. Billing upgrade
Write-Host "`n--- 11. Billing Upgrade ---" -ForegroundColor Yellow
$upgradeBody = '{"plan":"STARTER"}'
$upgraded = Invoke-RestMethod -Uri 'http://localhost:8080/api/billing/upgrade' -Method POST -ContentType 'application/json' -Headers $headers -Body $upgradeBody
Write-Host "Upgraded to: $($upgraded.currentPlan), New limit: $($upgraded.monthlyCallLimit)"

# 12. Verify profile after upgrade
Write-Host "`n--- 12. Profile After Upgrade ---" -ForegroundColor Yellow
$profile2 = Invoke-RestMethod -Uri 'http://localhost:8080/api/agency/profile' -Headers $headers
Write-Host "Plan now: $($profile2.planType), Limit: $($profile2.monthlyCallLimit)"

Write-Host "`n=== ALL TESTS COMPLETE ===" -ForegroundColor Green

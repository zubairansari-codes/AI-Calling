$ErrorActionPreference = "Stop"
# 1. Signup
$signupBody = '{"agencyName":"Script Test","ownerName":"Script","email":"script@gasagency.com","password":"testpassword","phone":"9876543210","city":"Delhi"}'
$signup = Invoke-RestMethod -Uri 'http://localhost:8080/auth/signup' -Method POST -ContentType 'application/json' -Body $signupBody
$headers = @{ Authorization = "Bearer $($signup.token)" }

# 2. Setup (will trigger ElevenLabs API!)
$setupBody = '{"agentName":"Raju","transferNumber":"+917060912970","emergencyNumber":"101"}'
$setup = Invoke-RestMethod -Uri 'http://localhost:8080/api/agency/setup' -Method POST -ContentType 'application/json' -Headers $headers -Body $setupBody
Write-Host "Setup completed! Has ElevenLabs ID: $($setup.elevenLabsAgentId -ne $null)"

# 3. Test Call!
try {
    $testCall = Invoke-RestMethod -Uri 'http://localhost:8080/api/agency/test-call' -Method POST -Headers $headers -ContentType 'application/json'
    Write-Host "Test Call Status: $($testCall.status)"
    Write-Host "Message: $($testCall.message)"
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    $stream = $_.Exception.Response.GetResponseStream()
    $reader = New-Object System.IO.StreamReader($stream)
    Write-Host "Status Code: $statusCode"
    Write-Host "Error Body:" $reader.ReadToEnd()
}

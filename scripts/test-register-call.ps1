$ErrorActionPreference = "Stop"
$ELEVENLABS_API_KEY = "sk_5d238d45c1af06e388b06d7548606d1abb8c0f8a528663c6"
$headers = @{
    "xi-api-key" = $ELEVENLABS_API_KEY
    "Content-Type" = "application/json"
}
$body = @"
{
  "agent_id": "agent_3401kp1qas56fnsb7k6mkdmy85z4"
}
"@

try {
    $res = Invoke-RestMethod -Uri "https://api.elevenlabs.io/v1/convai/twilio/register-call" -Method Post -Headers $headers -Body $body
    Write-Host "Success:"
    $res | ConvertTo-Json
} catch {
    Write-Host "Error status:" $_.Exception.Response.StatusCode
    $stream = $_.Exception.Response.GetResponseStream()
    $reader = New-Object System.IO.StreamReader($stream)
    Write-Host "Response Body:" $reader.ReadToEnd()
}

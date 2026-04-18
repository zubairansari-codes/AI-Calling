$ErrorActionPreference = "Stop"
$URI = "https://duchesslike-unlibellously-lovetta.ngrok-free.dev/webhook/elevenlabs?callSid=TEST_SID_MOCK"
$headers = @{ "Content-Type" = "application/json" }
$body = @"
{
  "tool_call_id": "call_abc123",
  "tool_name": "submit_dsc",
  "parameters": {
    "status": "dsc_collected",
    "dscNumber": "9876"
  }
}
"@

try {
    $res = Invoke-RestMethod -Uri $URI -Method Post -Headers $headers -Body $body
    Write-Host "Webhook Test Call Success:" $res
} catch {
    Write-Host "Error status:" $_.Exception.Response.StatusCode
    $stream = $_.Exception.Response.GetResponseStream()
    $reader = New-Object System.IO.StreamReader($stream)
    Write-Host "Response Body:" $reader.ReadToEnd()
}

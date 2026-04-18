$ErrorActionPreference = "Stop"
$body = @'
{
  "name": "Gas Agency Prod - DSC Agent",
  "conversation_config": {
    "agent": {
      "prompt": {
        "prompt": "Test sys prompt",
        "llm": "gpt-4o-mini",
        "tools": [
            {
                "type": "webhook",
                "name": "submit_dsc",
                "description": "Call this tool IMMEDIATELY",
                "api_schema": {
                    "url": "https://example.com",
                    "method": "POST",
                    "request_body_schema": {
                        "type": "object",
                        "properties": {
                            "status": {"type": "string", "description": "Must be 'dsc_collected'"},
                            "dscNumber": {"type": "string", "description": "The 4-digit code provided by the customer"}
                        },
                        "required": ["status", "dscNumber"]
                    }
                }
            }
        ]
      },
      "first_message": "Namaste ji",
      "language": "hi"
    },
    "tts": {
        "model_id": "eleven_turbo_v2_5"
    }
  }
}
'@

try {
    $res = Invoke-WebRequest -Uri "https://api.elevenlabs.io/v1/convai/agents/create" -Method Post -Headers @{ "xi-api-key"="sk_5d238d45c1af06e388b06d7548606d1abb8c0f8a528663c6"; "Content-Type"="application/json" } -Body $body
    Write-Host "Success!"
} catch {
    Write-Host "Status:" $_.Exception.Response.StatusCode
    Write-Host "Error string:" $_.ErrorDetails.Message
}

const https = require('https');
const API_KEY = 'sk_3adcbb8845aaf63c3febd6eede5b394014a2c9ba468b34d9';

// Try creating a test agent with GLM-4.5-Air to confirm the model ID
const payload = JSON.stringify({
  name: "model-test-delete-me",
  conversation_config: {
    agent: {
      prompt: {
        prompt: "test",
        llm: "glm-4.5-air",  // try this ID
        temperature: 0.1,
        tools: []
      },
      first_message: "test",
      language: "hi"
    },
    tts: { model_id: "eleven_flash_v2_5", voice_id: "3cedsbr7ryRZwBvoUmQZ" }
  }
});

const req = https.request({
  hostname: 'api.elevenlabs.io',
  path: '/v1/convai/agents/create',
  method: 'POST',
  headers: {
    'xi-api-key': API_KEY,
    'Content-Type': 'application/json',
    'Content-Length': Buffer.byteLength(payload)
  }
}, res => {
  let data = '';
  res.on('data', d => data += d);
  res.on('end', () => {
    const j = JSON.parse(data);
    if (j.agent_id) {
      console.log('✅ GLM-4.5-Air works! agent_id:', j.agent_id);
      // Delete the test agent
      https.request({
        hostname: 'api.elevenlabs.io',
        path: `/v1/convai/agents/${j.agent_id}`,
        method: 'DELETE',
        headers: { 'xi-api-key': API_KEY }
      }, r => { r.resume(); console.log('Test agent deleted.'); }).end();
    } else {
      console.log('❌ Failed:', JSON.stringify(j, null, 2));
    }
  });
});
req.on('error', e => console.error('Error:', e.message));
req.write(payload);
req.end();

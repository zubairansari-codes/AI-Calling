const https = require('https');
const API_KEY = 'sk_3adcbb8845aaf63c3febd6eede5b394014a2c9ba468b34d9';

// First test: create agent WITHOUT knowledge base to confirm base config works
const payload = JSON.stringify({
  name: "test-gemini-flash-delete",
  conversation_config: {
    agent: {
      prompt: {
        prompt: "test",
        llm: "gemini-2.5-flash",
        temperature: 0.1,
        tools: []
      },
      first_message: "नमस्ते जी!",
      language: "hi"
    },
    tts: { model_id: "eleven_flash_v2_5", voice_id: "3cedsbr7ryRZwBvoUmQZ" },
    turn: { turn_timeout: 4, turn_eagerness: "high" },
    conversation: { max_duration_seconds: 180 }
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
      console.log('✅ gemini-2.5-flash agent created:', j.agent_id);
      // cleanup
      https.request({ hostname:'api.elevenlabs.io', path:`/v1/convai/agents/${j.agent_id}`, method:'DELETE', headers:{'xi-api-key':API_KEY} }, r=>{r.resume();console.log('deleted')}).end();
    } else {
      console.log('❌ Error:', JSON.stringify(j, null, 2));
    }
  });
});
req.on('error', e => console.error(e.message));
req.write(payload);
req.end();

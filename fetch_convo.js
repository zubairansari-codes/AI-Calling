const https = require('https');

const API_KEY = 'sk_3adcbb8845aaf63c3febd6eede5b394014a2c9ba468b34d9';
const AGENT_ID = 'agent_0901kpzepfzvfg4sjbaa26t5anqm';
const CONV_ID  = 'conv_3001kq08tgnre97ttf5fzgb33xs4';

function get(url) {
  return new Promise((resolve, reject) => {
    https.get(url, { headers: { 'xi-api-key': API_KEY } }, res => {
      let data = '';
      res.on('data', d => data += d);
      res.on('end', () => resolve(JSON.parse(data)));
    }).on('error', reject);
  });
}

(async () => {
  // Get conversation detail
  const convo = await get(`https://api.elevenlabs.io/v1/convai/conversations/${CONV_ID}`);
  
  console.log('\n=== CONVERSATION STATUS ===');
  console.log('Status:', convo.status);
  console.log('Duration:', convo.metadata?.call_duration_secs, 'seconds');
  console.log('End reason:', convo.analysis?.call_successful);
  console.log('Termination reason:', convo.metadata?.termination_reason);

  console.log('\n=== TRANSCRIPT ===');
  if (convo.transcript) {
    convo.transcript.forEach(t => {
      console.log(`[${t.role.toUpperCase()}]: ${t.message}`);
    });
  } else {
    console.log('No transcript yet');
  }

  console.log('\n=== TOOL CALLS ===');
  if (convo.conversation_initiation_client_data) {
    console.log(JSON.stringify(convo.conversation_initiation_client_data, null, 2));
  }
  
  console.log('\n=== FULL ANALYSIS ===');
  console.log(JSON.stringify(convo.analysis, null, 2));
  
  console.log('\n=== RAW STATUS ===');
  console.log('conversation_id:', convo.conversation_id);
  console.log('agent_id:', convo.agent_id);
})();

const http = require('http');

function post(path, body, token) {
  return new Promise((resolve, reject) => {
    const data = JSON.stringify(body);
    const headers = { 'Content-Type': 'application/json' };
    if (token) headers['Authorization'] = `Bearer ${token}`;
    
    const req = http.request({ hostname: 'localhost', port: 8080, path, method: 'POST', headers }, (res) => {
      let body = '';
      res.on('data', chunk => body += chunk);
      res.on('end', () => {
        console.log(`${path} -> ${res.statusCode}: ${body}`);
        try { resolve(JSON.parse(body)); } catch { resolve(body); }
      });
    });
    req.on('error', reject);
    req.write(data);
    req.end();
  });
}

function postMultipart(path, campaignJson, csvContent, token) {
  return new Promise((resolve, reject) => {
    const boundary = '----FormBoundary' + Date.now();
    
    let body = '';
    // Campaign part
    body += `--${boundary}\r\n`;
    body += `Content-Disposition: form-data; name="campaign"\r\n`;
    body += `Content-Type: application/json\r\n\r\n`;
    body += JSON.stringify(campaignJson) + '\r\n';
    // File part
    body += `--${boundary}\r\n`;
    body += `Content-Disposition: form-data; name="file"; filename="customers.csv"\r\n`;
    body += `Content-Type: text/csv\r\n\r\n`;
    body += csvContent + '\r\n';
    body += `--${boundary}--\r\n`;
    
    const headers = {
      'Content-Type': `multipart/form-data; boundary=${boundary}`,
      'Authorization': `Bearer ${token}`
    };
    
    const req = http.request({ hostname: 'localhost', port: 8080, path, method: 'POST', headers }, (res) => {
      let respBody = '';
      res.on('data', chunk => respBody += chunk);
      res.on('end', () => {
        console.log(`${path} -> ${res.statusCode}: ${respBody}`);
        try { resolve(JSON.parse(respBody)); } catch { resolve(respBody); }
      });
    });
    req.on('error', reject);
    req.write(body);
    req.end();
  });
}

async function main() {
  try {
    // 1. Signup
    console.log('\n=== STEP 1: SIGNUP ===');
    const email = `final-${Date.now()}@test.com`;
    const loginRes = await post('/auth/signup', { agencyName: 'Live Test Agency', ownerName: 'Test', email: email, phone: '7060912970', password: 'Password123' });
    const token = loginRes.token;
    if (!token) { console.error('Login failed!', loginRes); return; }
    console.log('Token obtained:', token.substring(0, 30) + '...');

    // 2. Complete setup
    console.log('\n=== STEP 2: AGENCY SETUP ===');
    const setupRes = await post('/api/agency/setup', { agentName: 'Rahul', transferNumber: '7060912970', emergencyNumber: null }, token);
    
    // 3. Create campaign with CSV
    console.log('\n=== STEP 3: CREATE CAMPAIGN ===');
    const csv = 'name,phone\nRahul Kumar,+917060912970';
    const campaign = { name: 'Live Test Campaign', deliveryDate: new Date().toISOString().split('T')[0] };
    const campRes = await postMultipart('/api/campaigns', campaign, csv, token);
    
    // 4. Start campaign
    if (campRes && campRes.id) {
      console.log('\n=== STEP 4: START CAMPAIGN ===');
      const startRes = await post(`/api/campaigns/${campRes.id}/start`, {}, token);
      console.log('\n=== DONE! Call should be ringing! ===');
    } else {
      console.log('Campaign creation failed, cannot start');
    }
  } catch (err) {
    console.error('Error:', err);
  }
}

main();

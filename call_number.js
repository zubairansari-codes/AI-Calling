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
    body += `--${boundary}\r\n`;
    body += `Content-Disposition: form-data; name="campaign"\r\n`;
    body += `Content-Type: application/json\r\n\r\n`;
    body += JSON.stringify(campaignJson) + '\r\n';
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
    // Login with existing account
    console.log('\n=== LOGIN ===');
    const loginRes = await post('/auth/login', { email: 'final2@test.com', password: 'Password123' });
    const token = loginRes.token;
    if (!token) { console.error('Login failed!', loginRes); return; }

    // Create campaign targeting 6396330575
    console.log('\n=== CREATE CAMPAIGN ===');
    const csv = 'name,phone\nDirect Call,+916396330575';
    const campaign = { name: 'Direct Call Campaign', deliveryDate: new Date().toISOString().split('T')[0] };
    const campRes = await postMultipart('/api/campaigns', campaign, csv, token);

    // Start campaign
    if (campRes && campRes.id) {
      console.log('\n=== START CAMPAIGN ===');
      await post(`/api/campaigns/${campRes.id}/start`, {}, token);
      console.log('\n=== DONE! Call to 6396330575 should be ringing! ===');
    }
  } catch (err) {
    console.error('Error:', err);
  }
}

main();

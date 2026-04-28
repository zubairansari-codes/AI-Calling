const http = require('http');

const boundary = '----WebKitFormBoundaryfvCOokiihLeAmYNu';
const body = `--${boundary}\r\nContent-Disposition: form-data; name="campaign"; filename="blob"\r\nContent-Type: application/json\r\n\r\n{"name":"Initial AI Campaign","deliveryDate":"2026-04-22"}\r\n--${boundary}\r\nContent-Disposition: form-data; name="file"; filename="test_customers.csv"\r\nContent-Type: text/csv\r\n\r\nName,Phone\nTest,9876543210\r\n--${boundary}--\r\n`;

const token = 'eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXN0QGdtYWlsLmNvbSIsImFnZW5jeV9pZCI6Miwicm9sZSI6IkFHRU5DWSIsImlhdCI6MTc3Njg0NDM5NywiZXhwIjoxNzc2OTMwNzk3fQ.j6OqRLA2JrUNii3-iIJyHAeI9Ck5f2tAMWAmKA9O-errOZARPhAuq3KReuTR8AjCaHQc7OM-KV757IpVLCsRqQ';

const req = http.request({
  hostname: 'localhost',
  port: 8080,
  path: '/api/campaigns',
  method: 'POST',
  headers: {
    'Authorization': 'Bearer ' + token,
    'Content-Type': 'multipart/form-data; boundary=' + boundary,
    'Content-Length': Buffer.byteLength(body)
  }
}, (res) => {
  let data = '';
  res.on('data', chunk => data += chunk);
  res.on('end', () => console.log('Status:', res.statusCode, 'Body:', data));
});

req.on('error', e => console.error(e));
req.write(body);
req.end();

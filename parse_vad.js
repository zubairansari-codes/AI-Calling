const fs = require('fs');
const doc = JSON.parse(fs.readFileSync('openapi.json', 'utf8'));
const vadConfig = doc.components.schemas['VADConfig'];
console.log("VADConfig properties:", JSON.stringify(vadConfig, null, 2));

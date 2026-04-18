const fs = require('fs');
const doc = JSON.parse(fs.readFileSync('openapi.json', 'utf8'));

const turn = Object.keys(doc.components.schemas).filter(k => k.toLowerCase().includes('turn'));
console.log("Turn schemas:", turn);

const conv = doc.components.schemas['AgentConfig'];
console.log("AgentConfig:", JSON.stringify(conv, null, 2));

const turnConfig = doc.components.schemas['TurnConfig'];
console.log("TurnConfig:", JSON.stringify(turnConfig, null, 2));

const turnConfigObj = doc.components.schemas['AgentTurnConfig'];
console.log("AgentTurnConfig:", JSON.stringify(turnConfigObj, null, 2));

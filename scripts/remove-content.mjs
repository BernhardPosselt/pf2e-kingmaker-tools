// call with: node remove-content.mjs 0.0.1

import * as fs from 'fs';
import * as path from 'path';

const excludedPacks = new Set([
    'kingmaker-tools-custom-rolltables',
    'kingmaker-tools-custom-journals',
]);

const data = JSON.parse(fs.readFileSync('build/pf2e-kingmaker-tools/module.json', 'utf-8'));
excludedPacks.forEach(packName => {
    fs.rmSync(`build/pf2e-kingmaker-tools/packs/${packName}`, {recursive: true});
});
data.packs = data.packs.filter(p => !excludedPacks.has(p.name));
fs.writeFileSync('build/pf2e-kingmaker-tools/module.json', JSON.stringify(data));

async function sanitizeRollTable(packFileName, resultSanitizer) {
    const directory = `build/tmp/${packFileName}`;
    for (const fileName of fs.readdirSync(directory)) {
        const fullPath = path.join(directory, fileName);
        const value = JSON.parse(fs.readFileSync(fullPath));
        const result = resultSanitizer(value);
        fs.writeFileSync(fullPath, JSON.stringify(result));
    }
}

console.log("hi")
const sanitizeRandomEncounterRegex = /^(?<text>.*\((?:Moderate|Severe|Low|Trivial|Extreme) \d+\))(.+)?$/i;
await sanitizeRollTable('kingmaker-tools-random-encounters', (result) => {
    const match = sanitizeRandomEncounterRegex.exec(result.text);
    if (match) {
        return {...result, text: match.groups.text};
    }
    return result;
});

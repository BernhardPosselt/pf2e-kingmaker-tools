// call with: node remove-content.mjs 0.0.1

import * as fs from 'fs';
import * as readline from "readline";

const excludedPacks = new Set([
    'kingmaker-tools-custom-rolltables',
    'kingmaker-tools-custom-journals',
]);

const data = JSON.parse(fs.readFileSync('build/pf2e-kingmaker-tools/module.json', 'utf-8'));
excludedPacks.forEach(packName => {
    const config = data.packs.find(p => p.name === packName)
    fs.unlinkSync(`build/pf2e-kingmaker-tools/${config.path}`);
});
data.packs = data.packs.filter(p => !excludedPacks.has(p.name));
fs.writeFileSync('build/pf2e-kingmaker-tools/module.json', JSON.stringify(data));

async function sanitizeRollTable(packFileName, resultSanitizer) {
    const stream = readline.createInterface({
        input: fs.createReadStream(`build/pf2e-kingmaker-tools/packs/${packFileName}`),
        crlfDelay: Infinity
    });
    const sanitizedLines = [];
    for await (const line of stream) {
        const data = JSON.parse(line);
        data.results = data.results.map(result => resultSanitizer(data, result));
        sanitizedLines.push(JSON.stringify(data));
    }
    fs.writeFileSync(`build/pf2e-kingmaker-tools/packs/${packFileName}`, sanitizedLines.join('\n'));
}

const sanitizeRandomEncounterRegex = /^(?<text>.*\((?:Moderate|Severe|Low|Trivial|Extreme) \d+\))(.+)?$/i;
await sanitizeRollTable('random-encounters.db', (_, result) => {
    const match = sanitizeRandomEncounterRegex.exec(result.text);
    if (match) {
        return {...result, text: match.groups.text};
    }
    return result;
});

const sanitizeKingdomEventsRegex = /^@UUID\[.+]\{(?<name>.+)}(?<page>.+)$/i;
await sanitizeRollTable('rolltables.db', (data, result) => {
    if (data.name === 'Kingdom Events') {
        const match = sanitizeKingdomEventsRegex.exec(result.text);
        if (match) {
            return {...result, text: match.groups.name + match.groups.page};
        }
    }
    return result;
});

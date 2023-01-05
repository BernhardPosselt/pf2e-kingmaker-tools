// call with: node update-module-json.mjs 0.0.1

import {argv} from 'node:process';
import * as fs from 'fs';

const version = argv[argv.length - 1];
const data = JSON.parse(fs.readFileSync('module.json', 'utf-8'));

// update version
data.version = version;
data.download = `https://github.com/BernhardPosselt/pf2e-kingmaker-tools/releases/download/${version}/release.zip`;

fs.writeFileSync('module.json', JSON.stringify(data, null, 4));

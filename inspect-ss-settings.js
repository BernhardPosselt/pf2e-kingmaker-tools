const fs = require('fs');
const path = require('path');
const { ClassicLevel } = require('/home/node/foundry/node_modules/classic-level');

function copyDirSync(src, dest) {
  fs.mkdirSync(dest, { recursive: true });
  let entries = fs.readdirSync(src, { withFileTypes: true });

  for (let entry of entries) {
    let srcPath = path.join(src, entry.name);
    let destPath = path.join(dest, entry.name);

    if (entry.isDirectory()) {
      copyDirSync(srcPath, destPath);
    } else {
      if (entry.name === 'LOCK') continue;
      fs.copyFileSync(srcPath, destPath);
    }
  }
}

async function run() {
  const srcDir = '/home/node/.local/share/FoundryVTT/Data/worlds/kingmaker/data/settings';
  const destDir = '/tmp/settings-copy-ss';
  
  if (fs.existsSync(destDir)) {
    fs.rmSync(destDir, { recursive: true, force: true });
  }
  
  copyDirSync(srcDir, destDir);
  const db = new ClassicLevel(destDir);
  await db.open();

  console.log("=== Seasons & Stars Settings ===");
  for await (const [key, value] of db.iterator()) {
    if (key.includes('seasons-and-stars') || key.includes('seasons-stars')) {
      console.log(`Key: ${key}`);
      try {
        const data = JSON.parse(value);
        console.log("Value:", JSON.stringify(data, null, 2));
      } catch (e) {
        console.log("Raw Value:", value);
      }
      console.log("------------------------");
    }
  }
  await db.close();
  fs.rmSync(destDir, { recursive: true, force: true });
}

run().catch(console.error);

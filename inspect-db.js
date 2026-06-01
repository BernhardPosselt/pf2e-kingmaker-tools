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
  const destDir = '/tmp/settings-copy';
  
  if (fs.existsSync(destDir)) {
    fs.rmSync(destDir, { recursive: true, force: true });
  }
  
  copyDirSync(srcDir, destDir);
  const db = new ClassicLevel(destDir);
  await db.open();

  for await (const [key, value] of db.iterator()) {
    const data = JSON.parse(value);
    if (data.key === 'core.moduleConfiguration') {
      console.log("=== moduleConfiguration ===");
      console.log(data.value);
    }
  }
  await db.close();
  fs.rmSync(destDir, { recursive: true, force: true });
}

run().catch(console.error);

const fs = require('fs');
const path = require('path');
const { ClassicLevel } = require('/home/node/foundry/node_modules/classic-level');

async function run() {
  const dbDir = '/home/node/.local/share/FoundryVTT/Data/worlds/kingmaker/data/scenes';
  
  if (!fs.existsSync(dbDir)) {
    console.error(`Database directory not found: ${dbDir}`);
    return;
  }

  const db = new ClassicLevel(dbDir);
  await db.open();

  console.log("Reading scenes database...");
  let count = 0;
  let fixedCount = 0;

  for await (const [key, value] of db.iterator()) {
    const scene = JSON.parse(value);
    let modified = false;

    if (scene.drawings && Array.isArray(scene.drawings)) {
      for (let drawing of scene.drawings) {
        if (drawing.shape && drawing.shape.type === 'polygon') {
          console.log(`Found drawing in scene "${scene.name}" (${scene._id}) with shape.type "polygon". Fixing to "p".`);
          drawing.shape.type = 'p';
          modified = true;
          fixedCount++;
        }
      }
    }

    if (modified) {
      await db.put(key, JSON.stringify(scene));
      console.log(`Saved scene "${scene.name}" (${scene._id}) with fixes.`);
    }
    count++;
  }

  await db.close();
  console.log(`Completed. Checked ${count} scenes. Fixed ${fixedCount} drawings.`);
}

run().catch(console.error);

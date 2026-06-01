const { ClassicLevel } = require('/home/node/foundry/node_modules/classic-level');

async function run() {
  const dbDir = '/home/node/.local/share/FoundryVTT/Data/worlds/kingmaker/data/scenes';
  const db = new ClassicLevel(dbDir);
  await db.open();

  console.log("Fixing drawing shape types in LevelDB...");
  let checked = 0;
  let fixed = 0;

  for await (const [key, value] of db.iterator()) {
    if (key.startsWith('!scenes.drawings!')) {
      const drawing = JSON.parse(value);
      if (drawing.shape && drawing.shape.type === 'polygon') {
        console.log(`Found invalid polygon shape in drawing key: ${key}`);
        drawing.shape.type = 'p';
        await db.put(key, JSON.stringify(drawing));
        console.log(`Fixed drawing key ${key} to type "p".`);
        fixed++;
      }
      checked++;
    }
  }

  await db.close();
  console.log(`Completed. Checked ${checked} drawings. Fixed ${fixed} drawings.`);
}

run().catch(console.error);

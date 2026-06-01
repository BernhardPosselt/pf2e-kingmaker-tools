const { ClassicLevel } = require('/home/node/foundry/node_modules/classic-level');

async function run() {
  const dbDir = '/home/node/.local/share/FoundryVTT/Data/worlds/kingmaker/data/scenes';
  const db = new ClassicLevel(dbDir);
  await db.open();

  console.log("Dumping drawing shapes...");
  for await (const [key, value] of db.iterator()) {
    if (key.startsWith('!scenes.drawings!')) {
      const drawing = JSON.parse(value);
      console.log(`Key: ${key}, shape: ${JSON.stringify(drawing.shape)}`);
    }
  }

  await db.close();
}

run().catch(console.error);

const { ClassicLevel } = require('/home/node/foundry/node_modules/classic-level');

async function run() {
  const dbDir = '/home/node/.local/share/FoundryVTT/Data/worlds/kingmaker/data/scenes';
  const db = new ClassicLevel(dbDir);
  await db.open();

  console.log("Searching keys...");
  let count = 0;
  for await (const [key, value] of db.iterator()) {
    if (key.includes('drawings') || value.includes('polygon')) {
      console.log(`Key: ${key}`);
      console.log(`Value: ${value.substring(0, 100)}...`);
      count++;
      if (count > 20) break;
    }
  }

  await db.close();
}

run().catch(console.error);

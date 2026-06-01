const { ClassicLevel } = require('/home/node/foundry/node_modules/classic-level');

async function run() {
  const dbDir = '/home/node/.local/share/FoundryVTT/Data/worlds/kingmaker/data/scenes';
  const db = new ClassicLevel(dbDir);
  await db.open();

  console.log("Reading scenes keys...");
  let count = 0;
  for await (const [key, value] of db.iterator({ limit: 20 })) {
    console.log(`Key: ${key}`);
    count++;
  }

  await db.close();
}

run().catch(console.error);

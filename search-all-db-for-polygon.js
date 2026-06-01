const { ClassicLevel } = require('/home/node/foundry/node_modules/classic-level');

async function run() {
  const dbDir = '/home/node/.local/share/FoundryVTT/Data/worlds/kingmaker/data/scenes';
  const db = new ClassicLevel(dbDir);
  await db.open();

  console.log("Searching entire database for 'polygon'...");
  let count = 0;
  for await (const [key, value] of db.iterator()) {
    if (value.includes('"polygon"') || value.includes("'polygon'")) {
      console.log(`Found match! Key: ${key}`);
      console.log(`Value: ${value.substring(0, 300)}...`);
      count++;
    }
  }

  await db.close();
  console.log(`Search completed. Found ${count} matching records.`);
}

run().catch(console.error);

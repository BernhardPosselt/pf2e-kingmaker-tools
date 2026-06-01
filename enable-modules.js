const { ClassicLevel } = require('/home/node/foundry/node_modules/classic-level');

async function run() {
  const dbPath = '/home/node/.local/share/FoundryVTT/Data/worlds/kingmaker/data/settings';
  const db = new ClassicLevel(dbPath);
  await db.open();

  const key = '!settings!yaJVAzT3qUIRX3k8';
  const valStr = await db.get(key);
  const data = JSON.parse(valStr);
  
  console.log("Old module configuration string:", data.value);
  const modulesObj = JSON.parse(data.value);
  modulesObj['seasons-and-stars'] = true;
  modulesObj['seasons-and-stars-pf2e'] = true;
  data.value = JSON.stringify(modulesObj);
  console.log("New module configuration string:", data.value);

  await db.put(key, JSON.stringify(data));
  await db.close();
  console.log("Database updated successfully!");
}

run().catch(console.error);

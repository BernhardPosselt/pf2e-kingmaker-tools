try {
  const Ajv = require('/home/grego/code/pf2e-kingmaker-tools/build/js/node_modules/ajv');
  console.log("AJV is available!");
} catch (err) {
  console.log("AJV is not available in tools build node_modules:", err.message);
  try {
    const Ajv = require('ajv');
    console.log("AJV is available globally/locally!");
  } catch (e) {
    console.log("AJV is not available:", e.message);
  }
}

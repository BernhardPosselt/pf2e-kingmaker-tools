const fs = require('fs');
const Ajv = require('/home/grego/code/pf2e-kingmaker-tools/build/js/node_modules/ajv');

try {
  const ajv = new Ajv({ allErrors: true, verbose: true });
  
  try {
    const addFormats = require('/home/grego/code/pf2e-kingmaker-tools/build/js/node_modules/ajv-formats');
    addFormats(ajv);
  } catch (e) {
    console.log("Could not load ajv-formats:", e.message);
  }

  const schema = JSON.parse(fs.readFileSync('/home/grego/.local/share/FoundryVTT/Data/modules/seasons-and-stars/schemas/calendar-collection-v1.0.0.json', 'utf8'));
  const collection = JSON.parse(fs.readFileSync('/home/grego/.local/share/FoundryVTT/Data/modules/seasons-and-stars-pf2e/calendars/index.json', 'utf8'));

  delete collection['$schema'];

  const validate = ajv.compile(schema);
  const isValid = validate(collection);

  if (isValid) {
    console.log("Validation SUCCESS after removing $schema!");
  } else {
    console.error("Validation FAILED:", validate.errors);
  }
} catch (err) {
  console.error("Error during validation:", err);
}

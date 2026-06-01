const fs = require('fs');
const Ajv = require('/home/grego/code/pf2e-kingmaker-tools/build/js/node_modules/ajv');

try {
  const ajv = new Ajv({ allErrors: true, verbose: true });
  
  try {
    const addFormats = require('/home/grego/code/pf2e-kingmaker-tools/build/js/node_modules/ajv-formats');
    addFormats(ajv);
  } catch (e) {}

  const schema = JSON.parse(fs.readFileSync('/home/grego/.local/share/FoundryVTT/Data/modules/seasons-and-stars/schemas/calendar-collection-v1.0.0.json', 'utf8'));
  const collection = JSON.parse(fs.readFileSync('/home/grego/.local/share/FoundryVTT/Data/modules/seasons-and-stars-pf2e/calendars/index.json', 'utf8'));

  const validate = ajv.compile(schema);
  const isValid = validate(collection);

  if (isValid) {
    console.log("Collection validation SUCCESS: calendars/index.json is fully valid!");
  } else {
    console.error("Collection validation FAILED:");
    console.error(validate.errors);
  }
} catch (err) {
  console.error("Error during validation:", err);
}

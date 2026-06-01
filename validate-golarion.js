const fs = require('fs');
const Ajv = require('/home/grego/code/pf2e-kingmaker-tools/build/js/node_modules/ajv');

try {
  const ajv = new Ajv({ allErrors: true, verbose: true });
  
  // Add ajv-formats if available
  try {
    const addFormats = require('/home/grego/code/pf2e-kingmaker-tools/build/js/node_modules/ajv-formats');
    addFormats(ajv);
    console.log("Loaded ajv-formats.");
  } catch (e) {
    console.log("ajv-formats not loaded:", e.message);
  }

  const schema = JSON.parse(fs.readFileSync('/home/grego/.local/share/FoundryVTT/Data/modules/seasons-and-stars/schemas/calendar-v1.0.0.json', 'utf8'));
  const calendar = JSON.parse(fs.readFileSync('/home/grego/.local/share/FoundryVTT/Data/modules/seasons-and-stars-pf2e/calendars/golarion-pf2e.json', 'utf8'));

  const validate = ajv.compile(schema);
  const isValid = validate(calendar);

  if (isValid) {
    console.log("Calendar validation SUCCESS: golarion-pf2e is fully valid!");
  } else {
    console.error("Calendar validation FAILED:");
    console.error(validate.errors);
  }
} catch (err) {
  console.error("Error during validation:", err);
}

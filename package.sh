#!/usr/bin/env bash

# Run with ./package.sh 0.0.2
# requires zip, curl, jq and yarn

set -e

version="$1"

yarn run lint
yarn run test
yarn run clean
yarn run build

# create release directories
rm -rf ./build
mkdir -p build/pf2e-kingmaker-tools/

# create archive
node ./scripts/update-module-json.mjs "$version"
cp module.json map.json README.md LICENSE CHANGELOG.md OpenGameLicense.md build/pf2e-kingmaker-tools/
cp -r packs/ docs/ templates/ styles/ dist/ build/pf2e-kingmaker-tools/

node ./scripts/remove-content.mjs

cd build
zip -r release.zip pf2e-kingmaker-tools
cd -

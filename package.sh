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

# remove broken links
./node_modules/.bin/fvtt package workon "pf2e-kingmaker-tools"
./node_modules/.bin/fvtt package unpack "kingmaker-tools-random-encounters" --od build/tmp/kingmaker-tools-random-encounters
node ./scripts/remove-content.mjs
./node_modules/.bin/fvtt package pack "kingmaker-tools-random-encounters" --id build/tmp/kingmaker-tools-random-encounters --od build/pf2e-kingmaker-tools/packs

cd build
zip -r release.zip pf2e-kingmaker-tools
cd -

#!/usr/bin/env bash

# Run with ./release.sh 0.0.2 GITHUB_TOKEN
# requires zip, curl, jq and yarn

set -e

version="$1"
token="$2"

yarn run lint
yarn run test
yarn run clean
yarn run build

# create release directories
rm -rf ./build
mkdir -p build/pf2e-kingmaker-tools/

# update manifest file
sed -i "s/\"version\":.*/\"version\": \"$version\",/g" module.json
sed -i "s/\"download\":.*/\"download\": \"https:\/\/github.com\/BernhardPosselt\/pf2e-kingmaker-tools\/releases\/download\/$version\/release.zip\"/g" module.json

# create archive
cp module.json build/pf2e-kingmaker-tools/module.json
cp README.md LICENSE CHANGELOG.md OpenGameLicense.md build/pf2e-kingmaker-tools/
cp -r packs/ maps/ templates/ styles/ dist/ build/pf2e-kingmaker-tools/
cd build
zip -r release.zip pf2e-kingmaker-tools
cd -

# push new code
git add module.json
git commit -m "Release $version"
git tag "$version"
git push
git push --tags

# upload release zip
id=$(curl --no-progress-meter -X POST -H "Accept: application/vnd.github.v3+json" -H "Authorization: token $token" "https://api.github.com/repos/BernhardPosselt/pf2e-kingmaker-tools/releases" -d "{\"tag_name\":\"$version\",\"target_commitish\":\"master\",\"name\":\"$version\",\"body\":\"\",\"draft\":false,\"prerelease\":false,\"generate_release_notes\":false}" | jq ".id")
curl -X POST -H "Accept: application/vnd.github.v3+json" -H "Authorization: token $token" -H "Content-Type: application/zip" "https://uploads.github.com/repos/BernhardPosselt/pf2e-kingmaker-tools/releases/$id/assets?name=release.zip" --data-binary "@build/release.zip"

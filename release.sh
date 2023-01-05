#!/usr/bin/env bash

# Run with ./release.sh 0.0.2 GITHUB_TOKEN
# requires zip, curl, jq and yarn

set -e

version="$1"
token="$2"

./package.sh "$1"

# push new code
git add module.json
git commit -m "Release $version"
git tag "$version"
git push
git push --tags

# upload release zip
id=$(curl --no-progress-meter -X POST -H "Accept: application/vnd.github.v3+json" -H "Authorization: token $token" "https://api.github.com/repos/BernhardPosselt/pf2e-kingmaker-tools/releases" -d "{\"tag_name\":\"$version\",\"target_commitish\":\"master\",\"name\":\"$version\",\"body\":\"\",\"draft\":false,\"prerelease\":false,\"generate_release_notes\":false}" | jq ".id")
curl -X POST -H "Accept: application/vnd.github.v3+json" -H "Authorization: token $token" -H "Content-Type: application/zip" "https://uploads.github.com/repos/BernhardPosselt/pf2e-kingmaker-tools/releases/$id/assets?name=release.zip" --data-binary "@build/release.zip"

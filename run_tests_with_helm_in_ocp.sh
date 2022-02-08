#!/usr/bin/env bash
SOURCE_REPOSITORY_URL=${1:-https://github.com/snowdrop/cache-example}
SOURCE_REPOSITORY_REF=${2:-sb-2.5.x}

source scripts/waitFor.sh

helm install cache ./helm --set cute-name-service.route.expose=true --set cute-name-service.s2i.source.repo=$SOURCE_REPOSITORY_URL --set cute-name-service.s2i.source.ref=$SOURCE_REPOSITORY_REF --set greeting-service.route.expose=true --set greeting-service.s2i.source.repo=$SOURCE_REPOSITORY_URL --set greeting-service.s2i.source.ref=$SOURCE_REPOSITORY_REF
if [[ $(waitFor "spring-boot-cache-greeting" "app") -eq 1 ]] ; then
  echo "Application failed to deploy. Aborting"
  exit 1
fi

# Run OpenShift Tests
./mvnw -s .github/mvn-settings.xml clean verify -Popenshift,openshift-it

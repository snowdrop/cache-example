#!/usr/bin/env bash
SOURCE_REPOSITORY_URL=${1:-https://github.com/snowdrop/cache-example}
SOURCE_REPOSITORY_REF=${2:-sb-2.5.x}
S2I_BUILDER_IMAGE_REPO=registry.access.redhat.com/ubi8/openjdk-11
S2I_BUILDER_IMAGE_TAG=1.14

source scripts/waitFor.sh

helm install cache ./helm --set cute-name-service.route.expose=true --set cute-name-service.s2i.source.repo=$SOURCE_REPOSITORY_URL --set cute-name-service.s2i.source.ref=$SOURCE_REPOSITORY_REF --set greeting-service.route.expose=true --set greeting-service.s2i.source.repo=$SOURCE_REPOSITORY_URL --set greeting-service.s2i.source.ref=$SOURCE_REPOSITORY_REF --set cute-name-service.s2i.builderImage.repo=$S2I_BUILDER_IMAGE_REPO --set cute-name-service.s2i.builderImage.tag=$S2I_BUILDER_IMAGE_TAG --set greeting-service.s2i.builderImage.repo=$S2I_BUILDER_IMAGE_REPO --set greeting-service.s2i.builderImage.tag=$S2I_BUILDER_IMAGE_TAG
if [[ $(waitFor "spring-boot-cache-greeting" "app") -eq 1 ]] ; then
  echo "Application failed to deploy. Aborting"
  exit 1
fi

# Run OpenShift Tests
./mvnw -s .github/mvn-settings.xml clean verify -Popenshift,openshift-it

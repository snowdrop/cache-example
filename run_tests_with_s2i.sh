#!/usr/bin/env bash
SOURCE_REPOSITORY_URL=${1:-https://github.com/snowdrop/cache-example}
SOURCE_REPOSITORY_REF=${2:-sb-2.5.x}

source scripts/waitFor.sh

# launch the cache service
oc create -f .openshiftio/cache.yml
if [[ $(waitFor "cache-server" "app") -eq 1 ]] ; then
  echo "Cache server failed to deploy. Aborting"
  exit 1
fi

declare -Ar moduleMapping=( ["greeting-service"]="spring-boot-cache-greeting" ["cute-name-service"]="spring-boot-cache-cutename" )
for module in "${!moduleMapping[@]}"
do
  oc create -f ${module}/.openshiftio/application.yaml
  oc new-app --template=${moduleMapping[$module]} -p SOURCE_REPOSITORY_URL=$SOURCE_REPOSITORY_URL -p SOURCE_REPOSITORY_REF=$SOURCE_REPOSITORY_REF -p SOURCE_REPOSITORY_DIR=${module}
  if [[ $(waitFor ${moduleMapping[$module]} "app") -eq 1 ]] ; then
    echo "${module} service failed to deploy. Aborting"
    exit 1
  fi
done

# launch the tests without deploying the application
./mvnw -s .github/mvn-settings.xml clean verify -Popenshift,openshift-it

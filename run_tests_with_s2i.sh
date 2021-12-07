#!/usr/bin/env bash
SOURCE_REPOSITORY_URL=${1:-https://github.com/snowdrop/cache-example}
SOURCE_REPOSITORY_REF=${2:-sb-2.4.x}

# launch the cache service
oc create -f .openshiftio/cache.yml
sleep 30 # needed in order to bypass the 'Pending' state
timeout 300s bash -c 'while [[ $(oc get pod -o json | jq  ".items[] | select(.metadata.name | contains(\"deploy\"))  | .status  " | jq -rs "sort_by(.startTme) | last | .phase") == "Running" ]]; do sleep 20; done; echo ""'

declare -Ar moduleMapping=( ["greeting-service"]="spring-boot-cache-greeting" ["cute-name-service"]="spring-boot-cache-cutename" )
for module in "${!moduleMapping[@]}"
do
  oc create -f ${module}/.openshiftio/application.yaml
  oc new-app --template=${moduleMapping[$module]} -p SOURCE_REPOSITORY_URL=$SOURCE_REPOSITORY_URL -p SOURCE_REPOSITORY_REF=$SOURCE_REPOSITORY_REF -p SOURCE_REPOSITORY_DIR=${module}
  sleep 60 # needed in order to bypass the 'Pending' state
  # wait for the app to stand up
  timeout 300s bash -c 'while [[ $(oc get pod -o json | jq  ".items[] | select(.metadata.name | contains(\"build\"))  | .status  " | jq -rs "sort_by(.startTme) | last | .phase") == "Running" ]]; do sleep 20; done; echo ""'
done

# launch the tests without deploying the application
./mvnw -s .github/mvn-settings.xml clean verify -Popenshift,openshift-it

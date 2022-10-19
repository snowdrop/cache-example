#!/usr/bin/env bash
CONTAINER_REGISTRY=${1:-localhost:5000}
K8S_NAMESPACE=${2:-k8s}
MAVEN_OPTS=${3:-}

source scripts/waitFor.sh

kubectl config set-context --current --namespace=$K8S_NAMESPACE

# launch the cache service
kubectl apply -f .openshiftio/cache.yml
if [[ $(waitFor "cache-server" "app") -eq 1 ]] ; then
  echo "Cache server failed to deploy. Aborting"
  exit 1
fi

# 1.- Deploy Cute Name Service
./mvnw -s .github/mvn-settings.xml clean verify -pl cute-name-service -Pkubernetes -Ddekorate.docker.registry=$CONTAINER_REGISTRY -Dkubernetes.namespace=$K8S_NAMESPACE -Ddekorate.push=true -Ddekorate.deploy=true $MAVEN_OPTS
if [[ $(waitFor "spring-boot-cache-cutename" "app.kubernetes.io/name") -eq 1 ]] ; then
  echo "Cute name service failed to deploy. Aborting"
  exit 1
fi

# 2.- Deploy Greeting Service
./mvnw -s .github/mvn-settings.xml clean verify -pl greeting-service -Pkubernetes -Ddekorate.docker.registry=$CONTAINER_REGISTRY -Dkubernetes.namespace=$K8S_NAMESPACE -Ddekorate.push=true -Ddekorate.deploy=true $MAVEN_OPTS
if [[ $(waitFor "spring-boot-cache-greeting" "app.kubernetes.io/name") -eq 1 ]] ; then
  echo "Greeting name service failed to deploy. Aborting"
  exit 1
fi

# 3.- Run Tests
./mvnw -s .github/mvn-settings.xml verify -pl tests -Pkubernetes-it $MAVEN_OPTS

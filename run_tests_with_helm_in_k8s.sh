#!/usr/bin/env bash
CONTAINER_REGISTRY=${1:-localhost:5000}
K8S_NAMESPACE=${2:-helm}

source scripts/waitFor.sh
oc project $K8S_NAMESPACE

# Build
./mvnw -s .github/mvn-settings.xml clean package

# Create docker image and tag it in registry
## Cute name service:
CUTE_NAME_IMAGE=cache-cute-name:latest
docker build ./cute-name-service -t $CUTE_NAME_IMAGE
docker tag $CUTE_NAME_IMAGE $CONTAINER_REGISTRY/$CUTE_NAME_IMAGE
docker push $CONTAINER_REGISTRY/$CUTE_NAME_IMAGE

## Greeting service:
GREETING_IMAGE=cache-greeting:latest
docker build ./greeting-service -t $GREETING_IMAGE
docker tag $GREETING_IMAGE $CONTAINER_REGISTRY/$GREETING_IMAGE
docker push $CONTAINER_REGISTRY/$GREETING_IMAGE

helm install cache ./helm -n $K8S_NAMESPACE  --set cute-name-service.docker.image=$CONTAINER_REGISTRY/$CUTE_NAME_IMAGE --set greeting-service.docker.image=$CONTAINER_REGISTRY/$GREETING_IMAGE
if [[ $(waitFor "spring-boot-cache-greeting" "app") -eq 1 ]] ; then
  echo "Application failed to deploy. Aborting"
  exit 1
fi

# Run OpenShift Tests
./mvnw -s .github/mvn-settings.xml clean verify -Pkubernetes-it -Dkubernetes.namespace=$K8S_NAMESPACE

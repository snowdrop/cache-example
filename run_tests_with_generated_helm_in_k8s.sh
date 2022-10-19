#!/usr/bin/env bash
CONTAINER_REGISTRY=${1:-localhost:5000}
K8S_NAMESPACE=${2:-k8s}
MAVEN_OPTS=${3:-}
GROUP=user
TAG=genhelm

source scripts/waitFor.sh

kubectl config set-context --current --namespace=$K8S_NAMESPACE

# launch the cache service
kubectl apply -f .openshiftio/cache.yml
if [[ $(waitFor "cache-server" "app") -eq 1 ]] ; then
  echo "Cache server failed to deploy. Aborting"
  exit 1
fi

# 1.- Deploy Cute Name Service
./mvnw -s .github/mvn-settings.xml clean verify -pl cute-name-service -Pkubernetes,helm -DskipTests -Ddekorate.docker.registry=$CONTAINER_REGISTRY -Ddekorate.docker.group=$GROUP -Ddekorate.docker.version=$TAG -Dkubernetes.namespace=$K8S_NAMESPACE -Ddekorate.helm.name=cute-name-service -Ddekorate.push=true $MAVEN_OPTS
helm install cute-name-service ./cute-name-service/target/classes/META-INF/dekorate/helm/cute-name-service --set app.image=$CONTAINER_REGISTRY/$GROUP/spring-boot-cache-cutename:$TAG -n $K8S_NAMESPACE
if [[ $(waitFor "spring-boot-cache-cutename" "app.kubernetes.io/name") -eq 1 ]] ; then
  echo "Cute name service failed to deploy. Aborting"
  exit 1
fi

# 2.- Deploy Greeting Service
./mvnw -s .github/mvn-settings.xml clean verify -pl greeting-service -Pkubernetes,helm -DskipTests -Ddekorate.docker.registry=$CONTAINER_REGISTRY -Ddekorate.docker.group=$GROUP -Ddekorate.docker.version=$TAG -Dkubernetes.namespace=$K8S_NAMESPACE -Ddekorate.helm.name=greeting-service -Ddekorate.push=true $MAVEN_OPTS
helm install greeting-service ./greeting-service/target/classes/META-INF/dekorate/helm/greeting-service --set app.image=$CONTAINER_REGISTRY/$GROUP/spring-boot-cache-greeting:$TAG -n $K8S_NAMESPACE
if [[ $(waitFor "spring-boot-cache-greeting" "app.kubernetes.io/name") -eq 1 ]] ; then
  echo "Greeting name service failed to deploy. Aborting"
  exit 1
fi

# 3.- Run Tests
./mvnw -s .github/mvn-settings.xml verify -pl tests -Pkubernetes-it $MAVEN_OPTS

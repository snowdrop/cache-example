## Run locally

```bash
    mvn --projects cute-name-service spring-boot:run -Drun.arguments="--server.port=8081"
    mvn --projects greeting-service spring-boot:run -Drun.arguments="--service.name.baseURL=http://localhost:8081"
    curl http://localhost:8080/api/greeting
```

## Run on Openshift

The first step is to start JBoss Datagrid on Openshift

```bash
oc policy add-role-to-user view system:serviceaccount:$(oc project -q):default -n $(oc project -q)
oc policy add-role-to-user view system:serviceaccount:$(oc project -q):eap-service-account -n $(oc project -q)
oc new-app --name=cache-server --image-stream=jboss-datagrid71-openshift:1.1  -e INFINISPAN_CONNECTORS=hotrod -e DEFAULT_CACHE_EVICTION_MAX_ENTRIES=10 -e DEFAULT_CACHE_EXPIRATION_LIFESPAN=10000
```

Once JBoss Datagrid has been deployed, execute the following to deploy the apps

```bash
mvn clean fabric8:deploy -Popenshift
```

An integration test can be run to verify the behavior of the system by executing

```bash
mvn clean verify -Popenshift,openshift-it
```
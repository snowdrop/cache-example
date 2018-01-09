## Run locally

```bash
    mvn --projects cute-name-service spring-boot:run -Drun.arguments="--server.port=8081"
    mvn --projects greeting-service spring-boot:run -Drun.arguments="--service.name.baseURL=http://localhost:8081"
    curl http://localhost:8080/api/greeting
```

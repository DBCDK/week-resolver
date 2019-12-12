# Week-resolver
Service to hand out specific week codes for any given date.

To build:
```bash
mvn verify
docker build -t docker-io.dbc.dk/weekresolver:dev \ 
    -f $(pwd)/target/docker/Dockerfile .
```

Local run:
```bash

docker run -it -p 8080:8080 \
    -e JAVA_MAX_HEAP_SIZE=5G  \
    -e TZ=Europe/Copenhagen \
    --name test \
    --rm \
    docker-io.dbc.dk/weekresolver:dev
```


Endpoint curl example:
```bash
curl localhost:8080/api/v1/date/bpf/2019-12-29
```


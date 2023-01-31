```bash
skaffold dev \
  --module support \
  --skip-tests=true \
  --default-repo="localhost:32000" \
  --tail=false \
  --port-forward=user
```

```bash
skaffold dev \
  --module springboot2 \
  --skip-tests=true \
  --default-repo="localhost:32000" \
  --tail=false \
  --port-forward=user
```

```bash
skaffold delete \
  --module support
```

```bash 
docker exec -it broker1 /bin/sh
docker exec -it broker1 kafka-topics --bootstrap-server localhost:29091 --list

docker exec -it broker1 kafka-topics --bootstrap-server localhost:29091 --topic counter --describe
docker exec -it broker1 kafka-topics --bootstrap-server localhost:29091 --topic split0 --describe
docker exec -it broker1 kafka-topics --bootstrap-server localhost:29091 --topic split1 --describe
docker exec -it broker1 kafka-topics --bootstrap-server localhost:29091 --topic split2 --describe


docker exec -it broker1 kafka-configs --bootstrap-server localhost:29091 --alter --entity-type topics --entity-name split0 --add-config retention.ms=1000
docker exec -it broker1 kafka-configs --bootstrap-server localhost:29091 --alter --entity-type topics --entity-name split1 --add-config retention.ms=1000
docker exec -it broker1 kafka-configs --bootstrap-server localhost:29091 --alter --entity-type topics --entity-name split2 --add-config retention.ms=1000

#
docker exec -it broker1 kafka-configs --bootstrap-server localhost:29091 --alter --entity-type topics --entity-name split0 --add-config retention.ms=900000
docker exec -it broker1 kafka-configs --bootstrap-server localhost:29091 --alter --entity-type topics --entity-name split1 --add-config retention.ms=900000
docker exec -it broker1 kafka-configs --bootstrap-server localhost:29091 --alter --entity-type topics --entity-name split2 --add-config retention.ms=900000
```

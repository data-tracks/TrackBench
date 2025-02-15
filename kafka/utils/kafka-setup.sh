#! /bin/bash
docker rm -f trackbench-kafka
sleep 2

docker compose up -d

sleep 3

docker exec -ti trackbench-kafka /opt/kafka/bin/kafka-topics.sh --create --bootstrap-server :9092 --partitions 1 --topic f1

docker exec -ti trackbench-kafka /opt/kafka/bin/kafka-topics.sh --create --bootstrap-server :9092 --partitions 1 --topic f2

docker exec -ti trackbench-kafka /opt/kafka/bin/kafka-topics.sh --create --bootstrap-server :9092 --partitions 1 --topic window

docker exec -ti trackbench-kafka /opt/kafka/bin/kafka-topics.sh --create --bootstrap-server :9092 --partitions 1 --topic "large-window"

docker exec -ti trackbench-kafka /opt/kafka/bin/kafka-topics.sh --create --bootstrap-server :9092 --partitions 1 --topic errors

docker exec -ti trackbench-kafka /opt/kafka/bin/kafka-topics.sh --list --bootstrap-server localhost:9092

# listen
# docker exec -ti trackbench-kafka /opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic f2

#! /bin/bash

docker exec -ti trackbench-kafka /opt/kafka/bin/kafka-topics.sh --create --bootstrap-server :9092 --topic f1

docker exec -ti trackbench-kafka /opt/kafka/bin/kafka-topics.sh --create --bootstrap-server :9092 --topic mini-window

docker exec -ti trackbench-kafka /opt/kafka/bin/kafka-topics.sh --create --bootstrap-server :9092 --topic large-window

docker exec -ti trackbench-kafka /opt/kafka/bin/kafka-topics.sh --create --bootstrap-server :9092 --topic errors

docker exec -ti trackbench-kafka /opt/kafka/bin/kafka-topics.sh --list --bootstrap-server localhost:9092

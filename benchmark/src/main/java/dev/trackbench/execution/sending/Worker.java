package dev.trackbench.execution.sending;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.json.JSONObject;

import java.util.concurrent.BlockingQueue;

@Slf4j
public class Worker implements Runnable {
    //Topic
    String topic;
    //Blockingqueue
    BlockingQueue<JSONObject> queue;
    //Kafka
    KafkaProducer<String, String> producer;

    public Worker(BlockingQueue<JSONObject> messageQueue, String topic , KafkaProducer<String, String> producer) {
        this.queue = messageQueue;
        this.topic = topic;
        this.producer = producer;
    }

    @Override
    public void run() {
        while (!SendCoordinator.stop) {
            try { //TODO: solve problem, if multiple threads sometimes duplicate entries from here!!!!
                synchronized (queue) {
                    JSONObject message = queue.peek();
                    if (message == null) {
                        continue;
                    }
                    if (message.getInt("tick") == SendCoordinator.getTimeStep()) {
                        if (queue.peek().equals(message)) {
                            queue.take();
                        } else {
                            continue;
                        }
                        ProducerRecord<String, String> record = new ProducerRecord<>(topic, message.toString());
                        producer.send(record);
//                        log.info("Sent: " + record.toString());
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.info(Thread.currentThread().getName());
                log.info("Thread interrupted");
                break;
            }
        }
    }

    public boolean alive() {
        return Thread.currentThread().isAlive();
    }

    public int length() {
        return queue.size();
    }

}

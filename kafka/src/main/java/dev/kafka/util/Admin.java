package dev.kafka.util;

import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;

public class Admin {

    private static final String bootstrapServers = "localhost:9092"; // Change to your Kafka broker


    public static void truncateTopic( String topicName, int partitions ) {

        try {
            // Configure Kafka AdminClient
            Properties properties = new Properties();
            properties.put( AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers );

            try ( AdminClient adminClient = AdminClient.create( properties ) ) {
                deleteTopic( adminClient, topicName );

                try {
                    Thread.sleep( 5000 );
                } catch ( InterruptedException e ) {
                    throw new RuntimeException( e );
                }

                createTopic( adminClient, topicName, partitions, (short) 1 );
            }
        } catch ( Exception e ) {
            throw new RuntimeException( e );
        }
    }


    public static void deleteTopic( AdminClient adminClient, String topic ) throws ExecutionException, InterruptedException {
        System.out.println( "Deleting topic: " + topic );
        adminClient.deleteTopics( Collections.singletonList( topic ) ).all().get();
        System.out.println( "Topic '" + topic + "' deleted successfully." );
    }


    public static void createTopic( AdminClient adminClient, String topic, int numPartitions, short replicationFactor )
            throws ExecutionException, InterruptedException {
        System.out.println( "Creating topic: " + topic );
        NewTopic newTopic = new NewTopic( topic, numPartitions, replicationFactor );
        adminClient.createTopics( Collections.singletonList( newTopic ) ).all().get();
        System.out.println( "Topic '" + topic + "' created successfully with " + numPartitions + " partitions." );
    }


}
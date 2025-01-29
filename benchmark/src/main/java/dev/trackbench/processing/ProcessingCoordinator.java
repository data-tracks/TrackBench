package dev.trackbench.processing;

import dev.trackbench.BenchmarkConfig;
import dev.trackbench.BenchmarkContext;
import dev.trackbench.receiver.ReceiveCoordinator;
import dev.trackbench.sending.SendCoordinator;
import dev.trackbench.util.Clock;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProcessingCoordinator {

    public static void start( BenchmarkContext context ) {
        context.printProcessingTime();
        context.setClock( new Clock( context.getConfig() ) );
        try {
            // system might want to do some setting up
            context.getSystem().prepare();

            //Start sending to stream processing system and start receiver
            SendCoordinator sender = new SendCoordinator( context );
            ReceiveCoordinator receiver = new ReceiveCoordinator( context );

            receiver.start();
            sender.start();

            while ( !receiver.allReady() || !sender.allReady() ) {
                Thread.sleep( Duration.ofSeconds( 5 ) );
            }
            log.info( "All ready. Start the clock..." );
            context.getClock().start();

            sender.join();
            log.info( "All send. Waiting for receiver to finish..." );

            Thread.sleep( Duration.ofMinutes( BenchmarkConfig.executionMaxMin ) );
            //we can start stopping threads, the target system takes too long to process or finished everything
            receiver.stopReceivers();
            receiver.join();
            context.getClock().join();
        } catch ( InterruptedException e ) {
            throw new RuntimeException( e );
        }
    }




}

package dev.trackbench.execution;

import dev.trackbench.configuration.BenchmarkContext;
import dev.trackbench.display.Component;
import dev.trackbench.display.Display;
import dev.trackbench.display.Timer;
import dev.trackbench.execution.receiver.ReceiveCoordinator;
import dev.trackbench.execution.sending.SendCoordinator;
import dev.trackbench.util.Clock;
import dev.trackbench.util.Pair;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExecutionCoordinator {

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
                Thread.sleep( Duration.ofSeconds( 1 ) );
            }
            Display.INSTANCE.info( "All ready. Start the clock..." );
            context.getClock().start();

            sender.join();
            context.getClock().finishDisplay();
            Display.INSTANCE.info( "All send. Waiting {} mins for receiver to finish...", context.getConfig().executionMaxM() );

            waitExecution( context.getConfig().executionMaxM() );

            //we can start stopping threads, the target system takes too long to process or finished everything
            receiver.stopReceivers();
            receiver.join();

            context.getClock().shutdown();
            ;
            context.getClock().join();
        } catch ( InterruptedException e ) {
            throw new RuntimeException( e );
        }
        //Display.INSTANCE.info( "Max id is {}", JsonSource.of( context.getConfig().getResultFile( context.getWorkload( 0 ).getName() ), 10_000 ).countLines() );
    }


    private static void waitExecution( long minutes ) throws InterruptedException {
        Timer timer = new Timer( minutes );
        Display.INSTANCE.next( timer );

        Pair<Thread, Component> current = Display.INSTANCE.getCurrent();
        while ( current == null || current.right() != timer ) {
            Thread.sleep( 1000 );
            current = Display.INSTANCE.getCurrent();
        }
        current.left().join();
    }


}

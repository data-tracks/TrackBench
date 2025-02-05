package dev.trackbench.execution;

import dev.trackbench.configuration.BenchmarkConfig;
import dev.trackbench.configuration.BenchmarkContext;
import dev.trackbench.display.Display;
import dev.trackbench.execution.receiver.ReceiveCoordinator;
import dev.trackbench.execution.sending.SendCoordinator;
import dev.trackbench.util.Clock;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

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
                Thread.sleep( Duration.ofSeconds( 5 ) );
            }
            Display.INSTANCE.info( "All ready. Start the clock..." );
            context.getClock().start();

            sender.join();
            context.getClock().finishDisplay();
            Display.INSTANCE.info( "All send. Waiting {} mins for receiver to finish...", BenchmarkConfig.executionMaxMin );

            waitExecution(BenchmarkConfig.executionMaxMin);

            //we can start stopping threads, the target system takes too long to process or finished everything
            receiver.stopReceivers();
            receiver.join();

            context.getClock().shutdown();;
            context.getClock().join();
        } catch ( InterruptedException e ) {
            throw new RuntimeException( e );
        }
    }

    private static void waitExecution(long minutes) throws InterruptedException {
        for (long remainingSeconds = minutes*60; remainingSeconds >= 0; remainingSeconds--) {
            long displayMinutes = remainingSeconds / 60;
            long displaySeconds = remainingSeconds % 60;

            System.out.printf("\rTime left: %02d:%02d", displayMinutes, displaySeconds);

            TimeUnit.SECONDS.sleep(1);
        }
        System.out.print("\n");

    }


}

package dev.trackbench.execution.sending;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.trackbench.configuration.BenchmarkContext;
import dev.trackbench.util.ClockDisplay;
import dev.trackbench.util.file.JsonIterator;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.Setter;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.json.JSONArray;

public class SendCoordinator extends Thread {

    @Getter
    static int timeStep = 0;
    @Getter
    static boolean stop = false;

    AtomicBoolean initialized = new AtomicBoolean( false );

    private final BenchmarkContext context;

    List<Filler> fillers = new ArrayList<>();
    List<Sender> senders = new ArrayList<>();


    public SendCoordinator( BenchmarkContext context ) {
        this.context = context;
    }


    public boolean allReady() {
        return initialized.get() && fillers.stream().allMatch( f -> f.ready.get() ) && senders.stream().allMatch( s -> s.ready.get() );
    }


    @Override
    public void run() {
        prepare();
    }


    private void prepare() {
        List<File> files = context.getConfig().getFilesInFolder( context.getConfig().getDataWithErrorPath() );

        int id = 0;
        for ( File sensor : files ) {
            Sender sender = new Sender( id++, context.getConfig(), getSender(), context.getClock() );
            senders.add( sender );
            Filler filler = new Filler( sender.messageQueue, new JsonIterator( context.getConfig().readBatchSize(), sensor, true ), sender );
            fillers.add( filler );

            // they can start as they will wait for the clock
            sender.start();
            filler.start();
        }
        ClockDisplay clock = new ClockDisplay(context.getClock(), context.getConfig());
        initialized.set( true );
        try {
            for ( Sender sender : senders ) {
                sender.join();
            }

            for ( Filler filler : fillers ) {
                filler.join();
            }
        } catch ( InterruptedException e ) {
            throw new RuntimeException( e );
        }
        clock.stop();
    }


    private Consumer<JsonNode> getSender() {
        return context.getSystem().getSender();
    }

}

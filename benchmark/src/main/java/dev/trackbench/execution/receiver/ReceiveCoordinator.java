package dev.trackbench.execution.receiver;

import dev.trackbench.configuration.BenchmarkContext;
import dev.trackbench.util.file.FileJsonTarget;
import dev.trackbench.configuration.workloads.Workload;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ReceiveCoordinator extends Thread {

    public final BenchmarkContext context;

    @Getter
    List<Receiver> receivers = new ArrayList<>();


    public ReceiveCoordinator( BenchmarkContext context ) {
        this.context = context;
    }


    @Override
    public void run() {
        prepare();
    }


    public void prepare() {
        for ( Entry<Integer, Workload> entry : context.getWorkloads().entrySet() ) {
            for ( long i = 0; i < context.getConfig().receivers(); i++ ) {
                Receiver receiver = new Receiver( entry.getValue(), context.getSystem(), context.getClock(), new FileJsonTarget( context.getConfig().getResultFile( entry.getValue().getName(), i ), context.getConfig() ) );
                receivers.add( receiver );
                receiver.start();
            }
        }

        try {
            for ( Receiver receiver : receivers ) {
                receiver.join();
            }
        } catch ( Exception e ) {
            throw new RuntimeException( e );
        }

    }


    public boolean allReady() {
        return receivers.stream().allMatch( r -> r.ready.get() );
    }


    public void stopReceivers() {
        for ( Receiver receiver : this.receivers ) {
            receiver.interrupt();
        }
    }
}
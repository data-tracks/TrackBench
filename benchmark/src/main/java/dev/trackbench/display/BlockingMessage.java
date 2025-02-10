package dev.trackbench.display;

public class BlockingMessage implements Component{

    private final String msg;
    private Runnable finish;

    private boolean unpublished = false;

    public BlockingMessage(String msg) {
        this.msg = msg;
    }

    @Override
    public void start( Runnable runnable ) {
        if (unpublished) {
            // we ignore
            runnable.run();
            return;
        }
        this.finish = runnable;
        System.out.print( msg );
    }

    public void finish(){
        if ( finish == null ){
            // we did not yet get published
            unpublished = true;
            return;

        }
        System.out.print( "\r" );
        this.finish.run();
    }


    @Override
    public void render() {
        // nothing on purpose
    }

}

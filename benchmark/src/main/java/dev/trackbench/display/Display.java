package dev.trackbench.display;


import dev.trackbench.util.Pair;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import lombok.Getter;
import picocli.CommandLine.Help.Ansi;

public class Display extends Thread {

    @Getter
    private Pair<Thread, Component> current;
    private final Queue<Component> next = new ConcurrentLinkedQueue<>();


    public static final Display INSTANCE = new Display();

    private static int indent = 0;


    static {
        INSTANCE.start();

        Display.INSTANCE.doubleLine();
        System.out.println( "🚀 Welcome to " + red( "Tra" ) + yellow( "ck" ) + green( "Ben" ) + blue( "ch" ) + " 🚀" );
        Display.INSTANCE.doubleLine();
        INSTANCE.nextLine();
    }


    public static String red( String text ) {
        return color( text, "red" );
    }


    public static String blue( String text ) {
        return color( text, "blue" );
    }


    public static String yellow( String text ) {
        return color( text, "yellow" );
    }


    public static String green( String text ) {
        return color( text, "green" );
    }


    private static String color( String text, String color ) {
        return Ansi.ON.string( "@|" + color + " " + text + "|@" );
    }


    public static void bold( String text ) {
        INSTANCE.next( new Message( indent() + Ansi.ON.string( "@|bold " + text + "|@\n" ) ) );
    }


    public static void bold( String text, Object... args ) {
        bold( replace( text, args ) );
    }


    private Display() {
    }


    public Display reset() {
        System.out.println( "\r" );
        return this;
    }


    public Display write( String raw ) {
        next( new Message( raw ) );
        return this;
    }


    public Display writeln( String raw ) {
        next( new Message( indent() + raw + "\n" ) );
        return this;
    }


    public Display nextLine() {
        writeln( "" );
        return this;
    }


    private void finish() {
        this.current = null;
        if ( !next.isEmpty() ) {
            next( next.poll() );
        }
    }


    public void next( Component component ) {
        if ( current == null ) {
            Thread thread = new Thread( () -> {
                component.start( this::finish );
            } );
            thread.start();
            current = new Pair<>( thread, component );
        } else {
            this.next.add( component );
        }
    }


    public void warn( String text, Object... args ) {
        writeln( red( replace( text, args ) ) );
    }


    private static String replace( String text, Object... args ) {
        return String.format( text.replace( "{}", "%s" ), args );
    }


    public void info( String text, Object... args ) {
        writeln( yellow( replace( text, args ) ) );
    }


    public void preInfo( String text, Object... args ) {
        indent = 0;
        nextLine();
        writeln( "[INFO] " + yellow( replace( text, args ) ) );
        line();
        indent = 1;
    }

    public void setIndent( int indent ) {
        Display.indent = indent;
    }


    public void line() {
        indent = 0;
        writeln( "-----------------------------------------------" );
    }


    public void doubleLine() {
        indent = 0;
        writeln( "===================================================" );
    }


    public void error( String msg ) {
        Display.INSTANCE.writeln( "[ERROR] " + red( msg ) );
    }


    public static String indent() {
        return "  ".repeat( indent );
    }

}

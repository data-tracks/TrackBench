package dev.trackbench.display;


import dev.trackbench.util.Pair;
import lombok.Getter;
import picocli.CommandLine;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Display extends Thread {

    private Pair<Thread,Component> current;
    private final Queue<Component> next = new ConcurrentLinkedQueue<>();


    public static final Display INSTANCE = new Display();
    static {
        INSTANCE.start();

        System.out.println(red("Welcome to TrackBench..."));
        System.out.println(yellow("Welcome to TrackBench..."));
        System.out.println(green("Welcome to TrackBench..."));
        System.out.println(blue("Welcome to TrackBench..."));
        INSTANCE.nextLine();
    }

    public static String red(String text) {
        return color(text, "red");
    }

    public static String blue(String text) {
        return color(text, "blue");
    }

    public static String yellow(String text) {
        return color(text, "yellow");
    }

    public static String green(String text) {
        return color(text, "green");
    }

    private static String color(String text, String color) {
        return CommandLine.Help.Ansi.ON.string("@|" + color + " " + text + "|@");
    }

    private Display() {
    }

    public Display reset(){
        System.out.println("\r");
        return this;
    }

    public Display write(String raw){
        next(new Message(raw));
        return this;
    }

    public Display writeln(String raw){
        next(new Message(raw + "\n"));
        return this;
    }

    public Display nextLine() {
        writeln("");
        return this;
    }

    private void finish(){
        this.current = null;
        if (!next.isEmpty()) {
            next(next.poll());
        }
    }

    public void next(Component component){
        if (current == null) {
            Thread thread = new Thread(() -> {
                component.start(this::finish);
            });
            thread.start();
            current = new Pair<>(thread, component);
        }else {
            this.next.add(component);
        }
    }

    public void warn(String text, Object... args) {
        writeln( red( replace(text, args) ) );
    }

    private String replace(String text, Object... args) {
        return String.format(text.replace("{}", "%s"), args);
    }

    public void info(String text, Object... args) {
        writeln( yellow( replace(text, args) ) );
    }
}

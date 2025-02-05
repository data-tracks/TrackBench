package dev.trackbench.display;

public record Message(String text) implements Component {

    @Override
    public void render() {
        System.out.print(text);
    }

}

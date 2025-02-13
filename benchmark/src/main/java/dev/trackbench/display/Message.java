package dev.trackbench.display;

public final class Message extends Component {

    private final String text;

    public Message( String text ) {
        this.text = text;
    }

    @Override
    public void render() {
        this.getConsumer().accept( text );
    }

}

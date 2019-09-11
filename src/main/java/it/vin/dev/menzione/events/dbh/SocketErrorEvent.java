package it.vin.dev.menzione.events.dbh;

public class SocketErrorEvent extends SocketEvent{
    private Exception exception;

    public SocketErrorEvent() {
    }

    public SocketErrorEvent(Exception exception) {
        this.exception = exception;
    }

    public boolean hasException() {
        return exception != null;
    }

    public Exception getException() {
        return exception;
    }
}

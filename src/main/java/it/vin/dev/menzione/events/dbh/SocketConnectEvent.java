package it.vin.dev.menzione.events.dbh;

public class SocketConnectEvent extends SocketEvent {
    public enum Type {
        CONNECT, RECONNECT
    }

    public Type type;

    public SocketConnectEvent(Type type) {
        this.type = type;
    }
}

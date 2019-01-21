package it.vin.dev.menzione;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import it.vin.dev.menzione.events.CamionInsertEvent;
import it.vin.dev.menzione.events.CamionUpdateEvent;
import it.vin.dev.menzione.events.ViaggiEventBus;
import it.vin.dev.menzione.logica.Camion;

import java.net.URISyntaxException;

public class EventsTest {

    public EventsTest() {
    }

    public static void main(String[] args) throws URISyntaxException {
        Socket socket = IO.socket("http://localhost:3000");

        socket.on(Socket.EVENT_CONNECT, objects -> System.out.println("Connected")
        ).on("index", objects -> System.out.println(objects[0])
        ).on(Socket.EVENT_DISCONNECT, objects -> {
            System.out.println("Disconnection");
            socket.off("index");
        });
        socket.connect();
    }
}

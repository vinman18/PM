package it.vin.dev.menzione;

import io.socket.client.IO;
import io.socket.client.Socket;

import java.net.URISyntaxException;

@SuppressWarnings("Duplicates")
public class EventsTest2 {

    public EventsTest2() {
    }

    public static void main(String[] args) throws URISyntaxException, InterruptedException {
//        Socket socket = IO.socket("http://localhost:3000");
//
//        socket.on(Socket.EVENT_CONNECT, objects -> System.out.println("Connected"))
//                .on("message", objects -> System.out.println("Received message: '" + objects[0] + "'"))
//                .on(Socket.EVENT_DISCONNECT, objects -> {
//                    System.out.println("Disconnection");
//                    socket.off("index");
//                });
//        socket.connect();
//
//        socket.emit("message", EventsTest2.class.getName(), "Normal message");
//        Thread.sleep(5000);
//        socket.emit("broadcast", EventsTest2.class.getName(), "Broadcast message");
        String encoded = ViaggiUtils.encrypt("ciao");
        System.out.println(encoded);
        String decoded = ViaggiUtils.decrypt(encoded);

        System.out.println(decoded);
    }
}

package it.vin.dev.menzione.database_helper;

import io.socket.client.IO;
import io.socket.client.Socket;
import it.vin.dev.menzione.VerboseLogger;
import it.vin.dev.menzione.events.DateAddEvent;
import it.vin.dev.menzione.events.DateEventSource;
import it.vin.dev.menzione.events.DateDeleteEvent;
import it.vin.dev.menzione.events.ViaggiEventsBus;
import it.vin.dev.menzione.events.dbh.*;
import it.vin.dev.menzione.logica.ConfigurationManager;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.UUID;


public class DatabaseHelperChannel {
    static class SocketEvents {
        static final String ROW_INSERT = "row insert";
        static final String ROW_UPDATE = "row update";
        static final String ROW_DELETE = "row delete";
        static final String DATE_ADD = "date add";
        static final String DATE_DELETE = "date delete";
    }

    static class JsonEventFields {
        static final String TABLE_NAME = "table_name";
        static final String DATE = "date";
        static final String WHO_ID = "who_id";
        static final String WHO_NAME = "who_name";
        static final String TIMESTAMP = "timestamp";
    }

    private static DatabaseHelperChannel ourInstance = new DatabaseHelperChannel();

    private VerboseLogger logger = VerboseLogger.create(DatabaseHelperChannel.class);
    private String endpoint;
    private Socket socket;
    private DatabaseClient client;

    public static DatabaseHelperChannel getInstance() {
        return ourInstance;
    }

    private DatabaseHelperChannel() {
        logger.info("DatabaseHelperChannel: channel initialization");
        String host = ConfigurationManager.getInstance().getDBHelperHost();
        int port = ConfigurationManager.getInstance().getDBHelperPort();
        endpoint = String.format("http://%s:%d", host, port);
        try {
            socket = IO.socket(endpoint);
        } catch (URISyntaxException e) {
            logger.warn("DatabaseHelperChannel: wrong endpoint url");
            socket = null;
        }

        if(socket != null) {
            socket.on(Socket.EVENT_CONNECT, objects -> {
                logger.info("DatabaseHelperChannel: connected");
                logEventObjects(objects);
                registerSocketEvents();
                ViaggiEventsBus.getInstance().post(new SocketConnectEvent(SocketConnectEvent.Type.CONNECT));
            }).on(Socket.EVENT_DISCONNECT, objects -> {
                logger.info("DatabaseHelperChannel: disconnect");
                logEventObjects(objects);
                socket.off(SocketEvents.ROW_INSERT)
                        .off(SocketEvents.ROW_UPDATE)
                        .off(SocketEvents.ROW_DELETE)
                        .off(SocketEvents.DATE_ADD)
                        .off(SocketEvents.DATE_DELETE);
            }).on(Socket.EVENT_CONNECTING, objects -> {
                logger.verbose("DatabaseHelperChannel: connecting...");
                logEventObjects(objects);
            }).on(Socket.EVENT_CONNECT_ERROR, objects -> {
                logger.error("DatabaseHelperChannel: socket connection error");
                logEventObjects(objects);
                ViaggiEventsBus.getInstance().post(new SocketConnectionErrorEvent());
            }).on(Socket.EVENT_CONNECT_TIMEOUT, objects -> {
                logger.error("DatabaseHelperChannel: socket connection timeout");
                logEventObjects(objects);
                ViaggiEventsBus.getInstance().post(new SocketConnectionErrorEvent());
            }).on(Socket.EVENT_RECONNECTING, objects -> {
                logger.verbose("DatabaseHelperChannel: reconnecting...");
                logEventObjects(objects);
            }).on(Socket.EVENT_RECONNECT_ATTEMPT, objects -> {
                logger.verbose("DatabaseHelperChannel: reconnect attempt {}", objects[0]);
                logEventObjects(objects);
            }).on(Socket.EVENT_RECONNECT_ERROR, objects -> {
                logger.error("DatabaseHelperChannel: reconnect error");
                logEventObjects(objects);
            }).on(Socket.EVENT_RECONNECT_FAILED, objects -> {
                logger.error("DatabaseHelperChannel: reconnect failed");
                logEventObjects(objects);
            }).on(Socket.EVENT_RECONNECT, objects -> {
                logger.info("DatabaseHelperChannel: reconnected");
                logEventObjects(objects);
                ViaggiEventsBus.getInstance().post(new SocketConnectEvent(SocketConnectEvent.Type.RECONNECT));
            }).on(Socket.EVENT_ERROR, objects -> {
                logger.error("DatabaseHelperChannel: socket error: ", ((Exception) objects[0]));
                logEventObjects(objects);
                Exception exception = ((Exception) objects[0]);
                ViaggiEventsBus.getInstance().post(new SocketErrorEvent(new DatabaseHelperException(exception.getMessage(), exception)));
            });
        }

        client = new DatabaseClient(UUID.randomUUID().toString(), "undefined");
    }

    private void logEventObjects(Object[] objects) {
        if(objects == null) {
            logger.debug("logEventObjects: objects array is null");
        } else {
            int size = objects.length;
            if(size == 0) {
                logger.debug("logEventObjects: objects array is empty");
            } else {
                logger.debug("logEventObjects: objects size: {}. First object in array type '{}'. Content: {}", size, objects[0].getClass().getName(), objects[0].toString());
            }
        }
    }

    private void registerSocketEvents() {
        socket.on(SocketEvents.ROW_INSERT, objects -> rowEvent(SocketEvents.ROW_INSERT, (JSONObject) objects[0]))
                .on(SocketEvents.ROW_UPDATE, objects -> rowEvent(SocketEvents.ROW_UPDATE, (JSONObject) objects[0]))
                .on(SocketEvents.ROW_DELETE, objects -> rowEvent(SocketEvents.ROW_DELETE, (JSONObject) objects[0]))
                .on(SocketEvents.DATE_ADD, objects -> dateEvent(SocketEvents.DATE_ADD, ((JSONObject) objects[0])))
                .on(SocketEvents.DATE_DELETE, objects -> dateEvent(SocketEvents.DATE_DELETE, ((JSONObject) objects[0])));
    }

    private void rowEvent(String event, JSONObject eventArgs) {
        String tableName = eventArgs.optString(JsonEventFields.TABLE_NAME);
        String date = eventArgs.optString(JsonEventFields.DATE);
        String whoId = eventArgs.optString(JsonEventFields.WHO_ID);
        String whoName = eventArgs.optString(JsonEventFields.WHO_NAME);
        long timestamp = eventArgs.optLong(JsonEventFields.TIMESTAMP);

        if(whoId.equals(client.getId())) {
            logger.info("rowEvent: received an event from me. Ignored");
            return;
        }

        RowEvent rowEvent = null;

        switch (event) {
            case SocketEvents.ROW_INSERT:
                rowEvent = new RowInsertEvent(tableName, date, whoId, whoName, timestamp);
                break;
            case SocketEvents.ROW_UPDATE:
                rowEvent = new RowUpdateEvent(tableName, date, whoId, whoName, timestamp);
                break;
            case SocketEvents.ROW_DELETE:
                rowEvent = new RowDeleteEvent(tableName, date, whoId, whoName, timestamp);
                break;
        }

        if(rowEvent != null) {
            ViaggiEventsBus.getInstance().post(rowEvent);
        }
    }

    private void dateEvent(String event, JSONObject eventArgs) {
        String date = eventArgs.optString(JsonEventFields.DATE);
        String whoId = eventArgs.optString(JsonEventFields.WHO_ID);
        String whoName = eventArgs.optString(JsonEventFields.WHO_NAME);
        long timestamp = eventArgs.optLong(JsonEventFields.TIMESTAMP);

        if(whoId.equals(client.getId())) {
            logger.info("rowEvent: received an event from me. Ignored");
            return;
        }

        DateEvent dateEvent = null;

        switch (event) {
            case SocketEvents.DATE_ADD:
                dateEvent = new DateAddEvent(date, whoId, whoName, timestamp, DateEventSource.DATABASE_HELPER);
                break;
            case SocketEvents.DATE_DELETE:
                dateEvent = new DateDeleteEvent(date, whoId, whoName, timestamp, DateEventSource.DATABASE_HELPER);
                break;
        }

        if(dateEvent != null) {
            ViaggiEventsBus.getInstance().post(dateEvent);
        }
    }

    private JSONObject packDateJSONObject(String date) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(JsonEventFields.DATE, date);
        jsonObject.put(JsonEventFields.WHO_NAME, client.getName());
        jsonObject.put(JsonEventFields.WHO_ID, client.getId());
        jsonObject.put(JsonEventFields.TIMESTAMP, System.currentTimeMillis());

        return jsonObject;
    }

    private JSONObject packRowJSONObject(String tableName, String date) {
        JSONObject jsonObject = packDateJSONObject(date);
        jsonObject.put(JsonEventFields.TABLE_NAME, tableName);

        return jsonObject;
    }

    public void connect(DatabaseClient client) {
        this.client = client;
        logger.info("DatabaseHelperChannel: connecting to endpoint: '{}'", endpoint);
        socket.connect();
    }

    public void notifyRowInserted(String tableName, String date) throws DatabaseHelperException {
        if(socket == null || !socket.connected()) {
            throw  new DatabaseHelperException("Socket not connected");
        }

        JSONObject args = packRowJSONObject(tableName, date);
        socket.emit(SocketEvents.ROW_INSERT, args);
    }

    public void notifyRowUpdated(String tableName, String date) throws DatabaseHelperException {
        if(socket == null || !socket.connected()) {
            throw  new DatabaseHelperException("Socket not connected");
        }

        JSONObject args = packRowJSONObject(tableName, date);
        socket.emit(SocketEvents.ROW_UPDATE, args);
    }

    public void notifyDateAdded(String date) throws DatabaseHelperException {
        if(socket == null || !socket.connected()) {
            throw  new DatabaseHelperException("Socket not connected");
        }

        JSONObject args = packDateJSONObject(date);
        socket.emit(SocketEvents.DATE_ADD, args);
    }

    public void notifyDateRemoved(String date)  throws DatabaseHelperException {
        if(socket == null || !socket.connected()) {
            throw  new DatabaseHelperException("Socket not connected");
        }

        JSONObject args = packDateJSONObject(date);
        socket.emit(SocketEvents.DATE_DELETE, args);
    }

    public void disconnect() {
        if(socket != null) {
            socket.disconnect();
        }
    }
}

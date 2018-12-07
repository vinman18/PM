package it.vin.dev.menzione.database_helper;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class DatabaseClient extends UnicastRemoteObject implements IDatabaseClient {

    private DatabaseHelperListener listener;
    private String name;
    private String id;

    public DatabaseClient(String id, String name) throws RemoteException {
        this.name = name;
        this.id = id;
    }

    @Override
    public void onRowInserted(String tableName, String date, String whoId, String whoName, long timestamp) throws RemoteException {
        if(listener != null) {
            listener.onRowInserted(tableName, date, whoName, timestamp);
        }
    }

    @Override
    public void onRowUpdated(String tableName, String date, String whoId, String whoName, long timestamp) throws RemoteException {
        if(listener != null) {
            listener.onRowUpdated(tableName, date, whoName, timestamp);
        }
    }

    @Override
    public void onDateAdded(String date, String whoId, String whoName, long timestamp) throws RemoteException {
        if(listener != null) {
            listener.onDateAdded(date, whoName, timestamp);
        }
    }

    @Override
    public void onDateRemoved(String date, String whoId, String whoName, long timestamp) throws RemoteException {
        if(listener != null) {
            listener.onDateRemoved(date, whoName, timestamp);
        }
    }

    @Override
    public String getId() throws RemoteException {
        return id;
    }

    @Override
    public String getName() throws RemoteException {
        return name;
    }

    public void setListener(DatabaseHelperListener listener) {
        this.listener = listener;
    }
}

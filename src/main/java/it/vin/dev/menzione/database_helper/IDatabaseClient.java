package it.vin.dev.menzione.database_helper;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IDatabaseClient extends Remote {

    void onRowInserted(String tableName, String date, String whoId, String whoName, long timestamp) throws RemoteException;
    void onRowUpdated(String tableName, String date, String whoId, String whoName, long timestamp) throws RemoteException;
    void onDateAdded(String date, String whoId, String whoName, long timestamp) throws RemoteException;
    void onDateRemoved(String date, String whoId, String whoName, long timestamp) throws RemoteException;
    String getName() throws RemoteException;
    String getId() throws RemoteException;

}

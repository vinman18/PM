package it.vin.dev.menzione.database_helper;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IDatabaseHelper extends Remote {
    void connect(IDatabaseClient ref, String id) throws RemoteException;
    void notifyRowInserted(String tableName, String date, String whoId, String whoName) throws RemoteException;
    void notifyRowUpdated(String tableName, String date, String whoId, String whoName) throws RemoteException;
    void notifyDateAdded(String date, String whoId, String whoName) throws RemoteException;
    void notifyDateRemoved(String date, String whoId, String whoName) throws RemoteException;
    void disconnect(String id) throws RemoteException;
}


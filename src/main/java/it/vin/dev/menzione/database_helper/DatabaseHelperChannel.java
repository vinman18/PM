package it.vin.dev.menzione.database_helper;

import java.rmi.RemoteException;
import java.sql.Date;

public class DatabaseHelperChannel {
    private static DatabaseHelperChannel ourInstance = new DatabaseHelperChannel();

    public static DatabaseHelperChannel getInstance() {
        return ourInstance;
    }

    private IDatabaseHelper helper;
    private IDatabaseClient client;

    private DatabaseHelperChannel() {
    }

    public void setHelper(IDatabaseHelper helper) {
        this.helper = helper;
    }

    public void setClient(IDatabaseClient client) {
        this.client = client;
    }

    public void notifyRowInserted(String tableName, String date) throws RemoteException {
        if(helper != null) {
            try {
                helper.notifyRowInserted(tableName, date, client.getId(), client.getName());
            } catch (RemoteException e) {
                helper = null;
                throw e;
            }
        }
    }

    public void notifyRowUpdated(String tableName, String date) throws RemoteException {
        if (helper != null) {
            try {
                helper.notifyRowUpdated(tableName, date, client.getId(), client.getName());
            } catch (RemoteException e) {
                helper = null;
                throw e;
            }
        }
    }

    public void notifyDateAdded(String date) throws RemoteException {
        if (helper != null) {
            try {
                helper.notifyDateAdded(date, client.getId(), client.getName());
            } catch (RemoteException e) {
                helper = null;
                throw e;
            }
        }
    }

    public void notifyDateRemoved(String date) throws RemoteException {
        if (helper != null) {
            try {
                helper.notifyDateRemoved(date, client.getId(), client.getName());
            } catch (RemoteException e) {
                helper = null;
                throw e;
            }
        }
    }

    public void disconnect() throws RemoteException {
        if(helper != null && client != null) {
            helper.disconnect(client.getId());
        }
    }
}

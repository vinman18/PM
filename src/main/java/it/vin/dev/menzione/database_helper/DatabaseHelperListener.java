package it.vin.dev.menzione.database_helper;

public interface DatabaseHelperListener {
    void onRowInserted(String tableName, String date, String who, long timestamp);
    void onRowUpdated(String tableName, String date, String who, long timestamp);
    void onDateAdded(String date, String who, long timestamp);
    void onDateRemoved(String date, String who, long timestamp);
}

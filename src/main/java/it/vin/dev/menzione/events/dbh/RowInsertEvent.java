package it.vin.dev.menzione.events.dbh;

public class RowInsertEvent extends RowEvent {
    public RowInsertEvent(String tableName, String date, String whoId, String whoName, long timestamp) {
        super(tableName, date, whoId, whoName, timestamp);
    }
}

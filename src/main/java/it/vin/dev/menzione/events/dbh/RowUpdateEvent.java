package it.vin.dev.menzione.events.dbh;

public class RowUpdateEvent extends RowEvent {
    public RowUpdateEvent(String tableName, String date, String whoId, String whoName, long timestamp) {
        super(tableName, date, whoId, whoName, timestamp);
    }
}

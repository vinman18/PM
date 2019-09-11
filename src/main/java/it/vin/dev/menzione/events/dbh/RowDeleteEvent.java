package it.vin.dev.menzione.events.dbh;

public class RowDeleteEvent extends RowEvent {
    public RowDeleteEvent(String tableName, String date, String whoId, String whoName, long timestamp) {
        super(tableName, date, whoId, whoName, timestamp);
    }
}

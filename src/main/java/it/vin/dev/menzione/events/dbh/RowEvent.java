package it.vin.dev.menzione.events.dbh;

import it.vin.dev.menzione.ViaggiUtils;

import java.sql.Date;

public class RowEvent {
    public String tableName;
    public Date date;
    public String whoId;
    public String whoName;
    public long timestamp;

    public RowEvent(String tableName, String date, String whoId, String whoName, long timestamp) {
        this.tableName = tableName;
        this.date = ViaggiUtils.checkAndCreateDate(date, "-", true);
        this.whoId = whoId;
        this.whoName = whoName;
        this.timestamp = timestamp;
    }
}

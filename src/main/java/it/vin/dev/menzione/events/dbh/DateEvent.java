package it.vin.dev.menzione.events.dbh;

import it.vin.dev.menzione.ViaggiUtils;

import java.sql.Date;

public class DateEvent {
    public Date date;
    public String whoId;
    public String whoName;
    public long timestamp;

    public DateEvent(String date, String whoId, String whoName, long timestamp) {
        this.date = ViaggiUtils.checkAndCreateDate(date, "-", true);
        this.whoId = whoId;
        this.whoName = whoName;
        this.timestamp = timestamp;
    }

    public DateEvent(Date date) {
        this.date = date;
        this.whoId = null;
        this.whoName = null;
        this.timestamp = -1;
    }
}

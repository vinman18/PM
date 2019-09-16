package it.vin.dev.menzione.events.dbh;

import it.vin.dev.menzione.ViaggiUtils;
import it.vin.dev.menzione.events.DateEventSource;

import java.sql.Date;

public class DateEvent {
    public Date date;
    public String whoId;
    public String whoName;
    public long timestamp;

    private DateEventSource dateEventSource;

    public DateEvent(String date, String whoId, String whoName, long timestamp, DateEventSource source) {
        this.date = ViaggiUtils.checkAndCreateDate(date, "-", true);
        this.whoId = whoId;
        this.whoName = whoName;
        this.timestamp = timestamp;
        this.dateEventSource = source;
    }

    public DateEvent(Date date, DateEventSource source) {
        this.date = date;
        this.whoId = null;
        this.whoName = null;
        this.timestamp = -1;
        this.dateEventSource = source;
    }

    public DateEventSource getSource() {
        return dateEventSource;
    }

    public String getWhoId() {
        if(dateEventSource == DateEventSource.THIS_APPLICATION) {
            throw new IllegalStateException("Allowed only if source is DATABASE_HELPER");
        }

        return whoId;
    }

    public String getWhoName() {
        if(dateEventSource == DateEventSource.THIS_APPLICATION) {
            throw new IllegalStateException("Allowed only if source is DATABASE_HELPER");
        }

        return whoName;
    }

    public long getTimestamp() {
        if(dateEventSource == DateEventSource.THIS_APPLICATION) {
            throw new IllegalStateException("Allowed only if source is DATABASE_HELPER");
        }

        return timestamp;
    }


    public Date getDate() {
        return date;
    }
}

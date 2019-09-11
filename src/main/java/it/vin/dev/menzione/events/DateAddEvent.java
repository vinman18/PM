package it.vin.dev.menzione.events;

import it.vin.dev.menzione.events.dbh.DateEvent;

import java.sql.Date;

public class DateAddEvent extends DateEvent {
    public enum DateAddEventSource {
        ADD_DATE_FRAME,
        DATABASE_HELPER
    }

    private DateAddEventSource source;

    public DateAddEvent(Date newLastDate, DateAddEventSource source) {
        super(newLastDate);
        this.source = source;
    }

    public DateAddEvent(String date, String whoId, String whoName, long timestamp, DateAddEventSource source) {
        super(date, whoId, whoName, timestamp);
        this.source = source;
    }

    public DateAddEventSource getSource() {
        return source;
    }

    public Date getDate() {
        return super.date;
    }

    public String getWhoId() {
        if(source == DateAddEventSource.ADD_DATE_FRAME) {
            throw new IllegalStateException("Allowed only if source is DATABASE_HELPER");
        }

        return super.whoId;
    }

    public String getWhoName() {
        if(source == DateAddEventSource.ADD_DATE_FRAME) {
            throw new IllegalStateException("Allowed only if source is DATABASE_HELPER");
        }

        return super.whoName;
    }

    public long getTimestamp() {
        if(source == DateAddEventSource.ADD_DATE_FRAME) {
            throw new IllegalStateException("Allowed only if source is DATABASE_HELPER");
        }

        return super.timestamp;
    }
}

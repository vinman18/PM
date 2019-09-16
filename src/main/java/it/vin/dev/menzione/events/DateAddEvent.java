package it.vin.dev.menzione.events;

import it.vin.dev.menzione.events.dbh.DateEvent;

import java.sql.Date;

public class DateAddEvent extends DateEvent {

    public DateAddEvent(Date newLastDate, DateEventSource source) {
        super(newLastDate, source);
    }

    public DateAddEvent(String date, String whoId, String whoName, long timestamp, DateEventSource source) {
        super(date, whoId, whoName, timestamp, source);
    }

}

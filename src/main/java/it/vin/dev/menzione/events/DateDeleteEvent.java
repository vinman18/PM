package it.vin.dev.menzione.events;

import it.vin.dev.menzione.events.dbh.DateEvent;

import java.sql.Date;

public class DateDeleteEvent extends DateEvent {

    public DateDeleteEvent(String date, String whoId, String whoName, long timestamp, DateEventSource source) {
        super(date, whoId, whoName, timestamp, source);
    }

    public DateDeleteEvent(Date date, DateEventSource source) {
        super(date, source);
    }

}
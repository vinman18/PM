package it.vin.dev.menzione.events.dbh;

public class DateDeleteEvent extends DateEvent{
    public DateDeleteEvent(String date, String whoId, String whoName, long timestamp) {
        super(date, whoId, whoName, timestamp);
    }
}

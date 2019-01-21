package it.vin.dev.menzione.events;

import java.sql.Date;

public class NewDateAddedEvent {
    public enum NewDateEventSource {
        ADD_DATE_FRAME,
        DATABASE_HELPER
    }

    private Date newLastDate;
    private NewDateEventSource source;

    public NewDateAddedEvent(Date newLastDate, NewDateEventSource source) {
        this.newLastDate = newLastDate;
        this.source = source;
    }

    public Date getNewLastDate() {
        return newLastDate;
    }

    public NewDateEventSource getSource() {
        return source;
    }
}

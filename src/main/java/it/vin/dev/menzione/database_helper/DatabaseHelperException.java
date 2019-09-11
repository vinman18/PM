package it.vin.dev.menzione.database_helper;

public class DatabaseHelperException extends Exception {
    public DatabaseHelperException() {
    }

    public DatabaseHelperException(String message) {
        super(message);
    }

    public DatabaseHelperException(String message, Throwable cause) {
        super(message, cause);
    }

    public DatabaseHelperException(Throwable cause) {
        super(cause);
    }
}

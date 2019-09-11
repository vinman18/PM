package it.vin.dev.menzione.database_helper;

public class DatabaseClient {
    private String id;
    private String name;

    public DatabaseClient(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}

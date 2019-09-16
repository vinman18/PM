package it.vin.dev.menzione.logica;

import it.vin.dev.menzione.ViaggiUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class Configuration {

    public static final String USER =                   "local.user";
    public static final String DB_NAME =                "db.name";
    public static final String DB_LOCATION =            "db.location";
    public static final String DB_PORT =                "db.port";
    public static final String DB_USER =                "db.user";
    public static final String DB_PASSWORD =            "db.password";
    public static final String DBHELPER_HOST =          "dbhelper.host";
    public static final String DBHELPER_PORT =          "dbhelper.port";
    public static final String UNDO_WAIT_SECONDS =      "undo.wait.secs";
    public static final String EXISTING_DATES_LIMIT =   "existing.dates.limit";

    private Properties props;

    private static Configuration ourInstance;

    static {
        try{
            ourInstance = new Configuration();
        }catch (IOException e){
            throw new ExceptionInInitializerError(e);
        }
    }

    public static Configuration getInstance(){
        return ourInstance;
    }

    private Configuration() throws IOException {
        String configPath = ViaggiUtils.getConfigPath();

        props = new Properties();
        props.load(new FileInputStream(configPath));
    }

    public String getProperty(String type){
        return props.getProperty(type);
    }

    public Properties getProps(){
        return props;
    }

    public void setConfiguration(String nome, String valore){
        props.setProperty(nome, valore);
    }

    public String getLocation() {
        return props.getProperty(DB_LOCATION);
    }

    public String getDbUser() {
        return props.getProperty(DB_USER);
    }

    public String getDbPassword() {
        return props.getProperty(DB_PASSWORD);
    }

    public String getDbName() {
        return props.getProperty(DB_NAME);
    }

    public String getDbPort() {
        return props.getProperty(DB_PORT);
    }

    public String getDbhelperHost() {
        return props.getProperty(DBHELPER_HOST);
    }

    public String getUser() {
        return props.getProperty(USER, System.getProperty("user.name"));
    }

    public int getDbhelperPort() {
        return Integer.parseInt(props.getProperty(DBHELPER_PORT));
    }

    public void salvaProps() throws IOException {
        FileOutputStream strea = new FileOutputStream(ViaggiUtils.getConfigPath());
        props.store(strea, null);
    }
}

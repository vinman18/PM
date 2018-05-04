package it.vin.dev.menzione.logica;

import it.vin.dev.menzione.ViaggiUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class Configuration {

    /*public Configuration() {
    }

    public static String ip;
    public static String user;
    public static String password;
    public static String logfile;
    public static final int DBVERSION = 3;
    public static final String PROG_VERSION = "1.1b1";

    public static String getLogfile() {
        return logfile;
    }
    public static void setLogfile(String logfile) {
        Configuration.logfile = logfile;
    }
    public static String getLocation() {
        return ip;
    }
    public static void setIp(String ip) {
        Configuration.ip = ip;
    }
    public static String getUser() {
        return user;
    }
    public static void setUser(String user) {
        Configuration.user = user;
    }
    public static String getPassword() {
        return password;
    }
    public static void setPassword(String password) {
        Configuration.password = password;
    }
    */
    public static final String DB_NAME =     "db.name";
    public static final String DB_LOCATION = "db.location";
    public static final String DB_PORT =     "db.port";
    public static final String DB_USER =     "db.user";
    public static final String DB_PASSWORD = "db.password";
    public static final String DBHELPER_HOST = "dbhelper.host";
    public static final String DBHELPER_PORT = "dbhelper.port";

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

    public String getConfiguration(String type){
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

    public String getUser() {
        return props.getProperty(DB_USER);
    }

    public String getPassword() {
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

    public int getDbhelperPort() {
        return Integer.parseInt(props.getProperty(DBHELPER_PORT));
    }

    public void salvaProps() throws IOException {
        FileOutputStream strea = new FileOutputStream(ViaggiUtils.getConfigPath());
        props.store(strea, null);
    }
}

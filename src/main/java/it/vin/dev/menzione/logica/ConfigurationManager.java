package it.vin.dev.menzione.logica;

import it.vin.dev.menzione.VerboseLogger;
import it.vin.dev.menzione.ViaggiUtils;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ConfigurationDecoder;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.event.ConfigurationEvent;
import org.apache.commons.configuration2.event.Event;
import org.apache.commons.configuration2.event.EventListener;
import org.apache.commons.configuration2.ex.ConfigurationException;

import java.util.HashMap;
import java.util.Map;

public class ConfigurationManager {

    public enum FIELD_TYPES {
        INTEGER, TEXT, SECRET
    }

    public static final String USER =                   "local.user";
    public static final String DB_NAME =                "db.name";
    public static final String DB_LOCATION =            "db.location";
    public static final String DB_PORT =                "db.port";
    public static final String DB_USER =                "db.user";
    public static final String DB_PASSWORD =            "db.password";
    public static final String DBHELPER_HOST =          "dbhelper.host";
    public static final String DBHELPER_PORT =          "dbhelper.port";
    public static final String UNDO_WAIT_SECONDS =      "prefs.undo.wait.secs";
    public static final String EXISTING_DATES_LIMIT =   "prefs.existing.dates.limit";

    private static ConfigurationManager ourInstance;
    private static VerboseLogger logger = VerboseLogger.create(ConfigurationManager.class);

    public static final Map<String, FIELD_TYPES> keyTypesMap;

    private FileBasedConfigurationBuilder<FileBasedConfiguration> builder;

    static {
        ourInstance = new ConfigurationManager();

        keyTypesMap = new HashMap<>();
        keyTypesMap.put(USER, FIELD_TYPES.TEXT);
        keyTypesMap.put(DB_NAME, FIELD_TYPES.TEXT);
        keyTypesMap.put(DB_LOCATION, FIELD_TYPES.TEXT);
        keyTypesMap.put(DB_PORT, FIELD_TYPES.INTEGER);
        keyTypesMap.put(DB_USER, FIELD_TYPES.TEXT);
        keyTypesMap.put(DB_PASSWORD, FIELD_TYPES.SECRET);
        keyTypesMap.put(DBHELPER_HOST, FIELD_TYPES.TEXT);
        keyTypesMap.put(DBHELPER_PORT, FIELD_TYPES.INTEGER);
        keyTypesMap.put(UNDO_WAIT_SECONDS, FIELD_TYPES.INTEGER);
        keyTypesMap.put(EXISTING_DATES_LIMIT, FIELD_TYPES.INTEGER);
    }

    public static ConfigurationManager getInstance(){
        return ourInstance;
    }

    private ConfigurationManager() {
        String configPath = ViaggiUtils.getConfigPath();
        Parameters params = new Parameters();
        this.builder = new FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class)
                .configure(params.properties()
                        .setPath(configPath)
                .setConfigurationDecoder(new ConfigurationDecoder() {
                    @Override
                    public String decode(String s) {
                        return ViaggiUtils.decrypt(s);
                    }
                }));
    }

    public Configuration getConfiguration() {
        try {
            return this.builder.getConfiguration();
        } catch (ConfigurationException e) {
            logger.error("Configuration retrieving error", e);
        }

        return new PropertiesConfiguration();
    }

    public void save() throws ConfigurationException {
        this.builder.save();
    }

    public void addConfigurationListener(EventListener<ConfigurationEvent> listener) {
        this.builder.addEventListener(ConfigurationEvent.SET_PROPERTY, listener);
        this.builder.addEventListener(ConfigurationEvent.ADD_PROPERTY, listener);
    }

    public String getDatabaseLocation() {
        return getConfiguration().getString(DB_LOCATION, "127.0.0.1");
    }

    public String getDatabaseUser() {
        return getConfiguration().getString(DB_USER, "admin");
    }

    public String getDatabasePassword() {
        return getConfiguration().getEncodedString(DB_PASSWORD);
    }

    public String getDatabaseName() {
        return getConfiguration().getString(DB_NAME, "gestioneviaggi");
    }

    public int getDatabasePort() {
        return getConfiguration().getInt(DB_PORT, 3306);
    }

    public String getDBHelperHost() {
        return getConfiguration().getString(DBHELPER_HOST, "127.0.0.1");
    }

    public int getDBHelperPort() {
        return getConfiguration().getInt(DBHELPER_PORT, 3000);
    }

    public String getLocalUser() {
        return getConfiguration().getString(USER, System.getProperty("user.name"));
    }

    public int getUndoWaitSeconds() {
        return getConfiguration().getInt(UNDO_WAIT_SECONDS, 4);
    }

    public int getExistingDatesLimit() {
        return getConfiguration().getInt(EXISTING_DATES_LIMIT, 100);
    }
}

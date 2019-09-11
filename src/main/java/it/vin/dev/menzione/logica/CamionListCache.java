package it.vin.dev.menzione.logica;

import com.google.common.eventbus.Subscribe;
import it.vin.dev.menzione.events.CamionCacheUpdated;
import it.vin.dev.menzione.events.CamionEvent;
import it.vin.dev.menzione.events.ViaggiEventsBus;

import java.sql.SQLException;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

public class CamionListCache {
    private static CamionListCache ourInstance = new CamionListCache();

    public static CamionListCache getInstance() {
        return ourInstance;
    }

    private TreeMap<String, Camion> camionCache;

    private boolean isValid = false;

    private CamionListCache() {
        try {
            camionCache = new TreeMap<>();

            loadCacheFromDatabase();

            ViaggiEventsBus.getInstance().register(this);
        } catch (SQLException e) {
            //TODO: add logging
        }
    }

    private void loadCacheFromDatabase() throws SQLException {
        camionCache.clear();
        DatabaseService dbs = DatabaseService.create();
        Vector<Camion> camion = dbs.getCamion();

        for (Camion c : camion) {
            camionCache.put(c.getTarga(), c);
        }

        isValid = true;
        dbs.closeConnection();
    }

    private void validate() {
        if(!isValid) {
            try {
                loadCacheFromDatabase();
            } catch (SQLException e) {
                if(!isValid) {
                    throw new IllegalStateException("The cache is invalid");
                }
            }
        }
    }

    private void invalidate() {
        camionCache.clear();
        isValid = false;
    }

    public Vector<String> getKeyList() {
        validate();
        Set<String> keys = camionCache.keySet();
        return new Vector<>(keys);
    }

    public Camion getElementByTarga(String targa) {
        validate();
        return ((Camion) camionCache.get(targa).clone());
    }

    public void notifyCacheUpdate() {
        invalidate();
        ViaggiEventsBus.getInstance().post(new CamionCacheUpdated());
    }

    @Subscribe
    public void camionEvent(CamionEvent event) {
        notifyCacheUpdate();
    }
}

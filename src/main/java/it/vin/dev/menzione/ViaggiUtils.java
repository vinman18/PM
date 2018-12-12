package it.vin.dev.menzione;

import it.vin.dev.menzione.logica.Camion;
import it.vin.dev.menzione.logica.Ordine;
import it.vin.dev.menzione.logica.Viaggio;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Date;
import java.util.List;

public class ViaggiUtils {

    private static Logger logger = LogManager.getLogger(ViaggiUtils.class);

    public static String getConfigPath() {
        return System.getProperty("user.dir")  + File.separator + "Config.properties";
    }

    public static InputStream getDefaultConfig() {
        return ViaggiUtils.class.getClassLoader().getResourceAsStream("DefaultConfig.properties");
    }

    public static void checkAndCreateConfigFile() throws IOException {
        String configPath = getConfigPath();

        File configFile = new File(configPath);

        InputStream defaultConfig = getDefaultConfig();

        if(!configFile.exists()){
            logger.info("Config file not found. Copying default file in " + configFile.getAbsolutePath());

            Files.copy(defaultConfig,
                    configFile.toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /**
     * Transform a string date in a {@link java.util.Date} Object and check if the input string is a correct date
     * @param date te date string
     * @param separator separator character
     * @param reverseDate true if date's form is yyyy-mm-dd, false if date's form is dd-mm-yyyy
     * @return a java.util.date Object
     * @throws NumberFormatException if the string isn't a correct date
     */
    public static Date checkAndCreateDate(String date, String separator, boolean reverseDate) throws NumberFormatException{
        String[] tmp;
        boolean giornoOK = false, meseOK = false, annoOK = false;
        int giornoTMP = -1, meseTMP = -1, annoTMP = -1;
        Date result = null;

        tmp = date.split(separator);
        if(!reverseDate) {
            giornoTMP = Integer.parseInt(tmp[0]);
            meseTMP = Integer.parseInt(tmp[1]);
            annoTMP = Integer.parseInt(tmp[2]);
        } else {
            giornoTMP = Integer.parseInt(tmp[2]);
            meseTMP = Integer.parseInt(tmp[1]);
            annoTMP = Integer.parseInt(tmp[0]);
        }

        if(giornoTMP > 0 && giornoTMP <= 31) giornoOK = true;
        if(meseTMP > 0 && meseTMP <=12) meseOK = true;
        if(annoTMP > 1990) annoOK = true;

        if(giornoOK && meseOK && annoOK){
            result = Date.valueOf(""+annoTMP+"-"+meseTMP+"-"+giornoTMP);
        } else throw new NumberFormatException();

        return result;
    }

    public static Camion findCamionByTarga(List<Camion> camions, String targa) throws IllegalArgumentException{
        for(Camion c : camions){
            if(c.getTarga().compareTo(targa) == 0)
                return c;
        }
        throw new IllegalArgumentException("Impossibile trovare un camion con targa " + targa + ".");
    }

    public static String getViaggioValueByColumnIndex(Viaggio v, int col) {
        switch (col) {
            case 0:
                return v.getCamion().getTarga();
            case 1:
                return v.getCamion().getCaratteristiche();
            case 2:
                return v.getAutista();
            case 3:
                return v.getNote();
            case 4:
                return Viaggio.NORD.equals(v.getPosizione())
                        ? v.isSelezionato() ? "Selezionato" : "Deselezionato"
                        : String.valueOf(v.getLitriB());
            case 5:
                return v.isSelezionato() ? "Selezionato" : "Deselezionato";
            default:
                return null;
        }
    }

    public static String getOrdineValueFromColumnIndex(Ordine ordine, int col) {
        switch (col) {
            case 0:
                return ordine.getSelezionato() ? "Selezionato" : "Deselezionato";
            case 1:
                return ordine.getData();
            case 2:
                return ordine.getCliente();
            case 3:
                return ordine.getNote();
            default:
                return null;
        }
    }

    public static String createStringFromDate(Date d) {
        String anno, mese, giorno;String[] data = d.toString().split("-");
        anno = data[0];
        mese = data[1];
        giorno = data[2];
        String dat = giorno + "-" + mese +"-"+anno;
        return dat;
    }

    public static URL getMainIcon() {
        return ViaggiUtils.class.getClassLoader().getResource("Icons/main_icon.png");
    }

}

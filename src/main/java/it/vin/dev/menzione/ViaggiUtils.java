package it.vin.dev.menzione;

import com.google.common.base.Charsets;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.*;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Date;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.ResourceBundle;

public class ViaggiUtils {

    private static Logger logger = LogManager.getLogger(ViaggiUtils.class);
    //    private final static String userHome = System.getProperty("user.home");
    private final static String LOCAL_APP_DATA = System.getenv().get("LOCALAPPDATA");

    private static final byte[] KEY_BYTES = "DnyzRUcQuZ4yy68X".getBytes(Charsets.UTF_8);


    public static String getAppPath() {
        return LOCAL_APP_DATA + File.separator + "GestioneViaggi" + File.separator;
    }

    public static String getAppPath(String relativePath) {
        return getAppPath() + relativePath;
    }

    public static String getConfigPath() {
        return getAppPath("Config.properties");
    }

    public static String getColumnsPreferencesPath() {
        return getAppPath("ColumnsPreferences.json");
    }

    public static InputStream getDefaultConfig() {
        return getResourceAsStream("DefaultFiles/Config.properties");
    }

    private static List<String> getResourceFiles(String path) throws IOException {
        List<String> filenames = new ArrayList<>();

        try (
                InputStream in = getResourceAsStream(path);
                BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
            String resource;

            while ((resource = br.readLine()) != null) {
                filenames.add(resource);
            }
        }

        return filenames;
    }

    private static InputStream getResourceAsStream(String resource) {
        final InputStream in
                = getContextClassLoader().getResourceAsStream(resource);

        return in == null ? ViaggiUtils.class.getResourceAsStream(resource) : in;
    }

    private static ClassLoader getContextClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

    public static void checkAndCreateAppFiles() throws IOException {
        String appPath = getAppPath();
//        String configPath = getConfigPath();

        File appFolder = new File(appPath);
//        File configFile = new File(configPath);
//        File columnsFile = new File(getColumnsPreferencesPath());
//        InputStream defaultConfig = getDefaultConfig();
        List<String> defaultFiles = getResourceFiles("DefaultFiles");

        if(!appFolder.exists()) {
            logger.info("App folder not found. Creation...");
            boolean result = appFolder.mkdirs();
            if(!result) {
                throw new IOException("App folder creation return false");
            }
        }

        for(String file : defaultFiles) {
            String resourceFilePath = "DefaultFiles/" + file;
            String fileDestinationPath = getAppPath(file);
            File destinationFile = new File(fileDestinationPath);

            if(!destinationFile.exists()) {
                logger.info("{} file not found. Copying default file in {}", file, destinationFile.getAbsolutePath());

                Files.copy(getResourceAsStream(resourceFilePath),
                        destinationFile.toPath(),
                        StandardCopyOption.REPLACE_EXISTING);
            }
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

    public static String createStringFromDate(java.util.Date d, boolean compactString) {
        int year, month, day;
        String[] data = d.toString().split("-");
        year = Integer.parseInt(data[0]);
        month = Integer.parseInt(data[1]);
        day = Integer.parseInt(data[2]);

        //String dat = day + "-" + month +"-"+year;
        String[] months = ResourceBundle.getBundle("Localization/Strings").getString("generic.months").split(",");

        String dat;

        if(compactString) {
            dat = String.format("%d-%d-%d", day, month, year);
        } else {
            dat = String.format("%d %s %d", day, months[month - 1], year);
        }

        return dat;
    }

    public static String createStringFromDate(java.util.Date date, String separator) {
        String dateString = createStringFromDate(date, true);
        return dateString.replace("-", separator);
    }

    public static java.util.Date convertToDateViaInstant(LocalDate dateToConvert) {
        return java.util.Date.from(dateToConvert.atStartOfDay()
                .atZone(ZoneId.systemDefault())
                .toInstant());
    }

    public static Timer executeAfter(int milliseconds, ActionListener actionListener) {
        Timer t = new Timer(milliseconds, actionListener);
        t.setRepeats(false);
        t.start();
        return t;
    }

    public static Color adjustAlpha(Color color, float factor) {
        int alpha = Math.round(color.getAlpha() * factor);
        int red = color.getRed();
        int green = color.getGreen();
        int blue = color.getBlue();
        return new Color(red, green, blue, alpha);
    }

    public static String encrypt(String str) {
        byte[] cleartext = str.getBytes(Charsets.UTF_8);
        DESKeySpec keySpec = null;
        try {
            keySpec = new DESKeySpec(KEY_BYTES);
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey key = keyFactory.generateSecret(keySpec);
            Base64.Encoder base64encoder = Base64.getEncoder();
            Cipher cipher = Cipher.getInstance("DES"); // cipher is not thread safe
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return new String(base64encoder.encode(cipher.doFinal(cleartext)));
        } catch (InvalidKeyException | NoSuchAlgorithmException | InvalidKeySpecException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String decrypt(String encryptedPwd) {
        DESKeySpec keySpec = null;
        try {
            keySpec = new DESKeySpec(KEY_BYTES);
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey key = keyFactory.generateSecret(keySpec);
            Base64.Decoder base64decoder = Base64.getDecoder();
            byte[] encrypedPwdBytes = base64decoder.decode(encryptedPwd);
            Cipher cipher = Cipher.getInstance("DES");// cipher is not thread safe
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] plainTextPwdBytes = (cipher.doFinal(encrypedPwdBytes));
            return new String(plainTextPwdBytes, Charsets.UTF_8);
        } catch (InvalidKeyException | NoSuchAlgorithmException | InvalidKeySpecException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static URL getMainIcon() {
        return ViaggiUtils.class.getClassLoader().getResource("Icons/main_icon.png");
    }

}

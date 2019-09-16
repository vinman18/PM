package it.vin.dev.menzione;

import java.awt.*;

public class Consts {
    public static final int DBVERSION = 4;

    public static class TABLE_TYPES {
        public final static int VIAGGI_NORD = 0;
        public final static int VIAGGI_SUD = 1;
        public final static int ORDINI_SALITA = 2;
        public final static int ORDINI_DISCESA = 3;
    }

    public static class Colors {
        public final static Color MAIN = new Color(30, 100, 255, 255);
        public final static Color ACCENT = new Color(213, 82, 255, 255);
    }

//    public final static int VIAGGI_TM_TYPE_NORD = 0;
//    public final static int VIAGGI_TM_TYPE_SUD = 1;
//    public final static int ORDINI_SALITA = 2;
//    public final static int ORDINI_DISCESA = 3;

    public final static String VIAGGI_COLUMN_TARGA = "targa";
    public final static String VIAGGI_COLUMN_CARAT = "carat";
    public final static String VIAGGI_COLUMN_AUTISTA = "autista";
    public final static String VIAGGI_COLUMN_NOTE = "note";
    public final static String VIAGGI_COLUMN_LITRI = "litri";
    public final static String VIAGGI_COLUMN_SELECT = "select";

}

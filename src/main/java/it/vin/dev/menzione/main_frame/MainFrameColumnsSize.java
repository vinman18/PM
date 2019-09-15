package it.vin.dev.menzione.main_frame;

import com.google.gson.Gson;
import it.vin.dev.menzione.Consts;
import org.json.JSONObject;

public class MainFrameColumnsSize {

    public static String toJson(MainFrameColumnsSize mainFrameColumnsSizeObject) {
        Gson gson = new Gson();
        return gson.toJson(mainFrameColumnsSizeObject);
    }

    public static MainFrameColumnsSize fromJson(String jsonRepresentation) {
        return new Gson().fromJson(jsonRepresentation, MainFrameColumnsSize.class);
    }

    public float[] viaggiNord = {0.15f, 0.2f, 0.15f, 0.510f, 0.035f};
    public float[] viaggiSud = {0.15f, 0.2f, 0.15f, 0.433f, 0.05f, 0.035f};
    public float[] ordiniSalita = {0.035f, 0.10f, 0.35f, 0.515f};
    public float[] ordiniDiscesa = {0.035f, 0.10f, 0.35f, 0.515f};

    public MainFrameColumnsSize(float[] viaggiNord, float[] viaggiSud, float[] ordiniSalita, float[] ordiniDiscesa) {
        this.viaggiNord = viaggiNord;
        this.viaggiSud = viaggiSud;
        this.ordiniSalita = ordiniSalita;
        this.ordiniDiscesa = ordiniDiscesa;
    }

    public MainFrameColumnsSize() {
    }

    public float[] getTableColumnsSize(int tableType) {
        switch (tableType) {
            case Consts.TABLE_TYPES.VIAGGI_NORD: return this.viaggiNord;
            case Consts.TABLE_TYPES.VIAGGI_SUD: return this.viaggiSud;
            case Consts.TABLE_TYPES.ORDINI_SALITA: return this.ordiniSalita;
            case Consts.TABLE_TYPES.ORDINI_DISCESA: return this.ordiniDiscesa;
        }

        throw new IllegalArgumentException("Invalid tableType");
    }
}

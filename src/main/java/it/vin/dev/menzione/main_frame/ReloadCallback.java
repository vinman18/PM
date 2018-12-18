package it.vin.dev.menzione.main_frame;

import java.sql.Date;
import java.sql.SQLException;

public interface ReloadCallback {
    void reloadTableModel(Date d, int option) throws SQLException;

    void reloadOrdiniModel(Date d) throws SQLException;

    void reloadNote(Date d) throws SQLException;

    void loadDate(Date d, int mode);
}

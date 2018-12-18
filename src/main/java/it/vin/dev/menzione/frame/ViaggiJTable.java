package it.vin.dev.menzione.frame;

import java.awt.Color;
import java.awt.Component;
import java.util.List;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import com.google.common.eventbus.Subscribe;
import it.vin.dev.menzione.Consts;
import it.vin.dev.menzione.events.CamionCacheUpdated;
import it.vin.dev.menzione.events.ViaggiEventBus;
import it.vin.dev.menzione.logica.Camion;
import it.vin.dev.menzione.logica.CamionListCache;

import static it.vin.dev.menzione.Consts.*;

public class ViaggiJTable extends JTable {
    private int type;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public ViaggiJTable() {
        super();
        setCellSelectionEnabled(true);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        ViaggiEventBus.getInstance().register(this);
    }

    public ViaggiJTable(int type) {
        this();
        this.type = type;
    }

    @Override
    public void editingStopped(ChangeEvent e){
        int row = getEditingRow();
        int col = getEditingColumn();

        super.editingStopped(e);

        if(col == 0){
            changeSelection(row, 2, false, false);
            editCellAt(row, 2);
            requestFocus();
        }

        if(type == VIAGGI_TM_TYPE_NORD){
            if(col == 4){
                changeSelection(row, 0, false, false);
            }
        }else if(type == VIAGGI_TM_TYPE_SUD){
            changeSelection(row, 0, false, false);
        }
    }

    @Override
    public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
        Component c = super.prepareRenderer(renderer, row, col);
        int selCol = 0;
        if(type == VIAGGI_TM_TYPE_NORD){
            selCol = 4;
        }else if(type == VIAGGI_TM_TYPE_SUD){
            selCol = 5;
        }

        if(isCellSelected(row, col)){
            c.setBackground(super.getSelectionBackground());
            c.setForeground(super.getSelectionForeground());
        }else{
            boolean b = (boolean) getValueAt(row, selCol);
            if (b) {
                c.setBackground(new Color(255, 255, 80));
                c.setForeground(Color.BLACK);
            } else {
                c.setBackground(super.getBackground());
                c.setForeground(super.getForeground());
            }
        }
        return c;
    }

    /*private void setColumnIdentifiers() {
        getColumn(0).setIdentifier(Consts.VIAGGI_COLUMN_TARGA);
        getColumn(1).setIdentifier(Consts.VIAGGI_COLUMN_CARAT);
        getColumn(2).setIdentifier(Consts.VIAGGI_COLUMN_AUTISTA);
        getColumn(3).setIdentifier(Consts.VIAGGI_COLUMN_NOTE);

        if(type == Consts.VIAGGI_TM_TYPE_NORD) {
            getColumn(4).setIdentifier(Consts.VIAGGI_COLUMN_SELECT);
        } else {
            getColumn(4).setIdentifier(Consts.VIAGGI_COLUMN_LITRI);
            getColumn(5).setIdentifier(Consts.VIAGGI_COLUMN_SELECT);
        }
    }*/

    private void populateCamionDrop() {
        JComboBox<String> camionCombo = new JComboBox<>();
        List<String> camions = CamionListCache.getInstance().getKeyList();
        for(String c : camions){
            camionCombo.addItem(c);
        }

        TableColumn col;
        col = getColumnModel().getColumn(0); //Targa column
        col.setCellEditor(new DefaultCellEditor(camionCombo));
        col.setPreferredWidth(75);
    }

    public void doTableLayout() {
        populateCamionDrop();

        TableColumn col;
        col = getColumnModel().getColumn(1); //Caratt column
        col.setPreferredWidth(150);
        col = getColumnModel().getColumn(3); //Note column
        col.setPreferredWidth(300);
        if(type == Consts.VIAGGI_TM_TYPE_NORD) {
            col = getColumnModel().getColumn(4); //Nord Select column
        } else {
            getColumnModel().getColumn(4).setPreferredWidth(25); //Sud litri column
            col = getColumnModel().getColumn(5); //Sud select column
        }
        col.setPreferredWidth(16);
    }

    @Subscribe
    private void camionCacheUpdated(CamionCacheUpdated event) {
        populateCamionDrop();
    }
}

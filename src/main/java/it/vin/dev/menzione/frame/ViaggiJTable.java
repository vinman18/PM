package it.vin.dev.menzione.frame;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.List;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import com.google.common.eventbus.Subscribe;
import it.vin.dev.menzione.events.CamionCacheUpdated;
import it.vin.dev.menzione.events.ViaggiEventsBus;
import it.vin.dev.menzione.logica.CamionListCache;
import it.vin.dev.menzione.main_frame.MainFrameColumnsSize;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static it.vin.dev.menzione.Consts.*;

@SuppressWarnings("DuplicatedCode")
public class ViaggiJTable extends JTable {
    private int type = -1;
    private float[] columnWidthWeights = null;
    private int oldTableWidth = 0;

    private Logger logger = LogManager.getLogger(this.getClass());

    public ViaggiJTable() {
        super();
        setCellSelectionEnabled(true);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
        getTableHeader().setReorderingAllowed(false);
        ViaggiEventsBus.getInstance().register(this);

        addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if("model".equalsIgnoreCase(evt.getPropertyName())) {
                    if(evt.getNewValue() != null && (evt.getNewValue() instanceof TableModel)) {
                        logger.debug("Model changed");
                        ((TableModel) evt.getNewValue()).addTableModelListener(new TableModelListener() {
                            @Override
                            public void tableChanged(TableModelEvent e) {
                                populateCamionDrop();
                            }
                        });
                    }
                }
            }
        });

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int newTableWidth = e.getComponent().getWidth();
                if(Math.abs(oldTableWidth - newTableWidth) > 50) {
                    logger.debug("Recalculate table columns with width={}", e.getComponent().getWidth());
                    doTableLayout(columnWidthWeights);
                    oldTableWidth = newTableWidth;
                }
            }
        });
    }

    public ViaggiJTable(int type) {
        this();
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
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

        if(type == TABLE_TYPES.VIAGGI_NORD){
            if(col == 4){
                changeSelection(row, 0, false, false);
            }
        }else if(type == TABLE_TYPES.VIAGGI_SUD){
            changeSelection(row, 0, false, false);
        }
    }

    @Override
    public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
//        logger.entry(renderer, row, col);
        Component c = super.prepareRenderer(renderer, row, col);
        int selCol = 0;
        if(type == TABLE_TYPES.VIAGGI_NORD){
            selCol = 4;
        }else if(type == TABLE_TYPES.VIAGGI_SUD){
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

    public float[] getColumnsSize() {
        TableColumnModel columnModel = getColumnModel();
        int width = columnModel.getTotalColumnWidth();
        float[] toReturn = new float[columnModel.getColumnCount()];
        for(int i = 0; i < columnModel.getColumnCount(); i++) {
            toReturn[i] = ((float) columnModel.getColumn(i).getWidth() / width);
        }

        return toReturn;
    }

    private void populateCamionDrop() {
        JComboBox<String> camionCombo = new JComboBox<>();
        List<String> camions = CamionListCache.getInstance().getKeyList();
        for(String c : camions){
            camionCombo.addItem(c);
        }

        TableColumn col;
        col = getColumnModel().getColumn(0); //Targa column
        col.setCellEditor(new DefaultCellEditor(camionCombo));
    }

    public void doTableLayout(float[] columnsSize) {
//        populateCamionDrop();
        if(this.columnWidthWeights != columnsSize) {
            this.columnWidthWeights = columnsSize;
        }

        if(columnsSize == null) {
            TableColumn col;
            col = getColumnModel().getColumn(1); //Caratt column
            col.setPreferredWidth(150);
            col = getColumnModel().getColumn(3); //Note column
            col.setPreferredWidth(300);
            if (type == TABLE_TYPES.VIAGGI_NORD) {
                col = getColumnModel().getColumn(4); //Nord Select column
            } else {
                getColumnModel().getColumn(4).setPreferredWidth(25); //Sud litri column
                col = getColumnModel().getColumn(5); //Sud select column
            }
            col.setPreferredWidth(16);
        } else {
            int width = getWidth();
            for (int i = 0; i < columnsSize.length; i++) {
                getColumnModel().getColumn(i).setPreferredWidth(Math.round(columnsSize[i] * width));
                if(i == getColumnCount() - 1) { // select column
                    getColumnModel().getColumn(i).setMaxWidth(Math.round(columnsSize[i] * width));
                    getColumnModel().getColumn(i).setMinWidth(Math.round(columnsSize[i] * width));
                    getColumnModel().getColumn(i - 1).setResizable(false);
                }
            }
        }
    }

    @Subscribe
    private void camionCacheUpdated(CamionCacheUpdated event) {
        populateCamionDrop();
    }
}

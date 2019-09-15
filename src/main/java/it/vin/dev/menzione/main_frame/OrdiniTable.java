package it.vin.dev.menzione.main_frame;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Vector;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;


public class OrdiniTable extends JTable {
    private int oldTableWidth = 0;
    private float[] columnWidthWeights = null;

    public OrdiniTable() {
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
        getTableHeader().setReorderingAllowed(false);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int newTableWidth = e.getComponent().getWidth();
                if(Math.abs(oldTableWidth - newTableWidth) > 50) {
                    doTableLayout(columnWidthWeights);
                    oldTableWidth = newTableWidth;
                }
            }
        });
    }

    @Override
    public void editingStopped(ChangeEvent e){
        int row = getEditingRow();
        int col = getEditingColumn();

        super.editingStopped(e);

        if(col == 0){
            changeSelection(row, 3, false, false);
            changeSelection(row, 1, false, false);
        }
    }

    @Override
    public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
        Component c = super.prepareRenderer(renderer, row, col);
        int selCol = 0;

        if(isCellSelected(row, col)){
            c.setBackground(super.getSelectionBackground());
            c.setForeground(super.getSelectionForeground());
        }else{
            boolean b = (boolean) getValueAt(row, selCol);
            if (b) {
                c.setBackground(Color.YELLOW);
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

    public void doTableLayout(float[] columnsSize) {
        if(this.columnWidthWeights != columnsSize) {
            this.columnWidthWeights = columnsSize;
        }

        if(columnsSize == null) {
            int columns = getColumnModel().getColumnCount();
            for (int i = 0; i < columns; i++) {
                TableColumn col = getColumnModel().getColumn(i);
                if (i == 1) {
                    col.setMaxWidth(75);
                    col.setMaxWidth(100);
                    col.setPreferredWidth(75);
                } else if (i == 2) {
                    col.setMaxWidth(125);
                    col.setMinWidth(80);
                    col.setPreferredWidth(80);
                } else if (i == 0) {
                    col.setMaxWidth(16);
                    col.setMinWidth(16);
                    col.setPreferredWidth(16);
                }
            }
        } else {
            for (int i = 0; i < columnsSize.length; i++) {
                double width = getWidth();

                TableColumn column = getColumnModel().getColumn(i);
//                System.out.println(columnsSize[i] + " * " + width + " = " + Math.round(columnsSize[i] * width));
                column.setPreferredWidth((int) Math.round(columnsSize[i] * width));
                if(i == 0) {
                    column.setMaxWidth((int) Math.round(columnsSize[i] * width));
                    column.setMinWidth((int) Math.round(columnsSize[i] * width));
                    column.setResizable(false);
                }
            }
        }
    }
}

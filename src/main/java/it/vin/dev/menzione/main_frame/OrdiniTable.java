package it.vin.dev.menzione.main_frame;

import java.awt.Color;
import java.awt.Component;
import java.util.Vector;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;


public class OrdiniTable extends JTable {

	private static final long serialVersionUID = 139390780226189146L;

	public OrdiniTable() {
	}

	public OrdiniTable(TableModel dm) {
		super(dm);
	}

	public OrdiniTable(TableModel dm, TableColumnModel cm) {
		super(dm, cm);
	}

	public OrdiniTable(int numRows, int numColumns) {
		super(numRows, numColumns);
	}

	@SuppressWarnings("rawtypes")
	public OrdiniTable(Vector rowData, Vector columnNames) {
		super(rowData, columnNames);
	}

	public OrdiniTable(Object[][] rowData, Object[] columnNames) {
		super(rowData, columnNames);
	}

	public OrdiniTable(TableModel dm, TableColumnModel cm, ListSelectionModel sm) {
		super(dm, cm, sm);
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

	public void doTableLayout() {
	    int columns = getColumnModel().getColumnCount();
        for(int i=0; i<columns; i++){
            TableColumn col = getColumnModel().getColumn(i);
            if(i == 1) {
                col.setMaxWidth(75);
                col.setMaxWidth(100);
                col.setPreferredWidth(75);
            }else if(i == 2){
                col.setMaxWidth(125);
                col.setMinWidth(80);
                col.setPreferredWidth(80);
            }else if(i == 0){
                col.setMaxWidth(16);
                col.setMinWidth(16);
                col.setPreferredWidth(16);
            }
        }
    }
}

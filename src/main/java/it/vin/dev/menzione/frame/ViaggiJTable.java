package it.vin.dev.menzione.frame;

import java.awt.Color;
import java.awt.Component;
import java.util.Vector;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import it.vin.dev.menzione.logica.Viaggio;

public class ViaggiJTable extends JTable {

	private static final long serialVersionUID = 3993661459218810323L;
	private String type;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public ViaggiJTable() {
	}

	public ViaggiJTable(TableModel arg0) {
		super(arg0);
	}

	public ViaggiJTable(TableModel arg0, TableColumnModel arg1) {
		super(arg0, arg1);
	}

	public ViaggiJTable(int arg0, int arg1) {
		super(arg0, arg1);
	}

	@SuppressWarnings("rawtypes")
	public ViaggiJTable(Vector arg0, Vector arg1) {
		super(arg0, arg1);
	}

	public ViaggiJTable(Object[][] arg0, Object[] arg1) {
		super(arg0, arg1);
	}

	public ViaggiJTable(TableModel arg0, TableColumnModel arg1, ListSelectionModel arg2) {
		super(arg0, arg1, arg2);
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
		if(type.compareTo(Viaggio.NORD) == 0){
			if(col == 4){
				changeSelection(row, 0, false, false);
			}
		}else if(type.compareTo(Viaggio.SUD) == 0){
			changeSelection(row, 0, false, false);
		}
	}

	@Override
	public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
		Component c = super.prepareRenderer(renderer, row, col);
		int selCol = 0;
		if(type.compareTo(Viaggio.NORD) == 0){
			selCol = 4;
		}else if(type.compareTo(Viaggio.SUD) == 0){
			selCol = 5;
		}
		if(isCellSelected(row, col)){
			c.setBackground(super.getSelectionBackground());
			c.setForeground(super.getSelectionForeground());
		}else{
			boolean b = (boolean) getValueAt(row, selCol);
			if (b == true) {
				c.setBackground(new Color(255, 255, 80));
				c.setForeground(Color.BLACK);
			} else {
				c.setBackground(super.getBackground());
				c.setForeground(super.getForeground());
			}
		}
		return c;
	}
}

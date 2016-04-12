package it.vin.dev.menzione.frame;

import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.table.AbstractTableModel;

import it.vin.dev.menzione.logica.Ordine;

public class OrdiniTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 3190609378048624032L;
	protected Vector<Ordine> ordini;
	private String[] colonne = {"", "Data", "Cliente", "Note"};
	private Logger log = Logger.getGlobal();
	
	public Vector<Ordine> getData(){
		return ordini;
	}
	
	public OrdiniTableModel() {
		ordini = new Vector<>();
	}
	
	public OrdiniTableModel(Vector<Ordine> ordini) {
		this.ordini = ordini;
	}

	public void setData(Vector<Ordine> ordini){
		this.ordini = ordini;
		fireTableDataChanged();
	}
	
	
	@Override
	public int getColumnCount() {
		return colonne.length;
	}

	@Override
	public int getRowCount() {
		return ordini.size();
	}

	@Override
	public Class<?> getColumnClass(int col){
		return getValueAt(0, col).getClass();
	}
	
	@Override
	public String getColumnName(int col){
		return colonne[col];
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		Ordine o = ordini.elementAt(row);
		try {
			switch(col){
				case 0: return o.getSelezionato();
				case 1: return o.getData();
				case 2: return o.getCliente();
				case 3: return o.getNote();
			}
		} catch (NullPointerException e) {
			return "";
		}
		return "";
	}
	
	@Override
	public void setValueAt(Object value, int row, int col){
		log.info("OrdiniTable cell update. Riga="+row+" Colonna="+col+" Valore="+value.toString());
		if(col == 0){
			ordini.elementAt(row).setSelezionato(Boolean.parseBoolean(value.toString()));
		}else if(col == 1){
			ordini.elementAt(row).setData(value.toString());
		} else if(col == 2){
			if(value.toString().length() == 0) {
				throw new IllegalArgumentException();
			}
			ordini.elementAt(row).setCliente(value.toString());
			//log.info(viaggi.elementAt(row).getAutista());
		} else if(col == 3){
			ordini.elementAt(row).setNote(value.toString());
			//log.info(viaggi.elementAt(row).getNote());
		} 
			fireTableCellUpdated(row, col);
	}
	
	@Override
	public boolean isCellEditable(int row, int col){
		if(col >= 0) return true;
		else return false;
	}
	
	public Ordine getElementAt(int row){
		
		if(row >= 0){
			return ordini.elementAt(row);
		}
		
		return null;
	}
	
	
	public void addRow(Ordine o){
		ordini.addElement(o);
		int index = ordini.indexOf(o);
		fireTableRowsInserted(index, index);
	}
	
	public Ordine removeRow(int row){
		Ordine tmp = ordini.remove(row);
		fireTableRowsDeleted(row, row);
		return tmp;
	}
	


}

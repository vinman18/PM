package it.vin.dev.menzione.main_frame;

import java.util.Vector;

import javax.swing.table.AbstractTableModel;

import it.vin.dev.menzione.logica.Nota;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NoteTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 3190609378048624032L;
	protected Vector<Nota> note;
	private String[] colonne = {"Testo"};
	private Logger log = LogManager.getLogger(this.getClass());


	public Vector<Nota> getData(){
		return note;
	}
	
	public NoteTableModel() {
		note = new Vector<>();
	}
	
	public NoteTableModel(Vector<Nota> note) {
		this.note = note;
	}

	public void setData(Vector<Nota> note){
		this.note = note;
		fireTableDataChanged();
	}
	
	
	@Override
	public int getColumnCount() {
		return colonne.length;
	}

	@Override
	public int getRowCount() {
		return note.size();
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
		Nota n = note.elementAt(row);
		try {
			switch(col){
				case 0: return n.getTesto();
			}
		} catch (NullPointerException e) {
			return "";
		}
		return "";
	}
	
	@Override
	public void setValueAt(Object value, int row, int col){
		log.info("NoteTable cell update. Riga="+row+" Colonna="+col+" Valore="+value.toString());
		if(col == 0){
			note.elementAt(row).setTesto(value.toString());
		}
			fireTableCellUpdated(row, col);
	}
	
	@Override
	public boolean isCellEditable(int row, int col){
		if(col >= 0) return true;
		else return false;
	}
	
	public Nota getElementAt(int row){
		
		if(row >= 0){
			return note.elementAt(row);
		}
		
		return null;
	}
	
	
	public void addRow(Nota n){
		note.addElement(n);
		int index = note.indexOf(n);
		fireTableRowsInserted(index, index);
	}
	
	public Nota removeRow(int row){
		if(row < 0) {
			throw new IllegalArgumentException("Invalid row number");
		}

		Nota tmp = note.remove(row);
		fireTableRowsDeleted(row, row);
		return tmp;
	}
	


}

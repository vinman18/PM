package it.vin.dev.menzione.frame;

import java.sql.Date;
import java.util.Vector;
import java.util.logging.Logger;
import javax.swing.table.AbstractTableModel;
import it.vin.dev.menzione.logica.*;

public class ViaggiTableModel extends AbstractTableModel {


	private static final long serialVersionUID = -8602306676258552807L;
	protected Vector<Viaggio> viaggi;
	private String[] colonne;
	protected String type;
	private Date currentDate;
	private Logger log;


	public Vector<Viaggio> getData(){
		return viaggi;
	}

	public ViaggiTableModel(String type) {
		this.type = type;
		viaggi = new Vector<>();
		log = Logger.getLogger("global");
		if(type == Viaggio.NORD){
			String[] tmp = {"Targa", "Caratteristiche", "Autista", "Note", ""};
			colonne = tmp;
		}else{
			String[] tmp = {"Targa", "Caratteristiche", "Autista", "Note", "Litri", ""};
			colonne = tmp;
		}

	}

	public ViaggiTableModel(Vector<Viaggio> viaggi, String type) {
		this.type = type;
		this.viaggi = viaggi;
		log = Logger.getLogger("global");
		if(type == Viaggio.NORD){
			String[] tmp = {"Targa", "Caratteristiche", "Autista", "Note", ""};
			colonne = tmp;
		}else{
			String[] tmp = {"Targa", "Caratteristiche", "Autista", "Note", "Litri", ""};
			colonne = tmp;
		}
	}

	public void setData(Vector<Viaggio> viaggi){
		this.viaggi = viaggi;
		fireTableDataChanged();
	}


	public Date getCurrentDate() {
		return currentDate;
	}

	public void setCurrentDate(Date currentDate) {
		this.currentDate = currentDate;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public int getColumnCount() {
		return colonne.length;
	}

	@Override
	public int getRowCount() {
		return viaggi.size();
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
		Viaggio v = viaggi.elementAt(row);
		try {
			if(type == Viaggio.NORD){
				switch(col){
				case 0: return v.getCamion().getTarga();
				case 1: return v.getCamion().getCaratteristiche();
				case 2: return v.getAutista();
				case 3: return v.getNote();
				case 4: return v.isSelezionato();
				}
			}else if(type == Viaggio.SUD){
				switch(col){
				case 0: return v.getCamion().getTarga();
				case 1: return v.getCamion().getCaratteristiche();
				case 2: return v.getAutista();
				case 3: return v.getNote();
				case 4: return v.getLitriB();
				case 5: return v.isSelezionato();
				}
			}
		} catch (NullPointerException e) {
			return "";
		}
		return "";
	}

	@Override
	public void setValueAt(Object value, int row, int col){
		if(type == Viaggio.NORD){
			if(col == 0){
				Camion c = MainFrame.findCamionByTarga(value.toString());
				viaggi.elementAt(row).setCamion(c);
			} else if(col == 2){
				viaggi.elementAt(row).setAutista(value.toString());
				//log.info(viaggi.elementAt(row).getAutista());
			} else if(col == 3){
				viaggi.elementAt(row).setNote(value.toString());
				//log.info(viaggi.elementAt(row).getNote());
			} else if(col == 4){
				viaggi.elementAt(row).setSelezionato(Boolean.parseBoolean(value.toString()));
				//log.info(""+viaggi.elementAt(row).isSelezionato());
			}
		}else{
			if(col == 0){
				Camion c = MainFrame.findCamionByTarga(value.toString());
				viaggi.elementAt(row).setCamion(c);
			} else if(col == 2){
				viaggi.elementAt(row).setAutista(value.toString());
				//log.info(viaggi.elementAt(row).getAutista());
			} else if(col == 3){
				viaggi.elementAt(row).setNote(value.toString());
				//log.info(viaggi.elementAt(row).getNote());
			} else if(col == 4){
				viaggi.elementAt(row).setLitriB(Integer.parseInt(value.toString()));
				//log.info(""+viaggi.elementAt(row).getLitriB());
			} else if(col == 5){
				viaggi.elementAt(row).setSelezionato(Boolean.parseBoolean(value.toString()));
				//log.info(""+viaggi.elementAt(row).isSelezionato());
			}
		}
		fireTableCellUpdated(row, col);
	}

	@Override
	public boolean isCellEditable(int row, int col){
		if(col == 0 & getValueAt(row, col) == "" ) return true;
		else if(col >= 2) return true;
		else return false;
	}

	public Viaggio getElementAt(int row){

		if(row >= 0){
			return viaggi.elementAt(row);
		}

		return null;
	}

	/*@Override
	public void fireTableRowsInserted(int firstRow, int lastRow){
		super.fireTableRowsInserted(firstRow, lastRow);
		log.severe("Eseguito firetablerowsinserted in " + getClass().getName());
	}*/


	public void addRow(Viaggio v){
		if(v==null){
			Viaggio nuovo = new Viaggio();
			nuovo.setPosizione(type);
			nuovo.setData(currentDate);
			viaggi.addElement(nuovo);
			fireTableRowsInserted(getRowCount() , getRowCount());
		}else{
			viaggi.addElement(v);
			fireTableRowsInserted(getRowCount() , getRowCount());
		}
	}

	public Viaggio removeRow(int row){
		Viaggio rimosso = viaggi.elementAt(row);
		viaggi.removeElementAt(row);
		fireTableRowsDeleted(row, row);
		return rimosso;
	}

	public int existsCamion(Camion c){
		int result = 0;
		for(Viaggio v : viaggi){
			if(v.getCamion().getTarga().compareTo(c.getTarga()) == 0)
				result++;
		}

		return result;
	}
}
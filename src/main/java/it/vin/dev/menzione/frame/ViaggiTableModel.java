package it.vin.dev.menzione.frame;

import java.sql.Date;
import java.util.List;
import java.util.Vector;
import javax.swing.table.AbstractTableModel;

import it.vin.dev.menzione.Consts;
import it.vin.dev.menzione.ViaggiUtils;
import it.vin.dev.menzione.logica.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ViaggiTableModel extends AbstractTableModel {


	private static final long serialVersionUID = -8602306676258552807L;
	protected Vector<Viaggio> viaggi;
	private String[] colonne;
	protected int type;
	private Date currentDate;
	private Logger log;

	private List<Camion> camions;

	public Vector<Viaggio> getData(){
		return viaggi;
	}

	public ViaggiTableModel(int type, List<Camion> camions) {
		this.type = type;
		this.camions = camions;
		viaggi = new Vector<>();
		log = LogManager.getLogger(this.getClass());
		if(type == Consts.VIAGGI_TM_TYPE_NORD){
			colonne = new String[]{"Targa", "Caratteristiche", "Autista", "Note", ""};
		}else{
			colonne = new String[]{"Targa", "Caratteristiche", "Autista", "Note", "Litri", ""};
		}

	}

	public ViaggiTableModel(Vector<Viaggio> viaggi, int type, List<Camion> camions) {
		this(type, camions);
	    this.viaggi = viaggi;
        /*this.type = type;
        this.camions = camions;
		log = Logger.getLogger("global");
		if(Viaggio.NORD.equals(type)){
			colonne = new String[]{"Targa", "Caratteristiche", "Autista", "Note", ""};
		}else{
			colonne = new String[]{"Targa", "Caratteristiche", "Autista", "Note", "Litri", ""};
		}*/
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

	public int getType() {
		return type;
	}

	public void setType(int type) {
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
			if(type == Consts.VIAGGI_TM_TYPE_NORD){
				switch(col){
				case 0: return v.getCamion().getTarga();
				case 1: return v.getCamion().getCaratteristiche();
				case 2: return v.getAutista();
				case 3: return v.getNote();
				case 4: return v.isSelezionato();
				}
			}else if(type == Consts.VIAGGI_TYPE_SUD){
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
		if(type == Consts.VIAGGI_TM_TYPE_NORD){
			if(col == 0){
				Camion c = ViaggiUtils.findCamionByTarga(camions, value.toString());
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
				Camion c = ViaggiUtils.findCamionByTarga(camions, value.toString());
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
		if(col == 0 && getValueAt(row, col).toString().isEmpty()) return true;
		else if(col >= 2) return true;
		else return false;
	}

	public Viaggio getElementAt(int row){

		if(row >= 0){
			return viaggi.elementAt(row);
		}

		return null;
	}


	public void addRow(Viaggio v){
		if(v == null){
			Viaggio nuovo = new Viaggio();
			String posizione = type == Consts.VIAGGI_TM_TYPE_NORD ? Viaggio.NORD : Viaggio.SUD;
			nuovo.setPosizione(posizione);
			nuovo.setData(currentDate);
			viaggi.addElement(nuovo);
			fireTableRowsInserted(getRowCount() , getRowCount());
		}else{
			viaggi.addElement(v);
			fireTableRowsInserted(getRowCount() , getRowCount());
		}
	}

	public Viaggio removeRow(int row){
		fireTableRowsDeleted(row, row);
		Viaggio rimosso = viaggi.elementAt(row);
		viaggi.removeElementAt(row);
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

	public List<Camion> getCamions() {
	    return camions;
    }

	public void setCamions(List<Camion> camions) {
	    this.camions = camions;
    }
}
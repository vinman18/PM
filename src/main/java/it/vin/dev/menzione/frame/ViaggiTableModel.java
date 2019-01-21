package it.vin.dev.menzione.frame;

import java.sql.Date;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;

import it.vin.dev.menzione.Consts;
import it.vin.dev.menzione.logica.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ViaggiTableModel extends AbstractTableModel {

	private static final String[] COL_NORD = new String[]{"Targa", "Caratteristiche", "Autista", "Note", ""};
	private static final String[] COL_SUD = new String[]{"Targa", "Caratteristiche", "Autista", "Note", "Litri", ""};


	protected Vector<Viaggio> viaggi;
	private String[] colonne;
	protected int type;
	private Date currentDate;

	public static Object getViaggioValueByColumnIndex(Viaggio v, int col) {
		switch (col) {
			case 0:
				return v.getCamion().getTarga();
			case 1:
				return v.getCamion().getCaratteristiche();
			case 2:
				return v.getAutista();
			case 3:
				return v.getNote();
			case 4:
				return Viaggio.NORD.equals(v.getPosizione())
						? v.isSelezionato()
						: v.getLitriB();
			case 5:
				return v.isSelezionato();
			default:
				return null;
		}
	}

	public static String getViaggioColumnNameByIndex(ViaggiTableModel model, int col) {
		String[] colums = model.getType() == Consts.VIAGGI_TM_TYPE_NORD ? COL_NORD : COL_SUD;

		return colums[col];
	}

	public static String getTableModelName(ViaggiTableModel model) {
	    return "Viaggi " + (model.getType() == Consts.VIAGGI_TM_TYPE_NORD ? "NORD" : "SUD");
    }

	public Vector<Viaggio> getData(){
		return viaggi;
	}

	public ViaggiTableModel(int type) {
		this.type = type;
		viaggi = new Vector<>();
		if(type == Consts.VIAGGI_TM_TYPE_NORD){
			colonne = COL_NORD;
		}else{
			colonne = COL_SUD;
		}
	}

	public ViaggiTableModel(Vector<Viaggio> viaggi, int type) {
		this(type);
	    this.viaggi = viaggi;
	}

	public void setData(Vector<Viaggio> viaggi){
		this.viaggi = viaggi;
		fireTableDataChanged();
	}

    @Override
    public void addTableModelListener(TableModelListener l) {
        super.addTableModelListener(l);
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
			}else if(type == Consts.VIAGGI_TM_TYPE_SUD){
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
	public void setValueAt(Object value, int row, int col) {
		if(type == Consts.VIAGGI_TM_TYPE_NORD){
			if(col == 0){
				Camion c = CamionListCache.getInstance().getElementByTarga(value.toString());
				viaggi.elementAt(row).setCamion(c);
			} else if(col == 2){
				viaggi.elementAt(row).setAutista(value.toString());
			} else if(col == 3){
				viaggi.elementAt(row).setNote(value.toString());
			} else if(col == 4){
				viaggi.elementAt(row).setSelezionato(Boolean.parseBoolean(value.toString()));
			}
		}else{
			if(col == 0){
				Camion c = CamionListCache.getInstance().getElementByTarga(value.toString());
				viaggi.elementAt(row).setCamion(c);
			} else if(col == 2){
				viaggi.elementAt(row).setAutista(value.toString());
			} else if(col == 3){
				viaggi.elementAt(row).setNote(value.toString());
			} else if(col == 4){
				viaggi.elementAt(row).setLitriB(Integer.parseInt(value.toString()));
			} else if(col == 5){
				viaggi.elementAt(row).setSelezionato(Boolean.parseBoolean(value.toString()));
			}
		}
		fireTableCellUpdated(row, col);
	}

	@Override
	public boolean isCellEditable(int row, int col) {
		if(col == 0 && getValueAt(row, col).toString().isEmpty()) return true;
		else if(col >= 2) return true;
		else return false;
	}

	public Viaggio getElementAt(int row) {

		if(row >= 0){
			return viaggi.elementAt(row);
		}

		return null;
	}


	public void addRow(Viaggio v) {
		if(v == null){
			Viaggio nuovo = new Viaggio();
			String posizione = type == Consts.VIAGGI_TM_TYPE_NORD ? Viaggio.NORD : Viaggio.SUD;
			nuovo.setPosizione(posizione);
			nuovo.setData(currentDate);
			viaggi.addElement(nuovo);
			fireTableRowsInserted(getRowCount() - 1, getRowCount() - 1);
		}else{
			viaggi.addElement(v);
			fireTableRowsInserted(getRowCount() - 1, getRowCount() - 1);
		}
	}

	public Viaggio removeRow(int row) {
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
}
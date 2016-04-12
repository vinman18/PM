package it.vin.dev.menzione.frame;

import java.util.Vector;

import it.vin.dev.menzione.logica.Camion;
import it.vin.dev.menzione.logica.Viaggio;

public class ViaggiNuoviTableModel extends ViaggiTableModel {

	private static final long serialVersionUID = 6548494572029050017L;

	private String[] colonne;

	public ViaggiNuoviTableModel(String type) {
		super(type);
		if(type == Viaggio.NORD){
			String[] tmp = {"Targa", "Caratteristiche", "Autista", "Note"};
			colonne = tmp;
		}else{
			String[] tmp = {"Targa", "Caratteristiche", "Autista", "Note", "Litri"};
			colonne = tmp;
		}
	}

	public ViaggiNuoviTableModel(Vector<Viaggio> viaggi, String type) {
		super(viaggi, type);

		if(type == Viaggio.NORD){
			String[] tmp = {"Targa", "Caratteristiche", "Autista", "Note"};
			colonne = tmp;
		}else{
			String[] tmp = {"Targa", "Caratteristiche", "Autista", "Note", "Litri"};
			colonne = tmp;
		}
	}

	@Override
	public int getColumnCount() {
		return colonne.length;
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
			if(getType() == Viaggio.NORD){
				switch(col){
				case 0: return v.getCamion().getTarga();
				case 1: return v.getCamion().getCaratteristiche();
				case 2: return v.getAutista();
				case 3: return v.getNote();
				}
			}else{
				switch(col){
				case 0: return v.getCamion().getTarga();
				case 1: return v.getCamion().getCaratteristiche();
				case 2: return v.getAutista();
				case 3: return v.getNote();
				case 4: return v.getLitriB();
				}
			}
		} catch (NullPointerException e) {
			return "";
		}
		return "";
	}

	@Override
	public void setValueAt(Object value, int row, int col){
		if(getType() == Viaggio.NORD){
			if(col == 0){
				Camion c = MainFrame.findCamionByTarga(value.toString());
				viaggi.elementAt(row).setCamion(c);
			} else if(col == 2){
				viaggi.elementAt(row).setAutista(value.toString());
				//log.info(viaggi.elementAt(row).getAutista());
			} else if(col == 3){
				viaggi.elementAt(row).setNote(value.toString());
			} else if(col == 1){
				viaggi.elementAt(row).getCamion().setCaratteristiche(value.toString());
			}
		}else if(getType() == Viaggio.SUD){
			if(col == 0){
				Camion c = MainFrame.findCamionByTarga(value.toString());
				viaggi.elementAt(row).setCamion(c);
			} else if(col == 2){
				viaggi.elementAt(row).setAutista(value.toString());
				//log.info(viaggi.elementAt(row).getAutista());
			} else if(col == 3){
				viaggi.elementAt(row).setNote(value.toString());
			}else if(col == 4){
				viaggi.elementAt(row).setLitriB(Integer.parseInt(value.toString()));
			}else if(col == 1){
				viaggi.elementAt(row).getCamion().setCaratteristiche(value.toString());
			}
		}
		fireTableCellUpdated(row, col);
	}

	@Override
	public boolean isCellEditable(int row, int col){
		if(col == 0) return true;
		else if(col >= 2) return true;
		else return false;
	}
	
	
	public int replaceCaratt(String targa, String newCaratt){
		int result = 0;
		for(Viaggio v : viaggi){
			if(v.getCamion().getTarga().compareTo(targa)==0){
				v.getCamion().setCaratteristiche(newCaratt);
				result++;
			}
		}
		return result;
	}
}

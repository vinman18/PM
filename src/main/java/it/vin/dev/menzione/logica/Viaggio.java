package it.vin.dev.menzione.logica;

import java.io.Serializable;
import java.sql.Date;

public class Viaggio implements Serializable {

	private static final long serialVersionUID = -8003647121714337959L;
	private long id;
	private Camion camion;
	private String autista;
	private String posizione;
	private String note;
	private Date data;
	private int litriB;
	private boolean selezionato;
	public final static String NORD = "Nord";
	public final static String SUD = "Sud";
	
	@Override
	public String toString() {
		return "Viaggio [id=" + id + ", camion=" + camion + ", autista=" + autista + ", posizione=" + posizione
				+ ", note=" + note + ", data=" + data + ", litriB=" + litriB + ", selezionato=" + selezionato + "]";
	}

	public Viaggio(Camion camion, String autista, String posizione, Date data, long id) {
		this.camion = camion;
		this.autista = autista;
		this.posizione = posizione;
		this.data = data;
		this.note = new String();
		this.litriB = 0;
		this.setSelezionato(false);
		this.id = id;
	}
	
	public Viaggio(String targa, String caratteristiche, String autista, String posizione, Date data, long id){
		this.camion = new Camion(targa, caratteristiche);
		this.autista = autista;
		this.posizione = posizione;
		this.data = data;
		this.note = new String();
		this.setSelezionato(false);
		this.id = id;
	}
	
	public Viaggio(){
		this.camion = new Camion();
		this.autista = "";
		this.posizione = "";
		this.data = null;
		this.note = "";
		this.setSelezionato(false);
		this.id = -1;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Camion getCamion() {
		return camion;
	}

	public void setCamion(Camion camion) {
		this.camion = camion;
	}

	public String getAutista() {
		return autista;
	}

	public void setAutista(String autista) {
		this.autista = autista;
	}

	public String getPosizione() {
		return posizione;
	}

	public void setPosizione(String posizione) {
		this.posizione = posizione;
	}
	
	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public Date getData() {
		return data;
	}

	public void setData(Date data) {
		this.data = data;
	}


	public boolean isSelezionato() {
		return selezionato;
	}

	public void setSelezionato(boolean selezionato) {
		this.selezionato = selezionato;
	}

	public int getLitriB() {
		return litriB;
	}

	public void setLitriB(int litriB) {
		this.litriB = litriB;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Viaggio other = (Viaggio) obj;
		if (autista == null) {
			if (other.autista != null)
				return false;
		} else if (!autista.equals(other.autista))
			return false;
		if (camion == null) {
			if (other.camion != null)
				return false;
		} else if (!camion.equals(other.camion))
			return false;
		if (data == null) {
			if (other.data != null)
				return false;
		} else if (!data.equals(other.data))
			return false;
		if (id != other.id)
			return false;
		if (litriB != other.litriB)
			return false;
		if (note == null) {
			if (other.note != null)
				return false;
		} else if (!note.equals(other.note))
			return false;
		if (posizione == null) {
			if (other.posizione != null)
				return false;
		} else if (!posizione.equals(other.posizione))
			return false;
		if (selezionato != other.selezionato)
			return false;
		return true;
	}

}

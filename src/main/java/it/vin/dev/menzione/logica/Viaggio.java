package it.vin.dev.menzione.logica;

import java.io.Serializable;
import java.sql.Date;
import java.util.Objects;

public class Viaggio implements Serializable {

	private static final long serialVersionUID = -8003647121714337959L;
    public final static String NORD = "Nord";
    public final static String SUD = "Sud";

    public final static int COL_PINNED = 99;

    private long id;
    private Camion camion;
    private String autista;
    private String posizione;
    private String note;
    private Date data;
    private int litriB;
    private boolean selezionato;
    private boolean pinned;

	public Viaggio(Camion camion, String autista, String posizione, Date data, long id) {
		this.camion = camion;
		this.autista = autista;
		this.posizione = posizione;
		this.data = data;
		this.note = "";
		this.litriB = 0;
		this.setSelezionato(false);
		this.id = id;
	}
	
	public Viaggio(String targa, String caratteristiche, String autista, String posizione, Date data, long id){
		this.camion = new Camion(targa, caratteristiche);
		this.autista = autista;
		this.posizione = posizione;
		this.data = data;
		this.note = "";
		this.setSelezionato(false);
		this.id = id;
		this.litriB = 0;
	}
	
	public Viaggio(){
		this.camion = new Camion();
		this.autista = "";
		this.posizione = "";
		this.data = null;
		this.note = "";
		this.setSelezionato(false);
		this.id = -1;
		this.litriB = 0;
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

    public boolean isPinned() {
        return pinned;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Viaggio viaggio = (Viaggio) o;
		return id == viaggio.id &&
				litriB == viaggio.litriB &&
				selezionato == viaggio.selezionato &&
				pinned == viaggio.pinned &&
				Objects.equals(camion, viaggio.camion) &&
				Objects.equals(autista, viaggio.autista) &&
				Objects.equals(posizione, viaggio.posizione) &&
				Objects.equals(note, viaggio.note) &&
				Objects.equals(data, viaggio.data);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, camion, autista, posizione, note, data, litriB, selezionato, pinned);
	}

	@Override
	public String toString() {
		return "Viaggio{" +
				"id=" + id +
				", camion=" + camion +
				", autista='" + autista + '\'' +
				", posizione='" + posizione + '\'' +
				", note='" + note + '\'' +
				", data=" + data +
				", litriB=" + litriB +
				", selezionato=" + selezionato +
				", pinned=" + pinned +
				'}';
	}
}

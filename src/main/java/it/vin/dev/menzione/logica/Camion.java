package it.vin.dev.menzione.logica;

import java.io.Serializable;

public class Camion implements Serializable {

    private String targa;
	private String caratteristiche;
	

	public Camion(String targa, String caratteristiche) {
		this.targa = targa;
		this.caratteristiche = caratteristiche;
	}

	public Camion(){
		this.targa = "";
		this.caratteristiche = "";
	}

	public String getCaratteristiche() {
		return caratteristiche;
	}

	public void setCaratteristiche(String caratteristiche) {
		this.caratteristiche = caratteristiche;
	}

	public String getTarga() {
		return targa;
	}

	public void setTarga(String targa) {
		this.targa = targa;
	}

	@Override
	protected Object clone() {
		return new Camion(targa, caratteristiche);
	}

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Camion other = (Camion) obj;
        if (caratteristiche == null) {
            if (other.caratteristiche != null)
                return false;
        } else if (!caratteristiche.equals(other.caratteristiche))
            return false;
        if (targa == null) {
            if (other.targa != null)
                return false;
        } else if (!targa.equals(other.targa))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return targa;
    }
}

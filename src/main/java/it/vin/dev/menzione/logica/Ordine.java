package it.vin.dev.menzione.logica;

import java.sql.Date;

public class Ordine {

	private long id;
	private Date date;
	private String data;
	private String cliente;
	private String note;
	private Boolean selezionato;
	private String type;
	public static final String SALITA = "salita";
	public static final String DISCESA = "discesa";
	
	
	
	public String getType() {
		return type;
	}


	public void setType(String type2) {
		this.type = type2;
	}

	
	public Ordine(String data, String cliente, String note, Date date) {
		this.data = data;
		this.cliente = cliente;
		this.note = note;
		this.date = date;
		this.selezionato = false;
	}
	
	public Ordine(String cliente, Date date, String type){
		this.data = "";
		this.cliente = cliente;
		this.note = "";
		this.date = date;
		this.selezionato = false;
		this.type = type;
	}


	public Date getDate() {
		return date;
	}


	public void setDate(Date date) {
		this.date = date;
	}


	public String getData() {
		return data;
	}


	public void setData(String data) {
		this.data = data;
	}


	public String getCliente() {
		return cliente;
	}


	public void setCliente(String cliente) {
		this.cliente = cliente;
	}


	public String getNote() {
		return note;
	}


	public void setNote(String note) {
		this.note = note;
	}


	public long getId() {
		return id;
	}


	public void setId(long id) {
		this.id = id;
	}


	public Boolean getSelezionato() {
		return selezionato;
	}


	public void setSelezionato(Boolean selezionato) {
		this.selezionato = selezionato;
	}


	@Override
	public String toString() {
		return "Ordine [id=" + id + ", date=" + date + ", data=" + data + ", cliente=" + cliente + ", note=" + note
				+ ", selezionato=" + selezionato + ", type=" + type + "]";
	}



	
	
}

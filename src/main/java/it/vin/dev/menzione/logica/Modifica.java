package it.vin.dev.menzione.logica;

public class Modifica<T> {

	private T element;
	private int type;
	public final static int INSERT = 1;
	public final static int DELETE = 2;
	public final static int UPDATE = 3;
	
	
	public Modifica(T element, int type) {
		this.element = element;
		this.type = type;
	}


	public T getElement() {
		return element;
	}


	public int getType() {
		return type;
	}
	
	@Override
	public String toString(){
		return "Modifiche[Element = "+element.toString()+
				", Type = "+type;
		
	}

}

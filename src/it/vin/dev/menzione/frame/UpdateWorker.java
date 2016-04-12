package it.vin.dev.menzione.frame;

import java.sql.SQLException;
import java.util.Vector;
import java.util.logging.Logger;

import it.vin.dev.menzione.logica.*;

public class UpdateWorker implements Runnable {

	Vector<Modifica<Viaggio>> modifiche;
	DbUtil dbu;
	Logger log;
	
	public UpdateWorker(Vector<Modifica<Viaggio>> modifiche, DbUtil dbu){
		this.modifiche = modifiche;
		this.dbu = dbu;
		log = Logger.getGlobal();
	}

	@Override
	public void run() {
		Viaggio v = null;
		int type;
		log.info("N modifiche: "+modifiche.size());
		try{
			for(Modifica<Viaggio> m : modifiche){
				int i=0;
				log.info("STO ESEGUENDO LA MODIFICA: "+ m.toString()); 
				v = m.getElement();
				type = m.getType();

				if(type == Modifica.UPDATE){
					dbu.modificaViaggio(v);
				}else if(type == Modifica.DELETE){
					dbu.rimuoviViaggio(v);
				}else if(type == Modifica.INSERT){
					dbu.aggiungiViaggio(v);
				}
				modifiche.remove(i++);
			}
		}catch(SQLException e){
			e.printStackTrace();
		}
	}
		
	}


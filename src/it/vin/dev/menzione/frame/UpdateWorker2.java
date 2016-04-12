package it.vin.dev.menzione.frame;

import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingWorker;

import it.vin.dev.menzione.logica.Configuration;
import it.vin.dev.menzione.logica.DbUtil;
import it.vin.dev.menzione.logica.Viaggio;

public class UpdateWorker2 extends SwingWorker<Void, Void> {
	
	private Viaggio v;
	private DbUtil dbu;
	private int col;
	private Logger logger;
	

	public UpdateWorker2(Viaggio v, int col, DbUtil dbu) {
		super();
		this.v = v;
		this.col = col;
		this.dbu = dbu;
		logger = Logger.getGlobal();
		try {
			FileHandler fh = new FileHandler(Configuration.getLogfile()+"-UpdateWorker.log",
					true);
			logger.addHandler(fh);
		} catch (SecurityException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}



	@Override
	protected Void doInBackground() {
		try {
			dbu.modificaViaggio(v, col);
		} catch (SQLException e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
			e.printStackTrace();
		}
		return null;
	}

}

package it.vin.dev.menzione.frame;

import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import it.vin.dev.menzione.logica.Configuration;
import it.vin.dev.menzione.logica.DbUtil;
import it.vin.dev.menzione.logica.DbUtilFactory;
import it.vin.dev.menzione.logica.Nota;
import it.vin.dev.menzione.logica.Ordine;

public class NoteTableListener implements TableModelListener{

	private DbUtil dbu;
	private Logger logger;
	
	public NoteTableListener() throws SQLException {
		dbu = DbUtilFactory.createDbUtil();
		logger = Logger.getGlobal();
		try {
			FileHandler fh = new FileHandler(Configuration.getLogfile()+"-NoteTableModelListener.log",
					true);
			logger.addHandler(fh);
		} catch (SecurityException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	public void closeConnection(){
		try {
			dbu.closeConnection();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void tableChanged(TableModelEvent arg0) {

		NoteTableModel source = (NoteTableModel) arg0.getSource();
		int type = arg0.getType();
		int col = arg0.getColumn();
		int row = arg0.getFirstRow();
		
		
		if(type == TableModelEvent.UPDATE){
			if(col >=0){
				Nota n = source.getElementAt(row);
				try {
					dbu.modificaNota(n);
				} catch (SQLException e) {
					logger.log(Level.SEVERE, e.getMessage(), e);
					e.printStackTrace();
					JOptionPane.showMessageDialog(null, "Impossibile colleagarsi al database"+
							"Codice errore:"+e.getErrorCode()+"\n"+e.getMessage(),"ERRORE", 
							JOptionPane.ERROR_MESSAGE);
				}
			}
		}else if(type == TableModelEvent.INSERT){
			/*Ordine o = source.getElementAt(row);
			
			try {
				dbu.aggiungiOrdine(o);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
		}
		

	}

}

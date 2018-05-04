package it.vin.dev.menzione.main_frame;

import java.sql.SQLException;

import javax.swing.JOptionPane;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import it.vin.dev.menzione.logica.DatabaseService;
import it.vin.dev.menzione.logica.Ordine;
import it.vin.dev.menzione.workers.OrdiniUpdateWorker;
import it.vin.dev.menzione.workers.UpdateWorkerListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class OrdiniTableListener implements TableModelListener {

	//private DatabaseService dbu;
    private DatabaseService dbs;
    private UpdateWorkerListener<Ordine> listener;
	
	public OrdiniTableListener(DatabaseService dbs) throws SQLException {
		//dbu = DatabaseService.create();
        this.dbs = dbs;
	}
	
	/*public void closeConnection(){
		try {
			dbu.closeConnection();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}*/


	@Override
	public void tableChanged(TableModelEvent arg0) {

		OrdiniTableModel source = (OrdiniTableModel) arg0.getSource();
		int type = arg0.getType();
		int col = arg0.getColumn();
		int row = arg0.getFirstRow();
		
		
		if(type == TableModelEvent.UPDATE){
			if(col >=0){
				Ordine o = source.getElementAt(row);
				/*try {
					dbu.modificaOrdine(o, col);
				} catch (SQLException e) {
					logger.error(e.getMessage(), e);
					e.printStackTrace();
					JOptionPane.showMessageDialog(null, "Impossibile colleagarsi al database"+
							"Codice errore:"+e.getErrorCode()+"\n"+e.getMessage(),"ERRORE", 
							JOptionPane.ERROR_MESSAGE);
				}*/

                OrdiniUpdateWorker.connect(dbs)
                        .update(o, col)
                        .onResult(listener)
                        .execute();
			}
		}else if(type == TableModelEvent.INSERT){
			/*Ordine o = source.getElementAt(row);
			
			try {
				dbu.aggiungiOrdine(o);
			} catch (SQLException e) {
				e.printStackTrace();
			}*/
		}
		

	}

    public void setUpdateWorkerListener(UpdateWorkerListener<Ordine> listener) {
        this.listener = listener;
    }
}

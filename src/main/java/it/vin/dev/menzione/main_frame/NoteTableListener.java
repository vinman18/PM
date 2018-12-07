package it.vin.dev.menzione.main_frame;

import java.sql.SQLException;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import it.vin.dev.menzione.Msg;
import it.vin.dev.menzione.logica.DatabaseService;
import it.vin.dev.menzione.logica.Nota;
import it.vin.dev.menzione.workers.NoteUpdateWorker;
import it.vin.dev.menzione.workers.UpdateWorkerListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NoteTableListener implements TableModelListener{

	private DatabaseService dbs;
	private UpdateWorkerListener<Nota> resultListener;
	private Logger logger;
	
	public NoteTableListener(DatabaseService dbs) throws SQLException {
		//dbs = DatabaseService.create();
		this.dbs = dbs;
		logger = LogManager.getLogger(this.getClass());
	}
	
	/*public void closeConnection(){
		try {
			dbs.closeConnection();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}*/

	@Override
	public void tableChanged(TableModelEvent arg0) {

		NoteTableModel source = (NoteTableModel) arg0.getSource();
		int type = arg0.getType();
		int col = arg0.getColumn();
		int row = arg0.getFirstRow();


		if(type == TableModelEvent.UPDATE){
			if(col >=0){
				Nota n = source.getElementAt(row);
				/*try {
					dbs.modificaNota(n);
				} catch (SQLException e) {
					logger.error(e.getMessage(), e);
					e.printStackTrace();
                    Msg.error(null, "Impossibile colleagarsi al database"+
                            "Codice errore:"+e.getErrorCode()+"\n"+e.getMessage());
                }*/

                NoteUpdateWorker.connect(dbs)
                        .update(n)
                        .onResult(resultListener)
                        .execute();
			}
		}else if(type == TableModelEvent.INSERT){
			/*Ordine o = source.getElementAt(row);
			
			try {
				dbs.aggiungiOrdine(o);
			} catch (SQLException e) {
				e.printStackTrace();
			}*/
		}
		

	}

    public void setResultListener(UpdateWorkerListener<Nota> resultListener) {
        this.resultListener = resultListener;
    }
}

package it.vin.dev.menzione.workers;

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import it.vin.dev.menzione.database_helper.DatabaseHelperChannel;
import it.vin.dev.menzione.logica.DatabaseService;
import it.vin.dev.menzione.logica.Viaggio;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ViaggiUpdateWorker extends SwingWorker<Viaggio, Void> {

    private Viaggio v;
    private DatabaseService dbs;
    private int col = -1;
    private Logger logger = LogManager.getLogger(ViaggiUpdateWorker.class);
    private UpdateWorkerListener<Viaggio> listener;
    private UpdateType type;

    private Exception e = null;

    public ViaggiUpdateWorker(DatabaseService dbs) {
        super();
        this.dbs = dbs;
    }

    public static ViaggiUpdateWorker connect(DatabaseService dbs) {
        return new ViaggiUpdateWorker(dbs);
    }

    public ViaggiUpdateWorker update(Viaggio v, int col) {
        this.col = col;
        this.v = v;
        this.type = UpdateType.UPDATE;
        return this;
    }

    public ViaggiUpdateWorker insert(Viaggio v) {
        this.v = v;
        this.type = UpdateType.INSERT;
        return this;
    }


    public ViaggiUpdateWorker onResult(UpdateWorkerListener<Viaggio> listener) {
        this.listener = listener;
        return this;
    }

    @Override
    protected Viaggio doInBackground() {
        Viaggio viaggio = null;

        try {
            if(type == UpdateType.INSERT) {
                long id = dbs.aggiungiViaggio(v);
                viaggio = dbs.getViaggioById(id);
            } else {
                dbs.modificaViaggio(v, col);
                viaggio = dbs.getViaggioById(v.getId());
            }
        } catch (SQLException e) {
            this.e = e;
        }

        return viaggio;
    }

    @Override
    protected void done() {
        super.done();
        if(listener == null) {
            return;
        }

        if(this.e != null) {
            listener.onError(e);
            if (e instanceof SQLException) {
                return;
            }
        }

        Viaggio fromDb;
        try {
            fromDb = get();
        } catch (InterruptedException | ExecutionException e1) {
            listener.onError(e1);
            return;
        }

        switch (type) {
            case UPDATE:
                listener.onUpdate(fromDb, col);
                break;
            case INSERT:
                listener.onInsert(fromDb, fromDb.getId());
                break;
        }
    }
}

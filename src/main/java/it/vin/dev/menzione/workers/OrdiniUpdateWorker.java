package it.vin.dev.menzione.workers;

import it.vin.dev.menzione.logica.DatabaseService;
import it.vin.dev.menzione.logica.Ordine;

import javax.swing.*;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;

public class OrdiniUpdateWorker extends SwingWorker<Ordine, Void> {

    private Ordine o;
    private DatabaseService dbs;
    private int col;
    private UpdateWorkerListener<Ordine> listener;
    private UpdateType type;

    private Exception e = null;

    public OrdiniUpdateWorker(DatabaseService dbs) {
        super();
        this.dbs = dbs;
    }

    public static OrdiniUpdateWorker connect(DatabaseService dbs) {
        return new OrdiniUpdateWorker(dbs);
    }

    public OrdiniUpdateWorker update(Ordine o, int col) {
        this.col = col;
        this.o = o;
        this.type = UpdateType.UPDATE;
        return this;
    }

    public OrdiniUpdateWorker insert(Ordine o) {
        this.o = o;
        this.type = UpdateType.INSERT;
        return this;
    }


    public OrdiniUpdateWorker onResult(UpdateWorkerListener<Ordine> listener) {
        this.listener = listener;
        return this;
    }

    @Override
    protected Ordine doInBackground() throws Exception {
        Ordine ordine = null;

        try {
            if (type == UpdateType.INSERT) {
                long id = dbs.aggiungiOrdine(o);
                ordine = dbs.getOrdineById(id);
            } else if(type == UpdateType.UPDATE) {
                dbs.modificaOrdine(o, col);
                ordine = dbs.getOrdineById(o.getId());
            }
        } catch (SQLException e) {
            this.e = e;
        }

        return ordine;
    }

    @Override
    protected void done() {
        super.done();
        if(listener == null) {
            return;
        }

        if(this.e != null) {
            listener.onError(e);
            if(e instanceof SQLException) {
                return;
            }
        }

        Ordine fromDb;
        try {
            fromDb = get();
        } catch (InterruptedException | ExecutionException e1) {
            listener.onError(e1);
            return;
        }

        switch (type) {
            case INSERT:
                listener.onInsert(fromDb, fromDb.getId());
                break;
            case UPDATE:
                listener.onUpdate(fromDb, col);
                break;
        }
    }
}

package it.vin.dev.menzione.workers;

import it.vin.dev.menzione.database_helper.DatabaseHelperChannel;
import it.vin.dev.menzione.logica.DatabaseService;
import it.vin.dev.menzione.logica.Nota;

import javax.swing.*;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;

public class NoteUpdateWorker extends SwingWorker<Nota, Void>{
    private Nota n;
    private DatabaseService dbs;
    private UpdateWorkerListener<Nota> listener;
    private UpdateType type;

    private Exception e = null;

    public NoteUpdateWorker(DatabaseService dbs) {
        super();
        this.dbs = dbs;
    }

    public static NoteUpdateWorker connect(DatabaseService dbs) {
        return new NoteUpdateWorker(dbs);
    }

    public NoteUpdateWorker update(Nota n) {
        this.n = n;
        this.type = UpdateType.UPDATE;
        return this;
    }

    public NoteUpdateWorker insert(Nota n) {
        this.n = n;
        this.type = UpdateType.INSERT;
        return this;
    }


    public NoteUpdateWorker onResult(UpdateWorkerListener<Nota> listener) {
        this.listener = listener;
        return this;
    }

    @Override
    protected Nota doInBackground() {
        Nota nota = null;

        try {
            if (type == UpdateType.INSERT) {
                long id = dbs.aggiungiNota(n);
                nota = dbs.getNotaById(id);
            } else if(type == UpdateType.UPDATE) {
                dbs.modificaNota(n);
                nota = dbs.getNotaById(n.getId());
            }
        } catch (SQLException e) {
            this.e = e;
        }

        return nota;
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

        Nota fromDb;
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
                listener.onUpdate(fromDb, -1);
                break;
        }
    }
}

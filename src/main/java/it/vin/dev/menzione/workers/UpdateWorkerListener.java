package it.vin.dev.menzione.workers;

public interface UpdateWorkerListener<V> {
    void onUpdate(V updated, int col);
    void onError(Exception error);
    void onInsert(V inserted, long newId);
}

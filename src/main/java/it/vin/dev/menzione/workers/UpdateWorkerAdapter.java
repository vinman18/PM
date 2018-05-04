package it.vin.dev.menzione.workers;

public abstract class UpdateWorkerAdapter<V> implements UpdateWorkerListener<V> {
    @Override
    public void onUpdate(V updated, int col) {}

    @Override
    public void onError(Exception error) {}

    @Override
    public void onInsert(V inserted, long newId) {}
}

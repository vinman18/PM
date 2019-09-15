package it.vin.dev.menzione.frame;

import it.vin.dev.menzione.logica.Viaggio;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class ViaggiPinTableModel extends ViaggiTableModel {

    private Map<Integer, Viaggio> removedElements = new HashMap<>();

    public ViaggiPinTableModel(int type) {
        super(type);
    }

    public ViaggiPinTableModel(Vector<Viaggio> viaggi, int type) {
        super(viaggi, type);
    }


    @Override
    public Viaggio removeRow(int row) {
        Viaggio removed = viaggi.get(row);
        viaggi.removeElementAt(row);

        removedElements.put(row, removed);
        fireTableRowsDeleted(row, row);
        return removed;
    }

    public Viaggio unpinRow(int row) {
        getElementAt(row).setPinned(false);
        return removeRow(row);
    }

    public Viaggio getRemovedRow(int row) {
        return removedElements.remove(row);
    }
}

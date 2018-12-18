package it.vin.dev.menzione.events;

import it.vin.dev.menzione.logica.Camion;

public class CamionInsertEvent extends CamionEvent{

    public CamionInsertEvent(Camion item) {
        super(item);
    }

}

package it.vin.dev.menzione.events;

import it.vin.dev.menzione.logica.Camion;

public class CamionRemoveEvent extends CamionEvent {
    public CamionRemoveEvent(Camion element) {
        super(element);
    }
}

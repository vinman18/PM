package it.vin.dev.menzione.events;

import it.vin.dev.menzione.logica.Camion;

public class CamionUpdateEvent extends CamionEvent {
    public CamionUpdateEvent(Camion element) {
        super(element);
    }
}

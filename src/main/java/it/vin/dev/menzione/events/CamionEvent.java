package it.vin.dev.menzione.events;

import it.vin.dev.menzione.logica.Camion;

public abstract class CamionEvent extends ItemEvent<Camion> {
    public CamionEvent(Camion element) {
        super(element);
    }
}

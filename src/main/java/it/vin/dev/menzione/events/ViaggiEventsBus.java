package it.vin.dev.menzione.events;


import com.google.common.eventbus.EventBus;

public class ViaggiEventsBus {
    private static ViaggiEventsBus ourInstance = new ViaggiEventsBus();

    public static ViaggiEventsBus getInstance() {
        return ourInstance;
    }

    private EventBus eventBus;

    private ViaggiEventsBus() {
        eventBus = new EventBus();
    }

    public void post(Object event) {
        eventBus.post(event);
    }

    public void register(Object listener) {
        eventBus.register(listener);
    }
}

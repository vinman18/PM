package it.vin.dev.menzione.events;


import com.google.common.eventbus.EventBus;

public class ViaggiEventBus {
    private static ViaggiEventBus ourInstance = new ViaggiEventBus();

    public static ViaggiEventBus getInstance() {
        return ourInstance;
    }

    private EventBus eventBus;

    private ViaggiEventBus() {
        eventBus = new EventBus();
    }

    public void post(Object event) {
        eventBus.post(event);
    }

    public void register(Object listener) {
        eventBus.register(listener);
    }
}

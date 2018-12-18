package it.vin.dev.menzione;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import it.vin.dev.menzione.events.CamionInsertEvent;
import it.vin.dev.menzione.events.CamionUpdateEvent;
import it.vin.dev.menzione.events.ViaggiEventBus;
import it.vin.dev.menzione.logica.Camion;

public class EventsTest {

    class CamionEventListener {

        @Subscribe
        public void insertEventListener(CamionInsertEvent event) {
            System.out.printf("New camion inserted: %s - %s\n", event.element.getTarga(), event.element.getCaratteristiche());
        }
        @Subscribe
        public void updateEventListener(CamionUpdateEvent event) {
            System.out.printf("Camion with targa %s updated to %s\n", event.element.getTarga(), event.element.getCaratteristiche());
        }
    }

    public EventsTest() {
//        EventBus eventBus = ViaggiEventBus.getInstance().get();

        Camion c = new Camion("A123456", "BLA BLA BLA");
        CamionInsertEvent insertEvent = new CamionInsertEvent(c);

        ViaggiEventBus.getInstance().register(new CamionEventListener());

        ViaggiEventBus.getInstance().post(new CamionInsertEvent(c));

        c.setCaratteristiche("CIAO CIAO");

        ViaggiEventBus.getInstance().post(new CamionUpdateEvent(c));
    }

    public static void main(String[] args) {
        new EventsTest();
    }
}

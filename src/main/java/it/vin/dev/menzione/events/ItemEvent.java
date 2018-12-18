package it.vin.dev.menzione.events;

public class ItemEvent<T> {
    public final T element;

    public ItemEvent(T element) {
        this.element = element;
    }
}

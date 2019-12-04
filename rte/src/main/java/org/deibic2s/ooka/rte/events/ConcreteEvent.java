package org.deibic2s.ooka.rte.events;
import org.deibic2s.ooka.events.Event;

public class ConcreteEvent<E> implements Event<E> {
    private EventManager eventManager;

    public ConcreteEvent(EventManager eventManager){
        this.eventManager = eventManager;
    }

    @Override
    public void fire(String topic, E message) {
        
        eventManager.dispatch(topic, message);
    }
}

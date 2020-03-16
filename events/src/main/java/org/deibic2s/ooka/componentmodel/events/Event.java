package org.deibic2s.ooka.componentmodel.events;

public interface Event<E> {
    void fire(String topic, E message);
}

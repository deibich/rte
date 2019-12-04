package org.deibic2s.ooka.events;

public interface Event<E> {
    void fire(String topic, E message);
}

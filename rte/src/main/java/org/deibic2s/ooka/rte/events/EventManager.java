package org.deibic2s.ooka.rte.events;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.AbstractMap.SimpleEntry;

import org.deibic2s.ooka.rte.logging.RTELogCreator;

public class EventManager {
    private static EventManager myInstance;
    
    Map<String, List<SimpleEntry<Object, Method>>> registeredListeners;

    private EventManager(){
        registeredListeners = new HashMap<>();
    }

    public static EventManager Instance(){
        if(myInstance == null)
            myInstance = new EventManager();

        return myInstance;
    }

    public void addListener(String topic, Object componentInstance, Method listenerMethod){
        List<SimpleEntry<Object, Method>> listenersForTopic = registeredListeners.get(topic);

        if(listenersForTopic == null) {
            listenersForTopic = new ArrayList<>();
            registeredListeners.put(topic, listenersForTopic);
        }

        listenersForTopic.add(new SimpleEntry<>(componentInstance, listenerMethod));
    }

    void dispatch(String topic, Object message){

        RTELogCreator.getInstance().getLogger("events").info(
            "Dispatch event with topic " + topic.toString() + " with message " + message.toString()
        );
        
        for(Map.Entry<Object, Method> om : registeredListeners.get(topic)){
            try {
                om.getValue().invoke(om.getKey(), message);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch(IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
    }
}


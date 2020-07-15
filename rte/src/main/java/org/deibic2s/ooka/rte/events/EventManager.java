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
        SimpleEntry<Object, Method> bla = new SimpleEntry<>(componentInstance, listenerMethod);
        listenersForTopic.add(bla);
    }

    public void removeListenerForInstance(String topic, Object componentInstance) {
        List<SimpleEntry<Object, Method>> listenersForTopic = registeredListeners.get(topic);
        List<Integer> idxToRemove = new ArrayList<>();
        if(listenersForTopic != null){
            for(int i = 0; i < listenersForTopic.size(); i++) {
                SimpleEntry<Object, Method> entry = listenersForTopic.get(i);
                if(entry.getKey() == null){
                    idxToRemove.add(i);
                    continue;
                }
                if(entry.getKey().equals(componentInstance)){
                    idxToRemove.add(i);
                }
            }
            for(int i = idxToRemove.size()-1; i > 0; i--){
                listenersForTopic.remove(idxToRemove.get(i));
            }
            for(int i = listenersForTopic.size() -1; i >0; i--){

                if(listenersForTopic.get(i).getKey() == null)
                    listenersForTopic.remove(i);
            }
        }
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


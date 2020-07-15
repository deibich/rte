package org.deibic2s.ooka.rte.core;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import org.deibic2s.ooka.componentmodel.logging.ILogger;
import org.deibic2s.ooka.rte.events.EventManager;
import org.deibic2s.ooka.rte.persistence.ComponentDTO;
import org.deibic2s.ooka.rte.utils.ComponentState;
import java.lang.reflect.Type;
import java.util.AbstractMap.SimpleEntry;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deibic2s.ooka.componentmodel.events.Event;
import org.deibic2s.ooka.componentmodel.logging.ILoggerFactory;

import static org.deibic2s.ooka.rte.utils.ComponentState.*;

public class Component {
    private String name;
    private Integer id;
    private ClassLoader classLoader;
    private String pathToComponent;
    private Class<?> startStopClass;
    private Method startMethod;
    private Method stopMethod;
    private Object instance;
    private Runnable r;
    private Thread componentThread;
    private List<Command> possibleCommands;
    private ComponentState restoreState;
    private boolean shouldRestore;
    private Field componentLoggerField;
    private Field componentLoggerFactoryField;
    private ReadOnlyObjectWrapper<ComponentState> componentState;
    private List<SimpleEntry<Type, Field>> componentEventFields;

    private Map<String, List<SimpleEntry<Type, Method>>> eventListeners;


    // Constructors
    private Component() {
    }

    public Component(String pathToComponent, String componentName) {
        componentEventFields = new ArrayList<>();
        eventListeners = new HashMap<>();
        shouldRestore = false;

        this.name = componentName;
        this.pathToComponent = pathToComponent;
        possibleCommands = new ArrayList<>();
        restoreState = INITIALIZED;
        id = -1;
        componentState = new ReadOnlyObjectWrapper<>(INITIALIZED);
    }

    void rename(String newName) {
        this.name = newName;
    }

    public ComponentState getRestoreState() {
        return restoreState;
    }

    void setRestoreState(ComponentState state) {
        restoreState = state;
    }

    // Getter Setter

    Iterable<SimpleEntry<Type, Field>> getEventfields(){
        return componentEventFields;
    }

    int getEventFieldSize() {
        return componentEventFields.size();
    }
    
    

    
    Map<String, List<SimpleEntry<Type, Method>>> getEventListenerMethods(){
        
        return eventListeners;
    }

    void addEventListenerMethod(String topic, Type t, Method m){
        if(eventListeners == null)
            eventListeners = new HashMap<>();
        List<SimpleEntry<Type, Method>> listenerForTopic =  eventListeners.get(topic);
        if(listenerForTopic == null) {
            listenerForTopic = new ArrayList<>();
            eventListeners.put(topic, listenerForTopic);

        }
        listenerForTopic.add(new SimpleEntry<>(t, m));
    }

    void addEventField(Type t, Field f){
        componentEventFields.add(new SimpleEntry<>(t, f));
    }

    Class<?> getStartStopClass(){
        return startStopClass;
    }

    Object getComponentInstance() {
        return instance;
    }

    public Integer getId() {
        return id;
    }

    void setComponentState(ComponentState c) {
        componentState.set(c);
    }

    public ComponentState getComponentState() {
        return componentState.getValue();
    }

    public ReadOnlyObjectProperty<ComponentState> componentStateProperty() {
        return componentState.getReadOnlyProperty();
    }

    void setError() {
        componentState.set(ERROR);
    }

    public String getName() {
        return name;
    }

    void setClassLoader(ClassLoader classLoader) {
        if (this.classLoader == null && classLoader != null) {
            this.classLoader = classLoader;
        }
    }

    void setStartStopClass(Class<?> startStopClass) {
        if (this.startStopClass == null && startStopClass != null)
            this.startStopClass = startStopClass;
        else
            return;

        setDeploying();
    }

    void addObserveMethod(){

    }

    void setStartMethod(Method startMethod) {
        if (this.startMethod == null && startMethod != null)
            this.startMethod = startMethod;
        else
            return;

        setDeploying();
    }

    void setComponentLoggerField(Field f) {
        componentLoggerField = f;
    }
    
    void setComponentLoggerFactoryField(Field f){
        componentLoggerFactoryField = f;
    }

    void setEvents(Field field, Event<?> event){
        if(componentEventFields != null && componentEventFields.size() > 0 && field != null && event != null){
            try{
                field.set(instance, event);
            }catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    void removeEvents(){
        for (String topic : eventListeners.keySet()) {
            EventManager.Instance().removeListenerForInstance(topic, getComponentInstance());
        }
        eventListeners = new HashMap<>();

        if(getComponentInstance() != null){
            for (SimpleEntry<Type,Field> simpleEntry : componentEventFields) {
                try {
                    simpleEntry.getValue().set(getComponentInstance(), null);
                }catch(Exception e){

                }
            }
        }
        
        this.componentEventFields = new ArrayList<>(); 
    }

    void removeLogger() {
        
        if(getComponentInstance() != null) {
            try {
                componentLoggerField.set(getComponentInstance(), null);
            }catch(Exception e){
                
            }
        }
    }

    void removeLoggerFactory(){
        if(getComponentInstance() != null) {
            try {
            componentLoggerFactoryField.set(getComponentInstance(), null);
            }catch(Exception e){

            }
        }
        
    }


    void setLogger(ILogger l, ILoggerFactory il) {
        if (componentLoggerField != null && instance != null && l != null) {
            try {
                componentLoggerField.set(instance, l);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        if(componentLoggerFactoryField != null && instance != null && l != null ){
            try {
                componentLoggerFactoryField.set(instance, il);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

    }

    String getPathToComponent() {
        return pathToComponent;
    }

    ClassLoader getClassLoader() {
        return this.classLoader;
    }

    void setStopMethod(Method stopMethod) {
        if (this.stopMethod == null && stopMethod != null)
            this.stopMethod = stopMethod;
        else
            return;

        setDeploying();
    }

    void setComponentID(Integer id) {
        this.id = id;
    }

    public Command getCommand(Class<? extends Command> c) {
        for (Command command : possibleCommands)
            if (command.getClass() == c)
                return command;
        return null;
    }

    public ComponentDTO getDataComponent() {
        return new ComponentDTO(getName(), getId(), getComponentState(), getPathToComponent());
    }

    public boolean getShouldRestore() {
        return shouldRestore;
    }

    void setShouldRestore(boolean b) {
        shouldRestore = b;
    }

    // State modifier

    private void setDeploying() {
        if (isStartStopSet() && componentState.get() == INITIALIZED)
            componentState.set(ComponentState.DEPLOYING);
    }

    void setDeployed() {
        if (componentState.get() == ComponentState.DEPLOYING)
            componentState.set(ComponentState.DEPLOYED);
    }

    void prepareRemove() {
        componentLoggerField = null;
        componentState.set(REMOVING);
        startStopClass = null;
        startMethod = null;
        stopMethod = null;
        r = null;
        componentThread = null;
        instance = null;
        classLoader = null;
    }

    void removed() {
        componentState.set(REMOVED);
    }

    // Logic

    void addCommand(Command command) {
        possibleCommands.add(command);
    }

    void prepareStart() {
        if (componentState.get() != ComponentState.DEPLOYED) {
            return;
        }
        componentState.set(ComponentState.STARTING);
        try {
            instance = startStopClass.getDeclaredConstructor().newInstance();
            
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    void startComponent() {
        if (componentState.get() != ComponentState.STARTING) {
            return;
        }

        r = () -> {
            try {
                startMethod.invoke(instance);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        };
        componentThread = new Thread(r);
        componentThread.start();
        componentState.set(ComponentState.STARTED);
    }

    void stopComponent() {
        if (componentState.get() == ComponentState.STARTED) {
            componentState.set(ComponentState.STOPPING);
            try {
                stopMethod.invoke(instance);
                componentThread.interrupt();
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
            try {
                componentThread.join(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
                componentThread.stop();
            }
            componentState.set(ComponentState.DEPLOYED);
        }

    }

    boolean isStartStopSet() {
        return startStopClass != null && startMethod != null && stopMethod != null;
    }


}
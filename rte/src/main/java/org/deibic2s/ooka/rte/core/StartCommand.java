package org.deibic2s.ooka.rte.core;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.lang.reflect.Method;

import java.util.AbstractMap;
import java.util.List;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;

import org.deibic2s.ooka.componentmodel.logging.ILogger;
import org.deibic2s.ooka.rte.utils.CommandResult;
import org.deibic2s.ooka.rte.utils.ComponentState;
import org.deibic2s.ooka.rte.events.ConcreteEvent;
import org.deibic2s.ooka.rte.events.EventManager;
import org.deibic2s.ooka.rte.logging.RTELogCreator;

public class StartCommand extends Command{

   private Component component;
    private ILogger logger;

    private ComponentLoader cl;
    public StartCommand(Component component, ComponentLoader cl, ILogger l){
        this.component = component;
        this.logger = l;
        this.cl = cl;
        
    }

    @Override
    public CommandResult execute() {
        if(component.getComponentState() != ComponentState.DEPLOYED)
            return new CommandResult(CommandResult.CODE.WARNING, "Can't start component at this moment", component, null);

        component.prepareStart();
        component.setLogger(logger, RTELogCreator.getInstance());
        if(component.getEventListenerMethods() == null || component.getEventListenerMethods().isEmpty())
            cl.getEventListeners(component);
        if(component.getEventfields() == null || component.getEventFieldSize() == 0)
            cl.getInjectableEventFields(component);
        // Create Event for each type in componentEventFields
        // Add them to the instance
        for (AbstractMap.SimpleEntry<Type, Field> se : component.getEventfields()){
            component.setEvents(se.getValue(), new ConcreteEvent<>(EventManager.Instance()));
        }

        // get references to the methods and add them to the eventmanager
        for(Entry<String, List<SimpleEntry<Type, Method>>> es : component.getEventListenerMethods().entrySet()) {
            for(SimpleEntry<Type, Method> se : es.getValue()) {
                EventManager.Instance().addListener(es.getKey(), component.getComponentInstance(), se.getValue());
            }
        }
   
        component.startComponent();
        ComponentState cs = component.getComponentState();

        if(cs != ComponentState.STARTED){
            if(cs == ComponentState.STARTING)
                return new CommandResult(CommandResult.CODE.WARNING, "Cant't start component at this moment.", component, null);

            return new CommandResult(CommandResult.CODE.ERROR, "Component is in the wrong state to start.", component, null);
        }

        return new CommandResult(CommandResult.CODE.SUCCESS, "Component started.", component, null);
    }
}

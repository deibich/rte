package org.deibic2s.ooka.rte.core;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.AbstractMap;
import java.util.List;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;

import org.deibic2s.ooka.rte.events.ConcreteEvent;
import org.deibic2s.ooka.rte.events.EventManager;
import org.deibic2s.ooka.rte.utils.CommandResult;
import org.deibic2s.ooka.rte.utils.ComponentState;

public class InjectFieldsCommand extends Command {
    private Component c;
    private ComponentLoader cl;

    public InjectFieldsCommand(Component c, ComponentLoader cl){
        this.c = c;
        this.cl = cl;
    }

    @Override
    public CommandResult execute() {
        String message = "Can not inject. Component in wrong state.";
        CommandResult.CODE code = CommandResult.CODE.ERROR;
        if(c.getComponentState() == ComponentState.DEPLOYED || c.getComponentState() == ComponentState.STARTED) {
            c.removeEvents();
            c.removeLogger();
            c.removeLoggerFactory();
            cl.injectLogger(c);
            cl.injectLoggerFactory(c);
            cl.getEventListeners(c);
            cl.getInjectableEventFields(c);
            // Add them to the instance
            for (AbstractMap.SimpleEntry<Type, Field> se : c.getEventfields()){
                c.setEvents(se.getValue(), new ConcreteEvent<>(EventManager.Instance()));
            }

            // get references to the methods and add them to the eventmanager
            for(Entry<String, List<SimpleEntry<Type, Method>>> es : c.getEventListenerMethods().entrySet()) {
                for(SimpleEntry<Type, Method> se : es.getValue()) {
                    EventManager.Instance().addListener(es.getKey(), c.getComponentInstance(), se.getValue());
                }
            }
            code = CommandResult.CODE.SUCCESS;
        }
        return new CommandResult(code, message, c, null);
    }
}
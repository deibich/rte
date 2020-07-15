package org.deibic2s.ooka.rte.core;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.AbstractMap.SimpleEntry;

import org.deibic2s.ooka.rte.events.EventManager;
import org.deibic2s.ooka.rte.utils.CommandResult;
import org.deibic2s.ooka.rte.utils.ComponentState;

public class StopCommand extends Command{

    private Component component;

    public StopCommand(Component component){
        this.component = component;
    }

    @Override
    public CommandResult execute() {
        if(component.getComponentState() != ComponentState.STARTED)
            return new CommandResult(CommandResult.CODE.WARNING, "Component is in the wrong state to stop", component, null);


        component.stopComponent();
        component.removeEvents();
        component.removeLogger();
        component.removeLoggerFactory();
        if(component.getComponentState() != ComponentState.DEPLOYED)
            return new CommandResult(CommandResult.CODE.ERROR, "Cant stop component", component, null);

        return new CommandResult(CommandResult.CODE.SUCCESS, "Component Stopped", component, null);
    }
}

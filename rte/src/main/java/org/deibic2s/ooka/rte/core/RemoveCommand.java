package org.deibic2s.ooka.rte.core;

import org.deibic2s.ooka.rte.utils.CommandResult;

import static org.deibic2s.ooka.rte.utils.CommandResult.CODE.*;
import static org.deibic2s.ooka.rte.utils.ComponentState.*;

public class RemoveCommand extends Command {

    private Component component;

    public RemoveCommand(Component c){
        component = c;
    }

    @Override
    public CommandResult execute() {
        CommandResult cr = null;
        if(component.getComponentState() == STARTED){
            cr = component.getCommand(StopCommand.class).execute();
        }

        component.prepareRemove();
        component.removed();
        return new CommandResult(SUCCESS, "Component removed", component, cr);
    }
}

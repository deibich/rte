package org.deibic2s.ooka.rte.core;

import org.deibic2s.ooka.rte.logging.RTELogCreator;
import org.deibic2s.ooka.rte.utils.CommandResult;
import org.deibic2s.ooka.rte.utils.ComponentState;

public class RestoreCommand extends Command {

    private Component c;
    public RestoreCommand(Component c){
        this.c = c;
    }

    @Override
    public CommandResult execute() {
        CommandResult.CODE code = CommandResult.CODE.ERROR;
        String message = "Component is null.";
        if(c == null)
            return new CommandResult(code,message, null, null);

        message = "Component is in the wrong state for restoring";
        if(c.getComponentState() != ComponentState.DEPLOYED) {
            c.setShouldRestore(false);
            return new CommandResult(code, message, c, null);
        }

        message = "Component is not marked to restore.";
        if(!c.getShouldRestore())
            return new CommandResult(code, message, c, null);

        message = "Component was in state " + c.getRestoreState().toString() + " and is now in state ";
        // TODO: here
        CommandResult cr = null;
        switch (c.getRestoreState()){
            case DEPLOYED:
            case STARTING:
            case STOPPING: // Should currently not be possible
                // Component is deployed. ok
                message += ComponentState.DEPLOYED.toString();
                code = CommandResult.CODE.SUCCESS;
                break;
            case STARTED:
                // Start the component
                StartCommand sc = new StartCommand(c,RTELogCreator.getInstance().getLogger("component"));
                cr = sc.execute();
                if(cr.getCommandCode() != CommandResult.CODE.SUCCESS){
                    message += ComponentState.ERROR.toString();
                    message += ". Can't start Component " + c.getName() + " with ID " + c.getId() +".";
                    code = CommandResult.CODE.ERROR;
                    c.setError();
                }else {
                    message += ComponentState.STARTED.toString();
                }

                break;
            case REMOVING: // Should currently not be possible
            case REMOVED:
                // Remove Component
                RemoveCommand rc = new RemoveCommand(c);
                cr = rc.execute();
                if(cr.getCommandCode() != CommandResult.CODE.SUCCESS)
                {
                    code = CommandResult.CODE.ERROR;
                    message += ComponentState.ERROR.toString();
                    message += ". Was not able to remove Component";
                    c.setError();
                }else{
                    code = CommandResult.CODE.SUCCESS;
                    message += ComponentState.ERROR.toString();
                }
                break;
            case ERROR:
            case INITIALIZED: // Should currently not be possible
            case DEPLOYING: // Should currently not be possible
                // Set to state error
                message += ComponentState.ERROR .toString();
                code = CommandResult.CODE.SUCCESS;
                c.setError();
                break;

        }
        c.setShouldRestore(false);
        
        return new CommandResult(code, message, c, cr);
    }
}

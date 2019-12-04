package org.deibic2s.ooka.rte.core;

import org.deibic2s.ooka.rte.persistence.ComponentDTO;
import org.deibic2s.ooka.rte.utils.CommandResult;


public class PrepareRestoreCommand extends Command {

    private Component c;
    private ComponentDTO cdto;
    private ComponentLoader cl;

    public PrepareRestoreCommand(ComponentDTO cdto, ComponentLoader cl){
        this.cdto  = cdto;
        this.cl = cl;
    }

    @Override
    public CommandResult execute() {
        String message = "Can't restore Component from nullValue.";
        CommandResult.CODE code = CommandResult.CODE.ERROR;
        if(cdto == null)
            return new CommandResult(code, message, c, null);

        c = new Component(cdto.getPathToComponent(), cdto.getName());
        c.setComponentID(cdto.getComponentID());
        c.setRestoreState(cdto.getComponentState());

        DeployCommand deployCommand = new DeployCommand(c, cl, c.getId());
        CommandResult cr = deployCommand.execute();

        if(cr.getCommandCode() != CommandResult.CODE.SUCCESS) {
            message = "Can't restore Component " + c.getName() + " with ID " + c.getId() + ". Deploying not successful.";
            return new CommandResult(code, message, c, cr);
        }
        c.setShouldRestore(true);
        message = "Preparing component " + c.getName() + " with ID " + c.getId() + " successful";
        code = CommandResult.CODE.SUCCESS;
        return new CommandResult(code, message, c, cr);
    }
}
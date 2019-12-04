package org.deibic2s.ooka.rte.core;

import org.deibic2s.ooka.rte.logging.RTELogCreator;
import org.deibic2s.ooka.rte.utils.CommandResult;
import static org.deibic2s.ooka.rte.utils.ComponentState.INITIALIZED;

public class DeployCommand extends Command {
    private Component c;
    private ComponentLoader cl;
    private Integer id;

    public DeployCommand(Component c, ComponentLoader cl, Integer compID){
        this.id = compID;
        this.c = c;
        this.cl = cl;
    }

    @Override
    public CommandResult execute() {
        String message = "Can not deploy Component.";
        CommandResult.CODE code = CommandResult.CODE.ERROR;
        c.setComponentID(id);
        if(c.getComponentState() != INITIALIZED){
            return new CommandResult(code, message, c, null);
        }

        cl.loadComponentFromFile(c);

        if(c.getClassLoader() == null){
            // Can't load Classloader
            c.setError();
            message = "Can not load JAR-File.";

        }else {
            cl.setStartStopToComponent(c);
            if(!c.isStartStopSet()){
                c.setError();
                // Can't set class/methods
                message = "Can not find Class with Start/Stop annotation.";
            }else {
                c.setDeployed();
                message = "Component deployed.";
                code = CommandResult.CODE.SUCCESS;
                cl.injectLogger(c);
                cl.injectLoggerFactory(c);
                cl.getEventListeners(c);
                cl.getInjectableEventFields(c);
            }
        }

        c.addCommand(new RemoveCommand(c));

        if(code == CommandResult.CODE.SUCCESS) {
            c.addCommand(new StartCommand(c, RTELogCreator.getInstance().getLogger("component")));
            c.addCommand(new StopCommand(c));
        }

        return new CommandResult(code, message, c, null);
    }
}

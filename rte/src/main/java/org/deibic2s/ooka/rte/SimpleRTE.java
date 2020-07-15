package org.deibic2s.ooka.rte;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.deibic2s.ooka.componentmodel.logging.ILogger;
import org.deibic2s.ooka.rte.core.*;
import org.deibic2s.ooka.rte.utils.CommandResult;
import org.deibic2s.ooka.rte.utils.ComponentState;
import org.deibic2s.ooka.rte.persistence.ComponentDTO;
import org.deibic2s.ooka.rte.persistence.JDBCComponentDAO;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.ArrayList;
import java.util.List;

import static org.deibic2s.ooka.rte.utils.CommandResult.CODE.ERROR;
import static org.deibic2s.ooka.rte.utils.CommandResult.CODE.SUCCESS;

public class SimpleRTE {
    private ExecutorService executor;
    private ComponentLoader componentLoader;
    private ObservableList<Component> availableComponents;
    private ReadOnlyBooleanWrapper isRTERunning;
    private Integer nextID;
    private JDBCComponentDAO dataComponentDAO;
    private Scope currentScope;
    private ILogger logger;


    public SimpleRTE(){
        executor = Executors.newSingleThreadExecutor();
        dataComponentDAO = new JDBCComponentDAO();
        isRTERunning = new ReadOnlyBooleanWrapper(false);
        nextID = 0;
        componentLoader = new ComponentLoader(this);
        availableComponents = FXCollections.observableArrayList();
        currentScope = Scope.In_Production;
    }

    public boolean getIsRTERunning(){
        return isRTERunning.getValue();
    }

    private void setIsRTERunning(boolean b){
        isRTERunning.set(b);
    }

    public ReadOnlyBooleanProperty isRTERunningProperty(){
        return isRTERunning.getReadOnlyProperty();
    }

    public SimpleRTE(ILogger logger){
        this();
        this.logger = logger;
    }

    private void logResult(CommandResult cr, String commandname){
        if(logger == null)
            return;

        cr.logResult(logger, commandname);
    }

    public Future<CommandResult> deployComponent(String pathToComponent){
        return executor.submit(() -> {
            Command deployCommand = new DeployCommand(new Component(pathToComponent, "component"), componentLoader, nextID++);
            CommandResult cr = deployCommand.execute();

            availableComponents.add(cr.getComponent());
            if(cr.getCommandCode() == SUCCESS) {
                dataComponentDAO.createDataComponent(cr.getComponent().getDataComponent());
            }
            return cr;
        });
    }

    public Future<CommandResult> startComponent(Integer componentID){
        return executor.submit(() -> {
            CommandResult cr = doCommand(componentID, StartCommand.class);
            dataComponentDAO.updateDataComponent(cr.getComponent().getDataComponent());
            logResult(cr, "Start");
            return cr;
        });
    }

    public Future<CommandResult> stopComponent(Integer componentID){
        return executor.submit(() -> {
            CommandResult cr = doCommand(componentID, StopCommand.class);
            dataComponentDAO.updateDataComponent(cr.getComponent().getDataComponent());
            logResult(cr, "Stop");
            return cr;
        });
    }

    public Future<CommandResult> removeComponent(Integer componentID){
        return executor.submit(() -> {
            CommandResult cr = doCommand(componentID, RemoveCommand.class);

            logResult(cr, "Remove");
            if(cr.getCommandCode() == SUCCESS){
                availableComponents.remove(cr.getComponent());
                dataComponentDAO.removeDataComponent(cr.getComponent().getDataComponent());
            }else {
                dataComponentDAO.updateDataComponent(cr.getComponent().getDataComponent());
            }
            return cr;
        });
    }

    private CommandResult doCommand(Integer componentID, Class<? extends Command> commandClass){
        Component c = availableComponents.stream()
                .filter(component -> componentID.equals(component.getId()))
                .findAny().orElse(null);

        if(!getIsRTERunning() && commandClass != RemoveCommand.class){
            return new CommandResult(ERROR, "RTE is not running.", c, null);
        }

        if(c == null)
            return new CommandResult(ERROR, "Component for command not found", null, null);

        Command com = c.getCommand(commandClass);

        if(com == null)
            return new CommandResult(ERROR, "Command not found", c, null);
        
        return com.execute();
    }

    public void stopRTE(){
        for(Component c : availableComponents)
            stopComponent(c.getId());
        setIsRTERunning(false);
    }

    public void startRTE(){
        setIsRTERunning(true);
        dataComponentDAO.removeAllDataComponents();
        for(Component c: availableComponents){
            if(c.getShouldRestore()){
                dataComponentDAO.removeDataComponent(c.getDataComponent());
                RestoreCommand rc = new RestoreCommand(c, componentLoader);
                CommandResult cr = rc.execute();
                logResult(cr, "Restore");
                if(cr.getComponent().getComponentState() == ComponentState.REMOVED){
                    dataComponentDAO.removeDataComponent(cr.getComponent().getDataComponent());
                }else {
                    dataComponentDAO.createDataComponent(cr.getComponent().getDataComponent());

                }
            }else{
                dataComponentDAO.createDataComponent(c.getDataComponent());
            }
        }
    }

    public void prepareRestore(){
        if(getIsRTERunning()) {
            logger.info("Can't restore while running.");
            return;
        }
        List<ComponentDTO> restoreCandidates = dataComponentDAO.getAllDataComponents();
        if(restoreCandidates == null || restoreCandidates.isEmpty())
            logger.info("No Components to restore.");

        for(ComponentDTO dc : restoreCandidates){
            if(nextID >= dc.getComponentID())
                dc = new ComponentDTO(dc.getName(), nextID++, dc.getComponentState(), dc.getPathToComponent());

            PrepareRestoreCommand prc = new PrepareRestoreCommand(dc, componentLoader);
            CommandResult cr = prc.execute();
            if(cr.getCommandCode() == SUCCESS) {
                availableComponents.add(cr.getComponent());
            }
            logResult(cr, "PrepareRestore");

        }
        for(Component c : availableComponents){
            if(c.getId() >= nextID){
                nextID = c.getId()+1;
            }
        }
    }

    public ObservableList<Component> getReadonlyComponentList(){
        return FXCollections.unmodifiableObservableList(availableComponents);
    }

    public Scope getScope(){
        return currentScope;
    }

    public void setScope(Scope s) {
        Scope lastScope = currentScope;
        currentScope = s;
        if(lastScope != currentScope)
        {   
            if(!availableComponents.isEmpty()){
                for (Component component : availableComponents) {
                    if(component.getComponentState() == ComponentState.DEPLOYED || component.getComponentState() == ComponentState.STARTED){
                        doCommand(component.getId(), InjectFieldsCommand.class);
                    }
                }
            }
        }
    }
}

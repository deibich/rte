package org.deibic2s.ooka.rte.utils;

import org.deibic2s.ooka.componentmodel.logging.ILogger;
import org.deibic2s.ooka.rte.core.Component;

public class CommandResult {
    public enum CODE {
        SUCCESS,
        WARNING,
        ERROR
    }

    private CODE code;
    private String msg;
    private Component component;
    private CommandResult resultBefore;

    public CommandResult(CODE code, String msg, Component component, CommandResult before){
        this.code = code;
        this.component = component;
        this.msg = msg;
        this.resultBefore = before;
    }

    public CODE getCommandCode(){
        return code;
    }

    public String getMessage(){
        return msg;
    }

    public Component getComponent(){
        return component;
    }

    public CommandResult getResultBefore(){
        return resultBefore;
    }

    public void logResult(ILogger logger, String commandName) {
        if(logger == null)
            return;

        logger.info("Command " + commandName + " for Component: " + component.getName() + " with ID: " +
                component.getId() + " executed.\nGot " + code.toString() + " with message " +
                msg
        );

    }


}

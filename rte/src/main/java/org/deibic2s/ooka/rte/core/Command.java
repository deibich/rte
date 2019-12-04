package org.deibic2s.ooka.rte.core;

import org.deibic2s.ooka.rte.utils.CommandResult;

public abstract class Command {
    public abstract CommandResult execute();

    Command(){
    }
}

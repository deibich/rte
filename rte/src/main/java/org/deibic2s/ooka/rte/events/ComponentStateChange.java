
package org.deibic2s.ooka.rte.events;

import org.deibic2s.ooka.rte.utils.ComponentState;

public class ComponentStateChange {
    public String getSourceComponent() {
        return sourceComponent;
    }

    public ComponentState getNewState() {
        return newState;
    }

    final String sourceComponent;
    final ComponentState newState;
    
    public ComponentStateChange(String sourceComponent, ComponentState newState){
        this.sourceComponent = sourceComponent;
        this.newState = newState;
    }
}

package org.deibic2s.ooka.rte.persistence;

import java.util.Objects;
import javafx.beans.property.*;
import org.deibic2s.ooka.rte.utils.ComponentState;

public class ComponentDTO {
    private SimpleStringProperty name;
    private ReadOnlyIntegerWrapper componentID;
    private SimpleObjectProperty<ComponentState> componentState;
    private ReadOnlyStringWrapper pathToComponent;

    public ComponentDTO(String name, Integer id, ComponentState componentState, String pathToComponent){
        this.name = new SimpleStringProperty(name);
        this.componentID = new ReadOnlyIntegerWrapper(id);
        this.componentState = new SimpleObjectProperty<>(componentState);
        this.pathToComponent = new ReadOnlyStringWrapper(pathToComponent);
    }

    public String getName() {
        return name.get();
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public int getComponentID() {
        return componentID.get();
    }

    public ReadOnlyIntegerProperty componentIDProperty() {
        return componentID.getReadOnlyProperty();
    }

    public ComponentState getComponentState() {
        return componentState.get();
    }

    public SimpleObjectProperty<ComponentState> componentStateProperty() {
        return componentState;
    }

    void setComponentState(ComponentState componentState) {
        this.componentState.set(componentState);
    }

    public String getPathToComponent() {
        return pathToComponent.get();
    }

    public ReadOnlyStringProperty pathToComponentProperty() {
        return pathToComponent.getReadOnlyProperty();
    }

    public void setPathToComponent(String pathToComponent) {
        this.pathToComponent.set(pathToComponent);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ComponentDTO that = (ComponentDTO) o;
        return Objects.equals(getName(), that.getName()) &&
                Objects.equals(getComponentID(), that.getComponentID()) &&
                Objects.equals(getComponentState(), that.getComponentState()) &&
                Objects.equals(getPathToComponent(), that.getPathToComponent());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getComponentID(), getComponentState(), getPathToComponent());
    }
}

package org.deibic2s.ooka.rte.core;

public enum Scope {
    Under_Test,
    In_Production,
    Under_Inspection,
    In_Maintenance,
    All;

    @Override
    public String toString() {
        return String.join(" ", this.name().split("_"));
    }
}
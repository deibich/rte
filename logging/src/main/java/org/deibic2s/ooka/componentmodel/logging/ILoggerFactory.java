package org.deibic2s.ooka.componentmodel.logging;

public interface ILoggerFactory {
    public ILogger getLogger(String context);
}
package org.deibic2s.ooka.logging;

public interface ILoggerFactory {
    public ILogger getLogger(String context);
}
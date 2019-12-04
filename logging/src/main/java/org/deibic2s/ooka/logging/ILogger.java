package org.deibic2s.ooka.logging;


public interface ILogger {
    void debug(String msg);

    void info(String msg);

    void warn(String msg);

    void error(String msg);
}

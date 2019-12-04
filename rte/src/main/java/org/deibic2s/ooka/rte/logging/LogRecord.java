package org.deibic2s.ooka.rte.logging;

import java.util.Date;

/**
 * @author jewelsea
 * @see <a href="https://stackoverflow.com/questions/24116858/most-efficient-way-to-log-messages-to-javafx-textarea-via-threads-with-simple-cu">stackoverflow</a>
 */
public class LogRecord {
    private Date timestamp;
    private Level  level;
    private String context;
    private String message;

    public LogRecord(Level level, String context, String message) {
        this.timestamp = new Date();
        this.level     = level;
        this.context   = context;
        this.message   = message;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public Level getLevel() {
        return level;
    }

    public String getContext() {
        return context;
    }

    public String getMessage() {
        return message;
    }
}

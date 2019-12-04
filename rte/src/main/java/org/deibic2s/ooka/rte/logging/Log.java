package org.deibic2s.ooka.rte.logging;

import java.util.Collection;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * @author jewelsea
 * @see <a href="https://stackoverflow.com/questions/24116858/most-efficient-way-to-log-messages-to-javafx-textarea-via-threads-with-simple-cu">stackoverflow</a>
 */
public class Log {
    private static final int MAX_LOG_ENTRIES = 1_000_000;

    private final BlockingDeque<LogRecord> log = new LinkedBlockingDeque<>(MAX_LOG_ENTRIES);

    public void drainTo(Collection<? super LogRecord> collection) {
        log.drainTo(collection);
    }

    public void offer(LogRecord record) {
        log.offer(record);
    }
}

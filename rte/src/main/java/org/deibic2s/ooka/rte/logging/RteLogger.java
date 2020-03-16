package org.deibic2s.ooka.rte.logging;

import org.deibic2s.ooka.componentmodel.logging.ILogger;

public class RteLogger implements ILogger {
        private final Log log;
        private final String context;

        public RteLogger(Log log, String context) {
                this.log = log;
                this.context = context;
        }

        public void log(LogRecord record) {
                log.offer(record);
        }

        @Override
        public void debug(String msg) {
                log(new LogRecord(Level.DEBUG, context, msg));
        }

        @Override
        public void info(String msg) {
                log(new LogRecord(Level.INFO, context, msg));
        }

        @Override
        public void warn(String msg) {
                log(new LogRecord(Level.WARN, context, msg));
        }

        @Override
        public void error(String msg) {
                log(new LogRecord(Level.ERROR, context, msg));
        }

        public Log getLog() {
                return log;
        }
}

package org.deibic2s.ooka.rte.logging;

import java.util.HashMap;

import org.deibic2s.ooka.logging.ILoggerFactory;
import org.deibic2s.ooka.logging.ILogger;
public class RTELogCreator implements ILoggerFactory {

    private static HashMap<String, ILogger> availableLoggers;
    private static RTELogCreator _instance = new RTELogCreator();

    public static RTELogCreator getInstance() {
        return _instance;
    }

    private RTELogCreator() {
        initList();
    }

    private void initList(){
        if(availableLoggers != null)
            return;

        availableLoggers = new HashMap<>();
    }

    @Override
    public ILogger getLogger(String context) {
        if(availableLoggers.containsKey(context))
            return availableLoggers.get(context);
        Log l = new Log();
        ILogger logger = new RteLogger(l, context);
        availableLoggers.put(context, logger);
        return logger;
    }

    public RteLogger getRTELogger(String context) {
        ILogger logger = getLogger(context);
        return (RteLogger) logger;
    }

}
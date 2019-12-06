package org.deibic2s.ooka.rte;

import java.util.AbstractMap.SimpleEntry;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.deibic2s.ooka.rte.core.Command;
import org.deibic2s.ooka.rte.utils.CommandResult;


public class CommandDispatcher {
    private static CommandDispatcher dispatcher;
    
    private long id;
    private BlockingQueue<SimpleEntry<String, ? extends Command>> commandQueue;
    private ConcurrentMap<String, CommandResult> resultMap;

    private Thread workerThread;
    private boolean shouldRun;

    private CommandDispatcher() {
        commandQueue = new LinkedBlockingQueue<>();
        resultMap = new ConcurrentHashMap<>();
        id = 0;
        shouldRun = true;
        startConsumer();
    }
    
    public static CommandDispatcher getInstance(){
        if(dispatcher == null){
            dispatcher = new CommandDispatcher();
        }
        return dispatcher;
    }

    public static CommandResult getResult(String id) {
        try {
            return getInstance().resultMap.get(id);
        } catch(NullPointerException | ClassCastException e) {
            return null;
        }
    }
    
    private String dispatchCommand(Command c){
        if(c == null)
            return null;
        
        String currId = String.valueOf(id);
        try {
            commandQueue.put(new SimpleEntry<>(currId, c));
        } catch (InterruptedException e) {
            return null;
        }
        return currId;
    }
    
    public static void executeCommand(Command c){
        getInstance().dispatchCommand(c);
    }

    
    protected void stopConsumer() {
        if(workerThread != null){
            shouldRun = false;
            workerThread.interrupt();
            try {
                workerThread.join();    
            } catch(InterruptedException e){
                System.out.println("Could not join WorkerThread.");
            }
        }
        workerThread = null;
        commandQueue.clear();
    }
    
    protected void startConsumer() {
        if(workerThread == null)
        workerThread = new Thread(() -> {consumeCommands();});
    }
    
    private void consumeCommands(){
        while(shouldRun) {
            try {
                SimpleEntry<String, ? extends Command> commandEntry = commandQueue.take();
                resultMap.put(commandEntry.getKey(), commandEntry.getValue().execute());
            } catch(InterruptedException ie) {
                
            }
        }
    }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package info.dolezel.fatrat.plugins.util;

import java.util.concurrent.ExecutionException;

/**
 * A helper classed used to perform lengthy tasks in a background thread.
 * Inspired by SwingWorker.
 */
public abstract class BackgroundWorker<T, V> {
    
    /**
     * This method is executed in the background thread.
     * @return The result of the task.
     * @throws Exception 
     */
    public abstract T doInBackground() throws Exception;
    
    /**
     * This method is executed in the main thread after doInBackground() returns.
     */
    public abstract void done();
    
    /**
     * Reimplement this to receive progress updates in the main thread.
     * @param p Progress value
     */
    protected void progressUpdated(V p) {
    }
    
    /**
     * Publishes new progress information.
     * @param p Progress value
     */
    public native void updateProgress(V p);
    
    /**
     * Blocks until the background thread finishes and returns the value returned
     * from that thread.
     * @return The value returned from doInBackground()
     * @throws ExecutionException Wraps any exception thrown from doInBackground()
     */
    public native T get() throws ExecutionException;
    
    /**
     * Starts the background task.
     */
    public native void execute();

    @Override
    protected void finalize() throws Throwable {
        disposeNative();
    }

    private native void disposeNative();
}
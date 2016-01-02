/*
FatRat download manager
http://fatrat.dolezel.info

Copyright (C) 2006-2011 Lubos Dolezel <lubos a dolezel.info>

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
version 2 as published by the Free Software Foundation.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package info.dolezel.fatrat.plugins.util;

import info.dolezel.fatrat.plugins.NativeObject;
import java.util.concurrent.ExecutionException;

/**
 * A helper classed used to perform lengthy tasks in a background thread.
 * Inspired by <code>SwingWorker</code>.
 */
public abstract class BackgroundWorker<T, V> extends NativeObject {
    
    /**
     * This method is executed in the background thread.
     * @return The result of the task.
     * @throws Exception 
     */
    public abstract T doInBackground() throws Exception;
    
    /**
     * This method is executed in the main thread after {@link #doInBackground()} returns.
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
     * @return The value returned from {@link #doInBackground()}
     * @throws ExecutionException Wraps any exception thrown from {@link #doInBackground()}
     */
    public native T get() throws ExecutionException;
    
    /**
     * Starts the background task.
     */
    public native void execute();
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package info.dolezel.fatrat.plugins;

/**
 *
 * @author lubos
 */
public class NativeObject {
    
    private long nativeObjectId;

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        
        disposeNative();
    }
    
    private native void disposeNative();
    
}

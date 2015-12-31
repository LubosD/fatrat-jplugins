/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package info.dolezel.fatrat.applet.settings;

import java.applet.AppletContext;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author lubos
 */
public class AppletSettings {
    AppletContext context;
    List<SettingsListener> listeners = new ArrayList<SettingsListener>();

    public AppletSettings(AppletContext context) {
        this.context = context;
    }
    
    public void addListener(SettingsListener l) {
        listeners.add(l);
    }
    
    public void removeListener(SettingsListener l) {
        listeners.remove(l);
    }
    
    public void setStringValue(String key, String value) {
        ByteArrayInputStream bais = null;
        try {
            if (context == null)
                return;
            bais = new ByteArrayInputStream(value.getBytes("UTF-8"));
            context.setStream(key, bais);
            notifyChange(key);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        } finally {
            try {
                bais.close();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
    
    public String getStringValue(String key) {
        try {
            if (context == null)
                return null;
            
            InputStream is = context.getStream(key);
            if (is == null)
                return null;
            String rv = IOUtils.toString(is);
            is.close();
            
            return rv;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    public void setValue(String key, Object obj) {
        if (context == null)
            return;
        
        try {
            ObjectOutputStream oos = null;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(baos);
            oos.writeObject(obj);
            oos.close();
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            context.setStream(key, bais);
            notifyChange(key);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    
    public Object getValue(String key) {
        if (context == null)
            return null;
        
        try {
            ObjectInputStream ois = null;
            InputStream is = context.getStream(key);
            if (is == null)
                return null;
            ois = new ObjectInputStream(is);
            Object rv = ois.readObject();
            is.close();
            ois.close();
            return rv;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    
    public void setValue(String key, int i) {
        setValue(key, (Integer) i);
    }
    
    public int getValue(String key, int def) {
        Object o = getValue(key);
        
        if (o == null || !(o instanceof Integer))
            return def;
        return (Integer) o;
    }

    private void notifyChange(String key) {
        for (SettingsListener l : listeners)
            l.onKeyValueChanged(key);
    }
}

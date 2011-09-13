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
import org.apache.commons.io.IOUtils;

/**
 *
 * @author lubos
 */
public class AppletSettings {
    AppletContext context;

    public AppletSettings(AppletContext context) {
        this.context = context;
    }
    
    public void setStringValue(String key, String value) throws UnsupportedEncodingException, IOException {
        if (context == null)
            return;
        
        ByteArrayInputStream bais = new ByteArrayInputStream(value.getBytes("UTF-8"));
        context.setStream(key, bais);
    }
    
    public String getStringValue(String key) throws IOException {
        if (context == null)
            return null;
        
        InputStream is = context.getStream(key);
        if (is == null)
            return null;
        String rv = IOUtils.toString(is);
        is.close();
        
        return rv;
    }
    
    public void setValue(String key, Object obj) throws IOException {
        if (context == null)
            return;
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        
        oos.writeObject(obj);
        oos.close();
        
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        context.setStream(key, bais);
    }
    
    public Object getValue(String key) throws IOException, ClassNotFoundException {
        if (context == null)
            return null;
        
        InputStream is = context.getStream(key);
        if (is == null)
            return null;
        ObjectInputStream ois = new ObjectInputStream(is);
        
        Object rv = ois.readObject();
        
        is.close();
        ois.close();
        
        return rv;
    }
    
    public void setValue(String key, int i) throws IOException {
        setValue(key, (Integer) i);
    }
    
    public int getValue(String key, int def) throws IOException, ClassNotFoundException {
        Object o = getValue(key);
        
        if (o == null || !(o instanceof Integer))
            return def;
        return (Integer) o;
    }
}

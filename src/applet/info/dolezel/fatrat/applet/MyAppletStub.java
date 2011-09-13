/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package info.dolezel.fatrat.applet;

import java.applet.Applet;
import java.applet.AppletContext;
import java.applet.AppletStub;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author lubos
 */
public class MyAppletStub implements AppletStub {
    Applet applet;
    String url;
    
    public MyAppletStub(String[] argv, Applet applet) {
        this.applet = applet;
        url = argv[0];
    }

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public URL getDocumentBase() {
        try {
            return new URL(url);
        } catch (MalformedURLException ex) {
            Logger.getLogger(MyAppletStub.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public URL getCodeBase() {
        return getDocumentBase();
    }

    @Override
    public String getParameter(String name) {
        return null;
    }

    @Override
    public AppletContext getAppletContext() {
        return null;
    }

    @Override
    public void appletResize(int width, int height) {
        applet.resize(width, height);
    }
    
}

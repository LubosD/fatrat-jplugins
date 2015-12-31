/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package info.dolezel.fatrat.web.features;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 *
 * @author lubos
 */
public class JarClassLoader extends URLClassLoader {
    public JarClassLoader(String jar) throws MalformedURLException {
        super(new URL[] { new URL("file://" + jar) }, JarClassLoader.class.getClassLoader());
    }
}

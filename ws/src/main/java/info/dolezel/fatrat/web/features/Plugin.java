/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package info.dolezel.fatrat.web.features;

import info.dolezel.fatrat.web.UpdateServlet;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 *
 * @author lubos
 */
public class Plugin {
    public String name, version, desc;
    public Date updated = new Date();
    public List<Feature> features = new ArrayList<Feature>();
    
    public static Plugin getPluginInfo(File ifile) throws Exception {
        JarFile file = new JarFile(ifile);
        Manifest manifest = file.getManifest();
        Attributes attr = manifest.getMainAttributes();
        
        Plugin pi = new Plugin();
        
        pi.name = ifile.getName();
        pi.name = pi.name.substring(0, pi.name.length()-4);
        pi.version = attr.getValue("Implementation-Version");
        pi.desc = attr.getValue("Description");
        
        file.close();
        
        return pi;
    }
}

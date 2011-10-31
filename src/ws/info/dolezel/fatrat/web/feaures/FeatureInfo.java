/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package info.dolezel.fatrat.web.feaures;

import info.dolezel.fatrat.plugins.annotations.AccountStatusPluginInfo;
import info.dolezel.fatrat.plugins.annotations.DownloadPluginInfo;
import info.dolezel.fatrat.plugins.annotations.ExtractorPluginInfo;
import info.dolezel.fatrat.plugins.annotations.SearchPluginInfo;
import info.dolezel.fatrat.plugins.annotations.UploadPluginInfo;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 *
 * @author lubos
 */
public class FeatureInfo {
    static final List<Plugin> cachedInfo = new LinkedList<Plugin>();
    static File pluginPath;
    
    public static void init(String path) {
        pluginPath = new File(path);
    }
    
    public static List<Plugin> getPlugins() {
        if (!pluginPath.exists())
            return Collections.EMPTY_LIST;

        File[] jars = pluginPath.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".jar");
            }
        });
        
        synchronized(cachedInfo) {
            // Remove old
            for (Iterator<Plugin> it = cachedInfo.iterator(); it.hasNext(); ) {
                boolean found = false;
                Plugin p = it.next();
                
                for (File j : jars) {
                    if (j.getName().equals(p.name)) {
                        found = true;
                        break;
                    }
                }
                
                if (!found)
                    it.remove();
            }
            
            // Update/add new
            for (int i = 0; i < jars.length; i++) {
                Date lastMod = new Date(jars[i].lastModified());
                
                for (Iterator<Plugin> it = cachedInfo.iterator(); it.hasNext(); ) {
                    Plugin p = it.next();
                    
                    if (p.name.equals(jars[i].getName())) {
                        if (lastMod.after(p.updated))
                            it.remove();
                        else
                            jars[i] = null;
                        
                        break;
                    }
                }
            }
            
            // Now perform the analysis
            for (int i = 0; i < jars.length; i++) {
                if (jars[i] == null)
                    continue;
                
                Plugin p = processPlugin(jars[i].getName());
                
                if (p != null)
                    cachedInfo.add(p);
            }
        }
        
        return cachedInfo;
    }

    private static Plugin processPlugin(String name) {
        try {
            String path = pluginPath + "/" + name;
            JarClassLoader jcl = new JarClassLoader(path);
            Set<Class> clss;
            Plugin p = new Plugin();
            
            // Download plugins
            clss = findClasses(new File(path), "info.dolezel.fatrat.plugins", DownloadPluginInfo.class, jcl);
            
            for (Iterator<Class> it = clss.iterator(); it.hasNext(); ) {
                Class cls = it.next();
                DownloadPluginInfo ann = (DownloadPluginInfo) cls.getAnnotation(DownloadPluginInfo.class);
                Feature f = new Feature();
                
                f.type = Feature.FeatureType.DownloadPlugin;
                f.name = ann.name();
                f.info = "Regexp: "+ann.regexp();
                f.className = cls.getCanonicalName();
                
                p.features.add(f);
            }
            
            // Upload plugins
            clss = findClasses(new File(path), "info.dolezel.fatrat.plugins", UploadPluginInfo.class, jcl);
            
            for (Iterator<Class> it = clss.iterator(); it.hasNext(); ) {
                Class cls = it.next();
                UploadPluginInfo ann = (UploadPluginInfo) cls.getAnnotation(UploadPluginInfo.class);
                Feature f = new Feature();
                
                f.type = Feature.FeatureType.UploadPlugin;
                f.name = ann.name();
                f.info = "File size limit: " + ann.sizeLimit();
                f.className = cls.getCanonicalName();
                
                p.features.add(f);
            }
            
            // Extraction plugins
            clss = findClasses(new File(path), "info.dolezel.fatrat.plugins", ExtractorPluginInfo.class, jcl);
            
            for (Iterator<Class> it = clss.iterator(); it.hasNext(); ) {
                Class cls = it.next();
                ExtractorPluginInfo ann = (ExtractorPluginInfo) cls.getAnnotation(ExtractorPluginInfo.class);
                Feature f = new Feature();
                
                f.type = Feature.FeatureType.ExtractionPlugin;
                f.name = ann.name();
                f.info = "Regexp: "+ann.regexp();
                f.className = cls.getCanonicalName();
                
                p.features.add(f);
            }
            
            // Extraction plugins
            clss = findClasses(new File(path), "info.dolezel.fatrat.plugins", ExtractorPluginInfo.class, jcl);
            
            for (Iterator<Class> it = clss.iterator(); it.hasNext(); ) {
                Class cls = it.next();
                ExtractorPluginInfo ann = (ExtractorPluginInfo) cls.getAnnotation(ExtractorPluginInfo.class);
                Feature f = new Feature();
                
                f.type = Feature.FeatureType.ExtractionPlugin;
                f.name = ann.name();
                f.info = "Regexp: "+ann.regexp();
                f.className = cls.getCanonicalName();
                
                p.features.add(f);
            }
            
            // Account status plugins
            clss = findClasses(new File(path), "info.dolezel.fatrat.plugins", AccountStatusPluginInfo.class, jcl);
            
            for (Iterator<Class> it = clss.iterator(); it.hasNext(); ) {
                Class cls = it.next();
                AccountStatusPluginInfo ann = (AccountStatusPluginInfo) cls.getAnnotation(AccountStatusPluginInfo.class);
                Feature f = new Feature();
                
                f.type = Feature.FeatureType.AccountStatusPlugin;
                f.name = ann.name();
                f.className = cls.getCanonicalName();
                
                p.features.add(f);
            }
            
            // Search plugins
            clss = findClasses(new File(path), "info.dolezel.fatrat.plugins", SearchPluginInfo.class, jcl);
            
            for (Iterator<Class> it = clss.iterator(); it.hasNext(); ) {
                Class cls = it.next();
                SearchPluginInfo ann = (SearchPluginInfo) cls.getAnnotation(SearchPluginInfo.class);
                Feature f = new Feature();
                
                f.type = Feature.FeatureType.SearchPlugin;
                f.name = ann.name();
                f.className = cls.getCanonicalName();
                
                p.features.add(f);
            }
            
            return p;
        } catch (Exception ex) {
            return null;
        }
    }
    
    private static Set<Class> findClasses(File directory, String packageName, Class annotation, ClassLoader ldr) throws ClassNotFoundException, IOException {
        Set<Class> classes = new HashSet<Class>();
        if (!directory.exists()) {

            String fullPath = directory.toString();
            String jarPath = fullPath.replaceFirst("[.]jar[!].*", ".jar").replaceFirst("file:", "");
            JarFile jarFile = new JarFile(jarPath);
            Enumeration<JarEntry> entries = jarFile.entries();
            while(entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String entryName = entry.getName();
                if(entryName.endsWith(".class") && !entryName.contains("$")) {
                    String className = entryName.replace('/', '.').replace('\\', '.').replace(".class", "");
                    Class cls = ldr.loadClass(className);
                    
                    if (annotation == null || cls.isAnnotationPresent(annotation))
                        classes.add(cls);
                }
            }
        } else {
            File[] files = directory.listFiles();
            for (File file : files) {
                if (file.isDirectory()) {
                    assert !file.getName().contains(".");
                    classes.addAll(findClasses(file, packageName + "." + file.getName(), annotation, ldr));
                } else if (file.getName().endsWith(".class")) {
                    Class cls = ldr.loadClass(packageName + '.' + file.getName().substring(0, file.getName().length() - 6));

                    if (annotation == null || cls.isAnnotationPresent(annotation))
                        classes.add(cls);
                }
            }
        }
        return classes;
    }
}

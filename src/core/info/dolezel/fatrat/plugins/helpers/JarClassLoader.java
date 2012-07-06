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


package info.dolezel.fatrat.plugins.helpers;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author lubos
 */
class JarClassLoader extends URLClassLoader {
    Map<String, JarClassLoader> children = new HashMap<String, JarClassLoader>();
    Set<String> loadedExtensions = new HashSet<String>();
    Map<String, String> classCache = new HashMap<String,String>();
    
    public JarClassLoader(ClassLoader parent) {
        super(new URL[0], parent);
    }
    
    public JarClassLoader() {
        this(JarClassLoader.class.getClassLoader());
    }
    
    public JarClassLoader addExtension(String path) throws MalformedURLException {
        JarClassLoader child = new JarClassLoader(JarClassLoader.class.getClassLoader());
        child.addJar(path);
        children.put(path, child);
        loadedExtensions.add(path);
        
        return child;
    }
    
    public JarClassLoader removeExtension(String path) {
        loadedExtensions.remove(path);
        return children.remove(path);
    }


    private void addJar(String path) throws MalformedURLException {
        URL url;
        
        url = new URL("jar", "", "file:"+ path + "!/");
        addURL(url);
    }
    
    public Class loadExtensionClass(String name) {
        if (classCache.containsKey(name)) {
            JarClassLoader cl = children.get(classCache.get(name));
            if (cl != null) {
                try {
                    return cl.loadClass(name);
                } catch (ClassNotFoundException ex) {
                    classCache.remove(name);
                }
            }
        }
        
        Class c;
        try {
            c = this.loadClass(name);
        } catch (ClassNotFoundException ex) {
            c = null;
        }
        
        if (c != null)
            return c;
        
        for (Map.Entry<String,JarClassLoader> e : children.entrySet()) {
            c = e.getValue().loadExtensionClass(name);
            if (c != null) {
                classCache.put(name, e.getKey());
                return c;
            }
        }
        
        return null;
    }
    
    public void loadAllExtensions(String path) throws Exception {
        File f = new File(path);
        File[] extensions = f.listFiles(new FileFilter() {

            @Override
            public boolean accept(File pathname) {
                return pathname.getName().endsWith(".jar");
            }
        });
        
        for (File e : extensions)
            addExtension(e.getAbsolutePath());
    }
    
    public Map<String,String> getPackageVersions() throws IOException {
        Map<String,String> rv = new HashMap<String,String>();

        for (Iterator<String> it = loadedExtensions.iterator(); it.hasNext(); ) {
            getPackageVersion(new File(it.next()), rv);
        }

        return rv;
    }

    private static void getPackageVersion(File fo, Map<String,String> rv) throws IOException {
        if (!fo.exists())
            return;
        
        JarFile file = new JarFile(fo);
        Manifest manifest = file.getManifest();
        Attributes attr = manifest.getMainAttributes();

        rv.put(fo.getName(), attr.getValue("Implementation-Version"));
    }
    
    public String loadPackagedFile(Class c, String path) {
        try {
            InputStream stream = c.getClassLoader().getResourceAsStream(path);
            if (stream == null)
                return null;
            return IOUtils.toString(stream, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Class[] findAnnotatedClasses(String packageName, Class ann) throws IOException, ClassNotFoundException {
        Set<Class> rv = new HashSet<Class>();
        
        rv.addAll(findAnnotatedClassesLocal(packageName, ann));
        for (JarClassLoader cl : children.values()) {
            rv.addAll(cl.findAnnotatedClassesLocal(packageName, ann));
        }
        
        return rv.toArray(new Class[rv.size()]);
    }

    public Set<Class> findAnnotatedClassesLocal(String packageName, Class annotation) throws IOException, ClassNotFoundException {
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = this.getResources(path);
        Set<File> dirs = new HashSet<File>();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            dirs.add(new File(resource.getFile()));
        }
        for (URL url : this.getURLs()) {
            dirs.add(new File(url.getFile()));
        }

        HashSet<Class> classes = new HashSet<Class>();
        for (File directory : dirs) {
            classes.addAll(findClasses(directory, packageName, annotation));
        }

        return classes;
    }

    private Set<Class> findClasses(File directory, String packageName, Class annotation) throws ClassNotFoundException, IOException {
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
                    Class cls = this.loadClass(className);
                    
                    if (annotation == null || cls.isAnnotationPresent(annotation))
                        classes.add(cls);
                }
            }
        } else {
            File[] files = directory.listFiles();
            for (File file : files) {
                if (file.isDirectory()) {
                    assert !file.getName().contains(".");
                    classes.addAll(findClasses(file, packageName + "." + file.getName(), annotation));
                } else if (file.getName().endsWith(".class")) {
                    Class cls = this.loadClass(packageName + '.' + file.getName().substring(0, file.getName().length() - 6));

                    if (annotation == null || cls.isAnnotationPresent(annotation))
                        classes.add(cls);
                }
            }
        }
        return classes;
    }
}

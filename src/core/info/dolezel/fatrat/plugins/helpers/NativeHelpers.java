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
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import org.apache.commons.io.IOUtils;

/**
 * For internal use only.
 * @author lubos
 */
public class NativeHelpers {
    private static final MyClassLoader loader = new MyClassLoader(Thread.currentThread().getContextClassLoader());

    static class MyClassLoader extends URLClassLoader {
        public MyClassLoader(ClassLoader parent) {
            super(new URL[0], parent);
        }
        
        public void addJar(String path) throws MalformedURLException {
            URL url = new URL("jar", "", "file:"+ path + "!/");
            addURL(url);
        }
    }

    public static Map<String,String> getPackageVersions() throws IOException {
        Map<String,String> rv = new HashMap<String,String>();
        String[] jars = System.getProperty("java.class.path").split(":");

        for (String jar : jars) {
            File fo = new File(jar);
            String name;
            
            if (!fo.exists())
                continue;
            
            name = fo.getName();
            
            if (name.startsWith("fatrat-") && !name.equals("fatrat-jplugins.jar"))
                getPackageVersion(fo, rv);
        }
        for (URL url : loader.getURLs()) {
            String jarPath = url.getPath().replaceFirst("[.]jar[!].*", ".jar").replaceFirst("file:", "");
            getPackageVersion(new File(jarPath), rv);
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

    public static void loadPackage(String path) throws Exception {
        loader.addJar(path);
    }

    public static Class[] findAnnotatedClasses(String path, String packageName, String annotation) throws IOException, ClassNotFoundException {
        URL url = new URL("jar", "", "file:"+ path + "!/");
        Set<Class> classes = findClasses(new File(url.getFile()), packageName, Class.forName(annotation));
        return classes.toArray(new Class[classes.size()]);
    }

    public static Class[] findAnnotatedClasses(String packageName, String annotation) throws IOException, ClassNotFoundException {
        Class[] c = findAnnotatedClasses(packageName, Class.forName(annotation));
        return c;
    }

    public static Class[] findAnnotatedClasses(String packageName, Class annotation) throws IOException, ClassNotFoundException {
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = loader.getResources(path);
        Set<File> dirs = new HashSet<File>();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            dirs.add(new File(resource.getFile()));
        }
        for (URL url : loader.getURLs()) {
            dirs.add(new File(url.getFile()));
        }

        ArrayList<Class> classes = new ArrayList<Class>();
        for (File directory : dirs) {
            classes.addAll(findClasses(directory, packageName, annotation));
        }

        return classes.toArray(new Class[classes.size()]);
    }

    private static Set<Class> findClasses(File directory, String packageName, Class annotation) throws ClassNotFoundException, IOException {
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
                    Class cls = loader.loadClass(className);
                    
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
                    Class cls = loader.loadClass(packageName + '.' + file.getName().substring(0, file.getName().length() - 6));

                    if (annotation == null || cls.isAnnotationPresent(annotation))
                        classes.add(cls);
                }
            }
        }
        return classes;
    }

    public static String loadDialogFile(String path) {
        try {
            InputStream stream = loader.getResourceAsStream(path);
            if (stream == null)
                return null;
            return IOUtils.toString(stream, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /*
    public static void main(String[] args) {
        try {
            loader.addJar(args[0]);
            
            Class[] c = findAnnotatedClasses("info.dolezel.fatrat.plugins", "info.dolezel.fatrat.plugins.annotations.DownloadPluginInfo");
            for (Class cc : c)
                System.out.println(cc);
            getPackageVersions();
            
            //loadPackage(args[0], Class.forName("info.dolezel.fatrat.plugins.annotations.DownloadPluginInfo"));
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }
    */
}

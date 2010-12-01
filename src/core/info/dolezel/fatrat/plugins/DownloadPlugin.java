/*
FatRat download manager
http://fatrat.dolezel.info

Copyright (C) 2006-2010 Lubos Dolezel <lubos a dolezel.info>

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

package info.dolezel.fatrat.plugins;

import info.dolezel.fatrat.plugins.listeners.CaptchaListener;
import info.dolezel.fatrat.plugins.listeners.PageFetchListener;
import info.dolezel.fatrat.plugins.listeners.WaitListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public abstract class DownloadPlugin {

	public enum State {
		Waiting(0),
		Active(1),
		ForcedActive(2),
		Paused(3),
		Failed(4),
		Completed(5);

        private final int v;
        State(int v) {
            this.v = v;
        }
        public int value() {
            return v;
        }
	};

	protected native void setMessage(String msg);
	protected native void setState(State state);
	protected native void fetchPage(String url, PageFetchListener cb, String postData);
	protected native void startDownload(String url);
	protected native void startWait(int seconds, WaitListener cb);
	protected native void logMessage(String msg);
    protected native void solveCaptcha(String url, CaptchaListener cb);

	public abstract void processLink(String link);

	public void finalCheck(String filePath) {
	}

	public boolean truncIncomplete() {
		return true;
	}
    
    public boolean forceSingleTransfer() {
        return false;
    }

    public void setFailed(String error) {
        setMessage(error);
        setState(State.Failed);
    }

    public static PluginInfo getClassInfo(Class cls) {
        if (!cls.isAnnotationPresent(PluginInfo.class))
            return null;
        return (PluginInfo) cls.getAnnotation(PluginInfo.class);
    }

    public static Class[] findAnnotatedClasses(String packageName, String annotation) throws IOException, ClassNotFoundException {
        Class[] c = findAnnotatedClasses(packageName, Class.forName(annotation));
        return c;
    }

    public static Class[] findAnnotatedClasses(String packageName, Class annotation) throws IOException, ClassNotFoundException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        assert classLoader != null;
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);
        Set<File> dirs = new HashSet<File>();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            dirs.add(new File(resource.getFile()));
        }

        ArrayList<Class> classes = new ArrayList<Class>();
        for (File directory : dirs) {
            classes.addAll(findClasses(directory, packageName, annotation));
        }
        
        return classes.toArray(new Class[classes.size()]);
    }

    private static List<Class> findClasses(File directory, String packageName, Class annotation) throws ClassNotFoundException, IOException {
        List<Class> classes = new ArrayList<Class>();
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
                    Class cls = Class.forName(className);

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
                    Class cls = Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6));

                    if (annotation == null || cls.isAnnotationPresent(annotation))
                        classes.add(cls);
                }
            }
        }
        return classes;
    }


    public static void main(String[] args) {
        try {
            Class[] c = findAnnotatedClasses("info.dolezel.fatrat.plugins", "info.dolezel.fatrat.plugins.PluginInfo");
            for (Class cc : c)
                System.out.println(cc);
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }
}


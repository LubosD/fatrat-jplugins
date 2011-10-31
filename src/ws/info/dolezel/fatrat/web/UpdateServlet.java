/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package info.dolezel.fatrat.web;

import info.dolezel.fatrat.web.feaures.FeatureInfo;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author lubos
 */
public class UpdateServlet extends HttpServlet {
    ServletOutputStream os;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        os = resp.getOutputStream();
        
        try {
            String version = req.getParameter("version");
            
            if (version == null) {
                String ua = req.getHeader("User-Agent");
                if (ua.startsWith("FatRat/"))
                    version = ua.substring(7);
                else
                    version = getServletConfig().getInitParameter("defaultVersion");
            }
            
            if (version.contains("/"))
                throw new Exception("Invalid version");
            
            File dir = new File(getServletContext().getRealPath("/update/plugins/"+version));
            if (!dir.exists())
                throw new Exception("Version not supported");
            
            File[] jars = dir.listFiles(new FilenameFilter() {

                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(".jar");
                }
            });
            
            os.print("Version: "+version+"\n\n");
            
            for (File f : jars) {
                printPlugin(f);
            }
        } catch (Exception ex) {
            os.print("ERROR: "+ex);
        }
        
    }

    private void printPlugin(File ifile) throws Exception {
        JarFile file = new JarFile(ifile);
        Manifest manifest = file.getManifest();
        Attributes attr = manifest.getMainAttributes();
        String name = ifile.getName();
        String version, desc;
        
        name = name.substring(0, name.length()-4); // remove ".jar"
        version = attr.getValue("Implementation-Version");
        desc = attr.getValue("Description");
        
        os.print(name+"\t"+version+"\t"+desc+"\n");
        file.close();
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        
        String version = getServletConfig().getInitParameter("defaultVersion");
        FeatureInfo.init(getServletContext().getRealPath("/update/plugins/"+version));
    }
    
    
    
}

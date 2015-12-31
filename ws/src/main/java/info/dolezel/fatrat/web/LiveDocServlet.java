/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package info.dolezel.fatrat.web;

import java.io.File;
import java.io.IOException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;


/**
 *
 * @author lubos
 */
public class LiveDocServlet extends HttpServlet {
    static private String repoPath;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Process p = Runtime.getRuntime().exec(new String[]{"git", "show", "HEAD:doc"+req.getPathInfo()}, null, new File(repoPath));
        IOUtils.copy(p.getInputStream(), resp.getOutputStream());
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        
        repoPath = config.getInitParameter("repoPath");
    }
    
}

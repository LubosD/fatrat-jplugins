/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package info.dolezel.fatrat.plugins;

import info.dolezel.fatrat.plugins.annotations.DownloadPluginInfo;
import info.dolezel.fatrat.plugins.config.Settings;
import info.dolezel.fatrat.plugins.extra.URLAcceptableFilter;
import info.dolezel.fatrat.plugins.util.FileUtils;
import java.io.File;
import java.util.regex.Matcher;

/**
 *
 * @author lubos
 */

@DownloadPluginInfo(name = "RapidShare.com premium download", regexp = "http://(www\\.)?rapidshare\\.com/files/\\d+/.+", truncIncomplete = false, forceSingleTransfer = false)
public class RapidSharePremiumDownload extends DownloadPlugin implements URLAcceptableFilter {

    @Override
    public void processLink(String link) {
        String user = (String) Settings.getValue("rapidshare/username", "");
        String password = (String) Settings.getValue("rapidshare/password", "");
        
        if (user.isEmpty()) {
            setFailed("Missing account information");
            return;
        }
        
        int pos = link.indexOf("://");
        if (pos < 0) {
            setFailed("Invalid link");
            return;
        }
        
        String after = link.substring(pos+3);
        
        setMessage("Premium download");
        startDownload("http://"+user+":"+password+"@"+after);
    }

    @Override
    public void finalCheck(String filePath) {
        if (new File(filePath).length() > 10*1024)
            return;
        
        String data = FileUtils.fileReadAll(filePath);
        if (data != null && data.toLowerCase().contains("error"))
            setFailed("Link dead? Account expired?");
    }

    public int acceptable(String url) {
        Matcher m = RapidShareFreeDownload.reLink.matcher(url);
        if (m.matches()) {
            String user = (String) Settings.getValue("rapidshare/username", "");
            if (!"".equals(user))
                return 3; // user has an account
            else
                return 0; // no account
        }
        return 0;
    }
    
}

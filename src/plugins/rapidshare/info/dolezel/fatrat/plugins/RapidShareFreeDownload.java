/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package info.dolezel.fatrat.plugins;

import info.dolezel.fatrat.plugins.annotations.ConfigDialog;
import info.dolezel.fatrat.plugins.annotations.DownloadPluginInfo;
import info.dolezel.fatrat.plugins.config.Settings;
import info.dolezel.fatrat.plugins.extra.URLAcceptableFilter;
import info.dolezel.fatrat.plugins.listeners.PageFetchListener;
import info.dolezel.fatrat.plugins.listeners.WaitListener;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author lubos
 */
@DownloadPluginInfo(name = "RapidShare.com FREE download")
@ConfigDialog("rapidshare.xml")
public class RapidShareFreeDownload extends DownloadPlugin implements URLAcceptableFilter {
    static final Pattern reLink = Pattern.compile("http://(www\\.)?rapidshare\\.com/files/(\\d+)/(.+)");
    static final Pattern reWaitTime = Pattern.compile("(\\d+) sec");
    
    String myLink;
    long fileID;
    String fileName;

    @Override
    public void processLink(String link) {
        Matcher m = reLink.matcher(link);
        
        if (!m.matches()) {
            setFailed("URL not supported");
            return;
        }
        
        this.myLink = link;
        
        setMessage("Calling the API");
        fileID = Long.parseLong(m.group(1));
        fileName = m.group(2);
        
        fetchPage("http://api.rapidshare.com/cgi-bin/rsapi.cgi?sub=download_v1&fileid=" + fileID + "&filename=" + fileName,
                new PageFetchListener() {

            public void onCompleted(ByteBuffer buf, Map<String, String> headers) {
                CharBuffer cb = charsetUtf8.decode(buf);
                String text = cb.toString();
                
                if (text.startsWith("ERROR")) {
                    Matcher mTime = reWaitTime.matcher(text);
                    if (mTime.find()) {
                        doWaiting(Integer.parseInt(mTime.group(1)));
                        return;
                    } else {
                        int pos = text.indexOf('\n');
                        if (pos == -1)
                            setFailed(text);
                        else
                            setFailed(text.substring(0, pos));
                    }
                } else if (text.startsWith("DL:")) {
                    String[] parts = text.split(",");
                    if (parts.length < 3) {
                        setFailed("Unknown server response");
                        return;
                    }
                    
                    String hostname = parts[0].substring(3);
                    String dlauth = parts[1];
                    
                    String downloadUrl = "http://" + hostname + "/" + "/cgi-bin/rsapi.cgi?sub=download_v1&editparentlocation=0&bin=1&fileid="
                            + fileID + "&filename=" + fileName + "&dlauth=" + dlauth;
                    
                    finalStep(downloadUrl, Integer.parseInt(parts[2]));
                } else
                    setFailed("Unknown server response: "+text);
            }

            public void onFailed(String error) {
                setFailed(error);
            }
        });
    }

    public int acceptable(String url) {
        Matcher m = reLink.matcher(url);
        if (m.matches()) {
            int accType = (Integer) Settings.getValue("rapidshare/account", new Integer(0));
            if (accType == 2)
                return 1; // user has an account -> let CurlDownload handle this
            else
                return 3; // no account, it's up to us
        }
        return 0;
    }
    
    private void doWaiting(int secs) {
        this.startWait(secs, new WaitListener() {

            public void onSecondElapsed(int secondsLeft) {
                if (secondsLeft > 0)
                    setMessage("Waiting: "+DownloadPlugin.formatTime(secondsLeft)+" left until next attempt");
                else
                    processLink(myLink);
            }
        });
    }
    
    private void finalStep(final String downloadUrl, int secs) {
        this.startWait(secs, new WaitListener() {

            public void onSecondElapsed(int secondsLeft) {
                if (secondsLeft > 0)
                    setMessage("Waiting: "+DownloadPlugin.formatTime(secondsLeft)+" left");
                else
                    startDownload(downloadUrl);
            }
        });
    }
}

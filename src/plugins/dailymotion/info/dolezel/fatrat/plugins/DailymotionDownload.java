/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package info.dolezel.fatrat.plugins;

import info.dolezel.fatrat.plugins.annotations.DownloadPluginInfo;
import info.dolezel.fatrat.plugins.config.Settings;
import info.dolezel.fatrat.plugins.extra.DownloadUrl;
import info.dolezel.fatrat.plugins.listeners.PageFetchListener;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author lubos
 */
@DownloadPluginInfo(name = "DailyMotion.com video download", regexp = "http://www.dailymotion.com/.+", forceSingleTransfer = false, truncIncomplete = false)
public class DailymotionDownload extends DownloadPlugin {
    static final Pattern reJSON = Pattern.compile("\\.addVariable\\(\"sequence\", +\"([^\"]+)\"");
    static final Pattern reTitle = Pattern.compile("<meta property=\"og:title\" content=\"([^\"]+)\"");

    @Override
    public void processLink(String link) {
        Integer timeout = Settings.getValueInt("httpftp/timeout", 20);
        if (timeout < 30) {
            setFailed("Increase your HTTP/FTP timeout to 30, the current value may not suffice");
            return;
        }
        
        fetchPage(link, new PageFetchListener() {

            @Override
            public void onCompleted(ByteBuffer buf, Map<String, String> headers) {
                CharBuffer cb = charsetUtf8.decode(buf);
                Matcher mSeq = reJSON.matcher(cb);
                Matcher mTitle = reTitle.matcher(cb);
                
                try {
                    if (!mSeq.find())
                        throw new Exception("JSON data not found");
                    
                    String jsondata = URLDecoder.decode(mSeq.group(1), "UTF-8");
                    if (jsondata.startsWith("["))
                        jsondata = jsondata.substring(1, jsondata.length()-1);
                    
                    JSONObject root = new JSONObject(jsondata);
                    //JSONObject video = findVideoObject(root);
                    
                    //if (video == null)
                    //    throw new Exception("Failed to find the video object");
                    
                    DownloadUrl url;
                    
                    //if (video.has("hd720URL"))
                    //    url = new DownloadUrl(video.getString("hd720URL"));
                    //else if (video.has("hqURL"))
                    //    url = new DownloadUrl(video.getString("hqURL"));
                    //else if (video.has("sdURL"))
                    //    url = new DownloadUrl(video.getString("sdURL"));
                    //else
                    //    throw new Exception("URL key not found");
                    
                    String video = findKeyValue(root, "URL");
                    if (video == null)
                        throw new Exception("Failed to find the URL");
                    
                    url = new DownloadUrl(video);
                    
                    if (mTitle.find())
                        url.setFileName(mTitle.group(1)+".mp4");
                    
                    startDownload(url);
                } catch (Exception e) {
                    setFailed(e.getMessage());
                }
            }

            @Override
            public void onFailed(String error) {
                setFailed(error);
            }
        });
    }
    
    private static JSONObject findVideoObject(JSONObject o) {
        for (Iterator<String> it = o.keys(); it.hasNext(); ) {
            String key = it.next();
            JSONObject c = o.optJSONObject(key);
            
            if (c == null) {
                JSONArray arr = o.optJSONArray(key);
                if (arr != null) {
                
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject cc = arr.optJSONObject(i);
                        if (cc != null) {
                            cc = findVideoObject(cc);
                            if (cc != null)
                                return cc;
                        }
                    }
                }
            } else {
                if (key.equals("videoPluginParameters"))
                    return c;
            
                c = findVideoObject(c);
                if (c != null)
                    return c;
            }
        }
        return null;
    }
    
    private static String findKeyValue(JSONObject o, String name) {
        for (Iterator<String> it = o.keys(); it.hasNext(); ) {
            String key = it.next();
            JSONObject c = o.optJSONObject(key);
            JSONArray a = o.optJSONArray(key);
            
            if (c != null) {
                String v = findKeyValue(c, name);
                if (v != null)
                    return v;
            } else if (a != null) {
                for (int i = 0; i < a.length(); i++) {
                    JSONObject cc = a.optJSONObject(i);
                    if (cc != null) {
                        String v = findKeyValue(cc, name);
                        if (v != null)
                            return v;
                    }
                }
            } else if (key.equals(name)) {
                String v = o.optString(key);
                if (v != null)
                    return v;
            }
        }
        
        return null;
    }
    
}

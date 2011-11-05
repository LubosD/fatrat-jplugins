/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package info.dolezel.fatrat.plugins;

import info.dolezel.fatrat.plugins.annotations.DownloadPluginInfo;
import info.dolezel.fatrat.plugins.extra.DownloadUrl;
import info.dolezel.fatrat.plugins.listeners.PageFetchListener;
import info.dolezel.fatrat.plugins.util.FormatUtils;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONObject;

/**
 *
 * @author lubos
 */
@DownloadPluginInfo(name = "MetaCafe.com video download", regexp = "http://www\\.metacafe\\.com/watch/.+", forceSingleTransfer = false, truncIncomplete = false)
public class MetacafeDownload extends DownloadPlugin {
    static final Pattern reFlashVars = Pattern.compile("<param id=\"flashVars\" name=\"flashvars\" value=\"([^\"]+)\"");

    @Override
    public void processLink(String link) {
        fetchPage(link, new PageFetchListener() {

            @Override
            public void onCompleted(ByteBuffer buf, Map<String, String> headers) {
                CharBuffer cb = charsetUtf8.decode(buf);
                Matcher mVars = reFlashVars.matcher(cb);
                
                try {
                
                    if (!mVars.find())
                        throw new Exception("Failed to parse the page");

                    Map<String,String> vars = FormatUtils.parseQueryString(mVars.group(1));
                    String mediaData = vars.get("mediaData");

                    if (mediaData == null)
                        throw new Exception("Failed to find mediaData");

                    JSONObject obj = new JSONObject(mediaData);
                    JSONObject mp4;
                    
                    // Prefer HD
                    if (obj.has("highDefinitionMP4"))
                        mp4 = obj.getJSONObject("highDefinitionMP4");
                    else if (obj.has("MP4"))
                        mp4 = obj.getJSONObject("MP4");
                    else
                        throw new Exception("Unknown JSON data structure");
                    
                    DownloadUrl url = new DownloadUrl(mp4.getString("mediaURL") + "?__gda__=" + mp4.getString("key"));
                    
                    if (vars.containsKey("title"))
                        url.setFileName(vars.get("title").replace('/', '_')+".mp4");
                    
                    startDownload(url);
                } catch (Exception e) {
                    setFailed(e.toString());
                }
            }

            @Override
            public void onFailed(String error) {
                setFailed(error);
            }
        });
    }
    
}

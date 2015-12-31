/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package info.dolezel.fatrat.plugins;

import info.dolezel.fatrat.plugins.annotations.ExtractorPluginInfo;
import info.dolezel.fatrat.plugins.listeners.PageFetchListener;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author lubos
 */
@ExtractorPluginInfo(name="RapidShare.com link list extractor", regexp="http://rapidshare\\.com/users/.+")
public class RapidShareExtractor extends ExtractorPlugin {
    static final Pattern reUrl = Pattern.compile("http://rapidshare\\.com/users/(.+)");

    @Override
    public void extractList(String url, ByteBuffer data, Map<String, String> headers) throws Exception {
        Matcher m = reUrl.matcher(url);
        if (!m.matches()) {
            setFailed("Invalid URL");
            return;
        }
        
        fetchPage("http://api.rapidshare.com/cgi-bin/rsapi.cgi?sub=viewlinklist&linklist="+m.group(1), new PageFetchListener() {

            public void onCompleted(ByteBuffer buf, Map<String, String> headers) {
                CharBuffer cb = charsetUtf8.decode(buf);
                String[] lines = cb.toString().split("\n");
                List<String> urls = new ArrayList<String>(lines.length);
                
                for (String line : lines) {
                    String[] values = line.split("\",\"");
                    
                    if (!values[0].startsWith("\"1"))
                        continue;
                    
                    urls.add("http://rapidshare.com/files/"+values[2]+"/"+values[3]);
                }
                
                finishedExtraction(urls.toArray(new String[0]));
            }

            public void onFailed(String error) {
                setFailed(error);
            }
        }, null);
        
    }
    
}

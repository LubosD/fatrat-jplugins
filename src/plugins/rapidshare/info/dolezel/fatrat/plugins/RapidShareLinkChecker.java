/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package info.dolezel.fatrat.plugins;

import info.dolezel.fatrat.plugins.annotations.LinkCheckerPluginInfo;
import info.dolezel.fatrat.plugins.listeners.PageFetchListener;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

/**
 *
 * @author lubos
 */
@LinkCheckerPluginInfo(name = "RapidShare.com", regexp = "https?://(www\\.)?rapidshare\\.com/files/\\d+/.+")
public class RapidShareLinkChecker extends LinkCheckerPlugin {

    @Override
    public void checkLinks(final String[] links) {
        StringBuilder sbIds = new StringBuilder(), sbNames = new StringBuilder();
        final Map<Integer,String> map = new HashMap<Integer,String>(links.length);
        
        for (String link : links) {
            Matcher m = RapidShareFreeDownload.reLink.matcher(link);
            
            if (sbIds.length() > 0) {
                sbIds.append(',');
                sbNames.append(',');
            }
            
            sbIds.append(m.group(2));
            sbNames.append(m.group(3));
            
            map.put(Integer.parseInt(m.group(2)), link);
        }
        
        String url = "http://api.rapidshare.com/cgi-bin/rsapi.cgi?sub=checkfiles&files="+sbIds+"&filenames="+sbNames;
        fetchPage(url, new PageFetchListener() {

            @Override
            public void onCompleted(ByteBuffer buf, Map<String, String> headers) {
                CharBuffer cb = charsetUtf8.decode(buf);
                String[] lines = cb.toString().split("\n");
                List<String> working = new ArrayList<String>(), broken = new ArrayList<String>();
                
                for (String line : lines) {
                    String[] fields = line.split(",");
                    if (fields.length < 5)
                        continue;
                    int id = Integer.parseInt(fields[0]);
                    int status = Integer.parseInt(fields[4]);
                    
                    if (status != 1)
                        broken.add(map.get(id));
                    else
                        working.add(map.get(id));
                    map.remove(id);
                }
                
                broken.addAll(map.values());
                
                reportWorking(working.toArray(new String[working.size()]));
                reportBroken(broken.toArray(new String[broken.size()]));
            }

            @Override
            public void onFailed(String error) {
                reportBroken(links);
            }
        });
    }
    
}

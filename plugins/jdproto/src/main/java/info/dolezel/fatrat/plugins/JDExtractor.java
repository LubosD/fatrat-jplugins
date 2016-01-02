/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package info.dolezel.fatrat.plugins;

import org.apache.commons.codec.binary.Base64;
import info.dolezel.fatrat.plugins.annotations.ExtractorPluginInfo;
import java.nio.ByteBuffer;
import java.util.Map;

/**
 *
 * @author lubos
 */
@ExtractorPluginInfo(name = "JDownloader URI support", regexp = "(jd://.+)|(jdlist://.+)")
public class JDExtractor extends ExtractorPlugin {

    @Override
    public void extractList(String url, ByteBuffer data, Map<String, String> headers) throws Exception {
        if (url.startsWith("jd://")) {
            String real = "http://" + url.substring(5);
            this.finishedExtraction(new String[]{ real });
        } else if (url.startsWith("jdlist://")) {
            byte[] b = Base64.decodeBase64(url.substring(9));
            if (b == null) {
                setFailed("Invalid data in the URL, the link is broken");
                return;
            }

            String[] urls = new String(b, "ISO-8859-1").split(",");

            this.finishedExtraction(urls);
        } else {
            setFailed("Unsupported link");
        }
    }
    
}

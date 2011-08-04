/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package info.dolezel.fatrat.plugins;

import info.dolezel.fatrat.plugins.annotations.DownloadPluginInfo;
import info.dolezel.fatrat.plugins.extra.DownloadUrl;
import info.dolezel.fatrat.plugins.listeners.CaptchaListener;
import info.dolezel.fatrat.plugins.listeners.PageFetchListener;
import info.dolezel.fatrat.plugins.util.PostQuery;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.codec.binary.Base64;

/**
 *
 * @author lubos
 */
@DownloadPluginInfo(name = "HellShare download", regexp = "http://download\\.hellshare\\.\\w+/.+")
public class HellshareDownload extends DownloadPlugin {
    static final Pattern reCaptcha = Pattern.compile("http://download\\.hellshare\\.\\w+/antispam\\.php\\?sv=[^\"]+");
    static final Pattern reFormAction = Pattern.compile("action=\"([^\"]+)\"");
    static final Pattern reFileName = Pattern.compile("<strong id=\"FileName_master\">([^<]+)</strong>");

    @Override
    public void processLink(String link) {
        final String formLink = link + "free";
        this.fetchPage(formLink, new PageFetchListener() {

            public void onCompleted(ByteBuffer buf, Map<String, String> headers) {
                CharBuffer cb = charsetUtf8.decode(buf);
                
                try {
                    String formAction, captchaUrl;
                    Matcher m = reFormAction.matcher(cb);
                    
                    if (!m.find()) {
                        if (cb.toString().indexOf("http://www.hellspy.com/search/") != -1)
                            throw new Exception("Link dead");
                        else
                            throw new Exception("Parse error #1");
                    }
                    
                    formAction = m.group(1);
                    m = reCaptcha.matcher(cb);
                    
                    if (!m.find())
                        throw new Exception("Parse error #2");
                    
                    captchaUrl = m.group();
                    
                    captchaStep(formLink, formAction, captchaUrl);
                    
                } catch (Exception e) {
                    setFailed(e.getMessage());
                }
            }

            public void onFailed(String error) {
                setFailed(error);
            }
        }, null, Collections.singletonMap("Referer", link));

        this.fetchPage(link, new PageFetchListener() {
            public void onFailed(String error) {
            }
            
            public void onCompleted(ByteBuffer buf, Map<String, String> headers) {
                CharBuffer cb = charsetUtf8.decode(buf);
                Matcher m = reFileName.matcher(cb);
                
                if (m.find())
                    reportFileName(m.group(1));
            }
        }, null, null);
    }
    
    private void captchaStep(final String formLink, final String formAction, String captchaUrl) {
        // Fetch the captcha image contents (we cannot pass the URL, the captcha changes on every load)
        fetchPage(captchaUrl, new PageFetchListener() {

            public void onCompleted(ByteBuffer buf, Map<String, String> headers) {
                String encodedImage;
                byte[] data = new byte[buf.capacity()];

                buf.get(data);

                encodedImage = "data:image/gif;base64," + Base64.encodeBase64String(data);

                HellshareDownload.this.solveCaptcha(encodedImage, new CaptchaListener() {

                    public void onFailed() {
                        setFailed("Failed to solve captcha");
                    }

                    public void onSolved(String text) {
                        finalStep(formLink, formAction, text);
                    }
                });
            }

            public void onFailed(String error) {
                setFailed(error);
            }
        }, null, Collections.singletonMap("Referer", formLink));
    }
    
    private void finalStep(String formLink, final String formAction, String captchaText) {
        /*fetchPage(formAction, new PageFetchListener() {

            public void onCompleted(ByteBuffer buf, Map<String, String> headers) {
                String location = headers.get("location");
                if (location == null)
                    setFailed("Download failed, incorrectly typed captcha?");
                else
                    startDownload(location, formAction);
            }

            public void onFailed(String error) {
                setFailed(error);
            }
        }, new PostQuery().add("captcha", captchaText).add("submit", "Stáhnout").toString(),
        Collections.singletonMap("Referer", formLink));*/

        startDownload(new DownloadUrl(formAction)
                .setReferrer(formLink)
                .setPostData(new PostQuery().add("captcha", captchaText).add("submit", "Stáhnout").toString())
                );
    }
    
}

/*
FatRat download manager
http://fatrat.dolezel.info

Copyright (C) 2006-2011 Lubos Dolezel <lubos a dolezel.info>

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
version 2 as published by the Free Software Foundation.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package info.dolezel.fatrat.plugins;

import info.dolezel.fatrat.plugins.annotations.DownloadPluginInfo;
import info.dolezel.fatrat.plugins.config.Settings;
import info.dolezel.fatrat.plugins.extra.URLAcceptableFilter;
import info.dolezel.fatrat.plugins.listeners.CaptchaListener;
import info.dolezel.fatrat.plugins.listeners.PageFetchListener;
import info.dolezel.fatrat.plugins.listeners.WaitListener;
import info.dolezel.fatrat.plugins.util.FormatUtils;
import info.dolezel.fatrat.plugins.util.PostQuery;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author lubos
 */
@DownloadPluginInfo(name = "CZshare.com FREE download")
public class CzshareDownload extends DownloadPlugin implements URLAcceptableFilter {
    static private final Pattern reHidden = Pattern.compile("<input type=\"hidden\" name=\"([^\"]+)\" value=\"([^\"]+)\"");
    static private final String strAlreadyDownloading = "Z Vaší IP adresy momentálně";

    @Override
    public void processLink(final String link) {
        long id = 0;
        
        Matcher m = CzsharePremiumDownload.reNewUrl.matcher(link);
        if (m.matches())
            id = Long.parseLong(m.group(2));
        
        if (id == 0) {
            m = CzsharePremiumDownload.reOldUrl.matcher(link);
            if (m.matches())
                id = Long.parseLong(m.group(2));
        }
        
        if (id == 0) {
            m = CzsharePremiumDownload.reNewUrl2.matcher(link);
            if (m.matches())
                id = Long.parseLong(m.group(2));
        }
        
        if (id == 0) {
            setFailed("Unsupported URL");
            return;
        }
            
        String downloadPage = "http://czshare.com/download.php?id=" + id;

        fetchPage(downloadPage, new PageFetchListener() {

            @Override
            public void onCompleted(ByteBuffer buf, Map<String, String> headers) {
                CharBuffer cb = charsetUtf8.decode(buf);
                
                if (cb.toString().contains(strAlreadyDownloading)) {
                    startWait(30, new WaitListener() {

                        @Override
                        public void onSecondElapsed(int secondsLeft) {
                            if (secondsLeft > 0)
                                setMessage("Waiting, will retry in "+FormatUtils.formatTime(secondsLeft));
                            else
                                processLink(link);
                        }
                        
                    });
                }
                
                PostQuery pq = new PostQuery();
                Matcher m = reHidden.matcher(cb);
                
                while (m.find())
                    pq.add(m.group(1), m.group(2));
                
                captchaStep(pq);
            }

            @Override
            public void onFailed(String error) {
                setFailed(error);
            }

        });
    }
    
    private void captchaStep(final PostQuery pq) {
        this.solveCaptchaLoadLocally("http://czshare.com/captcha.php", new CaptchaListener() {

            @Override
            public void onFailed() {
                setFailed("Failed to solive captcha");
            }

            @Override
            public void onSolved(String text) {
                pq.add("captchastring2", text).add("freedown", "Ověřit a stáhnout");
                
                fetchPage("http://czshare.com/download.php", new PageFetchListener() {

                    @Override
                    public void onCompleted(ByteBuffer buf, Map<String, String> headers) {
                        if (!headers.containsKey("location"))
                            setFailed("Download failed, invalid captcha?");
                        else
                            startDownload(headers.get("location"));
                    }

                    @Override
                    public void onFailed(String error) {
                        setFailed(error);
                    }
                    
                }, pq.toString());
            }
        });
    }

    @Override
    public int acceptable(String url) {
        Matcher m = CzsharePremiumDownload.reMainRegExp.matcher(url);
        String usr = (String) Settings.getValue("czshare/username", null);
        String pwd = (String) Settings.getValue("czshare/password", null);

        boolean hasPremium = !StringUtils.isEmpty(usr) && !StringUtils.isEmpty(pwd);
            
        if (!m.matches())
            return 0;
        else if (hasPremium)
            return 2;
        else
            return 3;
    }
    
}

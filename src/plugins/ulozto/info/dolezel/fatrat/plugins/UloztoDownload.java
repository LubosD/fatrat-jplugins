/*
FatRat download manager
http://fatrat.dolezel.info

Copyright (C) 2006-2010 Lubos Dolezel <lubos a dolezel.info>

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
import info.dolezel.fatrat.plugins.listeners.CaptchaListener;
import info.dolezel.fatrat.plugins.listeners.PageFetchListener;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author lubos
 */
@DownloadPluginInfo(regexp = "http://www.uloz.to/(live/)?\\d+/.+", name = "Uloz.to FREE download", forceSingleTransfer = false)
public class UloztoDownload extends DownloadPlugin {

    static final Pattern reImage = Pattern.compile("src=\"(http://img\\.uloz\\.to/captcha/(\\d+)\\.png)\"");
    static final Pattern reAction = Pattern.compile("<form name=\"dwn\" action=\"([^\"]+)\"");
    static final Pattern reFileName = Pattern.compile("<h2 class=\"nadpis\" style=\"[^\"]+\"><a href=\"[^\"]+\">([^\"]+)</a></h2>");

    @Override
    public void processLink(String link) {

        if (link.contains("/live/"))
            link = link.replace("/live/", "/");
        if (link.startsWith("http://uloz.to"))
            link = link.replace("http://uloz.to", "http://www.uloz.to");

        fetchPage(link, new PageFetchListener() {

            public void onCompleted(ByteBuffer buf, Map<String,String> headers) {
                try {
                    if (headers.containsKey("location")) {
                        String location = headers.get("location");
                        if (location.contains("smazano") || location.contains("nenalezeno"))
                            setFailed("The file has been removed");
                        else
                            setFailed("Unexpected redirection");
                        return;
                    }

                    CharBuffer cb = charsetUtf8.decode(buf);
                    final Matcher m = reImage.matcher(cb);
                    final Matcher mAction = reAction.matcher(cb);
                    Matcher mName = reFileName.matcher(cb);

                    if (!m.find()) {
                        setFailed("Failed to find the captcha code");
                        return;
                    }
                    if (!mAction.find()) {
                        setFailed("Failed to find the form action");
                        return;
                    }
                    if (mName.find())
                        reportFileName(mName.group(1));

                    String captchaUrl = m.group(1);
                    UloztoDownload.this.solveCaptcha(captchaUrl, new CaptchaListener() {

                        public void onFailed() {
                            setFailed("Failed to decode the captcha code");
                        }

                        public void onSolved(String text) {
                            String captchaCode = m.group(2);
                            fetchPage(mAction.group(1), new PageFetchListener() {

                                public void onCompleted(ByteBuffer buf, Map<String, String> headers) {
                                    String target = headers.get("location");
                                    if (target == null)
                                        setFailed("Failed to get the redirect URL");
                                    else
                                        startDownload(target);
                                }

                                public void onFailed(String error) {
                                    setFailed(error);
                                }

                            }, "captcha_nb="+captchaCode+"&captcha_user="+text+"&download=St%C3%A1hnout%20FREE");

                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    setFailed(e.toString());
                }
            }

            public void onFailed(String error) {
                setFailed("Failed to load the initial page");
            }
        }, null);
    }

}

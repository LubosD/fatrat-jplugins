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

import info.dolezel.fatrat.plugins.listeners.CaptchaListener;
import info.dolezel.fatrat.plugins.listeners.PageFetchListener;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author lubos
 */
@PluginInfo(regexp = "http://www.uloz.to/(/live)?\\d+/\\.+", name = "Uloz.to FREE download")
public class UloztoDownload extends DownloadPlugin {

    static final Pattern reImage = Pattern.compile("src=\"(http://img\\.uloz\\.to/captcha/(\\d+)\\.png)\"");
    static final Pattern reAction = Pattern.compile("<form name=\"dwn\" action=\"([^\"]+)\"");
    final Charset charset = Charset.forName("UTF-8");

    @Override
    public void processLink(String link) {

        if (link.contains("/live/"))
            link = link.replace("/live/", "/");

        fetchPage(link, new PageFetchListener() {

            public void onCompleted(ByteBuffer buf, Map<String,String> headers) {
                try {
                    CharBuffer cb = charset.decode(buf);
                    final Matcher m = reImage.matcher(cb);
                    final Matcher mAction = reAction.matcher(cb);

                    if (!m.find()) {
                        setFailed("Failed to find the captcha code");
                        return;
                    }
                    if (!mAction.find()) {
                        setFailed("Failed to find the form action");
                        return;
                    }

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

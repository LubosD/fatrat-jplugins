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
import info.dolezel.fatrat.plugins.listeners.CaptchaListener;
import info.dolezel.fatrat.plugins.listeners.PageFetchListener;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author lubos
 */
@DownloadPluginInfo(name = "Netload.in FREE download", regexp = "http://netload.in/datei([^/]+)/(.+)", forceSingleTransfer = true)
public class NetloadDownload extends DownloadPlugin {

    static final Pattern reUrl = Pattern.compile("http://netload.in/datei([^/]+)/(.+)");
    static final Pattern reSecondPage = Pattern.compile("<a class=\"whitelink\" href=\"([^\"]+)\">&raquo; continue with a free download</a>");
    static final Pattern reCaptcha = Pattern.compile("src=\"(share/includes/captcha\\.php\\?t=\\d+)\"");
    static final Pattern reFileId = Pattern.compile("&file_id=([^&]+)&");
    static final Pattern reFormAction = Pattern.compile("<form method=\"post\" action=\"([^\"]+)\"");

    @Override
    public void processLink(final String link) {
        Matcher mUrl = reUrl.matcher(link);

        if (!mUrl.matches()) {
            setFailed("Unsupported URL");
            return;
        }

        String simpleUrl = "http://netload.in/datei"+mUrl.group(1)+".htm";

        fetchPage(simpleUrl, new PageFetchListener() {

            public void onCompleted(ByteBuffer buf, Map<String, String> headers) {
                CharBuffer cb = charsetUtf8.decode(buf);
                Matcher m = reSecondPage.matcher(cb);

                if (!m.find())
                    setFailed("Error parsing the first page");
                else {
                    String secondPage = "http://netload.in/" + m.group(1);
                    secondStep(link, removeAmps(secondPage));
                }
            }

            public void onFailed(String error) {
                setFailed(error);
            }
        }, null);
    }

    private void secondStep(String link, final String secondPage) {
        fetchPage(secondPage, new PageFetchListener() {

            public void onCompleted(ByteBuffer buf, Map<String, String> headers) {
                final CharBuffer cb = charsetUtf8.decode(buf);
                Matcher m = reCaptcha.matcher(cb);

                if (!m.find())
                    setFailed("Failed to find the captcha code");
                else {
                    solveCaptcha("http://netload.in/"+m.group(1), new CaptchaListener() {

                        public void onFailed() {
                            setFailed("Failed to solve captcha");
                        }

                        public void onSolved(String text) {
                            Matcher mFileId = reFileId.matcher(secondPage);
                            Matcher mFormAction = reFormAction.matcher(cb);
                            if (!mFileId.find())
                                setFailed("The URL structure has changed unexpectedly");
                            else if (!mFormAction.find())
                                setFailed("Failed to find the form action");
                            else {
                                lastStep(text, mFileId.group(1), mFormAction.group(1));
                            }
                        }
                    });
                }
            }

            public void onFailed(String error) {
                setFailed(error);
            }
        }, null);
    }

    private void lastStep(String captcha, String fileId, String formAction) {
        // <a class="Orange_Link" href="http://85.131.179.24/e0783a497a624d75c4f81d100020756e8a54a757" >
        
        String postData = "file_id="+fileId+"&captcha_check="+captcha+"&start=";
        fetchPage("http://netload.in/"+formAction, new PageFetchListener() {

            public void onCompleted(ByteBuffer buf, Map<String, String> headers) {
                String location = headers.get("location");
                if (location == null)
                    setFailed("Failed to get the download URL");
                else
                    startDownload(location);
            }

            public void onFailed(String error) {
                setFailed(error);
            }
        }, postData);
    }

    private static String removeAmps(String str) {
        return str.replaceAll("&amp;", "&");
    }
}

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
import info.dolezel.fatrat.plugins.listeners.WaitListener;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * http://hotfile.com/dl/87916055/09d2b2e/fatrat-chrome.tar.bz2.html
 * @author lubos
 */
@DownloadPluginInfo(regexp = "http://hotfile.com/dl/.+", name = "HotFile.com FREE download")
public class HotfileDownload extends DownloadPlugin {

    // ";document.getElementById('dwltxt').innerHTML="

    static final Pattern reHiddenField = Pattern.compile("<input type=hidden name=(\\w+) value=([^>]+)>");
    static final Pattern reWaitTime = Pattern.compile("timerend=d\\.getTime\\(\\)\\+(\\d+)");
    static final Pattern reRecaptchaCode = Pattern.compile("src=\"http://api\\.recaptcha\\.net/challenge\\?k=([^\"]+)");
    static final Pattern reImageCode = Pattern.compile("id=\"recaptcha_challenge_field\" value=\"([^\"]+)\"");
    static final Pattern reDownloadLink = Pattern.compile("<a href=\"([^\"]+)\" class=\"click_download\">");

    Map<String,String> hiddenValues;

    // pocka X sekund (v JS v ms)
    // submitne form
    // recaptcha
    // submitne form
    // dostane link <A>

    @Override
    public void processLink(final String link) {

        setMessage("Fetching the first page");

        fetchPage(link, new PageFetchListener() {

            public void onCompleted(ByteBuffer buf, Map<String, String> headers) {
                CharBuffer cb = charsetUtf8.decode(buf);
                Matcher mHidden = reHiddenField.matcher(cb);
                int wait;
                boolean longWait = false;

                hiddenValues = new HashMap<String,String>();
                while (mHidden.find())
                    hiddenValues.put(mHidden.group(1), mHidden.group(2));
                
                Matcher mTime = reWaitTime.matcher(cb);
                if (!mTime.find()) {
                    setFailed("Failed to find the waiting time - link dead?");
                    return;
                }

                wait = (int) Math.ceil(Integer.parseInt(mTime.group(1)) / 1000.0);
                if (mTime.find()) { // long waiting
                    int xwait = (int) Math.ceil(Integer.parseInt(mTime.group(1)) / 1000.0);
                    if (xwait > wait)
                    {
                        wait = xwait;
                        longWait = true;
                    }
                }

                final boolean longWaitF = longWait;

                HotfileDownload.this.startWait(wait, new WaitListener() {

                    public void onSecondElapsed(int secondsLeft) {
                        if (secondsLeft > 0)
                            setMessage("Waiting: "+DownloadPlugin.formatTime(secondsLeft)+" left");
                        else {
                            if (!longWaitF)
                                captchaStep(link);
                            else
                                processLink(link);
                        }
                    }
                });
            }

            public void onFailed(String error) {
                setFailed("Failed to fetch the first page: "+error);
            }
        }, null);
    }

    private void captchaStep(final String link) {
        StringBuilder sb = new StringBuilder();

        for (Map.Entry<String,String> e : hiddenValues.entrySet()) {
            if (sb.length() > 0)
                sb.append("&");
            sb.append(e.getKey()).append('=').append(e.getValue());
        }

        setMessage("Loading the second page");

        fetchPage(link, new PageFetchListener() {

            public void onCompleted(ByteBuffer buf, Map<String, String> headers) {
                CharBuffer cb = charsetUtf8.decode(buf);
                Matcher mRC = reRecaptchaCode.matcher(cb);
                String rcCode;

                if (!mRC.find()) {
                    Matcher md = reDownloadLink.matcher(cb);
                    if (md.find()) {
                        String url = md.group(1);
                        HotfileDownload.this.startDownload(url);
                    } else
                        setFailed("Failed to find the recaptcha code");
                    return;
                }

                rcCode = mRC.group(1);

                captchaStepRC(link, rcCode);
            }

            public void onFailed(String error) {
                setFailed("Failed to load the captcha page: "+error);
            }
        }, sb.toString());
    }

    private void captchaStepRC(final String link, final String rcCode) {
        fetchPage("http://www.google.com/recaptcha/api/noscript?k="+rcCode, new PageFetchListener() {

            public void onCompleted(ByteBuffer buf, Map<String, String> headers) {
                CharBuffer cb = charsetUtf8.decode(buf);
                Matcher m = reImageCode.matcher(cb);
                if (!m.find())
                    setFailed("Failed to find the captcha image code");
                else {
                    final String lcode = m.group(1);
                    HotfileDownload.this.solveCaptcha("http://www.google.com/recaptcha/api/image?c="+lcode,
                            new CaptchaListener() {

                        public void onFailed() {
                            setFailed("Failed to decode captcha");
                        }

                        public void onSolved(String text) {
                            try {
                                StringBuilder sb = new StringBuilder("action=checkcaptcha&");
                                sb.append("recaptcha_challenge_field=").append(lcode).append('&');
                                sb.append("recaptcha_response_field=").append(URLEncoder.encode(text, "UTF-8"));
                                HotfileDownload.this.finalStep(link, sb.toString());
                            } catch (UnsupportedEncodingException ex) {
                                setFailed("Exception: "+ex.toString());
                            }
                        }
                    });
                }
            }

            public void onFailed(String error) {
                setFailed("Failed to load the captcha page #2: "+error);
            }

        }, null);
    }

    private void finalStep(String link, String postData) {
        fetchPage(link, new PageFetchListener() {

            public void onCompleted(ByteBuffer buf, Map<String, String> headers) {
                CharBuffer cb = charsetUtf8.decode(buf);
                Matcher m = reDownloadLink.matcher(cb);

                if (!m.find())
                    setFailed("Cannot find the download link");
                else {
                    String url = m.group(1);
                    HotfileDownload.this.startDownload(url);
                }
            }

            public void onFailed(String error) {
                setFailed("Failed to load the final page: "+error);
            }
        }, postData);
    }
}

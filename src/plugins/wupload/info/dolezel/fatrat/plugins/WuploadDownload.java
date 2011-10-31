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
import info.dolezel.fatrat.plugins.listeners.PageFetchListener;
import info.dolezel.fatrat.plugins.listeners.ReCaptchaListener;
import info.dolezel.fatrat.plugins.listeners.WaitListener;
import info.dolezel.fatrat.plugins.util.PostQuery;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author lubos
 */
@DownloadPluginInfo(name = "Wupload.com download", regexp = "http://www\\.wupload\\.com/file/.+")
public class WuploadDownload extends DownloadPlugin {
    static final Pattern reWaitTime = Pattern.compile("var countDownDelay = (\\d+)");
    static final Pattern reCaptchaCode = Pattern.compile("Recaptcha.create\\(\"([^\"]+)\"");
    static final Pattern reDownloadCode = Pattern.compile("<a href=\"([^\"]+)\"><span>Download Now</span></a>");
    static final Pattern reHiddenField = Pattern.compile("<input type='hidden' id='[^']+' name='([^']+)' value='([^']+)' />");
    static final Pattern reFileName = Pattern.compile("\\[URL=http://www.wupload.com/file/\\d+/([^\\]]+)\\]");
    
    String downloadLink, postData = "";

    @Override
    public void processLink(String link) {
        downloadLink = link;
        
        fetchPage(downloadLink, new PageFetchListener() {

            @Override
            public void onCompleted(ByteBuffer buf, Map<String, String> headers) {
                CharBuffer cb = charsetUtf8.decode(buf);
                Matcher mFileName = reFileName.matcher(cb);
                if (mFileName.find())
                    reportFileName(mFileName.group(1));
            }

            @Override
            public void onFailed(String error) {
            }
        });
        
        doStep();
    }

    private void doStep() {
        this.fetchPage(downloadLink+"?start=1", new PageFetchListener() {

            @Override
            public void onCompleted(ByteBuffer buf, Map<String, String> headers) {
                CharBuffer cb = charsetUtf8.decode(buf);
                String str = cb.toString();
                
                Matcher mWait = reWaitTime.matcher(str);
                Matcher mCaptcha = reCaptchaCode.matcher(str);
                Matcher mDownload = reDownloadCode.matcher(str);
                
                if (mWait.find()) {
                    int secs = Integer.parseInt(mWait.group(1));
                    PostQuery pq = new PostQuery();
                    
                    Matcher mHidden = reHiddenField.matcher(str);
                    while (mHidden.find())
                        pq.add(mHidden.group(1), mHidden.group(2));
                    postData = pq.toString();
                    
                    WuploadDownload.this.startWait(secs+1, new WaitListener() {

                        @Override
                        public void onSecondElapsed(int secondsLeft) {
                            if (secondsLeft > 0)
                                setMessage("Waiting: "+formatTime(secondsLeft)+" left");
                            else
                                doStep();
                        }
                    });
                } else if (mCaptcha.find()) {
                    String rcCode = mCaptcha.group(1);
                    
                    solveReCaptcha(rcCode, new ReCaptchaListener() {

                        @Override
                        public void onFailed() {
                            setFailed("Failed to solve captcha");
                        }

                        @Override
                        public void onSolved(String text, String code) {
                            try {
                                postData = "recaptcha_challenge_field="+code+"&recaptcha_response_field="+URLEncoder.encode(text, "UTF-8");
                                doStep();
                            } catch (UnsupportedEncodingException ex) {
                            }
                        }
                    });
                } else if (mDownload.find()) {
                    startDownload(mDownload.group(1), downloadLink);
                } else if (str.contains("The file that you're trying to download is larger")) {
                    setFailed("The file is too large for free download");
                } else
                    setFailed("Unknown page contents.");
            }

            @Override
            public void onFailed(String error) {
                setFailed(error);
            }
        }, postData, Collections.singletonMap("X-Requested-With", "XMLHttpRequest"));
    }
    
}

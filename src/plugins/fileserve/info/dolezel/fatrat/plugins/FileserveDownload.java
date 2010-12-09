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

import info.dolezel.fatrat.plugins.annotations.PluginInfo;
import info.dolezel.fatrat.plugins.listeners.CaptchaListener;
import info.dolezel.fatrat.plugins.listeners.PageFetchListener;
import info.dolezel.fatrat.plugins.listeners.WaitListener;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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
@PluginInfo(regexp = "http://www\\.fileserve\\.com/file/.+", name = "FileServe.com FREE download")
public class FileserveDownload extends DownloadPlugin {

    static final Pattern reCaptchaKey = Pattern.compile("var reCAPTCHA_publickey='([^']+)';");
    static final Pattern reShortenCode = Pattern.compile("<input type=\"hidden\" id=\"recaptcha_shortencode_field\" name=\"recaptcha_shortencode_field\" value=\"([^\"]+)\" />");
    static final Pattern reImageCode = Pattern.compile("challenge : '([^']+)");
    static final Pattern reLongWait = Pattern.compile("(\\d+) seconds to start another download");

    private String shortenCode;

    @Override
    public boolean forceSingleTransfer() {
        return true;
    }

    @Override
    public void processLink(final String link) {
        setMessage("Fetching the first page");

        this.fetchPage(link, new PageFetchListener() {

            public void onCompleted(ByteBuffer buf, Map<String, String> headers) {
                CharBuffer cb = charsetUtf8.decode(buf);
                final Matcher mKey = reCaptchaKey.matcher(cb);
                Matcher mShort = reShortenCode.matcher(cb);
                String scb = cb.toString();

                if (scb.contains("The file could not be found") || scb.contains("File not available")) {
                    setFailed("File not found");
                    return;
                }

                if (!mKey.find() || !mShort.find()) {
                    setFailed("Cannot parse the first page");
                    return;
                }

                shortenCode = mShort.group(1);

                setMessage("Sending AJAX request #1");
                fetchPage(link, new PageFetchListener() {

                    public void onCompleted(ByteBuffer buf, Map<String, String> headers) {
                        CharBuffer cb = charsetUtf8.decode(buf);
                        String s = cb.toString();

                        System.out.println(s);
                        if (s.contains("timeLimit")) {
                            // detect the wait time
                            detectLongWaitingTime(link);
                        } else
                            captchaStepRC(link, mKey.group(1));
                    }

                    public void onFailed(String error) {
                        setFailed(error);
                    }
                }, "checkDownload=check");
            }

            public void onFailed(String error) {
                setFailed(error);
            }
        }, null);
    }

    private void detectLongWaitingTime(final String link) {
        fetchPage(link, new PageFetchListener() {

            public void onCompleted(ByteBuffer buf, Map<String, String> headers) {
                CharBuffer cb = charsetUtf8.decode(buf);
                Matcher m = reLongWait.matcher(cb);
                if (!m.find())
                    setFailed("Cannot detect the waiting time");
                 else {
                    int secs = Integer.parseInt(m.group(1));
                    startWait(secs, new WaitListener() {

                        public void onSecondElapsed(int secondsLeft) {
                            if (secondsLeft > 0)
                                setMessage("Waiting: " + DownloadPlugin.formatTime(secondsLeft) + " left until next attempt");
                            else
                                processLink(link);
                        }
                    });
                 }
            }

            public void onFailed(String error) {
                setFailed("Failed to determine the wait time: "+error);
            }

        }, "checkDownload=showError&errorType=timeLimit");
    }

    private void captchaStepRC(final String link, final String rcCode) {
        fetchPage("http://www.google.com/recaptcha/api/challenge?k="+rcCode+"&ajax=1", new PageFetchListener() {

            public void onCompleted(ByteBuffer buf, Map<String, String> headers) {
                CharBuffer cb = charsetUtf8.decode(buf);
                Matcher m = reImageCode.matcher(cb);
                if (!m.find())
                    setFailed("Failed to find the captcha image code");
                else {
                    final String lcode = m.group(1);
                    FileserveDownload.this.solveCaptcha("http://www.google.com/recaptcha/api/image?c="+lcode,
                            new CaptchaListener() {

                        public void onFailed() {
                            setFailed("Failed to decode captcha");
                        }

                        public void onSolved(String text) {
                            try {
                                StringBuilder sb = new StringBuilder();
                                sb.append("recaptcha_challenge_field=").append(lcode);
                                sb.append("&recaptcha_response_field=").append(URLEncoder.encode(text, "UTF-8"));
                                sb.append("&recaptcha_shortencode_field=").append(shortenCode);
                                FileserveDownload.this.captchaSendStep(link, sb.toString());
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

    private void captchaSendStep(final String link, String postData) {
        setMessage("Sending captcha response");

        this.fetchPage("http://www.fileserve.com/checkReCaptcha.php", new PageFetchListener() {

            public void onCompleted(ByteBuffer buf, Map<String, String> headers) {
                // TODO: check for {"success":1} ?
                CharBuffer cb = charsetUtf8.decode(buf);
                String s = cb.toString();

                if (s.contains("incorrect")) {
                    setFailed("Incorrect captcha entered");
                    return;
                }
                System.out.println(cb.toString());
                
                setMessage("Sending AJAX request #2");

                fetchPage(link, new PageFetchListener() {

                    public void onCompleted(ByteBuffer buf, Map<String, String> headers) {
                        try {
                            byte[] num = new byte[buf.capacity()];
                            int offset = 0;

                            buf.get(num);

                            if (buf.get(0) == -17) // get rid of the BOM
                                offset = 3;

                            int secs = Integer.parseInt(new String(num, offset, num.length - offset, "UTF-8"));
                            FileserveDownload.this.startWait(secs+2, new WaitListener() {

                                public void onSecondElapsed(int secondsLeft) {
                                    if (secondsLeft > 0) {
                                        setMessage("Waiting: " + DownloadPlugin.formatTime(secondsLeft) + " left");
                                    } else {
                                        downloadStep(link);
                                    }
                                }
                            });
                        } catch (UnsupportedEncodingException ex) {
                            setFailed(ex.toString());
                        }
                    }

                    public void onFailed(String error) {
                        setFailed("Failed to send the AJAX request #2: "+error);
                    }
                }, "downloadLink=wait");
            }

            public void onFailed(String error) {
                setFailed("Failed to submit the captcha response: "+error);
            }
        }, postData);
    }

    private void downloadStep(final String link) {
        setMessage("Sending AJAX request #3");

        fetchPage(link, new PageFetchListener() {

            public void onCompleted(ByteBuffer buf, Map<String, String> headers) {

                setMessage("Sending AJAX request #4");
                fetchPage(link, new PageFetchListener() {

                    public void onCompleted(ByteBuffer buf, Map<String, String> headers) {
                        CharBuffer cb = charsetUtf8.decode(buf);
                        System.out.println(cb.toString());
                        
                        String url = headers.get("location");
                        if (url == null)
                            setFailed("Didn't receive the redirect URL");
                        else {
                            setMessage("Starting the download");
                            FileserveDownload.this.startDownload(url);
                        }
                    }

                    public void onFailed(String error) {
                        setFailed("Failed to send the AJAX request #4: "+error);
                    }

                }, "download=normal");
            }

            public void onFailed(String error) {
                setFailed("Failed to send the AJAX request #3: "+error);
            }

        }, "downloadLink=show");
    }
}

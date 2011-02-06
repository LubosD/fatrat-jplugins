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
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.File;
import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 *
 * @author lubos
 */
@DownloadPluginInfo(regexp = "http://(www.)?uloz\\.to/(live/)?\\d+/.+", name = "Uloz.to FREE download", forceSingleTransfer = false, truncIncomplete = false)
public class UloztoDownload extends DownloadPlugin {

    static final Pattern reImage = Pattern.compile("src=\"(http://img\\.uloz\\.to/captcha/(\\d+)\\.png)\"");
    static final Pattern reAction = Pattern.compile("<form name=\"dwn\" action=\"([^\"]+)\"");
    static final Pattern reFileName = Pattern.compile("<h2 class=\"nadpis\" style=\"[^\"]+\"><a href=\"[^\"]+\">([^\"]+)</a></h2>");

    static RememberedCaptcha rememberedCaptcha;

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
                            processLink(location);
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

                    final String captchaUrl = m.group(1);
                    UloztoDownload.this.mySolveCaptcha(captchaUrl, new CachedCaptchaListener(m.group(2)) {

                        public void onFailed() {
                            setFailed("Failed to decode the captcha code");
                        }

                        public void onSolved(String text, String captchaCode) {
                            
                            rememberCaptcha(captchaCode, captchaUrl, text);
                            
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

    @Override
    public void finalCheck(String filePath) {
        File file = new File(filePath);

        try {
            if (!file.exists() || file.length() == 0) {
                setFailed("The download has failed for an unknown reason");
            } else {

                FileInputStream is = new FileInputStream(file);
                byte[] buf = new byte[9];
                int rd = is.read(buf);
                if (new String(buf, 0, rd).equals("<!DOCTYPE"))
                    setFailed("The download has failed for an unknown reason");
                is.close();
            }
        } catch (Exception e) {
            
        }
    }

    private void mySolveCaptcha(final String captchaUrl, final CachedCaptchaListener captchaListener) {
        if (rememberedCaptcha != null) {
            // check if still valid
            fetchPage(rememberedCaptcha.url, new PageFetchListener() {

                public void onCompleted(ByteBuffer buf, Map<String, String> headers) {
                    try {
                        if (Arrays.equals(rememberedCaptcha.md5, MD5(buf))) {
                            captchaListener.onSolved(rememberedCaptcha.code, rememberedCaptcha.id);
                            return;
                        }
                    } catch (Exception ex) {
                    }

                    rememberedCaptcha = null;
                    solveCaptcha(captchaUrl, captchaListener);
                }

                public void onFailed(String error) {
                    rememberedCaptcha = null;
                    solveCaptcha(captchaUrl, captchaListener);
                }
            }, null);
        } else
            solveCaptcha(captchaUrl, captchaListener);
    }

    private void rememberCaptcha(final String id, final String captchaUrl, final String solved) {
        if (rememberedCaptcha != null && rememberedCaptcha.id.equals(id))
            return;
        
        fetchPage(captchaUrl, new PageFetchListener() {

            public void onCompleted(ByteBuffer buf, Map<String, String> headers) {
                try {
                    RememberedCaptcha rc = new RememberedCaptcha();

                    rc.id = id;
                    rc.code = solved;
                    rc.url = captchaUrl;
                    rc.md5 = MD5(buf);

                    rememberedCaptcha = rc;
                } catch (Exception ex) {
                }
            }

            public void onFailed(String error) {
            }

        }, null);
    }

    @Override
    public void onFailed() {
        rememberedCaptcha = null;
    }

    public static byte[] MD5(ByteBuffer data) throws NoSuchAlgorithmException, UnsupportedEncodingException  {
        MessageDigest md;
        md = MessageDigest.getInstance("MD5");
        byte[] md5hash = new byte[32];
        md.update(data);
        md5hash = md.digest();
        return md5hash;
    }

    private static class RememberedCaptcha {
        public String id;
        public String code, url;
        public byte[] md5;
    }
}


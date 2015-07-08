/*
FatRat download manager
http://fatrat.dolezel.info

Copyright (C) 2006-2012 Lubos Dolezel <lubos a dolezel.info>

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

import info.dolezel.fatrat.plugins.UloztoAccountStatus.LoginResultCallback;
import info.dolezel.fatrat.plugins.annotations.ConfigDialog;
import info.dolezel.fatrat.plugins.annotations.DownloadPluginInfo;
import info.dolezel.fatrat.plugins.config.Settings;
import info.dolezel.fatrat.plugins.listeners.PageFetchListener;
import info.dolezel.fatrat.plugins.util.PostQuery;
import info.dolezel.fatrat.plugins.listeners.CaptchaListener;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.Map;
import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Random;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author lubos
 */
@DownloadPluginInfo(regexp = 
"http://(www\\.|m\\.)?uloz\\.to/(live/)?\\w+/.+", 
name = "Uloz.to download", forceSingleTransfer = false, truncIncomplete = false)
@ConfigDialog("ulozto.xml")
public class UloztoDownload extends DownloadPlugin {
    
    boolean loggedIn = false;

    @Override
    public void processLink(String link) {

        if (link.contains("/live/"))
            link = link.replace("/live/", "/");
        if (link.startsWith("http://uloz.to"))
            link = link.replace("http://uloz.to", "http://www.uloz.to");
        if (link.startsWith("http://m.uloz.to"))
            link = link.replace("http://m.uloz.to", "http://www.uloz.to");
        
        if (!logIn(link))
            return;
        
        final String downloadLink = link; // I can't make 'link' final

        fetchPage(link, new PageFetchListener() {

            @Override
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
                    
                    if (cb.toString().contains("?disclaimer=1")) {
                        processLink(downloadLink + "?disclaimer=1");
                        return;
                    }
                    
                    final Document doc = Jsoup.parse(cb.toString());
                    final Element freeForm = doc.getElementById("frm-downloadDialog-freeDownloadForm");
                    final Elements premiumLink = doc.select("#download a.button");
                    Element captchaImage = doc.getElementById("captcha_img");
                    
                    boolean usePremium = usePremium(downloadLink);

                    if (cb.toString().contains("Nemáš dostatek kreditu"))
                        setMessage("Credit depleted, using FREE download");
                    else if (usePremium && !premiumLink.isEmpty()) {
                        String msg = "Using premium download";
                        
                        Elements aCredits = doc.getElementsByAttributeValue("href", "/kredit");

                        if (!aCredits.isEmpty())
                            msg += " ("+aCredits.get(0).ownText() +" left)";

                        setMessage(msg);
                        
                        startDownload("http://www.uloz.to" + premiumLink.get(0).attr("href"));
                        return;

                    } else if (loggedIn)
                        setMessage("Login failed, using FREE download");
                    
                    Elements aNames = doc.getElementsByClass("jsShowDownload");
                    if (!aNames.isEmpty())
                        reportFileName(aNames.get(0).ownText());
                    if (captchaImage == null) {
                        Elements captchaClass = doc.getElementsByClass("captchaContainer");
                        if (captchaClass.isEmpty()) {
                            setFailed("Failed to find the captchaContainer");
                            return;
                        }
                        Elements captchaList = captchaClass.get(0).children();
                        if (captchaClass.get(0).children().isEmpty()) {
                            setFailed("Failed to find the captcha, captchaContainer is empty");
                            return;
                        }
                        captchaImage = captchaClass.get(0).child(0);
                    }
                    
                    final PostQuery pq = new PostQuery();
                    final Map<String,String> hdr = new HashMap<String,String>();
                    Elements eHiddens = freeForm.select("input[type=hidden]");
                    
                    hdr.put("X-Requested-With", "XMLHttpRequest");
                    hdr.put("Referer", downloadLink);
                    hdr.put("Accept", "application/json, text/javascript, */*; q=0.01");
                    
                    for (Element e : eHiddens)
                        pq.add(e.attr("name"), e.attr("value"));
                    
                    fetchPage("http://uloz.to/reloadXapca.php?rnd=" + Math.abs(new Random().nextInt()), new PageFetchListener() {

                        @Override
                        public void onCompleted(ByteBuffer buf, Map<String, String> headers) {
                            CharBuffer cb = charsetUtf8.decode(buf);
                            String captchaUrl;
                            
                            try {
                                JSONObject json = new JSONObject(cb.toString());
                                captchaUrl = json.getString("image");
                                pq.add("hash", json.getString("hash"));
                                pq.add("timestamp", "" + json.getInt("timestamp"));
                                pq.add("salt", ""+json.getInt("salt"));
                            } catch (JSONException e) {
                                setFailed("Error parsing captcha JSON");
                                return;
                            }
                            
                            solveCaptcha(captchaUrl, new CaptchaListener() {

                                @Override
                                public void onFailed() {
                                    setFailed("Failed to decode the captcha code");
                                }

                                @Override
                                public void onSolved(String text) {

                                    pq.add("captcha_value", text);

                                    fetchPage("http://www.uloz.to" + freeForm.attr("action"), new PageFetchListener() {

                                        @Override
                                        public void onCompleted(ByteBuffer buf, Map<String, String> headers) {
                                            try {
                                                CharBuffer cb = charsetUtf8.decode(buf);
                                                JSONObject obj = new JSONObject(cb.toString());

                                                startDownload(obj.getString("url"));
                                            } catch (Exception e) {
                                                setFailed(""+e);
                                            }
                                        }

                                        @Override
                                        public void onFailed(String error) {
                                            setFailed(error);
                                        }

                                    }, pq.toString(), hdr);

                                }
                            });
                        }

                        @Override
                        public void onFailed(String error) {
                            setFailed("Failed to load captcha AJAX page");
                        }
                        
                    });

                    
                } catch (Exception e) {
                    e.printStackTrace();
                    setFailed(e.toString());
                }
            }

            @Override
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

    private boolean logIn(final String link) {
        if (loggedIn)
            return true;
        
        if (!usePremium(link))
            return true;
        
        String user = (String) Settings.getValue("ulozto/user", "");
        if ("".equals(user))
            return true;
        
        UloztoAccountStatus.logIn(this, new LoginResultCallback() {

            @Override
            public void success() {
                loggedIn = true;
                processLink(link);
            }

            @Override
            public void failure() {
                setFailed("Failed to log in");
            }
        });
        
        return false;
    }
    
    private boolean usePremium(String link) {
        if (link.endsWith("#free"))
            return false;
        return !Settings.getValue("ulozto/user", "").toString().isEmpty();
    }
}


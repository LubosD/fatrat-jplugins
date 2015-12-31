/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package info.dolezel.fatrat.plugins;

import info.dolezel.fatrat.plugins.annotations.AccountStatusPluginInfo;
import info.dolezel.fatrat.plugins.config.Settings;
import info.dolezel.fatrat.plugins.listeners.PageFetchListener;
import info.dolezel.fatrat.plugins.util.FormatUtils;
import info.dolezel.fatrat.plugins.util.PostQuery;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.Map;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author lubos
 */
@AccountStatusPluginInfo(name = "Uloz.to")
public class UloztoAccountStatus extends AccountStatusPlugin {
    
    @Override
    public boolean queryAccountBalance() {
        String user = (String) Settings.getValue("ulozto/user", "");
        String password = (String) Settings.getValue("ulozto/password", "");
        
        if ("".equals(user))
            return false;
        
        logIn(this, new LoginResultCallback() {

            @Override
            public void success() {
                loadFrontPage();
            }

            @Override
            public void failure() {
                reportAccountBalance(AccountState.AccountError, "Nelze se přihlásit");
            }
        });
        
        return true;
    }
    
    private void loadFrontPage() {
        fetchPage("http://www.uloz.to", new PageFetchListener() {

            @Override
            public void onCompleted(ByteBuffer buf, Map<String, String> headers) {
                CharBuffer cb = charsetUtf8.decode(buf);
                Document doc = Jsoup.parse(cb.toString());
                
                Elements aCredits = doc.getElementsByAttributeValue("data-gaaction", "Credit");
                
                if (aCredits.isEmpty()) {
                    reportAccountBalance(AccountState.AccountError, "Login failed");
                } else {
                    String bal = aCredits.get(0).ownText();
                    long bytes = FormatUtils.parseSize(bal);
                    reportAccountBalance(adviseState(bytes), bal);
                }
            }

            @Override
            public void onFailed(String error) {
                reportAccountBalance(AccountState.AccountError, error);
            }
        });
        
    }
    
    public static interface LoginResultCallback {
        void success();
        void failure();
    }
    
    public static void logIn(final Plugin plugin, final LoginResultCallback callback) {
        final String user = (String) Settings.getValue("ulozto/user", "");
        final String password = (String) Settings.getValue("ulozto/password", "");
        
        plugin.fetchPage("http://www.uloz.to/?do=web-login", new PageFetchListener() {

            @Override
            public void onCompleted(ByteBuffer buf, Map<String, String> headers) {
                String loc = headers.get("location");
                if (loc == null) {
                    callback.failure();
                    return;
                }
                
                plugin.fetchPage(loc, new PageFetchListener() {
                    @Override
                    public void onCompleted(ByteBuffer buf, Map<String, String> headers) {
                        CharBuffer cb = plugin.charsetUtf8.decode(buf);
                        Document doc = Jsoup.parse(cb.toString());
                        Element e = doc.getElementById("frm-loginForm");
                        Elements inputs;
                        
                        if (e == null) {
                            callback.failure();
                            return;
                        }
                        
                        PostQuery pq = new PostQuery();
                        pq.add("login", "Přihlásit");
                        pq.add("username", user);
                        pq.add("password", password);
                        
                        inputs = e.select("input[type=hidden]");
                        
                        for (Element input : inputs) {
                            pq.add(input.attr("name"), input.attr("value"));
                        }

                        plugin.fetchPage("http://www.uloz.to" + e.attr("action"), new PageFetchListener() {

                            @Override
                            public void onCompleted(ByteBuffer buf, Map<String, String> headers) {
                                if (headers.containsKey("location"))
                                    callback.success();
                                else
                                    callback.failure();
                            }

                            @Override
                            public void onFailed(String error) {
                                callback.failure();
                            }
                        }, pq.toString());
                    }
                    @Override
                    public void onFailed(String error) {
                        callback.failure();
                    }
                });
                
            }

            @Override
            public void onFailed(String error) {
                callback.failure();
            }
        });
    }
    
}

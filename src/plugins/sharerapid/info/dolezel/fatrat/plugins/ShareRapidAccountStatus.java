/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package info.dolezel.fatrat.plugins;

import info.dolezel.fatrat.plugins.annotations.AccountStatusPluginInfo;
import info.dolezel.fatrat.plugins.config.Settings;
import info.dolezel.fatrat.plugins.listeners.PageFetchListener;
import info.dolezel.fatrat.plugins.util.PostQuery;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author lubos
 */
@AccountStatusPluginInfo(name="Share-Rapid.com")
public class ShareRapidAccountStatus extends AccountStatusPlugin {
    static final Pattern reBalance = Pattern.compile("<td>Kredit:</td><td>([^<]+)");
    static final Pattern reHidden = Pattern.compile("<input type=\"hidden\" id=\"[^\"]+\" name=\"hash\" value=\"([^\"]+)\"");

    @Override
    public boolean queryAccountBalance() {
        final String user = (String) Settings.getValue("sharerapid/user", "");
        final String password = (String) Settings.getValue("sharerapid/password", "");
        
        if (user.equals(""))
            return false;
        
        fetchPage("http://share-rapid.com/prihlaseni/", new PageFetchListener() {

            public void onCompleted(ByteBuffer buf, Map<String, String> headers) {
                CharBuffer cb = charsetUtf8.decode(buf);
                Matcher m = reHidden.matcher(cb);
                
                if (!m.find()) {
                    setFailed("Parse error #1");
                    return;
                }
                
                login(m.group(1), user, password);
            }

            public void onFailed(String error) {
                setFailed(error);
            }
            
        });
        
        
        
        return true;
    }
    
    private void fetchState() {
        fetchPage("http://share-rapid.com/mujucet/", new PageFetchListener() {

            public void onCompleted(ByteBuffer buf, Map<String, String> headers) {
                CharBuffer cb = charsetUtf8.decode(buf);
                Matcher m = reBalance.matcher(cb);
                
                if (!m.find())
                    setFailed("Parse error #2");
                else {
                    String bal = m.group(1);
                    reportAccountBalance(adviseState(parseSize(bal)), bal);
                }
            }

            public void onFailed(String error) {
                setFailed(error);
            }
        });
    }
    
    private void login(String hash, String user, String password) {
        fetchPage("http://share-rapid.com/prihlaseni/", new PageFetchListener() {

            public void onCompleted(ByteBuffer buf, Map<String, String> headers) {
                if (!headers.containsKey("location"))
                    setFailed("Failed to log in");
                else
                    fetchState();
            }

            public void onFailed(String error) {
                setFailed(error);
            }
        }, new PostQuery().add("hash", hash).add("login", user).add("pass1", password).add("sbmt", "Přihlásit").toString());
    }
    
}

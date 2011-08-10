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
import java.util.Collections;
import java.util.Map;
import java.util.regex.Matcher;

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
        
        this.fetchPage("http://www.uloz.to/?do=authForm-submit", new PageFetchListener() {

            public void onCompleted(ByteBuffer buf, Map<String, String> headers) {
                loadFrontPage();
            }

            public void onFailed(String error) {
                reportAccountBalance(AccountState.AccountError, error);
            }
        }, new PostQuery().add("username", user).add("password", password).add("login", "Přihlásit").toString(),
            Collections.singletonMap("Referer", "http://www.uloz.to/")
        );
        
        return true;
    }
    
    private void loadFrontPage() {
        fetchPage("http://www.uloz.to", new PageFetchListener() {

            public void onCompleted(ByteBuffer buf, Map<String, String> headers) {
                CharBuffer cb = charsetUtf8.decode(buf);
                
                Matcher m = UloztoDownload.rePremiumDataLeft.matcher(cb);
                if (!m.find()) {
                    reportAccountBalance(AccountState.AccountError, "Login failed");
                } else {
                    String bal = m.group(1);
                    long bytes = parseSize(bal);
                    reportAccountBalance(adviseState(bytes), bal);
                }
            }

            public void onFailed(String error) {
                reportAccountBalance(AccountState.AccountError, error);
            }
        });
        
    }
    
}

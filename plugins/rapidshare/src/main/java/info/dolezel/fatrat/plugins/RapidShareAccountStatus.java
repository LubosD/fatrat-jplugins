/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package info.dolezel.fatrat.plugins;

import info.dolezel.fatrat.plugins.annotations.AccountStatusPluginInfo;
import info.dolezel.fatrat.plugins.config.Settings;
import info.dolezel.fatrat.plugins.listeners.PageFetchListener;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author lubos
 */
@AccountStatusPluginInfo(name = "RapidShare.com")
public class RapidShareAccountStatus extends AccountStatusPlugin {
    
    static final int RAPIDS_PER_DAY = 33;

    @Override
    public boolean queryAccountBalance() {
        String user = (String) Settings.getValue("rapidshare/username", "");
        String password = (String) Settings.getValue("rapidshare/password", "");
        
        if (user.isEmpty())
            return false;
        
        fetchPage("http://api.rapidshare.com/cgi-bin/rsapi.cgi?sub=getaccountdetails_v1&login="+user+"&password="+password,
                new PageFetchListener() {

            public void onCompleted(ByteBuffer buf, Map<String, String> headers) {
                CharBuffer cb = charsetUtf8.decode(buf);
                String str = cb.toString();
                
                if (str.startsWith("ERROR")) {
                    setFailed(str);
                    return;
                }
                
                Map<String,String> data = new HashMap<String,String>();
                String[] lines = str.split("\r?\n");
                
                for (String line : lines) {
                    String[] pair = line.split("=", 2);
                    if (pair.length != 2)
                        continue;
                    data.put(pair[0], pair[1]);
                }
                
                int rapids = Integer.parseInt(data.get("rapids"));
                AccountState state;
                
                if (rapids < RAPIDS_PER_DAY)
                    state = AccountState.AccountBad;
                else if (rapids < 5*RAPIDS_PER_DAY)
                    state = AccountState.AccountWarning;
                else
                    state = AccountState.AccountGood;
                
                String bal = ""+rapids+" Rapids (~"+(rapids/RAPIDS_PER_DAY)+" days)";
                reportAccountBalance(state, bal);
            }

            public void onFailed(String error) {
                setFailed(error);
            }
        });
        
        return true;
    }
    
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package info.dolezel.fatrat.plugins;

import info.dolezel.fatrat.plugins.listeners.PageFetchListener;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.util.Map;
import org.mozilla.javascript.Callable;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;

/**
 *
 * @author lubos
 */
class UloztoLinkDecoder {
    Context context;
    Scriptable scope;
    Scriptable kapp;
    Callable decryptMethod;

    public UloztoLinkDecoder() {
        context = Context.enter();
        scope = context.initStandardObjects();
    }
    
    public void autoLoad(final Plugin p, final String searchPageData, final InitDone callback) {
        p.fetchPage("http://img5.uloz.to/ul3/js/jquery.min.js", new PageFetchListener() {

            @Override
            public void onCompleted(ByteBuffer buf, Map<String, String> headers) {
                try {
                    String cb = p.charsetUtf8.decode(buf).toString();
                    int pos = cb.indexOf("function kapp");
                    String kappData = cb.substring(pos);
                    
                    loadScript(kappData);
                    
                    int end, start = searchPageData.indexOf("<script>");
                    if (start == -1)
                        throw new Exception("<script> not found");
                    
                    start += 8;
                    end = searchPageData.indexOf("</script>", start);
                    if (end == -1)
                        throw new Exception("</script> not found");
                    
                    loadScript(searchPageData.substring(start, end));
                    
                    callback.done();
                } catch (Exception e) {
                    callback.failed();
                }
            }

            @Override
            public void onFailed(String error) {
                callback.failed();
            }
        });
    }
    
    public void loadScript(String data) throws IOException {
        Reader reader = new StringReader(data);
        Script script = context.compileReader(reader, "inputscript.js", 1, null);
        
        script.exec(context, scope);
    }
    
    private void initKapp() {
        kapp = (Scriptable) scope.get("ad", scope);
    }
    
    public String decode(String value) {
        if (kapp == null)
            initKapp();
        
        String result = context.evaluateString(scope, "ad.decrypt(kn['"+value+"'])", "<cmd>", 1, null).toString();
        
        return removeNonPrintable(result.toString());
    }
    
    // From http://stackoverflow.com/questions/7161534/fastest-way-to-strip-all-non-printable-characters-from-a-java-string
    private static String removeNonPrintable(String in) {
        char[] oldChars = new char[in.length()];
        in.getChars(0, in.length(), oldChars, 0);
        char[] newChars = new char[in.length()];
        int newLen = 0;
        
        for (int j = 0; j < in.length(); j++) {
            char ch = oldChars[j];
            if (ch >= ' ' && ch <= 'z') {
                newChars[newLen] = ch;
                newLen++;
            }
        }
        return new String(newChars, 0, newLen);
    }
    
    public static interface InitDone {
        public void done();
        public void failed();
    }
}

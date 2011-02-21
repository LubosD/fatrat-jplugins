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

import info.dolezel.fatrat.plugins.annotations.ConfigDialog;
import info.dolezel.fatrat.plugins.annotations.DownloadPluginInfo;
import info.dolezel.fatrat.plugins.config.Settings;
import info.dolezel.fatrat.plugins.listeners.PageFetchListener;
import info.dolezel.fatrat.plugins.util.PostQuery;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@DownloadPluginInfo(name = "CZshare.com premium download", regexp = "http://(www\\.)?czshare\\.com/(\\d+/.+|download_file\\.php\\?id=(\\d+)&file=(.+))")
@ConfigDialog("czshare.xml")
public class CzshareDownload extends DownloadPlugin {
    static final Pattern reOldUrl = Pattern.compile("http://(www\\.)?czshare\\.com/download_file\\.php\\?id=(\\d+)&file=(.+)");
    static final Pattern reConverted = Pattern.compile("<a href=\"(http://www\\d+.czshare.com/\\d+/[^\"]+/)\">");

    String link, username, password;

    @Override
    public void processLink(String link) {
        Matcher mOld = reOldUrl.matcher(link);
        if (mOld.matches())
            link = "http://czshare.com/" + mOld.group(2) + "/" + mOld.group(3);

        this.link = link;

        username = (String) Settings.getValue("czshare/username", null);
        password = (String) Settings.getValue("czshare/password", null);

        if (isEmpty(username) || isEmpty(password)) {
            setFailed("Premium account information required");
            return;
        }

        fetchPage("http://czshare.com/prihlasit.php", new PageFetchListener() {

            public void onCompleted(ByteBuffer buf, Map<String, String> headers) {
                // Now we have a PHPSESSID cookie

                doLogin();
            }

            public void onFailed(String error) {
                setFailed(error);
            }
        }, null);
    }

    private void doLogin() {
        fetchPage("http://czshare.com/prihlasit.php", new PageFetchListener() {

            public void onCompleted(ByteBuffer buf, Map<String, String> headers) {
                if (!headers.containsKey("location") || !headers.get("location").startsWith("http://czshare.com/profi")) {
                    setFailed("Login failed");
                    return;
                }

                convertLink();
            }

            public void onFailed(String error) {
                setFailed(error);
            }

        }, new PostQuery().add("id", "").add("file", "").add("prihlasit", "Přihlásit").add("step", "1")
                .add("jmeno2", username).add("heslo", password).toString());
    }

    private void convertLink() {
        fetchPage("http://czshare.com/profi/graber.php", new PageFetchListener() {

            public void onCompleted(ByteBuffer buf, Map<String, String> headers) {
                CharBuffer cb = charsetUtf8.decode(buf);
                Matcher m = reConverted.matcher(cb);

                if (!m.find()) {
                    setFailed("Failed to convert the link");
                    return;
                }

                startDownload(m.group(1));
            }

            public void onFailed(String error) {
                setFailed(error);
            }

        }, new PostQuery().add("stahovat", "stahovat").add("linky", link).toString());
    }

    private static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

}

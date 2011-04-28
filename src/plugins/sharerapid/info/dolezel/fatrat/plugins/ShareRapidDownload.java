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
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.Map;

@DownloadPluginInfo(name = "Share-rapid.com FREE download", regexp = "http://(share-rapid\\.com|sharerapid.cz)/stahuj/\\d+/.+", forceSingleTransfer = false)
@ConfigDialog("share-rapid.xml")
public class ShareRapidDownload extends DownloadPlugin {

    @Override
    public void processLink(final String link) {
        final String user = (String) Settings.getValue("sharerapid/user", null);
        final String password = (String) Settings.getValue("sharerapid/password", null);

        if (user == null || password == null || user.isEmpty() || password.isEmpty()) {
            setFailed("Missing username/password in settings");
            return;
        }

        fetchPage("http://share-rapid.com/checkfiles.php", new PageFetchListener() {

        /*
         * {"error":true,"msg":"Not found","msgex":"Soubor byl smaz"}

           {"error":false,"filename":"Takers.2010.DVD.XviD.CZ-KiNOBOX.part1.rar","filepath":"1902105\/takers-2010-dvd-xvid-cz-kinobox-part1.rar",
                "size":"1047527424","subdomain":"s02","msgId":200}\r\n

         */

            public void onCompleted(ByteBuffer buf, Map<String, String> headers) {
                CharBuffer cb = charsetUtf8.decode(buf);

                if (cb.toString().indexOf("\"error\":true") != -1) {
                    setFailed("The link is dead");
                } else {
                    String url = "http://" + user + ":" + password + "@" + link.substring(7);
                    startDownload(url, null, "share-rapid downloader");
                }
            }

            public void onFailed(String error) {
                setFailed("Failed to check the link");
            }

        }, "files="+link);
    }

}

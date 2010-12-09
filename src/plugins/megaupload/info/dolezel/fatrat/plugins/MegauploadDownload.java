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
import info.dolezel.fatrat.plugins.listeners.PageFetchListener;
import info.dolezel.fatrat.plugins.listeners.WaitListener;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author lubos
 */

@PluginInfo(regexp = "http://www\\.megaupload\\.com/\\?d=.+", name = "MegaUpload.com FREE download")
public class MegauploadDownload extends DownloadPlugin {
    // http://www.megaupload.com/?d=GQ7217A4
    // http://www.megaupload.com/?d=4HB740K4

    static final Pattern reWaitTime = Pattern.compile("count=(\\d+);");
    static final Pattern reDownloadLink = Pattern.compile("<a href=\"([^\"]+)\" class=\"down_butt1\"");
    static final Pattern reFileName = Pattern.compile("File name:</span> <span class=\"down_txt2\">([^<]+)<");

    @Override
    public void processLink(String link) {
        // "The file you are trying to access is temporarily unavailable. Please try again later."
        // "Unfortunately, the link you have clicked is not available."
        fetchPage(link, new PageFetchListener() {

            public void onCompleted(ByteBuffer buf, Map<String, String> headers) {
                CharBuffer cb = charsetUtf8.decode(buf);
                Matcher mSecs = reWaitTime.matcher(cb);

                if (!mSecs.find()) {
                    String scb = cb.toString();
                    if (scb.contains("not available"))
                        setFailed("The link is invalid");
                    else if (scb.contains("temporarily unavailable"))
                        setFailed("The link is temporarily unavailable");
                    else
                        setFailed("Failed to find the waiting time");
                }

                Matcher mName = reFileName.matcher(cb);
                if (mName.find())
                    reportFileName(mName.group(1));

                Matcher mLink = reDownloadLink.matcher(cb);
                if (!mLink.find()) {
                    setFailed("Download link not found");
                    return;
                }
                final String link = mLink.group(1);

                int seconds = Integer.parseInt(mSecs.group(1));
                if (seconds > 0) {
                    MegauploadDownload.this.startWait(seconds, new WaitListener() {

                        public void onSecondElapsed(int secondsLeft) {
                            if (secondsLeft > 0)
                                setMessage("Waiting: " + DownloadPlugin.formatTime(secondsLeft) + " left");
                            else
                                downloadStep(link);
                        }
                    });
                }  else
                    downloadStep(link);
            }

            public void onFailed(String error) {
                setFailed("Error fetching the first page: "+error);
            }
        }, null);
    }

    private void downloadStep(String link) {
        this.startDownload(link);
    }
}

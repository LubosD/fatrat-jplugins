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
@DownloadPluginInfo(name = "Edisk.cz FREE download", regexp = "http://www.edisk.(cz|sk)/stahni/\\d+/.+", forceSingleTransfer = true)
public class EdiskDownload extends DownloadPlugin {

    static final Pattern reUrl = Pattern.compile("http://www.edisk.(cz|sk)/stahni/(\\d+/.+)");
    static final Pattern reWaitTime = Pattern.compile("var waitSecs = (\\d+);");
    static final Pattern reNameHint = Pattern.compile("<span class=\"bold\">(.+) \\(\\d+ \\w+\\)</span>");

    @Override
    public void processLink(final String link) {
        Matcher mUrl = reUrl.matcher(link);

        if (!mUrl.matches()) {
            setFailed("Invalid URL");
            return;
        }

        final String urlEnd = mUrl.group(2);
        String waitingPage = "http://www.edisk.cz/stahni-pomalu/"+urlEnd;
        
        fetchPage(waitingPage, new PageFetchListener() {

            public void onCompleted(ByteBuffer buf, Map<String, String> headers) {
                CharBuffer cb = charsetUtf8.decode(buf);
                Matcher m = reWaitTime.matcher(cb);

                Matcher mName = reNameHint.matcher(cb);
                if (mName.find())
                    reportFileName(mName.group(1));

                if (!m.find()) {
                    startWait(60, new WaitListener() {

                        public void onSecondElapsed(int secondsLeft) {
                            if (secondsLeft > 0)
                                setMessage("Limit probably exceeded, "+DownloadPlugin.formatTime(secondsLeft)+" until next attempt");
                            else
                                processLink(link);
                        }
                    });
                } else {
                    int secs = Integer.parseInt(m.group(1));
                    startWait(secs, new WaitListener() {

                        public void onSecondElapsed(int secondsLeft) {
                            if (secondsLeft > 0)
                                setMessage("Waiting: "+DownloadPlugin.formatTime(secondsLeft)+" left");
                            else {
                                String postPage = "http://www.edisk.cz/cz/x-download/"+urlEnd;
                                String postData = "action="+urlEnd;

                                fetchPage(postPage, new PageFetchListener() {

                                    public void onCompleted(ByteBuffer buf, Map<String, String> headers) {
                                        CharBuffer cb = charsetUtf8.decode(buf);
                                        startDownload(cb.toString());
                                    }

                                    public void onFailed(String error) {
                                        setFailed(error);
                                    }
                                }, postData);
                            }
                        }

                    });
                }
            }

            public void onFailed(String error) {
                setFailed(error);
            }

        }, null);
    }

}

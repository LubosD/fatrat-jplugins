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
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringEscapeUtils;

//http://www.youtube.com/watch?v=fyPlhd7BE3w&feature=rec-LGOUT-exp_fresh+div-1r-1-HM
@DownloadPluginInfo(name = "YouTube.com video download", regexp = "https?://(www\\.)?youtube\\.com/watch.+", forceSingleTransfer = false)
@ConfigDialog("youtube.xml")
public class YouTubeDownload extends DownloadPlugin {
    static final Pattern reParamT = Pattern.compile("\"t\": \"([^\"]+)\"");
    static final Pattern reStreamMap = Pattern.compile("\"fmt_stream_map\": \"([^\"]+)\"");
    static final Pattern reVideoID = Pattern.compile("\"video_id\": \"([^\"]+)\"");
    static final Pattern reTitle = Pattern.compile("<meta name=\"title\" content=\"([^\"]+)\">");
    static final Pattern reUnicodeChar = Pattern.compile("\\\\u([0-9A-Fa-f]{4})");
    static final Pattern reHtmlEscapedChar = Pattern.compile("&#(\\d+);");

    static final int[] formatsMP4 = { 38, 37, 22, 18 }; // 18 always
    static final int[] formatsWebM = { 45, 43 };
    static final int[] formatsFLV = { 35, 34, 5 };
    static final String[] defaultPriorities = new String[] { "webm", "mp4", "flv" };

    @Override
    public void processLink(String link) {
        fetchPage(link, new PageFetchListener() {

            public void onCompleted(ByteBuffer buf, Map<String, String> headers) {
                CharBuffer cb = charsetUtf8.decode(buf);
                String t, streamMap, videoID, title = null;
                Map<Integer,String> formatMap;

                try {
                    Matcher m = reParamT.matcher(cb);

                    if (!m.find())
                        throw new Exception("Parse error");
                    t = m.group(1);

                    m = reStreamMap.matcher(cb);
                    if (!m.find())
                        throw new Exception("Parse error");
                    streamMap = m.group(1);

                    m = reVideoID.matcher(cb);
                    if (!m.find())
                        throw new Exception("Parse error");
                    videoID = m.group(1);

                    m = reTitle.matcher(cb);
                    if (!m.find())
                        throw new Exception("Parse error");
                    title = m.group(1);
                    title = StringEscapeUtils.unescapeHtml(title);
                } catch (Exception e) {
                    setFailed(e.getMessage());
                    return;
                }

                formatMap = new HashMap<Integer,String>();
                String[] fmts = streamMap.split(",");
                for (String fmt : fmts) {
                    String[] tokens = fmt.split("\\|");
                    int code = Integer.parseInt(tokens[0]);
                    String url = tokens[1].replaceAll("\\\\/", "/");
                    url = StringEscapeUtils.unescapeJavaScript(url);

                    formatMap.put(code, url);
                }

                if (!formatMap.containsKey(18))
                    formatMap.put(18, "http://www.youtube.com/get_video?fmt=18&video_id="+videoID+"&t="+t+"&asv=3");

                // Get user's priorities
                // Std. priorities: WebM, MP4, FLV
                String[] prios = (String[]) Settings.getValueArray("youtube/formats");
                if (prios == null || prios.length != 3)
                    prios = defaultPriorities;

                int sel = -1;
                String ext = null;
                
                outer:
                for (int i = 0; i < prios.length; i++) {
                    int[] codes = null;

                    ext = prios[i];
                    System.out.println("Analyzing format: "+ext);

                    if (prios[i].equals("webm"))
                        codes = formatsWebM;
                    else if (prios[i].equals("mp4"))
                        codes = formatsMP4;
                    else if (prios[i].equals("flv"))
                        codes = formatsFLV;
                    else
                        continue;

                    for (int code : codes) {
                        if (formatMap.containsKey(code)) {
                            sel = code;
                            break outer;
                        }
                    }
                }

                if (sel == -1) {
                    setFailed("No supported format found?");
                    return;
                }

                startDownload(formatMap.get(sel), null, null, title+"."+ext);
            }

            public void onFailed(String error) {
                setFailed(error);
            }
        }, null);
    }
}

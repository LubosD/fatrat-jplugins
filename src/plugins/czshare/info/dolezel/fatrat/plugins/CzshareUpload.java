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

import info.dolezel.fatrat.plugins.config.Settings;
import info.dolezel.fatrat.plugins.listeners.PageFetchListener;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author lubos
 */
public class CzshareUpload extends UploadPlugin {
    static final Pattern reUploadUrl = Pattern.compile("action=\"(http://www\\d+\\.czshare\\.com/cgi-bin/upload\\.cgi[^\\\"]+)\"");
    static final Pattern reHidden = Pattern.compile("<input type=\"hidden\" name=\"([^\\\"]+)\" value=\"([^\\\"]+)\">");
    static final Pattern reDownload = Pattern.compile("<!-dl->([^<]+)<");
    static final Pattern reKill = Pattern.compile("<!-kl->([^<]+)<");

    @Override
    public void processFile(String filePath) {
        fetchPage("http://czshare.com", new PageFetchListener() {

            public void onCompleted(ByteBuffer buf, Map<String, String> headers) {
                CharBuffer cb = charsetUtf8.decode(buf);
                Matcher m = reUploadUrl.matcher(cb);

                if (!m.find()) {
                    setFailed("Failed to find the upload URL");
                    return;
                }

                List<MimePart> fields = new ArrayList<MimePart>();
                Matcher mHidden = reHidden.matcher(cb);
                while (mHidden.find())
                    fields.add(new MimePartValue(m.group(1), m.group(2)));

                fields.add(new MimePartValue("popis", ""));
                fields.add(new MimePartFile("file[0]"));

                String partnerId = Settings.getValue("czshare/partnerid", "").toString();
                fields.add(new MimePartValue("partner_id", partnerId));

                startUpload(m.group(1), fields);
            }

            public void onFailed(String error) {
                setFailed(error);
            }
        }, null);
    }

    @Override
    public void checkResponse(ByteBuffer uploadResponse, Map<String, String> headers) {
        if (!headers.containsKey("location"))
            setFailed("The upload has failed for an unknown reason");
        else
            fetchPage(headers.get("location"), new PageFetchListener() {

                public void onCompleted(ByteBuffer buf, Map<String, String> headers) {
                    CharBuffer cb = charsetUtf8.decode(buf);
                    Matcher m = reDownload.matcher(cb);

                    if (!m.find()) {
                        setFailed("Failed to get the download link");
                        return;
                    }

                    String downloadUrl = m.group(1), killUrl = null;
                    m = reKill.matcher(cb);

                    if (m.find())
                        killUrl = m.group(1);

                    putDownloadLink(downloadUrl, killUrl);
                }

                public void onFailed(String error) {
                    setFailed(error);
                }
            }, null);
    }

}

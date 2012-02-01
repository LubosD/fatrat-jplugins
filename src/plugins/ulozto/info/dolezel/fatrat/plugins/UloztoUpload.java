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

import info.dolezel.fatrat.plugins.annotations.UploadPluginInfo;
import info.dolezel.fatrat.plugins.listeners.PageFetchListener;
import java.net.URLDecoder;
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
@UploadPluginInfo(name="Uloz.to uploader", sizeLimit = 1000*1000*1000*2)
public class UloztoUpload extends UploadPlugin {

    private static final Pattern reDownloadLink = Pattern.compile("-linkShow\" value=\"([^\"]+)\"");
    private static final Pattern reKillLink = Pattern.compile("-linkDelete\" value=\"([^\"]+)\"");

    @Override
    public void processFile(String filePath) {
            String url = "http://upload.uloz.to/upload?user_id=0&host=uloz.to";
            MimePart[] parts = new MimePart[3];
            parts[0] = new MimePartFile("upfile_0");
            parts[1] = new MimePartValue("no_script", "1");
            parts[2] = new MimePartValue("no_script_submit", "Nahr%C3%A9t+soubory");

            setMessage("Uploading");
            startUpload(url, parts);
    }

    @Override
    public void checkResponse(ByteBuffer uploadResponse, Map<String,String> headers) {
        String location = headers.get("location");
        if (location == null)
            setFailed("The upload has failed for an unknown reason (#1)");
        else {
            fetchPage(urlDecode(location), new PageFetchListener() {

                @Override
                public void onCompleted(ByteBuffer buf, Map<String, String> headers) {
                    String location = headers.get("location");
                    if (location == null)
                        setFailed("The upload has failed for an unknown reason (#2)");
                
                    checkResponse2(urlDecode(location));
                }

                @Override
                public void onFailed(String error) {
                    setFailed(error);
                }
            });
        }
    }
    
    private void checkResponse2(String url) {
        fetchPage(url, new PageFetchListener() {
            @Override
            public void onCompleted(ByteBuffer buf, Map<String, String> headers) {
                CharBuffer cb = charsetUtf8.decode(buf);
                Matcher mDownload = reDownloadLink.matcher(cb);
                Matcher mKill = reKillLink.matcher(cb);
                String kill = null;

                if (!mDownload.find()) {
                    setFailed("Failed to find the download link");
                    return;
                }

                if (mKill.find())
                    kill = mKill.group(1);

                UloztoUpload.this.putDownloadLink(mDownload.group(1), kill);
            }
            
            @Override
            public void onFailed(String error) {
                setFailed(error);
            }
        });
    }
    
    private static String urlDecode(String str) {
        try {
            return URLDecoder.decode(str, "UTF-8");
        } catch (Exception e) {
            return null;
        }
    }

}

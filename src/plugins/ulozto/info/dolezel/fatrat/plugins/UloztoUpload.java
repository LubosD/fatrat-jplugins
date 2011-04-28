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

    static final Pattern reSid = Pattern.compile("\"/ajax/uploader.php?tmp_sid=([^\"]+)\"");
    static final Pattern reFid = Pattern.compile("&fileid=(\\d+)");

    @Override
    public void processFile(String filePath) {
        setMessage("Getting a session ID");
        
        fetchPage("http://www.uloz.to", new PageFetchListener() {

            public void onCompleted(ByteBuffer buf, Map<String, String> headers) {
                CharBuffer cb = charsetUtf8.decode(buf);
                Matcher m = reSid.matcher(cb);

                if (!m.find()) {
                    setFailed("Failed to get a session ID");
                    return;
                }

                String url = "http://up.uloz.to/ul/upload.cgi?tmp_sid="+m.group(1)+"&user_id=0&host=www.uloz.to";
                List<MimePart> parts = new ArrayList<MimePart>(3);
                parts.add(new MimePartFile("upfile_0"));
                parts.add(new MimePartValue("no_script", "1"));
                parts.add(new MimePartValue("no_script_submit", "Nahr%C3%A9t+soubory"));

                setMessage("Uploading");
                startUpload(url, parts);
            }

            public void onFailed(String error) {
                setFailed("Failed to load www.uloz.to");
            }
        }, null);
    }

    @Override
    public void checkResponse(ByteBuffer uploadResponse, Map<String,String> headers) {
        String location = headers.get("location");
        if (location == null)
            setFailed("The upload has failed for an unknown reason (#1)");
        else {
            Matcher m = reFid.matcher(location);
            if (!m.find())
                setFailed("The upload has failed for an unknown reason (#2)");
            else {
                final String url = "http://www.uloz.to/"+m.group(1);

                setMessage("Getting the download URL");
                
                fetchPage(url, new PageFetchListener() {

                    public void onCompleted(ByteBuffer buf, Map<String, String> headers) {
                        String loc = headers.get("location");
                        if (loc == null)
                            loc = url;

                        putDownloadLink(loc, null);
                        setMessage("Done");
                    }

                    public void onFailed(String error) {
                        setFailed("Failed to get the download URL");
                    }
                }, null);
            }
        }
    }

}

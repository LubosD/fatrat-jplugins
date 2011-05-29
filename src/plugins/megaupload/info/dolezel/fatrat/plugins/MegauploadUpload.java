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
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author lubos
 */
@UploadPluginInfo(name="MegaUpload.com uploader", sizeLimit = 1024*1024*1024*2)
public class MegauploadUpload extends UploadPlugin {

    static final Pattern reURL = Pattern.compile("parent\\.downloadurl = '([^']+)';");
    static private Random random = new Random();

    @Override
    public void processFile(final String filePath) {
        setMessage("Locating a server available for upload");
        fetchPage("http://www.megaupload.com/ut/", new PageFetchListener() {

            public void onCompleted(ByteBuffer buf, Map<String, String> headers) {
                CharBuffer cb = charsetUtf8.decode(buf);
                String[] servers = cb.toString().split(",");
                int sid = random.nextInt(servers.length);
                String ulId = longRandom(32);
                File file = new File(filePath);
                long fileSize = file.length();

                String url = "http://www"+servers[sid]+".megaupload.com/upload_done.php"
                        + "?UPLOAD_IDENTIFIER="+ulId+"&s="+fileSize;

                List<MimePart> parts = new ArrayList<MimePart>(2);
                parts.add(new MimePartFile("Filedata"));
                parts.add(new MimePartValue("message", file.getName()));

                setMessage("Uploading");
                startUpload(url, parts.toArray(new MimePart[0]));
            }

            public void onFailed(String error) {
                setFailed("Failed to receive the info page: "+error);
            }
        }, null);
    }

    @Override
    public void checkResponse(ByteBuffer uploadResponse, Map<String,String> headers) {
        CharBuffer cb = charsetUtf8.decode(uploadResponse);
        Matcher m = reURL.matcher(cb);

        if (m.find())
            putDownloadLink(m.group(1), null);
        else
            setFailed("The upload has failed for an unknown reason");
    }

    private static String longRandom(int len) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++)
            sb.append(random.nextInt(10));
        return sb.toString();
    }

}

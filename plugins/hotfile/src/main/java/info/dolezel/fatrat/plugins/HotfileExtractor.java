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

import info.dolezel.fatrat.plugins.annotations.ExtractorPluginInfo;
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
@ExtractorPluginInfo(regexp = "http://hotfile\\.com/list/.+", name = "HotFile.com link list extractor", transferClass = HotfileDownload.class)
public class HotfileExtractor extends ExtractorPlugin {
    static final Pattern reLink = Pattern.compile("<a href=\"(http://hotfile\\.com/dl/[^\"]+)\">");

    public void extractList(String url, ByteBuffer data, Map<String,String> headers) throws Exception {
        List<String> rv = new ArrayList<String>();
        CharBuffer cb = charsetUtf8.decode(data);
        Matcher m = reLink.matcher(cb);

        while (m.find())
            rv.add(m.group(1));

        finishedExtraction((String[]) rv.toArray());
    }

}

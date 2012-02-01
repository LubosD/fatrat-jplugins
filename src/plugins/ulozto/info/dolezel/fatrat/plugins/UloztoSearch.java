/*
FatRat download manager
http://fatrat.dolezel.info

Copyright (C) 2006-2012 Lubos Dolezel <lubos a dolezel.info>

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

import info.dolezel.fatrat.plugins.annotations.SearchPluginInfo;
import info.dolezel.fatrat.plugins.listeners.PageFetchListener;
import info.dolezel.fatrat.plugins.util.FormatUtils;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringEscapeUtils;

/**
 *
 * @author lubos
 */
@SearchPluginInfo(name = "Ulož.to")
public class UloztoSearch extends SearchPlugin {
    private static final Pattern reLinkAndName = Pattern.compile("<a class=\"name\" href=\"([^\"]+)\" title=\"([^\"]+)\"");
    private static final Pattern reSizeAndDuration = Pattern.compile("<span class=\"fileSize\">(.+)");
    private static final Pattern reDuration = Pattern.compile("<span class=\"fileTime\">([^<]+)</span>");

    @Override
    public void search(final String query) {
        try {
            fetchPage("http://www.uloz.to/hledej/?q="+URLEncoder.encode(query, "UTF-8")+"&disclaimer=1", new PageFetchListener() {

                @Override
                public void onCompleted(ByteBuffer buf, Map<String, String> headers) {
                    CharBuffer cb = charsetUtf8.decode(buf);
                    List<SearchResult> results = new ArrayList<SearchResult>();
                    
                    Matcher mSD = reSizeAndDuration.matcher(cb);
                    while (mSD.find()) {
                        SearchResult sr = new SearchResult();
                        Matcher mDuration = reDuration.matcher(mSD.group(1));
                        
                        sr.fileSize = FormatUtils.parseSize(mSD.group(1));
                        
                        if (mDuration.find())
                            sr.extraInfo = "Duration: " + mDuration.group(1);
                        
                        Matcher mName = reLinkAndName.matcher(cb);
                        if (!mName.find(mSD.end()))
                            break;
                        
                        sr.name = StringEscapeUtils.unescapeHtml(mName.group(2));
                        sr.url = "http://www.uloz.to" + StringEscapeUtils.unescapeHtml(mName.group(1));
                        
                        results.add(sr);
                    }
                    
                    searchDone(results);
                }

                @Override
                public void onFailed(String error) {
                    searchFailed();
                }

            });
        } catch (Exception e) {
            searchFailed();
        }
    }
}

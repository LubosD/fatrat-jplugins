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

import info.dolezel.fatrat.plugins.annotations.SearchPluginInfo;
import info.dolezel.fatrat.plugins.listeners.PageFetchListener;
import info.dolezel.fatrat.plugins.util.FormatUtils;
import java.net.URLDecoder;
import java.net.URLEncoder;
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
@SearchPluginInfo(name = "CZshare.com")
public class CzshareSearch extends SearchPlugin {
    static private final Pattern reLink = Pattern.compile("<a href=\"(http://czshare\\.com/\\d+/([^\"]+))\"");

    @Override
    public void search(String query) {
        try {
            String url = "http://czshare.com/search.php?q="+URLEncoder.encode(query, "UTF-8") +"&size-from=&size-to=";
            fetchPage(url, new PageFetchListener() {

                @Override
                public void onCompleted(ByteBuffer buf, Map<String, String> headers) {
                    CharBuffer cb = charsetUtf8.decode(buf);
                    List<SearchResult> srs = new ArrayList<SearchResult>();
                    Matcher mLink = reLink.matcher(cb);
                    Matcher mSize = FormatUtils.getFileSizePattern().matcher(cb);
                    
                    try {
                        while (mLink.find()) {
                            SearchResult sr = new SearchResult();
                            sr.name = URLDecoder.decode(mLink.group(2), "UTF-8");
                            sr.url = mLink.group(1);

                            if (mSize.find(mLink.end()))
                                sr.fileSize = FormatUtils.parseSize(mSize.group());

                            srs.add(sr);
                        }
                        
                        searchDone(srs);
                    } catch (Exception e) {
                        searchFailed();
                    }
                }

                @Override
                public void onFailed(String error) {
                    searchFailed();
                }
            });
            
            
        } catch (Exception ex) {
            searchFailed();
        }
    }
    
}

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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONObject;

/**
 *
 * @author lubos
 */
@SearchPluginInfo(name = "Ulož.to")
public class UloztoSearch extends SearchPlugin {
    private static final Pattern reLinkAndName = Pattern.compile("<a class=\"name\" href=\"([^\"]+)\" title=\"([^\"]+)\"");

    @Override
    public void search(String query) {
        try {
            String url = "http://www.uloz.to/hledej/?q="+URLEncoder.encode(query, "UTF-8")+"&disclaimer=1&do=ajaxSearch";
            
            fetchPage(url, new PageFetchListener() {

                @Override
                public void onCompleted(ByteBuffer buf, Map<String, String> headers) {
                    try {
                        CharBuffer cb = charsetUtf8.decode(buf);
                        JSONObject obj = new JSONObject(cb.toString());
                        JSONObject snippets = obj.getJSONObject("snippets");
                        String searchData = snippets.getString("snippet--mainSearch");
                        
                        Matcher mName = reLinkAndName.matcher(searchData);
                        Matcher mSize = reFileSize.matcher(searchData);
                        List<SearchResult> results = new ArrayList<SearchResult>();
                        
                        while (mName.find()) {
                            SearchResult sr = new SearchResult();
                            sr.name = mName.group(2);
                            sr.url = mName.group(1);
                            
                            if (!mSize.find(mName.end()))
                                continue;
                            
                            sr.fileSize = parseSize(mSize.group());
                            results.add(sr);
                        }
                        
                        SearchResult[] arr = results.toArray(new SearchResult[results.size()]);
                        
                        searchDone(arr);
                        
                    } catch (Exception ex) {
                        searchDone(null);
                    }
                }

                @Override
                public void onFailed(String error) {
                    searchDone(null);
                }
            });
        } catch (UnsupportedEncodingException ex) {
            this.searchDone(null);
        }
    }
    
}

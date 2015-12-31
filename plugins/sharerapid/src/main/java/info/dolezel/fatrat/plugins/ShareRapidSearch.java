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
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author lubos
 */
@SearchPluginInfo(name = "Share-Rapid.com")
public class ShareRapidSearch extends SearchPlugin {

    @Override
    public void search(String query) {
        try {
            String url = "http://share-rapid.com/search.php?co="+URLEncoder.encode(query, "UTF-8") +"&zpusob=jakekolivslovo&typ=";
            this.fetchPage(url, new PageFetchListener() {

                @Override
                public void onCompleted(ByteBuffer buf, Map<String, String> headers) {
                    try {
                        CharBuffer cb = charsetUtf8.decode(buf);
                        JSONObject obj = new JSONObject(cb.toString());
                        JSONArray arr = obj.getJSONArray("results");
                        List<SearchResult> results = new ArrayList<SearchResult>(arr.length());
                        
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject result = arr.getJSONObject(i);
                            SearchResult r = new SearchResult();
                            
                            r.name = result.getString("filename");
                            r.url = result.getString("link");
                            r.fileSize = result.getLong("size");
                            r.extraInfo = "Downloads: "+result.getInt("downloads");
                            
                            results.add(r);
                        }
                        
                        searchDone(results);
                    } catch (Exception ex) {
                        searchFailed();
                    }
                }

                @Override
                public void onFailed(String error) {
                    searchFailed();
                }
            }, null, Collections.singletonMap("User-Agent", "share-rapid downloader"));
        } catch (Exception ex) {
            searchFailed();
        }
    }
    
}

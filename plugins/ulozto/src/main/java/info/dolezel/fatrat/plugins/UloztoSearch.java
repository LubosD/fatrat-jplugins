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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author lubos
 */
@SearchPluginInfo(name = "Ulož.to")
public class UloztoSearch extends SearchPlugin {
    
    private void processSearchResults(String cb, UloztoLinkDecoder dec) {
        List<SearchResult> results = new ArrayList<SearchResult>();
        Document doc = Jsoup.parseBodyFragment(cb);

        Elements lis = doc.getElementsByTag("li");

        for (Element li : lis) {
            SearchResult sr = new SearchResult();

            String encrypted;
            Elements name = li.select(".fileName .name");

            if (name.isEmpty())
                continue;

            sr.name = name.get(0).ownText();
            encrypted = name.get(0).attr("data-icon");

            Elements size = li.select(".fileInfo .fileSize");
            if (!size.isEmpty())
                sr.fileSize = FormatUtils.parseSize(size.get(0).ownText());

            size = li.select(".fileInfo .fileTime");
            if (!size.isEmpty())
                sr.extraInfo = "Duration: " + size.get(0).ownText();

            Elements link = li.select(".fileReset");
            if (link.isEmpty())
                continue;
            
            sr.url = "http://www.uloz.to" + dec.decode(encrypted);
            
            results.add(sr);
        }

        searchDone(results);
    }

    @Override
    public void search(final String query) {
        try {
            fetchPage("http://www.uloz.to/hledej/?q="+URLEncoder.encode(query, "UTF-8")+"&disclaimer=1", new PageFetchListener() {

                @Override
                public void onCompleted(ByteBuffer buf, Map<String, String> headers) {
                    final String cb = charsetUtf8.decode(buf).toString();
                    final UloztoLinkDecoder dec = new UloztoLinkDecoder();
                    
                    dec.autoLoad(UloztoSearch.this, cb, new UloztoLinkDecoder.InitDone() {

                        @Override
                        public void done() {
                            processSearchResults(cb, dec);
                        }

                        @Override
                        public void failed() {
                            searchFailed();
                        }
                    });
                }

                @Override
                public void onFailed(String error) {
                    searchFailed();
                }

            }, null, Collections.singletonMap("X-Requested-With", "XMLHttpRequest"));
        } catch (Exception e) {
            searchFailed();
        }
    }
}

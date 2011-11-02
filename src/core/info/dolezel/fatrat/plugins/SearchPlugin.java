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

import info.dolezel.fatrat.plugins.util.FormatUtils;
import java.util.List;

/**
 * Extend this class to add support for a new file sharing search engine.
 * Do not forget to add a {@link info.dolezel.fatrat.plugins.annotations.SearchPluginInfo} annotation.
 * @author lubos
 */
public abstract class SearchPlugin extends Plugin {
    
    /**
     * Implement this method to receive search queries.
     * @param query Unescaped search query
     */
    public abstract void search(String query);
    
    /**
     * Use this class to return search results to the application.
     */
    public static class SearchResult {
        public String name, url, extraInfo;
        public long fileSize;
    }
    
    /**
     * Call this method when the search has finished with success.
     * @param results 
     */
    protected final native void searchDone(SearchResult[] results);
    
    /**
     * Call this method when the search has finished with success.
     * @param results 
     */
    protected final void searchDone(List<SearchResult> results) {
        SearchResult[] arr = results.toArray(new SearchResult[results.size()]);
        searchDone(arr);
    }
    
    /**
     * Call this method when the search has finished with a failure.
     */
    protected final void searchFailed() {
        searchDone((SearchResult[]) null);
    }
    
    /**
     * @deprecated
     */
    protected static long parseSize(String str) {
        return FormatUtils.parseSize(str);
    }
}

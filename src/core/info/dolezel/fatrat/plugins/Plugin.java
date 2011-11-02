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

import info.dolezel.fatrat.plugins.listeners.PageFetchListener;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * Do not subclass this class directly.
 * @author lubos
 */
public abstract class Plugin extends NativeObject {
    
    /**
     * Useful for decoding ByteArrays from {@link #fetchPage}
     */
    final Charset charsetUtf8 = Charset.forName("UTF-8");
    
    protected Plugin() {
    }

    /**
     * Downloads the specified URL.
     * @param url The URL to be downloaded.
     * @param cb A callback object where you'll receive the data
     */
	protected final void fetchPage(String url, PageFetchListener cb) {
        fetchPage(url, cb, null, null);
    }
    
    /**
     * Downloads the specified URL.
     * @param url The URL to be downloaded.
     * @param cb A callback object where you'll receive the data
     * @param postData Optional data to be sent using the POST method
     */
    protected final void fetchPage(String url, PageFetchListener cb, String postData) {
        fetchPage(url, cb, postData, null);
    }
    
    protected final native void fetchPage(String url, PageFetchListener cb, String postData, Map<String,String> headers);
}

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


package info.dolezel.fatrat.plugins.listeners;

import java.nio.ByteBuffer;
import java.util.EventListener;
import java.util.Map;

/**
 * Implement this interface to receive data from a URL.
 * @author lubos
 */
public interface PageFetchListener extends EventListener {
    /**
     * Called after the request page has been fetched.
     * @param buf Buffer contains the response body
     * @param headers Contains HTTP headers [header name, value] with lower-case header names
     */
    void onCompleted(ByteBuffer buf, Map<String,String> headers);

    /**
     * Called if FatRat failed to fetch the requested URL.
     * @param error Error description
     */
    void onFailed(String error);
}

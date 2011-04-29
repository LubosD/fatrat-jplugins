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

import java.nio.ByteBuffer;
import java.util.Map;

/**
 *
 * @author lubos
 */
public abstract class ExtractorPlugin extends TransferPlugin {
    /**
     * Called by the application to extract all the links in the page.
     * @param url Original page URL
     * @param data Page contents
     * @return A list of extracted links
     * @throws Exception
     */
    public abstract void extractList(String url, ByteBuffer data, Map<String,String> headers) throws Exception;

    public native void finishedExtraction(String[] links);
}

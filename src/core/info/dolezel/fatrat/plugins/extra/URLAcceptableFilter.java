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

package info.dolezel.fatrat.plugins.extra;

/**
 * Only implement this interface if you know you really need to do so.
 * It is solely to be implemented by DownloadPlugins.
 *
 * Normally it should suffice to specify your regexp in DownloadPluginInfo.
 * The default native implementation will return 3 if the URL is matched, 0 otherwise.
 */
public interface URLAcceptableFilter {
    /**
     * Return 0 if the plugin cannot handle this URL.
     * Return 1 if you can handle the URL, but it's not desirable.
     * Return 2 if you can handle the URL, but you shouldn't be given priority.
     * Return 3 if you can handle the URL and are designed specifically for these URLs.
     * Return 4+ if you need to override something.
     * @param url The URL to check.
     */
    public int acceptable(String url);
}

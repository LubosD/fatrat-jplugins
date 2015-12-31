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

package info.dolezel.fatrat.plugins.extra;

/**
 * A structure that holds all the information needed in order to initiate a download.
 * Only to be used by {@link info.dolezel.fatrat.plugins.DownloadPlugin} subclasses.
 * @author lubos
 */
public class DownloadUrl {
    String url, referrer, userAgent, fileName, postData;
    
    /**
     * @param url The URL FatRat should start downloading from.
     */
    public DownloadUrl(String url) {
        this.url = url;
    }

    public String getFileName() {
        return fileName;
    }

    /**
     * Sets the file name to store the file under.
     * Use only if FatRat is unable to detect the correct file name.
     * @param fileName A file name
     * @return This object
     */
    public DownloadUrl setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    public String getPostData() {
        return postData;
    }

    /**
     * Sets the POST data if the transfer has to be initiated by a POST query.
     * @param postData URL-encoded data
     * @return This object
     */
    public DownloadUrl setPostData(String postData) {
        this.postData = postData;
        return this;
    }

    public String getReferrer() {
        return referrer;
    }

    /**
     * Sets the "HTTP Referer" if it is being checked by the server.
     * @param referrer An URL
     * @return This object
     */
    public DownloadUrl setReferrer(String referrer) {
        this.referrer = referrer;
        return this;
    }

    public String getUrl() {
        return url;
    }

    /**
     * Sets the URL to download from.
     * @param url An URL.
     * @return This object
     */
    public DownloadUrl setUrl(String url) {
        this.url = url;
        return this;
    }

    public String getUserAgent() {
        return userAgent;
    }

    /**
     * Sets the user agent to use if FatRat's default will not work.
     * @param userAgent The user agent string
     * @return This object
     */
    public DownloadUrl setUserAgent(String userAgent) {
        this.userAgent = userAgent;
        return this;
    }
}

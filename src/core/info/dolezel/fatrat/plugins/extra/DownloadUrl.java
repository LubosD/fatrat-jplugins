/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package info.dolezel.fatrat.plugins.extra;

/**
 * A structure that holds all the information needed in order to initiate a download.
 * @author lubos
 */
public class DownloadUrl {
    String url, referrer, userAgent, fileName, postData;
    
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

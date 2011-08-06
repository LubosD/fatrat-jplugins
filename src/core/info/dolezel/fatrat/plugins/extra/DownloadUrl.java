/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package info.dolezel.fatrat.plugins.extra;

/**
 *
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

    public DownloadUrl setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    public String getPostData() {
        return postData;
    }

    public DownloadUrl setPostData(String postData) {
        this.postData = postData;
        return this;
    }

    public String getReferrer() {
        return referrer;
    }

    public DownloadUrl setReferrer(String referrer) {
        this.referrer = referrer;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public DownloadUrl setUrl(String url) {
        this.url = url;
        return this;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public DownloadUrl setUserAgent(String userAgent) {
        this.userAgent = userAgent;
        return this;
    }
}

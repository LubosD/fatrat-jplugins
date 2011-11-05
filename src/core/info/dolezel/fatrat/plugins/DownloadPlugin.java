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

import info.dolezel.fatrat.plugins.extra.DownloadUrl;
import info.dolezel.fatrat.plugins.listeners.CaptchaListener;
import info.dolezel.fatrat.plugins.listeners.PageFetchListener;
import info.dolezel.fatrat.plugins.listeners.ReCaptchaListener;
import info.dolezel.fatrat.plugins.listeners.WaitListener;
import info.dolezel.fatrat.plugins.util.ByteBufferInputStream;
import info.dolezel.fatrat.plugins.util.FormatUtils;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;

/**
 * Extend this class to create a new download plugin.
 * Do not forget to add a {@link info.dolezel.fatrat.plugins.annotations.DownloadPluginInfo} annotation.
 * @author lubos
 */
public abstract class DownloadPlugin extends TransferPlugin {
    
    private static final Pattern reImageCode = Pattern.compile("challenge : '([^']+)");

    /**
     * This method will be called when a download is to be started.
     * This is the main method to be implemented in download extensions.
     */
    public abstract void processLink(String link);

    /**
     * Called from native code to notify the plugin that the download has failed either
     * because of {@link #setState} or {@link #setFailed} called by the extension or because of an HTTP
     * failure encountered in the native code.
     *
     * Override this method only if you need it as a captcha caching hint.
     */
    public void onFailed() {}
    
    /**
     * Gives FatRat the URL to download the desired file.
     * This is the last step in the whole procedure. Cookies from {@link #fetchPage} calls are automatically preserved.
     * @param url URL to download, along with all other information needed.
     */
    protected final native void startDownload(DownloadUrl url);

    /**
     * Gives FatRat the URL to download the desired file.
     * This is the last step in the whole procedure. Cookies from {@link #fetchPage} calls are automatically preserved.
     * @param url URL to download.
     * @param referrer Optional HTTP Referer URL
     */
	protected final void startDownload(String url, String referrer, String userAgent, String fileName) {
        startDownload(new DownloadUrl(url).setReferrer(referrer).setUserAgent(userAgent).setFileName(fileName));
    }

    /**
     * Gives FatRat the URL to download the desired file.
     * This is the last step in the whole procedure.
     * @param url URL to download, all received cookies will be used automatically
     */
    protected final void startDownload(String url) {
        startDownload(new DownloadUrl(url));
    }
    protected final void startDownload(String url, String referrer) {
        startDownload(new DownloadUrl(url).setReferrer(referrer));
    }
    protected final void startDownload(String url, String referrer, String userAgent) {
        startDownload(new DownloadUrl(url).setReferrer(referrer).setUserAgent(userAgent));
    }

    /**
     * Start a timer. FatRat will call back every second until the timer expires.
     * @param seconds The length of the timer in seconds.
     * @param cb Callback listener.
     */
	protected final native void startWait(int seconds, WaitListener cb);

    /**
     * Ask FatRat to solve a captcha image.
     * See the description of {@link #solveCaptchaLoadLocally} before using this method.
     * @param url URL of the image
     * @param cb Callback with the result
     */
    protected final native void solveCaptcha(String url, CaptchaListener cb);
    
    /**
     * If the captcha image URL depends on the IP address, cookies or other circumstances
     * that won't exist if that URL is loaded in a different environment or location,
     * use this method instead of {@link #solveCaptcha}.
     * @param url URL of the image
     * @param cb Callback with the result
     */
    protected void solveCaptchaLoadLocally(String url, final CaptchaListener cb) {
        fetchPage(url, new PageFetchListener() {

            @Override
            public void onCompleted(ByteBuffer buf, Map<String, String> headers) {
                String type;
                if (headers.containsKey("content-type"))
                    type = headers.get("content-type");
                else
                    type = "";

                byte[] b = new byte[buf.remaining()];
                buf.get(b);

                String encodedImage = "data:"+type+";base64," + Base64.encodeBase64String(b);
                solveCaptcha(encodedImage, cb);
            }

            @Override
            public void onFailed(String error) {
                cb.onFailed();
            }
        });
    }
    
    protected void solveReCaptcha(String code, final ReCaptchaListener cl) {
        fetchPage("http://www.google.com/recaptcha/api/challenge?k="+code+"&ajax=1", new PageFetchListener() {

            @Override
            public void onCompleted(ByteBuffer buf, Map<String, String> headers) {
                CharBuffer cb = charsetUtf8.decode(buf);
                final Matcher m = reImageCode.matcher(cb);

                if (!m.find()) {
                    setFailed("Failed to find the captcha image code");
                    return;
                }

                solveCaptcha("http://www.google.com/recaptcha/api/image?c="+m.group(1), new CaptchaListener() {

                    @Override
                    public void onFailed() {
                        cl.onFailed();
                    }

                    @Override
                    public void onSolved(String text) {
                        cl.onSolved(text, m.group(1));
                    }
                });
            }

            @Override
            public void onFailed(String error) {
                setFailed(error);
            }
            
        });
    }

    /**
     * Give FatRat a hint on the real file name, if it cannot be properly deduced from the URL.
     * This name is used only for display purposes, the on-disk file name is never based on this.
     * The name given is used only until {@link #startDownload} is called.
     * @param name A file name
     */
    protected final native void reportFileName(String name);

    /**
     * Reimplement if you need to check e.g. for HTML files containing 'File not found' after you call {@link #startDownload}
     * @param filePath The path where the file has been downloaded
     */
	public void finalCheck(String filePath) {
	}

    /**
     * Formats the time in seconds into a user friendly string.
     * @deprecated Replaced by {@link FormatUtils.formatTime}.
     */
    public static String formatTime(int seconds) {
        return FormatUtils.formatTime(seconds);
    }
}


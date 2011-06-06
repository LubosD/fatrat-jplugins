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

import info.dolezel.fatrat.plugins.listeners.CaptchaListener;
import info.dolezel.fatrat.plugins.listeners.PageFetchListener;
import info.dolezel.fatrat.plugins.listeners.ReCaptchaListener;
import info.dolezel.fatrat.plugins.listeners.WaitListener;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extend this class to create a new download plugin.
 * @author lubos
 */
public abstract class DownloadPlugin extends TransferPlugin {
    
    private static final Pattern reImageCode = Pattern.compile("challenge : '([^']+)");

    /**
     * This method will be called when a download is to be started.
     */
    public abstract void processLink(String link);

    /**
     * Called from native code to notify the plugin that the download has failed either
     * because of setState/setFailed called by the extension or because of an HTTP
     * failure encountered in the native code.
     *
     * Override this method only if you need it as a captcha caching hint.
     */
    public void onFailed() {}

    /**
     * Gives FatRat the URL to download the desired file.
     * This is the last step in the whole procedure.
     * @param url URL to download, all received cookies will be used automatically
     * @param referrer Optional HTTP Referer URL
     */
	protected native void startDownload(String url, String referrer, String userAgent, String fileName);

    /**
     * Gives FatRat the URL to download the desired file.
     * This is the last step in the whole procedure.
     * @param url URL to download, all received cookies will be used automatically
     */
    protected void startDownload(String url) {
        startDownload(url, null, null, null);
    }
    protected void startDownload(String url, String referrer) {
        startDownload(url, referrer, null, null);
    }
    protected void startDownload(String url, String referrer, String userAgent) {
        startDownload(url, referrer, userAgent, null);
    }

    /**
     * FatRat will call back every second until the timer expires
     * @param seconds Wait period
     * @param cb Callback
     */
	protected native void startWait(int seconds, WaitListener cb);

    /**
     * Ask FatRat to solve a captcha image
     * @param url URL of the image
     * @param cb Callback with the result
     */
    protected native void solveCaptcha(String url, CaptchaListener cb);
    
    protected void solveReCaptcha(String code, final ReCaptchaListener cl) {
        fetchPage("http://www.google.com/recaptcha/api/challenge?k="+code+"&ajax=1", new PageFetchListener() {

            public void onCompleted(ByteBuffer buf, Map<String, String> headers) {
                CharBuffer cb = charsetUtf8.decode(buf);
                final Matcher m = reImageCode.matcher(cb);

                if (!m.find()) {
                    setFailed("Failed to find the captcha image code");
                    return;
                }

                solveCaptcha("http://www.google.com/recaptcha/api/image?c="+m.group(1), new CaptchaListener() {

                    public void onFailed() {
                        cl.onFailed();
                    }

                    public void onSolved(String text) {
                        cl.onSolved(text, m.group(1));
                    }
                });
            }

            public void onFailed(String error) {
                setFailed(error);
            }
            
        });
    }

    /**
     * Give FatRat a hint on the real file name, if it cannot be properly deduced from the URL
     * @param name A file name
     */
    protected native void reportFileName(String name);

    /**
     * Reimplement if you need to check e.g. for HTML files containing 'File not found' after you call startDownload()
     * @param filePath The path where the file has been downloaded
     */
	public void finalCheck(String filePath) {
	}

    /**
     * Formats the time in seconds into a user friendly String.
     */
    public static String formatTime(int seconds) {
        StringBuilder result = new StringBuilder();
        int days,hrs,mins,secs;
        days = seconds/(60*60*24);
        seconds %= 60*60*24;

        hrs = seconds/(60*60);
        seconds %= 60*60;

        mins = seconds/60;
        secs = seconds%60;

        if (days > 0)
            result.append(days).append("d ");
        if (hrs > 0)
            result.append(hrs).append("h ");
        if (mins > 0)
            result.append(mins).append("m ");
        if (secs > 0)
            result.append(secs).append("s ");

        return result.toString();
    }
}


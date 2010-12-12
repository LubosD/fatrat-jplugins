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

import info.dolezel.fatrat.plugins.listeners.CaptchaListener;
import info.dolezel.fatrat.plugins.listeners.WaitListener;

public abstract class DownloadPlugin extends TransferPlugin {
    public abstract void processLink(String link);

    /**
     * Gives FatRat the URL to download the desired file.
     * This is the last step in the whole procedure.
     * @param url
     */
	protected native void startDownload(String url);

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


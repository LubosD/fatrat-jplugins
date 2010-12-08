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
import info.dolezel.fatrat.plugins.listeners.PageFetchListener;
import info.dolezel.fatrat.plugins.listeners.WaitListener;
import java.nio.charset.Charset;

public abstract class DownloadPlugin {
    final Charset charsetUtf8 = Charset.forName("UTF-8");

	public enum State {
		Waiting(0),
		Active(1),
		ForcedActive(2),
		Paused(3),
		Failed(4),
		Completed(5);

        private final int v;
        State(int v) {
            this.v = v;
        }
        public int value() {
            return v;
        }
	};

    public abstract void processLink(String link);

	protected native void setMessage(String msg);
	protected native void setState(State state);
	protected native void fetchPage(String url, PageFetchListener cb, String postData);
	protected native void startDownload(String url);
	protected native void startWait(int seconds, WaitListener cb);
	protected native void logMessage(String msg);
    protected native void solveCaptcha(String url, CaptchaListener cb);

	public void finalCheck(String filePath) {
	}

	public boolean truncIncomplete() {
		return true;
	}
    
    public boolean forceSingleTransfer() {
        return false;
    }

    public void setFailed(String error) {
        setMessage(error);
        setState(State.Failed);
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


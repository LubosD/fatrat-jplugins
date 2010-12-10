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

import info.dolezel.fatrat.plugins.listeners.PageFetchListener;
import java.nio.charset.Charset;

/**
 *
 * @author lubos
 */
public class TransferPlugin {
    final Charset charsetUtf8 = Charset.forName("UTF-8");

    /**
     * You should probably only use the Failed state.
     * Other states are to be set by the user or by the queue manager.
     */
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

    /**
     * Downloads the specified URL.
     * @param url The URL to be downloaded.
     * @param cb A callback object where you'll receive the data
     * @param postData Optional data to be sent using the POST method
     */
	protected native void fetchPage(String url, PageFetchListener cb, String postData);

    /**
     * Sets the message seen in the transfer list
     */
    protected native void setMessage(String msg);

    /**
     * Sets the transfer state
     */
	protected native void setState(State state);

    /**
     * Puts a message into the transfer log
     */
    protected native void logMessage(String msg);

    /**
     * An equivalent of setMessage(error), setState(State.Failed)
     */
    protected void setFailed(String error) {
        setMessage(error);
        setState(State.Failed);
    }
}

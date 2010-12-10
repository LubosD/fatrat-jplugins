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

import java.nio.charset.Charset;

/**
 *
 * @author lubos
 */
public abstract class TransferPlugin extends Plugin {
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

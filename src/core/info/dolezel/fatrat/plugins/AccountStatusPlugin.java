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

/**
 * Premium account balance display plugin.
 * @author lubos
 */
public abstract class AccountStatusPlugin extends Plugin {
    static public enum AccountState {
        AccountOK,
        AccountWarn,
        AccountBad
    }
    
    /**
     * Called by FatRat to retrieve account information.
     * @return false to report that no premium account credentials are available.
     * The app won't wait for a reportAccountBalance call in such case.
     */
    public abstract boolean queryAccountBalance();
    
    /**
     * Reports account balance to the user.
     * @param state Used for a graphical icon.
     * @param balance A string that will be displayed to the user.
     */
    protected native void reportAccountBalance(AccountState state, String balance);
}

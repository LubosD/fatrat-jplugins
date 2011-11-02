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

import info.dolezel.fatrat.plugins.util.FormatUtils;

/**
 * Premium account balance display plugin.
 * Do not forget to add a {@link info.dolezel.fatrat.plugins.annotations.AccountStatusPluginInfo} annotation.
 * @author lubos
 */
public abstract class AccountStatusPlugin extends Plugin {
    
    /**
     * Account state. It is up to the plugin to judge the state, there are no rules set.
     */
    static public enum AccountState {
        AccountGood,
        AccountWarning,
        AccountBad,
        AccountError
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
     * @param balance A string that will be displayed to the user. Can be <code>null</code> if state is AccountError.
     */
    protected native final void reportAccountBalance(AccountState state, String balance);
    
    /**
     * @deprecated
     */
    protected static long parseSize(String str) {
        return FormatUtils.parseSize(str);
    }
    
    /**
     * This method is used to make assess the number of bytes left on the account.
     * @param bytesLeft Number of bytes
     * @return An account state enum value
     */
    protected static AccountState adviseState(long bytesLeft) {
        if (bytesLeft < 0)
            return AccountState.AccountGood;
        else if (bytesLeft < 512l*1024l*1024l)
            return AccountState.AccountBad;
        else if (bytesLeft < 2l*1024l*1024l*1024l)
            return AccountState.AccountWarning;
        else
            return AccountState.AccountGood;
    }
    
    /**
     * Call if you failed to retrieve the account state.
     * @param error 
     */
    protected final void setFailed(String error) {
        reportAccountBalance(AccountState.AccountBad, error);
    }
}

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
    protected native void reportAccountBalance(AccountState state, String balance);
    
    protected static long parseSize(String str) {
        String[] p = str.trim().replaceAll(" {2,}", " ").split(" ");
        double d;
        long mul = 0;
        
        if (p.length != 2)
            return 0;
        
        try {
            d = Double.parseDouble(p[0].replace(',', '.'));
        } catch (Exception e) {
            return 0;
        }
        
        p[1] = p[1].toLowerCase();
        
        if (p[1].equals("b"))
            mul = 1;
        else if (p[1].equals("kb"))
            mul = 1024;
        else if (p[1].equals("mb"))
            mul = 1024*1024;
        else if (p[1].equals("gb"))
            mul = 1024*1024*1024;
        else if (p[1].equals("tb"))
            mul = 1024L*1024L*1024L*1024L;
        else if (p[1].equals("pb"))
            mul = 1024L*1024L*1024L*1024L*1024L;
        
        return (long) (d*mul);
    }
    
    protected static AccountState adviseState(long bytesLeft) {
        if (bytesLeft < 512L*1024L*1024L)
            return AccountState.AccountBad;
        else if (bytesLeft < 2L*1024L*1024L*1024L)
            return AccountState.AccountWarning;
        else
            return AccountState.AccountGood;
    }
    
    protected void setFailed(String error) {
        reportAccountBalance(AccountState.AccountBad, error);
    }
}

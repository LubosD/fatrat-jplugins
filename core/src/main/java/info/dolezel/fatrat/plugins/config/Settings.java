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

package info.dolezel.fatrat.plugins.config;

/**
 * Access FatRat's settings file.
 * Typically this is used to retrieve settings saved through a config dialog specified by {@link info.dolezel.fatrat.plugins.annotations.ConfigDialog}.
 * @author lubos
 */
public final class Settings {
    private Settings() {
    }
    
    /**
     * Sets a global persistent config value.
     * @param name The key
     * @param value The value
     */
    public static native void setValue(String name, String value);
    
    /**
     * Sets a global persistent config value.
     * @param name The key
     * @param value The value
     */
    public static native void setValue(String name, long value);
    
    /**
     * Sets a global persistent config value.
     * @param name The key
     * @param value The value
     */
    public static native void setValue(String name, boolean value);
    
    /**
     * Sets a global persistent config value.
     * @param name The key
     * @param value The value
     */
    public static native void setValue(String name, double value);

    /**
     * Gets a global persistent value.
     * @param name The key
     * @param defValue The value to return in the key is non-existent.
     */
    public static native Object getValue(String name, Object defValue);
    
    public static Integer getValueInt(String name, Integer defValue) {
        Object o = getValue(name, defValue);
        
        if (o instanceof Number)
            return ((Number) o).intValue();
        else if (o instanceof String) {
            try {
                return Integer.parseInt((String) o);
            } catch (Exception e) {
                return null;
            }
        } else
            return null;
    }
    
    /**
     * Gets an array of values under the key.
     * @param name The key
     * @return An array of values or <code>null</code>.
     */
    public static native Object[] getValueArray(String name);
    
    public static int[] getValueArrayInt(String name) {
        Object[] o = getValueArray(name);
        
        if (o == null)
            return null;
        
        int[] rv = new int[o.length];
        
        for (int i = 0; i < rv.length; i++) {
            if (o[i] instanceof String) {
                try {
                    rv[i] = Integer.parseInt((String) o[i]);
                } catch (Exception e) {
                    return null;
                }
            } else if (o[i] instanceof Number) {
                rv[i] = ((Number) o[i]).intValue();
            } else
                return null;
        }
        
        return rv;
    }
}

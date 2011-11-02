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
    
    /**
     * Gets an array of values under the key.
     * @param name The key
     * @return An array of values or <code>null</code>.
     */
    public static native Object[] getValueArray(String name);
}

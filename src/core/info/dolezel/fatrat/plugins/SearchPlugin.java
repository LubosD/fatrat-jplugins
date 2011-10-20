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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author lubos
 */
public abstract class SearchPlugin extends Plugin {
    protected static final Pattern reFileSize = Pattern.compile("([\\d\\.,]+)\\s*(b|ki?b|mi?b|gi?b|ti?b|pi?b)", Pattern.CASE_INSENSITIVE);
    
    public abstract void search(String query);
    
    public static class SearchResult {
        public String name, url;
        public long fileSize;
    }
    
    protected native void searchDone(SearchResult[] results);
    
    protected static long parseSize(String str) {
        Matcher m = reFileSize.matcher(str);
        if (!m.find())
            return -1;
        
        String number = m.group(1).replace(',', '.');
        String unit = m.group(2).toLowerCase().replace("i", "");
        
        long result = Long.parseLong(number);
        if (unit.equals("b"))
            ;
        else if (unit.equals("kb"))
            result *= 1024;
        else if (unit.equals("mb"))
            result *= 1024l*1024l;
        else if (unit.equals("gb"))
            result *= 1024l*1024l*1024l;
        else if (unit.equals("tb"))
            result *= 1024l*1024l*1024l*1024l;
        else if (unit.equals("pb"))
            result *= 1024l*1024l*1024l*1024l*1024l;
        else
            result = -1;
        
        return result;
    }
}

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
package info.dolezel.fatrat.plugins.util;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author lubos
 */
public final class FileUtils {
    
    private FileUtils() {
    }

    /**
      * Returns the first line from the file.
      * @return <code>null</code> if the operation failed for any reason.
      */
    public static String fileReadLine(String file) {
        FileInputStream fis = null;
        BufferedReader br = null;
        DataInputStream dis = null;
        InputStreamReader isr = null;
        
        try {
            fis = new FileInputStream(file);
            dis = new DataInputStream(fis);
            isr = new InputStreamReader(dis);
            br = new BufferedReader(isr);
            
            return br.readLine();
        } catch (IOException ex) {
            return null;
        } finally {
            IOUtils.closeQuietly(fis);
        }
    }
    
    /**
     * Reads the whole file and returns it as a String.
     * The UTF-8 encoding is assumed.
     * @param file The file path.
     * @return The file contents or <code>null</code> if the operation failed.
     */
    public static String fileReadAll(String file) {
        FileInputStream fis = null;
        
        try {
            fis = new FileInputStream(file);
            
            return IOUtils.toString(fis, "UTF-8");
        } catch (IOException ex) {
            return null;
        } finally {
            IOUtils.closeQuietly(fis);
        }
    }
}

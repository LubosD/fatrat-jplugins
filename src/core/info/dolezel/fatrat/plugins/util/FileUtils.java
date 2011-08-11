/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package info.dolezel.fatrat.plugins.util;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 *
 * @author lubos
 */
public class FileUtils {

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
            try {
                fis.close();
            } catch (IOException ex) {
            }
        }
    }
    
    public static String fileReadAll(String file) {
        FileInputStream fis = null;
        DataInputStream dis = null;
        InputStreamReader isr = null;
        
        try {
            fis = new FileInputStream(file);
            dis = new DataInputStream(fis);
            isr = new InputStreamReader(dis);
            
            StringBuilder sb = new StringBuilder();
            char[] c = new char[4096];
            
            while (true) {
                int r = isr.read(c);
                if (r < 0)
                    return sb.toString();
                sb.append(c, 0, r);
            }
            
        } catch (IOException ex) {
            return null;
        } finally {
            try {
                fis.close();
            } catch (IOException ex) {
            }
        }
    }
}

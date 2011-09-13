/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package info.dolezel.fatrat.applet.util;

import java.text.DecimalFormat;

/**
 *
 * @author lubos
 */
public class Util {
    public static String formatSize(long bytes) {
        if (bytes < 1024)
            return bytes + " B";
        else if (bytes < 1024*1024)
            return singleDecimalDigit(bytes/1024.0) + " KB";
        else if (bytes < 1024*1024*1024)
            return singleDecimalDigit(bytes/1024.0/1024.0) + " MB";
        else
            return singleDecimalDigit(bytes/1024.0/1024.0/1024.0) + " GB";
    }
    
    public static String singleDecimalDigit(double d) {
        return new DecimalFormat("#.#").format(d);
    }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package info.dolezel.fatrat.plugins.util;

import java.text.DecimalFormat;

/**
 *
 * @author lubos
 */
public class FormatUtils {
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
    
    public static String formatTime(int seconds) {
        StringBuilder result = new StringBuilder();
        int days,hrs,mins,secs;
        days = seconds/(60*60*24);
        seconds %= 60*60*24;

        hrs = seconds/(60*60);
        seconds %= 60*60;

        mins = seconds/60;
        secs = seconds%60;

        if (days > 0)
            result.append(days).append("d ");
        if (hrs > 0)
            result.append(hrs).append("h ");
        if (mins > 0)
            result.append(mins).append("m ");
        if (secs > 0)
            result.append(secs).append("s ");

        return result.toString();
    }
}

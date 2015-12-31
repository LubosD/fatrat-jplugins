/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package info.dolezel.fatrat.applet.components;

import info.dolezel.fatrat.applet.util.Util;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.util.Collections;

/**
 *
 * @author lubos
 */
public class SpeedGraph extends Component {

    public static class DataSample {
        public int down, up;
    }
    
    static final Color colorDownLine = new Color(0,0,128);
    static final Color colorDownFill = new Color(0,0,128,64);
    static final Color colorUpLine = new Color(128,0,0);
    static final Color colorUpFill = new Color(128,0,0,64);
    static final Color colorSpeedLine = new Color(128,128,128,128);
    
    static final Stroke ordinaryStroke = new BasicStroke(1);
    static final Stroke dashedStroke = new BasicStroke(1,  BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0);
    
    DataSample[] data;
    int graphMinutes = 5;

    public SpeedGraph() {
    }

    @Override
    public void paint(Graphics g) {
        int width = this.getWidth();
        int height = this.getHeight();
        Graphics2D g2d = (Graphics2D) g;
        
        g2d.addRenderingHints(Collections.singletonMap(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON));
        
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);
        
        if (data == null || data.length == 0 || isEmpty(data)) {
            drawNoData(g, width, height);  
            return;
        }
        
        int max = findMax();
        int seconds = graphMinutes*60;
        double perpt = (width / (Math.max((double) data.length, seconds)-1));
        
        max = (int) Math.round(Math.max(max/10.0*11.0, 10.0*1024));
        
        double pos = width;
        int elems = data.length;

        if (elems > seconds) {
             // update seconds value?
            return;
        }

        // download
        int[] polyX, polyY;
        
        polyX = new int[elems+2];
        polyY = new int[elems+2];

        for (int i = 0; i < elems; i++) {
            int y = (int) Math.round(height - height/((double)max)*data[elems - i - 1].down);
            polyX[i] = (int) Math.round(pos);
            polyY[i] = y;
            
            pos -= perpt;
        }
        
        polyX[elems] = (int) Math.round(pos+perpt);
        polyY[elems] = height;
        
        polyX[elems+1] = width;
        polyY[elems+1] = height;

        g.setColor(colorDownLine);
        g.drawPolyline(polyX, polyY, elems);
        g.setColor(colorDownFill);
        g.fillPolygon(polyX, polyY, elems+2);

        // upload
        pos = width;

        for (int i = 0; i < elems; i++) {
            int y = (int) Math.round(height - height/((double)max)*data[elems - i - 1].up);
            polyX[i] = (int) Math.round(pos);
            polyY[i] = y;
            pos -= perpt;
        }

        polyX[elems] = (int) Math.round(pos+perpt);
        polyY[elems] = height;
        
        polyX[elems+1] = width;
        polyY[elems+1] = height;
        
        g.setColor(colorUpLine);
        g.drawPolyline(polyX, polyY, elems);
        g.setColor(colorUpFill);
        g.fillPolygon(polyX, polyY, elems+2);

        g.setColor(colorDownLine);
        g.drawLine(2, 7, 12, 7);
        
        g.setColor(colorUpLine);
        g.drawLine(2, 19, 12, 19);

        g.setColor(Color.BLACK);
        g.setFont(g.getFont().deriveFont(10.0f));
        g.drawString("Download", 15, 12);
        g.drawString("Upload", 15, 24);
        
        for (int i = 0; i < 4; i++) {
            int x = width - (i+1)*(width/4);
            g.drawLine(x, height, x, height-15);

            int mins = (int) Math.round((seconds/4) * (i+1) / 60.0);
            g.drawString(""+mins+" mins ago", x+2, height-2);
        }

        g2d.setStroke(dashedStroke);
        g.setColor(colorSpeedLine);
        
        for (int i = 1; i < 10; i++) {
            int mpos = (int) Math.round(height / 10.0 * i);
            g.drawLine(0, mpos, width, mpos);
            
            int speed = (int) Math.round(max/10.0*(10-i));
            g.drawString(Util.formatSize(speed)+"/s", 0, mpos-10);
        }
        
    }
    
    private int findMax() {
        int max = 0;
        for (int i = 0; i < data.length; i++) {
            max = Math.max(max, data[i].down);
            max = Math.max(max, data[i].up);
        }
        return max;
    }

    private void drawNoData(Graphics g, int width, int height) {
        g.setFont(g.getFont().deriveFont(20.0f));
        g.setColor(Color.GRAY);
        
        String s = "NO DATA";
        FontMetrics fm   = g.getFontMetrics(g.getFont());
        java.awt.geom.Rectangle2D rect = fm.getStringBounds(s, g);

        int textHeight = (int)(rect.getHeight()); 
        int textWidth  = (int)(rect.getWidth());

        // Center text horizontally and vertically
        int x = (width  - textWidth)  / 2;
        int y = (height - textHeight) / 2  + fm.getAscent();

        g.drawString(s, x, y);  // Draw the string.
    }

    public DataSample[] getData() {
        return data;
    }

    public void setData(DataSample[] data) {
        this.data = data;
        this.repaint();
    }
    
    private boolean isEmpty(DataSample[] data) {
        for (int i = 0; i < data.length; i++) {
            if (data[i] != null && (data[i].down > 0 || data[i].up > 0))
                return false;
        }
        return true;
    }

    public void setGraphMinutes(int graphMinutes) {
        this.graphMinutes = graphMinutes;
    }
    
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package info.dolezel.fatrat.applet.models.renderers;

import java.awt.Component;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author lubos
 */
public class ProgressRenderer extends JProgressBar implements TableCellRenderer {

    public ProgressRenderer() {
        this.setStringPainted(true);
        this.setMaximum(100);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        float fl = (Float) value;
        
        if (fl == 0.0f)
            this.setString("?");
        else
            this.setString(String.format("%.01f%%", fl));
        this.setValue((int) fl);
        
        return this;
    }
    
}

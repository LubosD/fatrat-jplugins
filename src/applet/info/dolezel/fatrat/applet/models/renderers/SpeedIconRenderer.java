/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package info.dolezel.fatrat.applet.models.renderers;

import java.awt.Component;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author lubos
 */
public class SpeedIconRenderer extends DefaultTableCellRenderer {
    
    Icon icon;
    public SpeedIconRenderer(Icon icon) {
        this.icon = icon;
        this.setHorizontalTextPosition(SwingConstants.RIGHT);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component retr = table.getTableHeader().getDefaultRenderer().
            getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        if (JLabel.class.isAssignableFrom(retr.getClass())) {
            JLabel jl = (JLabel) retr;
            jl.setHorizontalTextPosition(SwingConstants.RIGHT);
            jl.setIcon(icon);
        }

        return retr;
    }
    
}

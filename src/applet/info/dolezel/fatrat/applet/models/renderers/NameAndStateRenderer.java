/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package info.dolezel.fatrat.applet.models.renderers;

import info.dolezel.fatrat.applet.models.data.NameAndState;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author lubos
 */
public class NameAndStateRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component retr = table.getDefaultRenderer(String.class).
            getTableCellRendererComponent(table, "", isSelected, hasFocus, row, column);

        if (JLabel.class.isAssignableFrom(retr.getClass())) {
            JLabel jl = (JLabel) retr;
            NameAndState ns = (NameAndState) value;
            
            jl.setText(ns.getName());
            jl.setIcon(ns.getStateIcon());
            jl.setHorizontalTextPosition(SwingConstants.RIGHT);
        }

        return retr;
    }
}

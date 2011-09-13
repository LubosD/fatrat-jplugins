/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package info.dolezel.fatrat.applet.menus;

import info.dolezel.fatrat.applet.FatRatApplet;
import info.dolezel.fatrat.applet.IconLoader;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

/**
 *
 * @author lubos
 */
public class TransferPopupMenu extends JPopupMenu {
    FatRatApplet applet;

    public TransferPopupMenu(final FatRatApplet applet) {
        this.applet = applet;
        
        JMenuItem item;

        item = new JMenuItem();
        item.setAction(applet.actionResume);
        item.setActionCommand("Active");
        item.setText("Resume");
        item.setIcon(applet.loadIcon("/css/icons/states/active.png"));
        this.add(item);
        
        item = new JMenuItem();
        item.setAction(applet.actionForceResume);
        item.setActionCommand("ForcedActive");
        item.setText("Force resume");
        item.setIcon(applet.loadIcon("/css/icons/states/forcedactive.png"));
        this.add(item);
        
        item = new JMenuItem();
        item.setAction(applet.actionPause);
        item.setActionCommand("Paused");
        item.setText("Pause");
        item.setIcon(applet.loadIcon("/css/icons/states/paused.png"));
        this.add(item);
        
        this.addSeparator();
        
        item = new JMenuItem();
        item.setAction(applet.actionRemove);
        item.setActionCommand("");
        item.setText("Delete");
        item.setIcon(applet.loadIcon("/css/icons/delete.png"));
        this.add(item);
        
        item = new JMenuItem();
        item.setAction(applet.actionRemoveWithData);
        item.setActionCommand("data");
        item.setText("Delete with data");
        item.setIcon(applet.loadIcon("/css/icons/delete_with_data.png"));
        this.add(item);
        
        this.addSeparator();
        
        item = new JMenuItem();
        item.setActionCommand("top");
        item.setAction(applet.actionMoveToTop);
        item.setText("Move to top");
        item.setIcon(applet.loadIcon("/css/icons/move/top.png"));
        this.add(item);
        
        item = new JMenuItem();
        item.setActionCommand("up");
        item.setAction(applet.actionMoveUp);
        item.setText("Move up");
        item.setIcon(applet.loadIcon("/css/icons/move/up.png"));
        this.add(item);
        
        item = new JMenuItem();
        item.setActionCommand("down");
        item.setAction(applet.actionMoveDown);
        item.setText("Move down");
        item.setIcon(applet.loadIcon("/css/icons/move/down.png"));
        this.add(item);
        
        item = new JMenuItem();
        item.setActionCommand("bottom");
        item.setAction(applet.actionMoveToBottom);
        item.setText("Move to bottom");
        item.setIcon(applet.loadIcon("/css/icons/move/bottom.png"));
        this.add(item);
    }
    
}

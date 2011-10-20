/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package info.dolezel.fatrat.applet;

import info.dolezel.fatrat.applet.FatRatApplet;
import info.dolezel.fatrat.applet.components.InfoBar;
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

        item = new JMenuItem(applet.actionResume);
        //item.setAction(applet.actionResume);
        item.setActionCommand("Active");
        //item.setText("Resume");
        //item.setIcon(applet.loadIcon("/css/icons/states/active.png"));
        this.add(item);
        
        item = new JMenuItem(applet.actionForceResume);
        //item.setAction(applet.actionForceResume);
        item.setActionCommand("ForcedActive");
        //item.setText("Force resume");
        //item.setIcon(applet.loadIcon("/css/icons/states/forcedactive.png"));
        this.add(item);
        
        item = new JMenuItem(applet.actionPause);
        //item.setAction(applet.actionPause);
        item.setActionCommand("Paused");
        //item.setText("Pause");
        //item.setIcon(applet.loadIcon("/css/icons/states/paused.png"));
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
        
        item = new JMenuItem(applet.actionMoveToTop);
        item.setActionCommand("top");
        //item.setAction(applet.actionMoveToTop);
        //item.setText("Move to top");
        //item.setIcon(applet.loadIcon("/css/icons/move/top.png"));
        this.add(item);
        
        item = new JMenuItem(applet.actionMoveUp);
        item.setActionCommand("up");
        //item.setAction(applet.actionMoveUp);
        //item.setText("Move up");
        //item.setIcon(applet.loadIcon("/css/icons/move/up.png"));
        this.add(item);
        
        item = new JMenuItem(applet.actionMoveDown);
        item.setActionCommand("down");
        //item.setAction(applet.actionMoveDown);
        //item.setText("Move down");
        //item.setIcon(applet.loadIcon("/css/icons/move/down.png"));
        this.add(item);
        
        item = new JMenuItem(applet.actionMoveToBottom);
        item.setActionCommand("bottom");
        //item.setAction(applet.actionMoveToBottom);
        //item.setText("Move to bottom");
        //item.setIcon(applet.loadIcon("/css/icons/move/bottom.png"));
        this.add(item);
        
        this.addSeparator();
        
        item = new JMenuItem("Show info bar");
        item.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                String uuid;
                
                uuid = applet.getCurrentTransfer().get("uuid").toString();
                
                InfoBar bar = new InfoBar(uuid, applet.getSettings(), applet.getClient());
                bar.setVisible(true);
            }
        });
        this.add(item);
    }
    
}

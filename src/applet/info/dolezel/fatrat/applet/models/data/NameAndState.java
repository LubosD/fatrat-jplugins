/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package info.dolezel.fatrat.applet.models.data;

import java.util.HashMap;
import java.util.Map;
import javax.swing.Icon;

/**
 *
 * @author lubos
 */
public class NameAndState {
    private String name, state;
    private static Map<String,Icon> stateIcons = new HashMap<String,Icon>();

    public NameAndState(String name, String state) {
        this.name = name;
        this.state = state;
    }

    public NameAndState(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
    
    public Icon getStateIcon() {
        return stateIcons.get(state);
    }
    
    public static void addIcon(String name, Icon icon) {
        stateIcons.put(name, icon);
    }
}

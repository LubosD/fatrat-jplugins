/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package info.dolezel.fatrat.applet.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.ComboBoxModel;
import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

/**
 *
 * @author lubos
 */
public class QueueModel implements ListModel {
    
    List<ListDataListener> listeners = new ArrayList<ListDataListener>();
    Object[] data;

    @Override
    public int getSize() {
        if (data == null)
            return 0;
        return data.length;
    }

    @Override
    public Object getElementAt(int index) {
        return getData(index).get("name");
    }

    @Override
    public void addListDataListener(ListDataListener l) {
        listeners.add(l);
    }

    @Override
    public void removeListDataListener(ListDataListener l) {
        listeners.remove(l);
    }

    public Object[] getData() {
        return data;
    }
    
    public Map<String,Object> getData(int index) {
        return (Map<String,Object>) data[index];
    }

    public void setData(Object[] data) {
        this.data = data;
    }
    
    public void refresh() {
        ListDataEvent ev = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, getSize());
        for (ListDataListener l : listeners)
            l.contentsChanged(ev);
    }
    
}

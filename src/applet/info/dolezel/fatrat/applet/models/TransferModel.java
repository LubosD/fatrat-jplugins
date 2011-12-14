/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package info.dolezel.fatrat.applet.models;

import info.dolezel.fatrat.applet.models.data.NameAndState;
import info.dolezel.fatrat.applet.util.Util;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

/**
 *
 * @author lubos
 */
public class TransferModel implements TableModel {
    
    List<TableModelListener> listeners = new ArrayList<TableModelListener>();
    Object[] data;
    int lastSize = 0;
    
    static final String[] COLUMN_NAMES = { "Name", "Progress", "Size", "Speed", "Speed", "Time Left", "Message" };

    @Override
    public int getRowCount() {
        if (data == null)
            return 0;
        return data.length;
    }

    @Override
    public int getColumnCount() {
        return 7;
    }

    @Override
    public String getColumnName(int columnIndex) {
        return COLUMN_NAMES[columnIndex];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return Object.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Map<String,Object> map = getData(rowIndex);
        String mode = map.get("mode").toString();
        String primaryMode = map.get("primaryMode").toString();
        String state = map.get("state").toString();
        boolean isActive = state.equals("Active") || state.equals("ForcedActive");
        
        switch (columnIndex) {
            case 0: {
                NameAndState ns = new NameAndState(map.get("name").toString());
                
                if (mode.equals("Upload") && !(primaryMode.equals("Download") && state.equals("Completed")))
                    state = state + "_upload";
                
                ns.setState(state);
                
                return ns;
            }
            case 1: {
                Float rv = 0.0f;
                double total = (Long) map.get("total");
                double done = (Long) map.get("done");
                
                if (total <= 0)
                    return rv;
                
                rv = (float) (done / total * 100.0f);
                
                return rv;
            }
            case 2:
                return Util.formatSize((Long) map.get("total"));
            case 3: {
                String text = "";
                Object[] speeds = (Object[]) map.get("speeds");
                int down = (Integer) speeds[0];
                
                if (down > 0 || (isActive && primaryMode.equals("Download")))
                    text = Util.formatSize(down) + "/s";
                
                return text;
            }
            case 4: {
                String text = "";
                Object[] speeds = (Object[]) map.get("speeds");
                int up = (Integer) speeds[1];
                
                if (up > 0 || (isActive && mode.equals("Upload")))
                    text = Util.formatSize(up) + "/s";
                
                return text;
            }
            case 5: {
                // ETA
                if (!state.equals("Active") && !state.equals("ForcedActive"))
                    return "";
                
                Object[] speeds = (Object[]) map.get("speeds");
                long total = (Long) map.get("total");
                long done = (Long) map.get("done");
                long rem = total - done;
                int speed;
                
                if (done <= 0)
                    return "";
                
                if ( (primaryMode.equals("Download") && mode.equals("Download"))) {
                    speed = (Integer) speeds[0];
                } else if (primaryMode.equals("Upload"))
                    speed = (Integer) speeds[1];
                else
                    return "";
                
                if (speed == 0)
                    return "∞";
                
                return Util.formatTime((int) rem / speed);
            }
            case 6:
                return map.get("message").toString();
            
            default:
                return null;
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void addTableModelListener(TableModelListener l) {
        listeners.add(l);
    }

    @Override
    public void removeTableModelListener(TableModelListener l) {
        listeners.remove(l);
    }
    
    public Object[] getData() {
        return data;
    }
    
    public Map<String,Object> getData(int index) {
        return (Map<String,Object>) data[index];
    }

    public void setData(Object[] data) {
        if (this.data != null)
            lastSize = this.data.length;
        this.data = data;
    }
    
    public void refresh() {
        TableModelEvent ev1 = null, ev2 = null;
        
        if (data == null)
            return;
        
        if (lastSize > 0) {
            if (data.length > lastSize)
                ev2 = new TableModelEvent(this, lastSize-1, data.length-1, TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT);
            else if (data.length < lastSize)
                ev2 = new TableModelEvent(this, data.length-1, lastSize-1, TableModelEvent.ALL_COLUMNS, TableModelEvent.DELETE);
            if (data.length > 0)
                ev1 = new TableModelEvent(this, 0, Math.min(data.length, lastSize));
        } else
            ev1 = new TableModelEvent(this);
        
        for (TableModelListener l : listeners) {
            if (ev1 != null)
                l.tableChanged(ev1);
            if (ev2 != null)
                l.tableChanged(ev2);
        }
    }
    
}

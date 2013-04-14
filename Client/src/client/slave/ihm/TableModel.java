package client.slave.ihm;

import javax.swing.table.AbstractTableModel;

@SuppressWarnings("serial")
public class TableModel extends AbstractTableModel {
    public TableModel() {
        super();
    }
 
    public int getRowCount() {
        return 8;
    }
 
    public int getColumnCount() {
        return 10;
    }
 
    public String getColumnName(int columnIndex) {
    	if(columnIndex == 0)
    		return "";
    	else
            return String.valueOf(columnIndex*5) + " km";
    }
 
    public Object getValueAt(int rowIndex, int columnIndex) {
        if(columnIndex == 0)
        	return String.valueOf(8+2*rowIndex) + " Km/h";
        else
        	return 60*columnIndex*5/(8+2*rowIndex);
    }
    
}
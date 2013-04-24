package client.master.ihm;

import java.util.ArrayList;


import javax.swing.table.AbstractTableModel;

@SuppressWarnings("serial")
public class TableModelMaster extends AbstractTableModel {

	private ArrayList<String> _acceptedClients;
	
	private final String[] entetes = {"Client(s) accepté(s) : "};
	public TableModelMaster(ArrayList<String> acceptedClients) {
    	_acceptedClients = acceptedClients;
    }
 
    @Override
	public String getColumnName(int column) {
		return entetes[column];
	} // getColumnName ()
    
    @Override
	public int getColumnCount() {
		return entetes.length;
	} // getColumnCount ()

	@Override
	public int getRowCount() {
		return _acceptedClients.size();
	} // getRowCount ()
	
	@Override
	public Object getValueAt(int row, int col) {
		switch(col){
			case 0:
				return _acceptedClients.get(row);
			default:
				return null;
		}
	} // getValueAt ()
	
}

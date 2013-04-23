package client.slave.ihm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.swing.table.AbstractTableModel;

@SuppressWarnings("serial")
public class TableModel extends AbstractTableModel {
	
	private ArrayList<String> _listIp;
	private ArrayList<String> _listGr;
	
	private final String[] entetes = {"Adresse(s) Ip(s)", "Groupe(s)"};
	
    public TableModel(HashMap<String, String> liste) {
    	_listIp = new ArrayList<String>();
		_listGr = new ArrayList<String>();
		for(Entry<String, String> entry : liste.entrySet()) {
		    String cle = entry.getKey();
		    String valeur = entry.getValue();
		    _listIp.add(cle); _listGr.add(valeur);
		}
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
		return _listIp.size();
	} // getRowCount ()
	
	@Override
	public Object getValueAt(int row, int col) {
		switch(col){
			case 0:
				return _listIp.get(row);
			case 1:
				return _listGr.get(row);
			default:
				return null;
		}
	} // getValueAt ()
    
}
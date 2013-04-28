package client.slave.ihm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.swing.table.AbstractTableModel;

@SuppressWarnings("serial")
public class TableModel extends AbstractTableModel {
	
	/** */
	private ArrayList<String> _listIp;
	/** */
	private ArrayList<String> _listGr;
	/** */
	private String _certificate;
	
	/** */
	private final String[] headers = {"Creator's ip", "Pseudo", "Group name", "Certificate"};
	
	/**
	 * Constructor
	 * @param liste
	 */
    public TableModel (HashMap<String, String> liste, String certificate) {
    	_listIp = new ArrayList<String>();
		_listGr = new ArrayList<String>();
		for(Entry<String, String> entry : liste.entrySet()) {
		    String cle = entry.getKey();
		    String valeur = entry.getValue();
		    _listIp.add(cle); _listGr.add(valeur);
		}
		_certificate = certificate;
		
    } // TableModel ()
 
    @Override
	public String getColumnName (int column) {
		return headers[column];
		
	} // getColumnName ()
    
    @Override
	public int getColumnCount () {
		return headers.length;
		
	} // getColumnCount ()

	@Override
	public int getRowCount () {
		return _listIp.size();
		
	} // getRowCount ()
	
	@Override
	public Object getValueAt (int row, int col) {
		switch(col){
			case 0:
				return _listIp.get(row);
			case 2:
				return _listGr.get(row);
			case 3:
				return _certificate;
			default:
				return null;
		}
		
	} // getValueAt ()
    
} // TableModel

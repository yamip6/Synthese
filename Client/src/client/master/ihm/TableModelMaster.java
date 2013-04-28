package client.master.ihm;

import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

@SuppressWarnings("serial")
public class TableModelMaster extends AbstractTableModel {
    /** */
	private ArrayList<String> _acceptedClients;
	/** */
	private String _certificate;
	/** */
	private final String[] headers = {"Accepted client", "Pseudo", "Certificate"};
	
	/**
	 * Constructor
	 * @param acceptedClients
	 */
	public TableModelMaster (ArrayList<String> acceptedClients, String certificate) {
    	_acceptedClients = acceptedClients;
    	_certificate = certificate;
    	
    } // TableModelMaster ()
 
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
		return _acceptedClients.size();
		
	} // getRowCount ()
	
	@Override
	public Object getValueAt (int row, int col) {
		switch(col){
			case 0:
				return _acceptedClients.get(row);
			case 2 :
				return _certificate;
			default:
				return null;
		}
		
	} // getValueAt ()
	
} // TableModelMaster

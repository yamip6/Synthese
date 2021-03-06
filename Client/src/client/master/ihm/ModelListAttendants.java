package client.master.ihm;

import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

@SuppressWarnings("serial")
public class ModelListAttendants extends AbstractTableModel{

	private ArrayList<String> _members;
	
	public ModelListAttendants (){
		_members = new ArrayList<String>();
	}
	
	 @Override
	public String getColumnName (int column) {
		return new String("Participants");	
	} // getColumnName ()
	 
	@Override
	public int getColumnCount() {
		return 1;
	}

	@Override
	public int getRowCount() {
		return _members.size();
	}

	@Override
	public Object getValueAt(int row, int col) {
		if (col == 0)
			return _members.get(row);
		else
			return 0;
	}

	public void set_members(ArrayList<String> _members) {
		this._members = _members;
	}

	
}

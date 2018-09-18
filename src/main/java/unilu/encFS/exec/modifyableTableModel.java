package unilu.encFS.exec;

import javax.swing.table.DefaultTableModel;

public class modifyableTableModel extends DefaultTableModel
{
	public modifyableTableModel()
	{
		super(new String[]{"First","Second","Third"},0);			
	}
	public void addNewRow()
	{
		addRow(new Integer[]{1,2,3});
	}
	
	public void removeFirstRow()
	{
		removeRow(0);
	}
}
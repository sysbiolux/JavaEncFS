package unilu.encFS;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;

import unilu.encFS.commands.EncFSCommand;
import unilu.encFS.commands.EncFSController;
import unilu.encFS.model.EncFSModel;

public class EncFSGUI extends JFrame implements ListSelectionListener, TableModelListener{
	EncFSModel model;
	EncFSController controller;
	JTable volumeTable;
	SelectionButton mountButton;
	SelectionButton pwButton;
	SelectionButton closeButton;
	SelectionButton removeButton;

	public EncFSGUI(EncFSModel model, EncFSController controller)
	{
		this.model = model;
		this.controller = controller;
		volumeTable = new JTable(model);				
		volumeTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);		
		volumeTable.setRowSelectionAllowed(true);
		volumeTable.setColumnSelectionAllowed(false);
		volumeTable.getSelectionModel().addListSelectionListener(this);
		volumeTable.getColumnModel().getColumn(0).setMinWidth(200);
		volumeTable.getColumnModel().getColumn(1).setMinWidth(80);
		volumeTable.getColumnModel().getColumn(2).setMinWidth(80);
		volumeTable.getColumnModel().getColumn(1).setMaxWidth(80);
		volumeTable.getColumnModel().getColumn(2).setMaxWidth(80);
		volumeTable.setMinimumSize(new Dimension(360, 80));
		volumeTable.getColumnModel().getColumn(0).setCellRenderer(new StorageCellRenderer(model));
		model.addTableModelListener(volumeTable);
		Container contentPane = this.getContentPane();
		this.getContentPane().setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 3;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weighty = 5;
		contentPane.add(setupScrollPane(volumeTable), gbc);
		
		//gbc.fill = GridBagConstraints.NONE;
		gbc.weighty = 0.5;
		gbc.weightx = 1;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		mountButton = new SelectionButton(EncFSCommand.MOUNT_COMMAND);
		mountButton.addActionListener(controller);
		mountButton.setEnabled(false);
		contentPane.add(mountButton,gbc);
		
		gbc.gridx = 1;
		pwButton = new SelectionButton(EncFSCommand.PWADD_COMMAND);
		pwButton.addActionListener(controller);
		pwButton.setEnabled(false);
		contentPane.add(pwButton,gbc);
		
		gbc.gridx = 2;
		contentPane.add(createNewButton(),gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 2;		
		closeButton = new SelectionButton(EncFSCommand.UNMOUNT_COMMAND);
		closeButton.addActionListener(controller);				
		closeButton.setEnabled(false);
		contentPane.add(closeButton,gbc);
		
		gbc.gridx = 1;
		removeButton = new SelectionButton(EncFSCommand.REMOVE_COMMAND);
		removeButton.addActionListener(controller);				
		removeButton.setEnabled(false);
		contentPane.add(removeButton,gbc);	
		
		this.pack();
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		this.addComponentListener(controller);	
		model.addTableModelListener(this);
	}


	public JScrollPane setupScrollPane(JTable volumeTable)
	{
		JScrollPane pane = new JScrollPane(volumeTable, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		pane.setPreferredSize(new Dimension(400,200));
		return pane;
	}
	
	private class SelectionButton extends JButton
	{
		private String command;
		
		public SelectionButton(String Command)
		{
			super(Command);
			command = Command;
		}
		public void setActiveStore(String store)
		{
			setActionCommand(command + "_" + store);
		}
	}	
	

	public JButton createNewButton()
	{
		JButton createNew = new JButton("Create Volume");
		createNew.addActionListener(controller);
		createNew.setActionCommand(EncFSCommand.NEW_COMMAND);		
		return createNew;
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		// TODO Auto-generated method stub
		updateButtons();
	}
	
	/**
	 * Update the buttons activity.
	 */
	public void updateButtons()
	{
		int crow = volumeTable.getSelectedRow() ;			
		if(crow > -1)
		{
			//Enable unmount and disable mount for active (and vice versa)
			removeButton.setEnabled(true);
			String storeName = (String)model.getStoreNameAt(crow);			
			if(model.isActive(storeName))
			{
				mountButton.setEnabled(false);
				closeButton.setEnabled(true);
			}
			else
			{
				mountButton.setEnabled(true);
				closeButton.setEnabled(false);
			}
			//Activate the pw add button if no pw is set.
			if(model.isPWSet(storeName))
			{
				pwButton.setEnabled(false);
			}
			else
			{
				pwButton.setEnabled(true);
			}
			mountButton.setActiveStore(storeName);
			closeButton.setActiveStore(storeName);
			pwButton.setActiveStore(storeName);
		}
		else
		{
			removeButton.setEnabled(false);
		}
	}


	@Override
	public void tableChanged(TableModelEvent e) {
		// TODO Auto-generated method stub
		updateButtons();
	}
	
	class StorageCellRenderer extends DefaultTableCellRenderer {
		
		EncFSModel model;
		public StorageCellRenderer(EncFSModel model) {
			// TODO Auto-generated constructor stub
			super();
			this.model = model;
			
		}
	    public Component getTableCellRendererComponent(
	                        JTable table, Object value,
	                        boolean isSelected, boolean hasFocus,
	                        int row, int column) {
	        JLabel c = (JLabel)super.getTableCellRendererComponent( table, value, isSelected, hasFocus, row, column) ;
	        String storeName = model.getStoreNameAt(row);
	        String EncryptedFolder = model.getEncryptedFolder(storeName);
	        String DecryptedFolder = model.getEncryptedFolder(storeName);
	        c.setToolTipText("<html>Name: " + storeName + "<br>Encrypted Folder: " + EncryptedFolder + "<br>Decrypted Folder: " + DecryptedFolder + "</html>");
	        return c;
	    }
	}

}

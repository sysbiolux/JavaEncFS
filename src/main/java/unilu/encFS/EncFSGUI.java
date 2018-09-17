package unilu.encFS;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import unilu.encFS.commands.EncFSCommand;
import unilu.encFS.commands.EncFSController;
import unilu.encFS.model.EncFSModel;

public class EncFSGUI extends JFrame implements ListSelectionListener{
	EncFSModel model;
	EncFSController controller;
	JTable volumeTable;
	SelectionButton mountButton;
	SelectionButton pwButton;
	SelectionButton closeButton;

	public EncFSGUI(EncFSModel model, EncFSController controller)
	{
		this.model = model;
		this.controller = controller;
		volumeTable = new JTable(model);		
		volumeTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);		
		volumeTable.setRowSelectionAllowed(true);
		volumeTable.setColumnSelectionAllowed(false);
		volumeTable.getSelectionModel().addListSelectionListener(this);
		Container contentPane = this.getContentPane();
		this.getContentPane().setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 3;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weighty = 5;
		contentPane.add(setupScrollPane(volumeTable), gbc);
		
		gbc.fill = GridBagConstraints.NONE;
		gbc.weighty = 0.5;
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
		
		this.pack();
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		this.addComponentListener(controller);		
	}


	public JScrollPane setupScrollPane(JTable volumeTable)
	{
		JScrollPane pane = new JScrollPane(volumeTable);
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
		int crow = volumeTable.getSelectedRow() ;	
		System.out.println("The selected Row is number " + crow );
		if(crow > -1)
		{
			String storeName = (String)model.getStoreNameAt(crow);
			System.out.println("The store name is: " + storeName);
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

	}
}

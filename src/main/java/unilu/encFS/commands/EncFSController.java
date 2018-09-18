package unilu.encFS.commands;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JOptionPane;

import unilu.encFS.CreateNewModelGUI;
import unilu.encFS.EncFSMenu;
import unilu.encFS.model.EncFSModel;
import unilu.encFS.model.EncFSProperties;

public class EncFSController implements ActionListener ,ComponentListener{

	private EncFSModel model;
	private List<String> commands;
	private EncFSMenu trayMenu;
	public EncFSController(EncFSModel model)
	{
		this.model = model;
		commands = new LinkedList<>();
		commands.add(EncFSCommand.MOUNT_COMMAND);
		commands.add(EncFSCommand.PWADD_COMMAND);
		commands.add(EncFSCommand.UNMOUNT_COMMAND);
		commands.add(EncFSCommand.REMOVE_COMMAND);
	}

	public void setTrayMenu(EncFSMenu menu)
	{
		trayMenu = menu;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		String command = e.getActionCommand();
		if(command.equals(EncFSCommand.EXIT_COMMAND))
		{
			model.shutDown();			
			//Exit was called, so we close.
			System.exit(0);
		}		
		if(command.equals(EncFSCommand.NEW_COMMAND))
		{
			EncFSProperties props = CreateNewModelGUI.requestFolders();
			if(props != null)
			{
				boolean storepasswd = props.password != null;
				model.createStore(props.storeName, props.EncryptedFolder, props.DecryptedFolder, storepasswd);
				trayMenu.buildMenu();
			}
		}
		else
		{
			String currentMenu = command.split("_")[0];					
			if(commands.contains(currentMenu))
			{
				String storage = command.split("_", 2)[1];				
				switch(currentMenu)
				{
				case EncFSCommand.MOUNT_COMMAND: 
				{
					try
					{
						model.unlockStorage(storage);
						trayMenu.buildMenu();
						currentMenu = null;
					}
					catch(Exception ex)
					{
						EncFSModel.showErrorMessage(ex);
					}
					break;
				}
				case EncFSCommand.UNMOUNT_COMMAND: 
				{
					try
					{
						model.lockStorage(storage);
						trayMenu.buildMenu();
						currentMenu = null;
					}
					catch(Exception ex)
					{
						EncFSModel.showErrorMessage(ex);
					}
					break;
				}
				case EncFSCommand.PWADD_COMMAND: 
				{
					try
					{
						model.unlockStorage(storage, null, true);
						trayMenu.buildMenu();
						currentMenu = null;
					}
					catch(Exception ex)
					{
						EncFSModel.showErrorMessage(ex);
					}
					break;
				}
				case EncFSCommand.REMOVE_COMMAND:
				{
					try
					{
						JOptionPane.showMessageDialog(null, "This will NOT remove any files from your system\nIf you want to delete the files, you will have to manually delete them.!");
						model.lockStorage(storage);
						model.removeStorage(storage);
						currentMenu = null;
					}
					catch(Exception ex)
					{
						EncFSModel.showErrorMessage(ex);
					}
					break;
				}

				}

			}
		}		
	}

	@Override
	public void componentResized(ComponentEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void componentMoved(ComponentEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void componentShown(ComponentEvent e) {
		// TODO Auto-generated method stub
		trayMenu.buildMenu();
	}

	@Override
	public void componentHidden(ComponentEvent e) {
		// TODO Auto-generated method stub
		trayMenu.buildMenu();
	}


}

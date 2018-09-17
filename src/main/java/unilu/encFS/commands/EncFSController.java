package unilu.encFS.commands;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.nio.channels.ShutdownChannelGroupException;
import java.util.LinkedList;
import java.util.List;

import unilu.encFS.CreateNewModelGUI;
import unilu.encFS.EncFSMenu;
import unilu.encFS.misc.RequestPasswordDialog;
import unilu.encFS.model.EncFSModel;
import unilu.encFS.model.EncFSProperties;

public class EncFSController implements ActionListener ,ComponentListener{

	private EncFSModel model;
	private String currentMenu = null;
	private List<String> commands;
	private EncFSMenu trayMenu;
	public EncFSController(EncFSModel model)
	{
		this.model = model;
		commands = new LinkedList<>();
		commands.add(EncFSCommand.MOUNT_COMMAND);
		commands.add(EncFSCommand.PWADD_COMMAND);
		commands.add(EncFSCommand.UNMOUNT_COMMAND);		
	}

	public void setTrayMenu(EncFSMenu menu)
	{
		trayMenu = menu;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		String command = e.getActionCommand();
		System.out.println(command);
		if(command.equals(EncFSCommand.EXIT_COMMAND))
		{
			model.saveModel();
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
						System.out.println("Trying to close storage" + storage);
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
						if(model.isActive(storage))
						{
							throw new IllegalStateException("Cannot add password to an open storage.");
						}
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

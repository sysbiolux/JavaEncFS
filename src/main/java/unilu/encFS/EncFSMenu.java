package unilu.encFS;

import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.MenuShortcut;
import java.awt.PopupMenu;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JFrame;

import unilu.encFS.commands.EncFSCommand;
import unilu.encFS.commands.EncFSController;
import unilu.encFS.model.EncFSModel;

public class EncFSMenu extends PopupMenu implements ActionListener {
	private static final long serialVersionUID = 1L;
	private EncFSModel model;
	private EncFSController controller;
	JFrame mainGUI;
	public EncFSMenu(EncFSModel model, EncFSController controller) {
		super();
		this.model = model;
		this.controller = controller;
		mainGUI = new EncFSGUI(model, controller);
		mainGUI.pack();
		mainGUI.setVisible(true);
		buildMenu();
		controller.setTrayMenu(this);
	}
	
	public void buildMenu()
	{
		this.removeAll();
		if(!mainGUI.isVisible())
		{
			this.add(openGUI());
			this.addSeparator();	
		}
		
		this.add(newItem());
		this.addSeparator();
		this.add(openMenu());
		this.add(unmountMenu());
		this.addSeparator();
		this.add(exitItem());		
	}
	
	
	private MenuItem openGUI()
	{
		MenuItem gui = new MenuItem("Open Manager");
		gui.addActionListener(this);
		gui.setActionCommand(EncFSCommand.OPENGUI_COMMAND);
		return gui;
	}
	
	private MenuItem exitItem()
	{
		MenuItem exit = new MenuItem("Exit");
		exit.setShortcut(new MenuShortcut(69)); //e for exit
		exit.setActionCommand(EncFSCommand.EXIT_COMMAND);
		exit.addActionListener(controller);
		return exit;
	}
	
	private MenuItem newItem()
	{
		MenuItem newItem = new MenuItem("Create New");
		newItem.setShortcut(new MenuShortcut(78)); //n for new
		newItem.setActionCommand(EncFSCommand.NEW_COMMAND);
		newItem.addActionListener(controller);
		return newItem;
	}
	
	private Menu openMenu()
	{
		Menu openMenu = new Menu(EncFSCommand.MOUNT_COMMAND);
		List<String> Stores = model.getStoreNames();
		for(String cStore : Stores)
		{
			if(!model.isActive(cStore))
			{
				openMenu.add(selectItem(cStore, true));			
			}
		}
		return openMenu;
	}
	
	
	private Menu unmountMenu()
	{
		Menu unmount = new Menu(EncFSCommand.UNMOUNT_COMMAND);
		List<String> Stores = model.getStoreNames();
		for(String cStore : Stores)
		{
			if(model.isActive(cStore))
			{
				unmount.add(selectItem(cStore, false));
			}
		}
		return unmount;
	}
	
	private MenuItem selectItem(String StoreName, boolean mount)
	{
		MenuItem selectItem = new MenuItem(StoreName);
		String Command = null;
		if(mount)
		{
			Command = EncFSCommand.MOUNT_COMMAND + "_" + StoreName;
		}
		else
		{
			Command = EncFSCommand.UNMOUNT_COMMAND + "_" + StoreName;
		};  
		selectItem.setActionCommand(Command);
		selectItem.addActionListener(controller);
		return selectItem;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		if(e.getActionCommand()!= null && e.getActionCommand().equals(EncFSCommand.OPENGUI_COMMAND))
		{
			if(!mainGUI.isVisible())
			{
				mainGUI.setVisible(true);
			}
		}
	}
}

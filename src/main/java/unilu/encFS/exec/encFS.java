package unilu.encFS.exec;


import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.SystemTray;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

import unilu.encFS.EncFSTray;
import unilu.encFS.commands.EncFSController;
import unilu.encFS.model.EncFSModel;
import unilu.encFS.model.EncFSModelRefresher;

public class encFS {

    
	
	public static void main(String[] args) 	{
		SwingUtilities.invokeLater(new Runnable() {
	        public void run() {
	            createAndShowGUI();
	        }
	    });
	}
	public static void createAndShowGUI()
	{
		// TODO Auto-generated method stub
		System.out.println("Hello World");
		EncFSModel mod = new EncFSModel();
		EncFSController controller = new EncFSController(mod);
		EncFSModelRefresher refresher = new EncFSModelRefresher(mod);
				
		try{
			BufferedImage myPicture = null;			
			
			myPicture = ImageIO.read(mod.getClass().getClassLoader().getResource("Lock_tray.png"));
			
			EncFSTray tray = new EncFSTray(myPicture, mod, controller);
			if(SystemTray.isSupported())
			{
				final SystemTray stray = SystemTray.getSystemTray();
				stray.add(tray);
			}
			else
			{
				System.out.println("Tray not supported");				
			}
			Thread refresherThread = new Thread(refresher);
			refresherThread.start();
		}

		catch(Exception e)
		{
			e.printStackTrace(System.out);
		}
	}

	public static void createAndShowGUI2()
	{
		EncFSModel mod = new EncFSModel();
		
	}
}


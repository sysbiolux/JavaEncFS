package unilu.encFS.exec;


import java.awt.SystemTray;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

import unilu.encFS.CreateNewModelGUI;
import unilu.encFS.EncFSTray;
import unilu.encFS.commands.EncFSController;
import unilu.encFS.model.EncFSModel;

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
			/*Path tempDecrypt = Files.createTempDirectory("Decrypt");
			Path tempEncrypt = Files.createTempDirectory("Encrypt");
			System.out.println("Decrypt Folder is:" + tempDecrypt.toString());
			System.out.println("Encrypt Folder is:" + tempEncrypt.toString());
			mod.createStore("TestStore", tempEncrypt.toString(), tempDecrypt.toString(), false);
			File[] encFSFiles = tempEncrypt.toFile().listFiles();
			//assertEquals(encFSFiles.length, 1);
			File tempFile = File.createTempFile("test", "out", tempDecrypt.toFile()); 
			BufferedWriter bw = new BufferedWriter(new FileWriter(tempFile));		
			bw.write("Some test strings");
			bw.close();
			tempFile = File.createTempFile("test", "out", tempDecrypt.toFile());
			//assertEquals(encFSFiles.length, 2);
			encFSFiles = tempEncrypt.toFile().listFiles();
			mod.lockStorage("TestStore");*/
		}

		catch(Exception e)
		{
			e.printStackTrace(System.out);
		}
	}

}

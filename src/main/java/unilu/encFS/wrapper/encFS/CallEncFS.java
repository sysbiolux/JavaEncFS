package unilu.encFS.wrapper.encFS;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import javax.swing.JOptionPane;

import unilu.encFS.exceptions.NonEmptyFolderException;
import unilu.encFS.misc.RequestPasswordDialog;

public class CallEncFS {		

	public static int OPENMOUNT = 1;
	public static int NEWMOUNT = 1;
	
	
	public static Process startEncFSProcess(String encryptedFolder, String DecryptedFolder) throws NonEmptyFolderException, IOException
	{
		File f = new File(DecryptedFolder);
		if(!f.exists())
		{
			int opt = JOptionPane.showConfirmDialog(null, "Shall " + DecryptedFolder + " be created?", "Create Decryption Folder", JOptionPane.YES_NO_OPTION);
			if(opt == JOptionPane.YES_OPTION)
			{
				f.mkdirs();
			}
			else
			{
				return null;
			}
		}
		if(!f.isDirectory())
		{
			JOptionPane.showMessageDialog(null, "Decryption volume is not a directory.", "Invalid Decryption Folder", JOptionPane.ERROR_MESSAGE);
			return null;
		}
		else
		{
			if(f.list().length > 0)
			{
				JOptionPane.showMessageDialog(null, "Decryption volume is not empty.\nCannot mount to a non empty folder.", "Invalid Decryption Folder", JOptionPane.ERROR_MESSAGE);
				return null;
			}
			else
			{
				ProcessBuilder pb = new ProcessBuilder("encfs", encryptedFolder, DecryptedFolder);
				pb.redirectErrorStream(true);
				return pb.start();	
			}
		}		
	}
	
	public static int getEncFSStatus(Process encFSProcess) throws IOException
	{
		//This function assumes, that the encFS process is started, but nothing is yet read from the process (as it could otherwise block.
		BufferedReader br = new BufferedReader(new InputStreamReader(encFSProcess.getInputStream()));
		String currentLine = br.readLine();
		if(currentLine.startsWith("Creating"))
		{
			return NEWMOUNT;
		}
		else
		{
			return OPENMOUNT;
		}		
	}
	
	public static void openExistingMount(Process encFSProcess, String password) throws IOException
	{
		BufferedReader br = new BufferedReader(new InputStreamReader(encFSProcess.getInputStream()));
		OutputStreamWriter osw = new OutputStreamWriter(encFSProcess.getOutputStream());
		String currentLine;
		while(br.ready())
		{
			currentLine = br.readLine();
			System.out.println(currentLine);
		}		
		osw.write(password);				
		osw.write("\n");
		osw.flush();											
	}
	
	
	public static void generateNewMount(Process encFSProcess, String password) throws IOException
	{				
		BufferedReader br = new BufferedReader(new InputStreamReader(encFSProcess.getInputStream()));
		OutputStreamWriter osw = new OutputStreamWriter(encFSProcess.getOutputStream());
		String currentLine;		
		while(br.ready())
		{
			currentLine = br.readLine();
			System.out.println(currentLine);
		}
		osw.write("\n");
		osw.flush();					
		while(br.ready())
		{				
			int currentChar = br.read(); 
			System.out.print((char)currentChar);											
		}
		osw.write(password);				
		osw.write("\n");
		osw.flush();						
		while(br.ready())
		{				
			int currentChar = br.read(); 
			System.out.print((char)currentChar);											
		}
		osw.write(password);				
		osw.write("\n");
		osw.flush();
		while(br.ready())
		{				
			int currentChar = br.read(); 
			System.out.print((char)currentChar);											
		}		
	}

}

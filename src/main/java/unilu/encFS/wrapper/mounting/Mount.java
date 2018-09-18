package unilu.encFS.wrapper.mounting;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Mount {

	public static final int ENCFS_MOUNT = 0;
	public static final int OTHER_MOUNT = 1;
	public static final int UNMOUNTED = 2;
	public static int getMountType(String foldername) throws IOException
	{
		ProcessBuilder pb = new ProcessBuilder("mount");
		Process mount = pb.start();
		try{
			mount.waitFor();
		}
		catch(InterruptedException e)
		{
			//Do nothing.
		}
		
		BufferedReader br = new BufferedReader(new InputStreamReader(mount.getInputStream()));
		int mountStatus = UNMOUNTED;
		while(br.ready())
		{
			String currentLine = br.readLine();
			if(currentLine.contains(" " + foldername + " "))
			{
				if(currentLine.startsWith("encfs"))
				{
					mountStatus = ENCFS_MOUNT;					
				}
				else
				{
					mountStatus = OTHER_MOUNT;
				}
				break;
			}
		}
		
		return mountStatus;
		
	}
	
	
}

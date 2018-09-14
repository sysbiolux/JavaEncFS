package unilu.encFS.wrapper.mounting;

import java.io.IOException;

public class LinuxUnmounter {
	
	public static void unmount(String targetFolder) throws IOException
	{
		ProcessBuilder pb = new ProcessBuilder("fusermount", "-u", targetFolder);
		pb.start();
	}
}

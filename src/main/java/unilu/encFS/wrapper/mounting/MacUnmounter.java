package unilu.encFS.wrapper.mounting;

import java.io.IOException;

public class MacUnmounter {
	public static void unmount(String targetFolder) throws IOException
	{
		ProcessBuilder pb = new ProcessBuilder("umount", targetFolder);
		pb.start();
	}

}

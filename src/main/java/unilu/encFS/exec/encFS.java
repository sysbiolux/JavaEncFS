package unilu.encFS.exec;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import unilu.encFS.wrapper.encFS.CallEncFS;

public class encFS {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("Hello World");
		CallEncFS caller = new CallEncFS();
		try
		{
			Process encFS = caller.startEncFSProcess("/home/thomas/Test/Encrypt", "/home/thomas/Test/Decrypt");		
			caller.openExistingMount(encFS, "blubb");
			BufferedReader br = new BufferedReader(new InputStreamReader(encFS.getInputStream()));
			encFS.waitFor();
			while(br.ready())
			{
				System.out.println(br.readLine());
			}
			if(!encFS.isAlive())
			{
				System.out.println("Process terminated with exit value: " + encFS.exitValue());
			}
			
		}
		catch(Exception e)
		{
			e.printStackTrace(System.out);
		}
	}

}

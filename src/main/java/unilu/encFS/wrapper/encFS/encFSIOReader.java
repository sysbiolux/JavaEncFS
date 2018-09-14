package unilu.encFS.wrapper.encFS;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public class encFSIOReader implements Runnable
	{
		BufferedReader br;
		BufferedWriter bw;
		boolean isReady = false;
		String Request; 
		public encFSIOReader(InputStream is, OutputStream os)
		{
			br = new BufferedReader(new InputStreamReader(is));			
			bw = new BufferedWriter(new OutputStreamWriter(os));
		}
		public void run()
		{
			try
			{
			int currentline = br.read();

			while(currentline != -1 )
			{			
				
				if(br.ready())
				{
					System.out.print((char)currentline);
					currentline = br.read();
					Request = Request + "\n" + (char)currentline;					
				}	
				else
				{	bw.notify();				
					bw.flush();
				}
			}
			}
			catch(IOException e)
			{};
			isReady = true;
		}
		
		public boolean isReady()
		{
			return isReady;			
		}
		public String getRequest()
		{
			return Request;
		}
	}
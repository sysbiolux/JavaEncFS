package unilu.encFS.model;
import java.util.concurrent.TimeUnit;

public class KeyStore implements Runnable {
	private String key;
	private long currentTime;	
	public void run()
	{
		while(true)
		{
			if(System.currentTimeMillis() - currentTime > 300000) //more than five minutes
			{
				key = null;
			}
			try
			{
				TimeUnit.SECONDS.sleep(10);	
			}
			catch(InterruptedException e)
			{
				break;
			}
		}
	}

	
	public String getKey()
	{
		return key;
	}
	
	public synchronized void setKey(String key)
	{
		this.key = key;
		currentTime = System.currentTimeMillis();
	}

}

package unilu.encFS.model;

import java.util.concurrent.TimeUnit;

public class EncFSModelRefresher implements Runnable {

	private EncFSModel model;
	
	public EncFSModelRefresher(EncFSModel model) {
		this.model = model;
		// TODO Auto-generated constructor stub
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub		
		while(true)
		{
			try{
				model.refresh();
				//System.out.println("Refreshing Model");
				TimeUnit.SECONDS.sleep(15);
			}
			catch(InterruptedException e)
			{
				break;
			}
		}
	}

}

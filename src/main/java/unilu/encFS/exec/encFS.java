package unilu.encFS.exec;


import javax.swing.JFrame;
import javax.swing.JTable;

import unilu.encFS.model.EncFSModel;

public class encFS {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("Hello World");
		
		EncFSModel model = new EncFSModel();
		model.createStore("Test", "/home/thomas/Test/Encrypt", "/home/thomas/Test/Decrypt", false);
		JTable tab = new JTable(model);
		JFrame frame = new JFrame("TestFrame");
		frame.getContentPane().add(tab);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(frame.EXIT_ON_CLOSE);
		try
		{
			
			
		}
		catch(Exception e)
		{
			e.printStackTrace(System.out);
		}
	}

}

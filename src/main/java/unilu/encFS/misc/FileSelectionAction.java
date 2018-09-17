package unilu.encFS.misc;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JTextField;

public class FileSelectionAction extends AbstractAction{

	JTextField target;
	String title;
	public FileSelectionAction(JTextField target, String title)
	{
		this.target = target;
		this.title = title;
	}

	
	private static File chooseFile(String title)
	{
		JFileChooser fcEnc = new JFileChooser();
		fcEnc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fcEnc.setDialogTitle(title);
		fcEnc.showOpenDialog(null);
		File cFile = fcEnc.getSelectedFile();
		return cFile;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		File f = chooseFile(title);
		target.setText(f.getAbsolutePath());
	}

}


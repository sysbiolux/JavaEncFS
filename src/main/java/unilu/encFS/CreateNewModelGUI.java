package unilu.encFS;

import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import unilu.encFS.misc.FileSelectionAction;
import unilu.encFS.misc.SpringUtilities;
import unilu.encFS.model.EncFSProperties;

public class CreateNewModelGUI {
	
	public static EncFSProperties requestFolders()
	{

		
		SpringLayout clayout = new SpringLayout();
        JPanel p = new JPanel(clayout);
        JLabel n = new JLabel("Volume Name", JLabel.TRAILING);
        JTextField volumeName = new JTextField();
        JLabel nEnd = new JLabel("", JLabel.TRAILING);
        JLabel e = new JLabel("Encrypted Folder", JLabel.TRAILING);
        JTextField encFolder = new JTextField();
        encFolder.setEditable(false);
        JButton encSelect = new JButton("Select Encrypted Folder");
        encSelect.setAction(new FileSelectionAction(encFolder, "Select Encrypted Folder"));
        encSelect.setText("Select Encrypted Folder");
        
        JLabel d = new JLabel("Decrypted Folder", JLabel.TRAILING);
        JTextField decFolder = new JTextField();
        decFolder.setEditable(false);
        JButton decSelect = new JButton("Select Encrypted Folder");
        decSelect.setAction(new FileSelectionAction(decFolder, "Select Decrypted Folder"));        
        decSelect.setText("Select Decrypted Folder");
        
        JLabel pw = new JLabel("Should the password be stored?", JLabel.TRAILING);
        JCheckBox pwCheck = new JCheckBox();        
        JLabel pw2 = new JLabel();
        p.add(n);
        p.add(volumeName);        
        p.add(nEnd);
        p.add(e);
        p.add(encFolder);        
        p.add(encSelect);
        p.add(d);
        p.add(decFolder);
        p.add(decSelect);
        p.add(pw);                
        p.add(pwCheck);                
        p.add(pw2);              
        SpringUtilities.makeGrid(p,
                4, 3, //rows, cols
                5, 5, //initialX, initialY
                5, 5);//xPad, yPad
        //Lay out the panel.
        int result = JOptionPane.showConfirmDialog(null, p, "Select your Folders for Encryption", JOptionPane.OK_CANCEL_OPTION);
        if(result == JOptionPane.OK_OPTION)
        {
        	EncFSProperties props = new EncFSProperties();
        	props.storeName = volumeName.getText();
        	props.DecryptedFolder = decFolder.getText();
        	props.EncryptedFolder = encFolder.getText();
        	if(pwCheck.isSelected())
        	{
        		props.password = "";
        	}
        	return props;
        }
        return null;
        
	}
}

package unilu.encFS;

import java.awt.Image;
import java.awt.TrayIcon;

import unilu.encFS.commands.EncFSController;
import unilu.encFS.model.EncFSModel;

public class EncFSTray extends TrayIcon{

	
	public EncFSTray(Image image, EncFSModel model, EncFSController controller) {
		super(image);
		this.setToolTip("EncFS Access");
		this.setPopupMenu(new EncFSMenu(model,controller));
	}
	

}

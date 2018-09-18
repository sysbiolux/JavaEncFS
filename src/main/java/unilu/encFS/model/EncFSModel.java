package unilu.encFS.model;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

import unilu.encFS.exceptions.InvalidStoreName;
import unilu.encFS.exceptions.NonEmptyFolderException;
import unilu.encFS.exceptions.NotMountableException;
import unilu.encFS.misc.Encryption;
import unilu.encFS.misc.RequestPasswordDialog;
import unilu.encFS.wrapper.encFS.CallEncFS;
import unilu.encFS.wrapper.mounting.LinuxUnmounter;
import unilu.encFS.wrapper.mounting.MacUnmounter;
import unilu.encFS.wrapper.mounting.Mount;

public class EncFSModel extends DefaultTableModel{

	private HashMap<String, EncFSProperties> storages;
	private KeyStore key;
	private Thread keyThread;
	private boolean hasMasterKey;
	private static String keyTest = "encFSMasterKey";
	private String encodedKey = null;
	private LinkedList<String> stores;
	public static String encFSSaveFile = "encfs.props";
	private DefaultTableModel tabModel;
	public static final int COL_NAME = 0;
	public static final int COL_ACTIVE = 1;
	public static final int COL_PASS = 2;		
	private Icon active;
	private Icon inactive;
	private String testPassword = null; 
	public EncFSModel()
	{			
		this(false);
	}

	public EncFSModel(boolean testing)
	{	
		super(new String[]{"Name","Mounted","Password"},0);
		tabModel = new DefaultTableModel(new String[]{"Name","Mounted","Password"},0);
		storages = new HashMap<>();
		stores = new LinkedList<>();
		key = new KeyStore();
		keyThread = new Thread(key);
		keyThread.start();
		try{
			if(!testing)
			{
			System.out.println("Trying to read Images");
			inactive = new ImageIcon(ImageIO.read(getClass().getResource("/inactive.png")));
			active = new ImageIcon(ImageIO.read(getClass().getResource("/active.png")));
			}
			else
			{
				inactive = new ImageIcon();
				active = new ImageIcon();
			}
		}
		catch(IOException e)
		{
			e.printStackTrace(System.out);
		};
    	if (testing)
		{
			testPassword = "testtest";
		}
		else
		{
			loadModel();		
		}

	}

	/**
	 * Remove a Volume Information with the given Name
	 * @param Name The Name of the storage
	 */
	public void removeStorage(String Name)
	{	
		if(isActive(Name))
		{
			lockStorage(Name);
		}
		storages.remove(Name);
		tabModel.removeRow(stores.indexOf(Name));
		removeRow(stores.indexOf(Name));
		stores.remove(Name);	
		fireTableDataChanged();
	}
	
	/**
	 * Add a Storage Volume for the Encrypted/Decrypted folder combination with the given name.
	 * @param Name the name of the storage
	 * @param EncryptedFolder The absolute path to the encrypted folder
	 * @param DecryptedFolder The absolute path to the decrypted folder
	 */
	private void addStorage(String Name, String EncryptedFolder, String DecryptedFolder)
	{
		EncFSProperties props = new EncFSProperties();
		//Build the properties
		props.DecryptedFolder = DecryptedFolder;
		props.EncryptedFolder = EncryptedFolder;
		props.storeName = Name;
		//Add to the lists
		storages.put(Name, props);
		stores.add(Name);
		//System.out.println("Adding store " + Name);
		//update the model
		tabModel.addRow(new Object[]{Name,false,false});
		addRow(new Object[]{Name,false,false});
		fireTableDataChanged();
	}
	
	public List<String> getStoreNames()
	{
		LinkedList<String> storeNames = new LinkedList<String>(storages.keySet());
		Collections.sort(storeNames);
		return storeNames;
	}

	public boolean isActive(String storeName)
	{
		if(stores.indexOf(storeName) >= 0)
		{
			return (boolean)tabModel.getValueAt(stores.indexOf(storeName), COL_ACTIVE);
		}
		else
		{
			return false;
		}
	}		

	private void setActive(String storeName,boolean active)
	{
		setValueAt(active, stores.indexOf(storeName), COL_ACTIVE);		
	}


	private void setPWPresent(String storeName, boolean present)
	{
		if(!present)
		{
			storages.get(storeName).password = null;
		}
		setValueAt(present, stores.indexOf(storeName), COL_PASS);
	}

	public boolean isPWSet(String storeName)
	{				
		return storages.get(storeName).password != null;
	}



	private String getMasterKey()
	{
		String masterKey = key.getKey();
		if(masterKey == null)
		{
			if(encodedKey == null)
			{
				//no Master key set!
				try{
					masterKey = RequestPasswordDialog.requestConfirmedPassword("Please Select a Master Key for the Storage",testPassword);
					encodedKey = Encryption.encrypt(keyTest, masterKey);
					key.setKey(masterKey);
					return masterKey;
				}
				catch(Exception e)
				{
					showErrorMessage(e);
					return null;
				}
			}
			try{
				while(!keyIsValid(masterKey))
				{
					masterKey = RequestPasswordDialog.requestPassword("Enter Master Key", testPassword);
					if(!keyIsValid(masterKey))
					{
						int opt = JOptionPane.showConfirmDialog(null, "Key Invalid, try again", "Invalid Key", JOptionPane.OK_CANCEL_OPTION);
						if(opt == JOptionPane.CANCEL_OPTION)
						{
							throw new InvalidKeyException();
						}
					}
				}
				key.setKey(masterKey);				
			}
			catch(Exception e)
			{
				showErrorMessage(e);
				return null;
			}

		}
		return masterKey;
	}


	public void createStore(String Name, String EncryptedFolder, String DecryptedFolder, boolean storePassword)
	{
		if(stores.contains(Name))
		{
			JOptionPane.showMessageDialog(null, "The choosen Store Name already exists, please select a different Name", "Duplicate Store Name", JOptionPane.ERROR_MESSAGE, null); 
		}
		EncFSProperties props = new EncFSProperties();
		props.EncryptedFolder = EncryptedFolder;
		props.DecryptedFolder = DecryptedFolder;
		props.storeName = Name;
//		System.out.println("unlocking Store");
		unlockStorage(Name,props,storePassword);
	}

	public void addPasswordToStore(String StoreName, String password) throws InvalidKeyException
	{		
		if(!hasMasterKey)
		{
			//Request a non empty key to be added to the
			String masterPass = RequestPasswordDialog.requestConfirmedPassword("A Masterkey is required to store passwords", testPassword);
			if(masterPass != null)
			{
				key.setKey(masterPass);		
				try{
					encodedKey = Encryption.encrypt(keyTest, masterPass);
				}
				catch(Exception e)
				{
					showErrorMessage(e);
				}
			}
			else
			{
				throw new InvalidKeyException("A Password is needed!");
			}
		}
		//Try to obtain a stored key
		String masterKey = getMasterKey(); 
		try
		{
			storages.get(StoreName).password = Encryption.encrypt(password, masterKey);
			setValueAt(true, stores.indexOf(StoreName), 2);
		}
		catch(Exception e)
		{
			throw new InvalidKeyException("Password invalid");
		}

	}


	public void unlockStorage(String storeName) throws InvalidStoreName
	{
		unlockStorage(storeName,null, false);
	}

	public void unlockStorage(String storeName, EncFSProperties encFSProps, boolean storePassword)
	{
		String password = null;
		//This is an existing Storage
		if(encFSProps == null)
		{				
			encFSProps = storages.get(storeName);
			//System.out.println("Props for Store " + storeName + " are " + encFSProps);

		}
		if(stores.contains(storeName))
		{	//check, whether its already active
			//System.out.println("Found Storage with Name " + storeName + "\nTrying to open");
			if(isActive(storeName))
			{
				//Nothing to do if we don't want to store the pw.
				if(!storePassword)
				{
					return;
				}
				else
				{
					if(encFSProps.password == null)
					{
						try{
							Process encFS = CallEncFS.startEncFSProcess(encFSProps.EncryptedFolder, encFSProps.DecryptedFolder);
							CallEncFS.getEncFSStatus(encFS);
							password = openExistingVolume(encFS, encFSProps);
							if(password == null)
							{
								throw new InvalidKeyException("No Key provided");
							}	
						}
						catch(Exception e)
						{
							showErrorMessage(e);
							return;
						}
					}
				}
			}
			else
			{
				//Test, whether this is a new storage or an existing one.
				try
				{
					Process encFS = CallEncFS.startEncFSProcess(encFSProps.EncryptedFolder, encFSProps.DecryptedFolder);
					//Get the encFS status out of the way (likely nothing)
					int openType = CallEncFS.getEncFSStatus(encFS);
					if(openType == CallEncFS.NEWMOUNT)
					{
						//this should not happen!! 
						//it indicates a store, which was set up, but does not exist on the hard drive.
						//we will remove the store and error.
						removeStorage(storeName); 
						throw new IOException("Could not find the indicated store. Removing it from the volumes.");
					}
					//Since it is stored, we just open it.
					password = openExistingVolume(encFS, encFSProps);
					if(password == null)
					{
						throw new InvalidKeyException("No Key provided");
					}			
				}
				catch(Exception e)
				{
					showErrorMessage(e);
					return;
				}
			}
		}
		else
		{ 
			//This is not yet a store!
			//So it might be an existing store on the hard drive. 
			//We will test this and add it accordingly.
			//System.out.println("Generating New Store with name" + storeName);
			try{
//				System.err.println("Starting encFS");
				Process encFS = CallEncFS.startEncFSProcess(encFSProps.EncryptedFolder, encFSProps.DecryptedFolder);
				int openType = CallEncFS.getEncFSStatus(encFS);								
//				System.err.println("EncFS Status is " + openType);
				if(openType == CallEncFS.NEWMOUNT)
				{				
					password = RequestPasswordDialog.requestConfirmedPassword("Please select a password for the storage. (Minimum length 8)", testPassword);
					while(password != null && password.length() < 8)
					{
						password = RequestPasswordDialog.requestConfirmedPassword("The password must have at least a legth fo 8.\nPlease select a password for the storage", testPassword);	
					}
//					System.err.println("PAssword is " + testPassword);					
					if(password != null)
					{
						//IF we got a password, 
						CallEncFS.generateNewMount(encFS, password);
//						System.err.println("Mount being created, waiting for encFS to finish");
						encFS.waitFor();
					}
					else
					{
						encFS.destroy();
						return;
					}
//					System.out.println("New mount created");
				}
				else
				{
					try
					{
						password = openExistingVolume(encFS, encFSProps);
					}
					catch(Exception e)
					{
						showErrorMessage(e);
						return;
					}
				}
			}
			catch(Exception e)
			{				
				showErrorMessage(e);
				return;
			}
			addStorage(storeName, encFSProps.EncryptedFolder, encFSProps.DecryptedFolder);

		}
		setActive(storeName, true);		
		if(storePassword)
		{
			String masterKey = getMasterKey();
			if(masterKey == null)
			{
				//There is no key set. so we can't do anything!
				return;
			}
			try{
				encFSProps.password = Encryption.encrypt(password, masterKey);
				setPWPresent(storeName,true);
			}
			catch(Exception e)
			{
				showErrorMessage(e);
			}
		}			


	}	


	public void lockStorage(String storeName)
	{
		if(!storages.containsKey(storeName))
		{
			return;
		}
		//OS Dependent (fusermount -u for linux; umount for mac.)
		String os = System.getProperty("os.name");
		try
		{
			if(os.startsWith("Linux"))
			{
				LinuxUnmounter.unmount(storages.get(storeName).DecryptedFolder);		
			}
			else if(os.startsWith("mac") || os.startsWith("Mac"))
			{
				MacUnmounter.unmount(storages.get(storeName).DecryptedFolder);
			}
			setActive(storeName, false);
			//TODO: update model	
		}
		catch(IOException e)
		{
			showErrorMessage(e);
		}

	}

	public void saveModel()
	{
		//We store ourselves in the user folder / JavaencFS  
		String userfolder = System.getProperty("user.home");
		File encFSFolder = new File(userfolder + File.separator + "encFS");
		if(!encFSFolder.exists())
		{
			encFSFolder.mkdirs();
		}
		File storageFile = new File(encFSFolder.getAbsolutePath() + File.separator + encFSSaveFile);
		List<Serializable> outputobjects = new LinkedList<>();
		outputobjects.add(encodedKey);
		for(String storage : storages.keySet())
		{
			outputobjects.add(storage);
			outputobjects.add(storages.get(storage));
		}
		try{
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(storageFile));
			oos.writeObject(outputobjects);
			oos.close();
		}
		catch(IOException e)
		{
			showErrorMessage(e);
		}		

	}

	public void loadModel()
	{
		//We store ourselves in the user folder / JavaencFS  
		String userfolder = System.getProperty("user.home");
		File encFSConfig = new File(userfolder + File.separator + "encFS" + File.separator + encFSSaveFile);
		if(encFSConfig.exists())
		{
			try{
				ObjectInputStream ois = new ObjectInputStream(new FileInputStream(encFSConfig));			
				List<Serializable> inputObjects = (List<Serializable>)ois.readObject();			
				Iterator<Serializable> loader = inputObjects.iterator();
				encodedKey = (String)loader.next();
				while(loader.hasNext())
				{
					String storeName = (String)loader.next();
					EncFSProperties props = (EncFSProperties)loader.next();
					//System.out.println("Loading Properties for " + storeName + ":" );
					//System.out.println(props);
					storages.put(storeName, props);
					stores.add(storeName);

				}
				for(String store : stores)
				{
					Object[] rowData = new Object[3];
					rowData[0] = store;
					rowData[1] = false;
					rowData[2] = storages.get(store).password != null;
					tabModel.addRow(rowData);
					addRow(rowData);
				}
				ois.close();
				//TODO: Update model;
			}
			catch(IOException e)
			{
				showErrorMessage(e);
			}		
			catch(ClassNotFoundException e)
			{
				showErrorMessage(e);
			}
		}
	}

	private String openExistingVolume(Process encFS, EncFSProperties encFSProps) throws InvalidKeyException,IOException,InterruptedException,NonEmptyFolderException
	{
		//Request the password for the 		

		int mountStatus = Mount.getMountType(encFSProps.DecryptedFolder);
		//If it is a new one, we request a confirmed password.
		if(mountStatus == Mount.OTHER_MOUNT)
		{		
			throw new IOException("Cannot create Mount point. The target volume is already mounted.");
		}
		else
		{
			String password = encFSProps.password;
			if(password == null)
			{
				password = RequestPasswordDialog.requestPassword("Please enter the Password for the storage", testPassword);
			}		
			else
			{
				String masterKey = getMasterKey();
				try{
					password = Encryption.decrypt(password, masterKey);
				}
				catch(Exception e)
				{
					showErrorMessage(e);
					password = RequestPasswordDialog.requestPassword("Could not retrieve password, please enter the Password for the storage", testPassword);										
				}
			}
			if(password == null)
			{
				throw new InvalidKeyException("No Password provided!");
			}

			boolean pwcorrect = false;
			while(!pwcorrect)
			{
				try{
					CallEncFS.openExistingMount(encFS, password);
					encFS.waitFor();
					if(mountStatus == Mount.UNMOUNTED && encFS.exitValue() == 0)
					{
						pwcorrect = true;
					}
				}
				catch(NotMountableException e)
				{
					//ok, we couldn't mount, but it is already mounted. so we can proceed.
					if(mountStatus == Mount.ENCFS_MOUNT)
					{
						return password;
					}						
				}
				catch(InvalidKeyException e)
				{
					password = RequestPasswordDialog.requestPassword("Password Invalid, Please enter the Password for the storage", testPassword);
					if(encFSProps.password != null)
					{
						setPWPresent(encFSProps.storeName, false);
					}
					if(password == null)
					{
						throw new InvalidKeyException("No Password provided!");
					}
					encFS = CallEncFS.startEncFSProcess(encFSProps.EncryptedFolder, encFSProps.DecryptedFolder);
				}
			}


			return password;
		}			
	}

	private boolean keyIsValid(String key) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException
	{		
		if(key == null)
		{
			return false;
		}
		String decryptedValidator = Encryption.decrypt(encodedKey, key);
		return decryptedValidator.equals(keyTest);
	}

	public static void showErrorMessage(Exception e)
	{		
		JOptionPane.showMessageDialog(null, "An internal error occurd:\n" + e.getMessage() ,e.getClass().getName(),JOptionPane.ERROR_MESSAGE);
		e.printStackTrace(System.err);
	}

	/**
	 * Get the store name at a particular position, for use with the table.
	 * @param row
	 * @return
	 */
	public String getStoreNameAt(int row)
	{
		return (String)tabModel.getValueAt(row, 0);
	}

	public void shutDown()
	{
		//kill the key thread.
		keyThread.interrupt();
		//save the model
		saveModel();
		//unmount the volumes
		for(String store : stores)
		{
			if(isActive(store))
			{
				lockStorage(store);
			}
		}
	}
	//refresh settings, in case something went wrong.
	public void refresh()
	{
		for(String store : stores)
		{
			try{
			int mountType = Mount.getMountType(storages.get(store).DecryptedFolder);			
			if(isActive(store))
			{
				if(mountType != Mount.ENCFS_MOUNT)
				{
					//This is no longer an encFS storage, so we deactivate it.
					setActive(store, false);
				}

			}
			else
			{
				if(mountType == Mount.ENCFS_MOUNT)
				{
					setActive(store, true);
				}
			}
		}
			catch(Exception e)
			{
				e.printStackTrace(System.out);
			}

		}
	}
	
	
	@Override
	public Object getValueAt(int row, int column) {
		//System.out.println("Requesting value at " + row + "/" + column);
		switch(column)
		{
		case COL_NAME:
		{
			String name = (String)tabModel.getValueAt(row, column);
			EncFSProperties props = storages.get(name);
			return name + "(" + props.DecryptedFolder + ")";
		}
		case COL_ACTIVE:
		{
			boolean act = (boolean) tabModel.getValueAt(row, column);
			if(act)
				return active;
			else
				return inactive;
		}
		case COL_PASS:
		{
			boolean act = (boolean) tabModel.getValueAt(row, column);
			if(act)
				return active;
			else
				return inactive;
		}
		}
		return tabModel.getValueAt(row, column);
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		if(getRowCount() == 0)
		{
			return tabModel.getColumnClass(columnIndex);
		}
		else
		{
			return getValueAt(0, columnIndex).getClass();
		}		
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		//update the table model if something was changed.
		tabModel.setValueAt(aValue, rowIndex, columnIndex);
		//We will call the super method to update Listeners!
		super.setValueAt(aValue, rowIndex, columnIndex);
	}
}

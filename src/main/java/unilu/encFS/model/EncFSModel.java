package unilu.encFS.model;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.JOptionPane;

import unilu.encFS.exceptions.InvalidStoreName;
import unilu.encFS.misc.RequestPasswordDialog;
import unilu.encFS.wrapper.encFS.CallEncFS;
import unilu.encFS.wrapper.mounting.LinuxUnmounter;
import unilu.encFS.wrapper.mounting.MacUnmounter;

public class EncFSModel {

	private HashMap<String, EncFSProperties> storages;
	private KeyStore key;
	private boolean hasMasterKey;
	private static String keyTest = "encFSMasterKey";
	private String encodedKey = null;
	private LinkedList<ModelUpdateListeners> listeners;		
	private List<String> activeStores;
	public static String encFSSaveFile = "encfs.props";
	public EncFSModel()
	{
		storages = new HashMap<>();
		listeners = new LinkedList<>();
		key = new KeyStore();
		activeStores = new LinkedList<>();
	}

	
	public Collection<String> getStoreNames()
	{
		return storages.keySet();
	}
	
	public Collection<String> getActiveStores()
	{
		return activeStores;
	}

	public Collection<String> getInactiveStores()
	{
		LinkedList<String> inactives = new LinkedList<>();
		inactives.addAll(storages.keySet());
		inactives.removeAll(activeStores);
		return inactives;
	}
	
	private void fireModelChanged()
	{
		for(ModelUpdateListeners listener: listeners)
		{
			listener.modelUpdated();
		}
	}
	private void addStorage(String Name, String EncryptedFolder, String DecryptedFolder)
	{
		EncFSProperties props = new EncFSProperties();
		props.DecryptedFolder = DecryptedFolder;
		props.EncryptedFolder = EncryptedFolder;
		storages.put(Name, props);
		fireModelChanged();
	}

	public void createStore(String Name, String EncryptedFolder, String DecryptedFolder, boolean storePassword)
	{
		String password = RequestPasswordDialog.requestConfirmedPassword("Enter a password for the Encrypted Storage");
		if(password == null || password.length() < 8)
		{
			JOptionPane.showMessageDialog(null, "Need a password with at least length eight to process");
			return;	
		}		
		addStorage(Name, EncryptedFolder, DecryptedFolder);
		try
		{
			unlockStorage(Name,storePassword);
		}
		catch(InvalidStoreName e)
		{
			//Should not happen..getClass().
		}
		
	}
	
	public void removeStorage(String Name)
	{	
		if(activeStores.contains(Name))
		{
			lockStorage(Name);
		}
		storages.remove(Name);		
	}
	
	public void addPasswordToStore(String StoreName, String password) throws InvalidKeyException
	{		
		if(!hasMasterKey)
		{
			//Request a non empty key to be added to the
			String masterPass = RequestPasswordDialog.requestConfirmedPassword("A Masterkey is required to store passwords");
			if(masterPass != null)
			{
				key.setKey(masterPass);		
				try{
					encodedKey = encrypt(keyTest, masterPass);
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
			storages.get(StoreName).password = encrypt(password, masterKey);
		}
		catch(Exception e)
		{
			throw new InvalidKeyException("Password invalid");
		}

	}

	
	public void unlockStorage(String storeName) throws InvalidStoreName
	{
		unlockStorage(storeName,false);
	}
	
	public void unlockStorage(String storeName, boolean storePassword) throws InvalidStoreName
	{
		String password;
		if(storages.containsKey(storeName))
		{
			EncFSProperties props = storages.get(storeName);
			//check, whether its already active
			if(activeStores.contains(storeName))
			{
				//Nothing to do...
				return;
			}
			try{
				//Test, whether this is a new storage or an existing one.
				Process encFS = CallEncFS.startEncFSProcess(props.EncryptedFolder, props.DecryptedFolder);
				int openType = CallEncFS.getEncFSStatus(encFS);
				//If it is a new one, we request a confirmed password.
				if(openType == CallEncFS.NEWMOUNT)
				{				
					password = RequestPasswordDialog.requestConfirmedPassword("Please select a password for the storage. (Minimum length 8)");
					while(password != null && password.length() < 8)
					{
						password = RequestPasswordDialog.requestConfirmedPassword("The password must have at least a legth fo 8.\nPlease select a password for the storage");	
					}
					if(password != null)
					{
						//IF we got a password, 
						CallEncFS.generateNewMount(encFS, password);
					}
					else
					{
						encFS.destroy();						
					}
				}
				else
				{		
					//Otherwise we request a normal password.
					password = RequestPasswordDialog.requestPassword("Please enter the Password for the storage");
					CallEncFS.openExistingMount(encFS, password);
					encFS.waitFor();
					//regular exit
					while(encFS.exitValue() != 0)
					{						
						password = RequestPasswordDialog.requestPassword("Password Invalid, Please enter the Password for the storage");
						encFS = CallEncFS.startEncFSProcess(props.EncryptedFolder, props.DecryptedFolder);
						CallEncFS.openExistingMount(encFS, password);
						BufferedReader br = new BufferedReader(new InputStreamReader(encFS.getInputStream()));													
						encFS.waitFor();
						String message = br.readLine();
						if(message.startsWith("Error decoding volume key"))
						{
							throw new IOException("Cannot access encrypted volume. Is it already mounted externally?");
						}
					}					
				}				
			}
			catch(Exception e)
			{
				showErrorMessage(e);
				return;
			}
			activeStores.add(storeName);
			fireModelChanged();
			if(storePassword)
			{
				String masterKey = getMasterKey();
				try{
					props.password = encrypt(password, masterKey);
				}
				catch(Exception e)
				{
					showErrorMessage(e);
				}
			}			
		}
		
		else
		{
			throw new InvalidStoreName(storeName);
		}

	}	


	private String getMasterKey()
	{
		String masterKey = key.getKey();
		if(masterKey == null)
		{

			try{
				while(!keyIsValid(masterKey))
				{
					masterKey = RequestPasswordDialog.requestPassword("Enter Master Key");
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

	public void lockStorage(String StoreName)
	{
		if(!storages.containsKey(StoreName))
		{
			return;
		}
		//OS Dependent (fusermount -u for linux; umount for mac.)
		String os = System.getProperty("os.name");
		try
		{
			if(os.startsWith("Linux"))
			{
				LinuxUnmounter.unmount(storages.get(StoreName).DecryptedFolder);		
			}
			else if(os.startsWith("mac") || os.startsWith("Mac"))
			{
				MacUnmounter.unmount(storages.get(StoreName).DecryptedFolder);
			}
			activeStores.remove(StoreName);
			fireModelChanged();
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
		try{
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(encFSConfig));			
			List<Serializable> inputObjects = (List<Serializable>)ois.readObject();			
			Iterator<Serializable> loader = inputObjects.iterator();
			encodedKey = (String)loader.next();
			while(loader.hasNext())
			{
				String storeName = (String)loader.next();
				EncFSProperties props = (EncFSProperties)loader.next();
				storages.put(storeName, props);
			}
					
			fireModelChanged();
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

	private boolean keyIsValid(String key) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException
	{		
		String decryptedValidator = decrypt(encodedKey, key);
		return decryptedValidator.equals(keyTest);
	}

	private String encrypt(String plainText, String key) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException 
	{		
		SecretKeySpec skeyspec = new SecretKeySpec(key.getBytes(), "AES/CBC/PKCS5Padding");
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cipher.init(Cipher.ENCRYPT_MODE, skeyspec);
		byte[] encrypted_bytes = cipher.doFinal(plainText.getBytes());
		String encrypted = new String(encrypted_bytes);
		return encrypted;
	}

	private String decrypt(String encryptedText, String key) throws NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException, InvalidKeyException, NoSuchPaddingException 
	{		
		SecretKeySpec skeyspec = new SecretKeySpec(key.getBytes(), "AES/CBC/PKCS5Padding");
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cipher.init(Cipher.DECRYPT_MODE, skeyspec);
		byte[] decrypted_bytes = cipher.doFinal(encryptedText.getBytes());
		String decrypted = new String(decrypted_bytes);
		return decrypted;
	}

	private static void showErrorMessage(Exception e)
	{
		JOptionPane.showMessageDialog(null, "An internal error occurd:\n" + e.getMessage() ,e.getClass().getName(),JOptionPane.ERROR_MESSAGE);
	}

}

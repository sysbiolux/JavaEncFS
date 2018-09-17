package encFS.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import unilu.encFS.model.EncFSModel;

public class modelTest {

	public EncFSModel mod;
	
	@BeforeEach
    public void oneTimeSetUp() {
 
    	mod = new EncFSModel(true);
    }
 
    @AfterEach
    public void oneTimeTearDown() {
        // one-time cleanup code
    	for(String store :  mod.getStoreNames())
    	{
    		mod.lockStorage(store);
    	}
    }
    
	@Test
	void testCreateAndAccessEncFS()
	{			
		try{
			Path tempDecrypt = Files.createTempDirectory("Decrypt");
			Path tempEncrypt = Files.createTempDirectory("Encrypt");			
			mod.createStore("TestStore", tempEncrypt.toString(), tempDecrypt.toString(), false);
			File[] encFSFiles = tempEncrypt.toFile().listFiles();
			assertEquals(encFSFiles.length, 1);
			File tempFile = File.createTempFile("test", "out", tempDecrypt.toFile()); 
			BufferedWriter bw = new BufferedWriter(new FileWriter(tempFile));		
			bw.write("Some test strings");
			bw.close();
			encFSFiles = tempEncrypt.toFile().listFiles();
			assertEquals(encFSFiles.length, 2);		
			mod.lockStorage("TestStore");
		}
		catch(IOException e)
		{
			assertNotNull(null);
		}
	}
}

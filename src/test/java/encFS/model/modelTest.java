package encFS.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import unilu.encFS.exec.encFS;
import unilu.encFS.model.EncFSModel;

public class modelTest {

		
 

	@Test
	void testCreateAndAccessEncFS() throws Exception
	{		
		EncFSModel mod;
		mod = new EncFSModel(true);
		Path tempDecrypt = Files.createTempDirectory("Decrypt");
		Path tempEncrypt = Files.createTempDirectory("Encrypt");			
		System.out.println("creating Store");
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
		mod.shutDown();
	}
	@Test
	void testCreateUnmountAndMountEncFS() throws Exception
	{			
		
		EncFSModel mod;
		mod = new EncFSModel(true);
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
		//give the OS some time to update
		TimeUnit.SECONDS.sleep(2);
		mod.unlockStorage("TestStore");
		encFSFiles = tempDecrypt.toFile().listFiles();
		assertEquals(encFSFiles.length,1);
		mod.shutDown();
	}
}

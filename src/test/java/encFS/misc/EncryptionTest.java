package encFS.misc;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import unilu.encFS.misc.Encryption;

public class EncryptionTest {
	@Test
	void testEncryption()
	{
		String key = "12345";
		String testData = "Whatever works";
		try{
			String encrypted = Encryption.encrypt(testData, key);
			String Decrypted = Encryption.decrypt(encrypted, key);
			assertEquals(testData, Decrypted);
		}
		catch(Exception e)
		{
			e.printStackTrace(System.out);
			assertTrue(false);
		}
	}
	
}

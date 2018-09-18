package unilu.encFS.misc;

import java.io.ByteArrayOutputStream;
import java.security.AlgorithmParameters;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

public class Encryption {

	public static String encrypt(String plainText, String key) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException 
	{		
		try {
	        SecureRandom random = new SecureRandom();
	        byte[] salt = new byte[16];
	        random.nextBytes(salt);

	        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
	        KeySpec spec = new PBEKeySpec(key.toCharArray(), salt, 65536, 256);
	        SecretKey tmp = factory.generateSecret(spec);
	        SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");

	        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
	        cipher.init(Cipher.ENCRYPT_MODE, secret);
	        AlgorithmParameters params = cipher.getParameters();
	        byte[] iv = params.getParameterSpec(IvParameterSpec.class).getIV();
	        byte[] encryptedText = cipher.doFinal(plainText.getBytes("UTF-8"));

	        // concatenate salt + iv + ciphertext
	        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	        outputStream.write(salt);
	        outputStream.write(iv);
	        outputStream.write(encryptedText);

	        // properly encode the complete ciphertext
	        return DatatypeConverter.printBase64Binary(outputStream.toByteArray());
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return null;
	}
	

	public static String decrypt(String encryptedText, String key) throws NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException, InvalidKeyException, NoSuchPaddingException 
	{		
	    try {
	        byte[] ciphertext = DatatypeConverter.parseBase64Binary(encryptedText);
	        if (ciphertext.length < 48) {
	            return null;
	        }
	        byte[] salt = Arrays.copyOfRange(ciphertext, 0, 16);
	        byte[] iv = Arrays.copyOfRange(ciphertext, 16, 32);
	        byte[] ct = Arrays.copyOfRange(ciphertext, 32, ciphertext.length);

	        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
	        KeySpec spec = new PBEKeySpec(key.toCharArray(), salt, 65536, 256);
	        SecretKey tmp = factory.generateSecret(spec);
	        SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");
	        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

	        cipher.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(iv));
	        byte[] plaintext = cipher.doFinal(ct);

	        return new String(plaintext, "UTF-8");
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return null;
	}

}

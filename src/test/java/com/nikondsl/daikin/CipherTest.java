package com.nikondsl.daikin;

import org.junit.Test;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class CipherTest {
	
	
	
	public byte[] encryptMessage(byte[] message, byte[] keyBytes)
			throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException {
		Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
		SecretKey secretKey = new SecretKeySpec(keyBytes, "AES");
		
		cipher.init(Cipher.ENCRYPT_MODE, secretKey);
		return cipher.doFinal(message);
	}
	
	public byte[] decryptMessage(byte[] encryptedMessage, byte[] keyBytes)
			throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException,
			BadPaddingException, IllegalBlockSizeException {
		
		Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
		SecretKey secretKey = new SecretKeySpec(keyBytes, "AES");
		cipher.init(Cipher.DECRYPT_MODE, secretKey);
		return cipher.doFinal(encryptedMessage);
	}
	
	@Test
	public void test() throws Exception {
		byte[] secretKey = "secretKey0123456".getBytes("utf-8");//16/24/32 bytes
		String text = Base64.getEncoder().encodeToString(encryptMessage("this is Daikin test".getBytes("utf-8"), secretKey));
		System.err.println(""+new String(decryptMessage(Base64.getDecoder().decode(text), secretKey)));
	}
	
}

package utils;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import java.io.File;
import java.io.IOException;

public class Crypto {
	
	/**
	 * 
	 * @param buffer
	 * @throws NoSuchAlgorithmException
	 */
	public static void randomFillBuffer (byte[] buffer) throws NoSuchAlgorithmException {
		SecureRandom inst = SecureRandom.getInstance("SHA1PRNG");
		inst.nextBytes(buffer);
		
	} // randomFillBuffer ()

	/**
	 * 
	 * @param algorithm
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	public static KeyPair generateKeyPair (String algorithm) throws NoSuchAlgorithmException {
		KeyPairGenerator generator = KeyPairGenerator.getInstance(algorithm);
		return generator.generateKeyPair();
		
	} // generateKeyPair ()

	/**
	 * Method which generate an AES key from a password
	 * @param message
	 * @param salt
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 * @throws IOException
	 */
	public static SecretKey getKeyFromPBKDF (String message, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
		return getKeyFromPBKDF(message, salt, 1000, 128);
		
	} // getKeyFromPBKDF ()

	/**
	 * Method which generate an AES key from a password
	 * @param message
	 * @param salt
	 * @param iterationCount
	 * @param keyLength
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 * @throws IOException
	 */
	public static SecretKey getKeyFromPBKDF (String message, byte[] salt, int iterationCount, int keyLength) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
		SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WITHHMACSHA1");
		char[] password = Utils.readPassword(message);
		PBEKeySpec keySpec = new PBEKeySpec(password, salt, iterationCount, keyLength);
		for (int i = 0; i < password.length; i++)
			password[i] = 0;
		
		return new SecretKeySpec(factory.generateSecret(keySpec).getEncoded(), "AES");
		
	} // getKeyFromPBKDF ()

	/**
	 * 
	 * @param priv
	 * @param salt
	 * @param pub
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 * @throws IOException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 */
	public static KeyPair loadKeyPair (File priv, File salt, File pub) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException, NoSuchPaddingException, InvalidKeyException {
		KeyFactory factory = KeyFactory.getInstance("RSA");

		// Loading the public key
		PublicKey pubKey = factory.generatePublic(new X509EncodedKeySpec(Utils.readBuffer(pub)));
	
		// Loading the private key
		byte[] wrappedPrivateKey = Utils.readBuffer(priv);
		byte[] secretKeySalt = Utils.readBuffer(salt);
		SecretKey secretKey = Crypto.getKeyFromPBKDF("Enter your private key passphrase:", secretKeySalt);
		Cipher keyWrapper = Cipher.getInstance("AES");
		keyWrapper.init(Cipher.UNWRAP_MODE, secretKey);
		PrivateKey privKey = (PrivateKey) keyWrapper.unwrap(wrappedPrivateKey, "RSA", Cipher.PRIVATE_KEY);
		return new KeyPair(pubKey, privKey);
		
	} // loadKeyPair ()
	
	/**
	 * 
	 * @param pub
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 * @throws IOException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 */
	public static PublicKey loadPubKey (File pub) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException, NoSuchPaddingException, InvalidKeyException {
		KeyFactory factory = KeyFactory.getInstance("RSA");

		// Loading the public key
		return factory.generatePublic(new X509EncodedKeySpec(Utils.readBuffer(pub)));
		
	} // loadPubKey ()
	
} // Crypto

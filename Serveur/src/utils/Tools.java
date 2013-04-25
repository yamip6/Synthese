package utils;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class Tools {
	/**
	 * M�thod which permits to generate a RSA key pair
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
	 * @throws IOException
	 * @throws InvalidKeySpecException
	 */
	public static void keyGenerator () throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, IOException, InvalidKeySpecException {
		// Generation of key pair
		KeyPair keyPair = Crypto.generateKeyPair("RSA");
		PublicKey pubKey = keyPair.getPublic();
		PrivateKey privKey = keyPair.getPrivate();

		// Generating a key from a passphrase to protect the private key
		byte[] secretKeySalt = new byte[16];
		Crypto.randomFillBuffer(secretKeySalt);
		SecretKey secretKey = Crypto.getKeyFromPBKDF("Enter a pass to protect your private key: ", secretKeySalt);

		// Wrapping the private key
		Cipher keyWrapper = Cipher.getInstance("AES");
		keyWrapper.init(Cipher.WRAP_MODE, secretKey);
		byte[] wrappedPrivateKey = keyWrapper.wrap(privKey);

		// Saving keys
		Utils.saveBuffer(wrappedPrivateKey, new File("keys/private.key"));
		Utils.saveBuffer(secretKeySalt, new File("keys/private.salt.key"));
		Utils.saveBuffer(pubKey.getEncoded(), new File("keys/public.key"));
		
	} // keyGenerator ()
	
	public static byte[] tryChallenge (String username, String pass, byte[] challenge) throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchPaddingException {
		// G�n�ration des param�tres pour le chiffrement
	    byte[] key = (username + pass).getBytes(); // + sel...
	    MessageDigest sha = MessageDigest.getInstance("SHA-1");
	    key = sha.digest(key);
	    key = Arrays.copyOf(key, 16); // use only first 128 bit
		SecretKey secretKey = new SecretKeySpec(key, "AES");
							 
		// Initialisation du chiffrement
		byte[] iv = new byte[] { (byte)0xe0, 0x4f, (byte)0xd0, 0x20, (byte)0xea, 0x3a, 0x69, 0x10, (byte)0xa2, (byte)0xd8, 0x08, 0x00, 0x2b, 0x30, 0x30, (byte)0x9d };
		
		String cipherAlgorithm = "AES/CTR/NoPadding";
		Cipher cipher = Cipher.getInstance(cipherAlgorithm);
		cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));
		return cipher.doFinal(challenge);
		
	} // tryChallenge ()
	
	public static byte[] testAuth (String username, String pass, byte[] challenge) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
		// G�n�ration des param�tres pour le chiffrement
	    byte[] key = (username + pass).getBytes(); // + sel...
	    MessageDigest sha = MessageDigest.getInstance("SHA-1");
	    key = sha.digest(key);
	    key = Arrays.copyOf(key, 16); // use only first 128 bit
		SecretKey secretKey = new SecretKeySpec(key, "AES");
							 
		// Initialisation du chiffrement
		byte[] iv = new byte[] { (byte)0xe0, 0x4f, (byte)0xd0, 0x20, (byte)0xea, 0x3a, 0x69, 0x10, (byte)0xa2, (byte)0xd8, 0x08, 0x00, 0x2b, 0x30, 0x30, (byte)0x9d };
		
		String cipherAlgorithm = "AES/CTR/NoPadding";
		Cipher cipher = Cipher.getInstance(cipherAlgorithm);
		cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(iv));
		return cipher.doFinal(challenge);
		
	} // testAuth ()
	
	/**
	 * Method to determine whether a public key is already stored
	 * @param hash : Hash of the public key
	 * @return True if it is already stored, false otherwise
	 */
	public static boolean isPubKeyStored (byte[] hash) {
		if(new File("contacts/" + Utils.byteArrayToHexString(hash) + ".key").exists())
			return true;
		return false;
		
	} // isPubKeyStored ()
	
	/**
	 * Method for generating a random sequence of 16 bytes
	 * @return The byte array resulting
	 */
	public static byte[] getChallenge () throws NoSuchAlgorithmException {
		SecureRandom inst = SecureRandom.getInstance("SHA1PRNG");
		byte buffer[] = new byte[16];
		inst.nextBytes(buffer);
		return buffer;
		
	} // getChallenge ()
	
	/**
	 * Method to verify a signature SHA1WITHRSA
	 * @param data : The data you want to check
	 * @param publicKey : The public key of the sender
	 * @param signature : The signature resulting
	 * @return True if the signature is valid and false otherwise
	 */
	public static boolean verifSign (byte[] data, byte[] publicKey, byte[] signature) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, InvalidKeySpecException, NoSuchPaddingException, IOException {
		PublicKey pubKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(publicKey));
		
		String signAlgorithm = "SHA1WITHRSA";
		Signature sign = Signature.getInstance(signAlgorithm);
		sign.initVerify(pubKey);

		sign.update(data);
		
		boolean signResult = sign.verify(signature);
		if (signResult)
		    return true;
		else
		    return false;
		
	} // verifSign ()
	
	/**
	 * Method to perform a MD5 hash of a byte array
	 * @param message : Byte array that you want to hash
	 * @return The byte array hashed
	 */
	public static byte[] hash (byte[] message) throws NoSuchAlgorithmException, IOException {
		MessageDigest digest = MessageDigest.getInstance("MD5");
		digest.update(message);
	
		return digest.digest();
		
	} // hash ()
	
	/**
	 * Method to perform a MD5 hash of a file
	 * @param pathname : File path to hash
	 * @return The byte array hashed
	 */
	public static byte[] hashFile (String pathname) throws NoSuchAlgorithmException, IOException {
		File inputFile = new File(pathname);
		InputStream fis = new FileInputStream(inputFile);
		byte buffer[] = new byte[16384];
		int readSize;
		MessageDigest digest = MessageDigest.getInstance("MD5");
		while ((readSize = fis.read(buffer)) > 0) {
			digest.update(buffer, 0, readSize);
		}
		fis.close();
	
		return digest.digest();
		
	} // hashFile ()
	
	/**
	 * Method to sign with the algorithm SHA1WITHRSA a byte array
	 * @param privateKey : Private key used for the signature
	 * @param message : Byte array to sign
	 * @return The signature of message
	 */
	public static byte[] sign (PrivateKey privateKey, byte[] message) throws IOException, SignatureException, NoSuchAlgorithmException, InvalidKeyException {
		// Signature initialization
		String signAlgorithm = "SHA1WITHRSA";
		Signature sign = Signature.getInstance(signAlgorithm);
		sign.initSign(privateKey);
	
		sign.update(message);

		return sign.sign();
		
	} // sign ()
	
	/**
	 * M�thode permettant d'effectuer un d�chiffrement asym�trique RSA
	 * @param data : Donn�es que l'on souhaite d�chiffrer
	 * @param privKey : Clef priv�e utilis�e pour le d�chiffrement
	 * @return Le tableau d'octets correspondant au message clair
	 */
	public static byte[] decrypt(byte[] data, PrivateKey privKey) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, SignatureException, IllegalBlockSizeException, BadPaddingException {
		// Initialisation du chiffrement
		String cipherAlgorithm = "RSA";
		Cipher cipher = Cipher.getInstance(cipherAlgorithm);
		
		cipher = Cipher.getInstance(cipherAlgorithm);
		cipher.init(Cipher.DECRYPT_MODE, privKey);
		cipher.update(data);

		return cipher.doFinal();
		
	} // decrypt ()

} // Tools

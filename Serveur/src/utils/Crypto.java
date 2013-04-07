/**
 * Classe définissant les utilitaires cryptographique de base de l'application.
 */

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
	public static void randomFillBuffer(byte[] buffer) throws NoSuchAlgorithmException {
		SecureRandom inst = SecureRandom.getInstance("SHA1PRNG");
		inst.nextBytes(buffer);
	} // randomFillBuffer()

	public static KeyPair generateKeyPair(String algorithm) throws NoSuchAlgorithmException {
		KeyPairGenerator generator = KeyPairGenerator.getInstance(algorithm);
		return generator.generateKeyPair();
	} // generateKeyPair()

	/** Generate an AES key from a password */
	public static SecretKey getKeyFromPBKDF(String message, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
		return getKeyFromPBKDF(message, salt, 1000, 128);
	} // getKeyFromPBKDF()

	/** Generate an AES key from a password */
	public static SecretKey getKeyFromPBKDF(String message, byte[] salt, int iterationCount, int keyLength) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
		SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WITHHMACSHA1");
		char[] password = Utils.readPassword(message);
		PBEKeySpec keySpec = new PBEKeySpec(password, salt, iterationCount, keyLength);
		for (int i = 0; i < password.length; i++)
			password[i] = 0;
		
		return new SecretKeySpec(factory.generateSecret(keySpec).getEncoded(), "AES");
	} // getKeyFromPBKDF()

	public static KeyPair loadKeyPair(File priv, File salt, File pub) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException, NoSuchPaddingException, InvalidKeyException {
		KeyFactory factory = KeyFactory.getInstance("RSA");

		// Chargement de la clé publique
		PublicKey pubKey = factory.generatePublic(new X509EncodedKeySpec(Utils.readBuffer(pub)));
	
		// Chargement de la clé privée
		byte[] wrappedPrivateKey = Utils.readBuffer(priv);
		byte[] secretKeySalt = Utils.readBuffer(salt);
		SecretKey secretKey = Crypto.getKeyFromPBKDF("Entrez votre passphrase de clef privée :", secretKeySalt);
		Cipher keyWrapper = Cipher.getInstance("AES");
		keyWrapper.init(Cipher.UNWRAP_MODE, secretKey);
		PrivateKey privKey = (PrivateKey) keyWrapper.unwrap(wrappedPrivateKey, "RSA", Cipher.PRIVATE_KEY);
		return new KeyPair(pubKey, privKey);
	} // loadKeyPair()
	
	public static PublicKey loadPubKey(File pub) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException, NoSuchPaddingException, InvalidKeyException {
		KeyFactory factory = KeyFactory.getInstance("RSA");

		// Chargement de la clé publique
		return factory.generatePublic(new X509EncodedKeySpec(Utils.readBuffer(pub)));
	} // loadKeyPair()
	
} // Crypto

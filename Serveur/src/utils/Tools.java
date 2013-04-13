/**
 * Classe contenant les outils utilis�s par l'application
 * @author Benjamin Gastaldi
 * @version 1.0 
 */

package utils;

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

import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

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
	public static void keyGenerator() throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, IOException, InvalidKeySpecException {
		// G�n�ration de la pair de clefs
		KeyPair keyPair = Crypto.generateKeyPair("RSA");
		PublicKey pubKey = keyPair.getPublic();
		PrivateKey privKey = keyPair.getPrivate();

		// G�n�ration d'une clef � partir d'une passphrase pour prot�ger la cl� priv�e
		byte[] secretKeySalt = new byte[16];
		Crypto.randomFillBuffer(secretKeySalt);
		SecretKey secretKey = Crypto.getKeyFromPBKDF("Enter a pass to protect your private key : ", secretKeySalt);

		// Encapsulation de la clef priv�e
		Cipher keyWrapper = Cipher.getInstance("AES");
		keyWrapper.init(Cipher.WRAP_MODE, secretKey);
		byte[] wrappedPrivateKey = keyWrapper.wrap(privKey);

		// Sauvegarde des clefs
		Utils.saveBuffer(wrappedPrivateKey, new File("keys/private.key"));
		Utils.saveBuffer(secretKeySalt, new File("keys/private.salt.key"));
		Utils.saveBuffer(pubKey.getEncoded(), new File("keys/public.key"));
		
	} // keyGenerator()
	
	/**
	 * M�thode permettant de d�terminer si une clef publique est d�j� enregistr�e
	 * @param hash : Hash de la clef publique
	 * @param side : Partie concern�e (client ou serveur)
	 * @return Vrai si elle est d�j� stock�e et faux sinon
	 */
	public static boolean isPubKeyStored(byte[] hash) {
		if(new File("contacts/" + Utils.byteArrayToHexString(hash) + ".key").exists())
			return true;
		return false;
		
	} // verifHash()
	
	/**
	 * M�thode permettant de g�n�rer une suite de 16 octets al�atoire
	 * @return Le tableau d'octets obtenu
	 */
	public static byte[] getChallenge() throws NoSuchAlgorithmException {
		SecureRandom inst = SecureRandom.getInstance("SHA1PRNG");
		byte buffer[] = new byte[16];
		inst.nextBytes(buffer);
		return buffer;
		
	} // getChallenge()
	
	/**
     * M�thode permettant de concat�ner 2 tableaux d'octets
     * @param a : Premier tableau d'octets
     * @param b : Second tableau d'octets
     * @return Le tableau d'octets compos� de a suivit de b
     */
	public static byte[] concatenateByteArray(byte[] a, byte[] b) {
		byte[] c = new byte[a.length + b.length];
		System.arraycopy(a, 0, c, 0, a.length);
		System.arraycopy(b, 0, c, a.length, b.length);
		return c;
		
	} // concatenateByteArray()	
	
	/**
	 * M�thode permettant de v�rifier une signature SHA1WITHRSA
	 * @param data : Les donn�es que l'on souhaite v�rifier
	 * @param publicKey : La clef publique de l'emetteur
	 * @param signature : La signature obtenue
	 * @return Vrai si la signature est valide et faux sinon
	 */
	public static boolean verifSign(byte[] data, byte[] publicKey, byte[] signature) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, InvalidKeySpecException, NoSuchPaddingException, IOException {
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
		
	} // verifSign()
	
	/**
	 * M�thode permettant d'effectuer un hash MD5 d'un tableau d'octets
	 * @param message : Tableau d'octets que l'on souhaite hasher
	 * @return Le tableau d'octets hash�
	 */
	public static byte[] hash(byte[] message) throws NoSuchAlgorithmException, IOException {
		MessageDigest digest = MessageDigest.getInstance("MD5");
		digest.update(message);
	
		return digest.digest();
		
	} // hashByteArray()
	
	/**
	 * M�thode permettant d'effectuer un hash MD5 d'un fichier
	 * @param pathname : Chemin du fichier � hasher
	 * @return Le tableau d'octets hash�
	 */
	public static byte[] hashFile(String pathname) throws NoSuchAlgorithmException, IOException {
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
		
	} // hashFile()
	
	/**
	 * M�thode permettant de signer avec l'algorithme SHA1WITHRSA un tableau d'octets
	 * @param privateKey : Clef priv�e utilis�e pour la signature
	 * @param message : Tableau d'octets � signer
	 * @return La signature du message
	 */
	public static byte[] sign(PrivateKey privateKey, byte[] message) throws IOException, SignatureException, NoSuchAlgorithmException, InvalidKeyException {
		// Initialisation de la signature
		String signAlgorithm = "SHA1WITHRSA";
		Signature sign = Signature.getInstance(signAlgorithm);
		sign.initSign(privateKey);
	
		sign.update(message);

		return sign.sign();
		
	} // sign()

} // Tools

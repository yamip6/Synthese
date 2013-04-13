/**
 * Classe contenant les outils utilisés par l'application
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
	 * Méthod which permits to generate a RSA key pair
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
	 * @throws IOException
	 * @throws InvalidKeySpecException
	 */
	public static void keyGenerator() throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, IOException, InvalidKeySpecException {
		// Génération de la pair de clefs
		KeyPair keyPair = Crypto.generateKeyPair("RSA");
		PublicKey pubKey = keyPair.getPublic();
		PrivateKey privKey = keyPair.getPrivate();

		// Génération d'une clef à partir d'une passphrase pour protéger la clé privée
		byte[] secretKeySalt = new byte[16];
		Crypto.randomFillBuffer(secretKeySalt);
		SecretKey secretKey = Crypto.getKeyFromPBKDF("Enter a pass to protect your private key : ", secretKeySalt);

		// Encapsulation de la clef privée
		Cipher keyWrapper = Cipher.getInstance("AES");
		keyWrapper.init(Cipher.WRAP_MODE, secretKey);
		byte[] wrappedPrivateKey = keyWrapper.wrap(privKey);

		// Sauvegarde des clefs
		Utils.saveBuffer(wrappedPrivateKey, new File("keys/private.key"));
		Utils.saveBuffer(secretKeySalt, new File("keys/private.salt.key"));
		Utils.saveBuffer(pubKey.getEncoded(), new File("keys/public.key"));
		
	} // keyGenerator()
	
	/**
	 * Méthode permettant de déterminer si une clef publique est déjà enregistrée
	 * @param hash : Hash de la clef publique
	 * @param side : Partie concernée (client ou serveur)
	 * @return Vrai si elle est déjà stockée et faux sinon
	 */
	public static boolean isPubKeyStored(byte[] hash) {
		if(new File("contacts/" + Utils.byteArrayToHexString(hash) + ".key").exists())
			return true;
		return false;
		
	} // verifHash()
	
	/**
	 * Méthode permettant de générer une suite de 16 octets aléatoire
	 * @return Le tableau d'octets obtenu
	 */
	public static byte[] getChallenge() throws NoSuchAlgorithmException {
		SecureRandom inst = SecureRandom.getInstance("SHA1PRNG");
		byte buffer[] = new byte[16];
		inst.nextBytes(buffer);
		return buffer;
		
	} // getChallenge()
	
	/**
     * Méthode permettant de concaténer 2 tableaux d'octets
     * @param a : Premier tableau d'octets
     * @param b : Second tableau d'octets
     * @return Le tableau d'octets composé de a suivit de b
     */
	public static byte[] concatenateByteArray(byte[] a, byte[] b) {
		byte[] c = new byte[a.length + b.length];
		System.arraycopy(a, 0, c, 0, a.length);
		System.arraycopy(b, 0, c, a.length, b.length);
		return c;
		
	} // concatenateByteArray()	
	
	/**
	 * Méthode permettant de vérifier une signature SHA1WITHRSA
	 * @param data : Les données que l'on souhaite vérifier
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
	 * Méthode permettant d'effectuer un hash MD5 d'un tableau d'octets
	 * @param message : Tableau d'octets que l'on souhaite hasher
	 * @return Le tableau d'octets hashé
	 */
	public static byte[] hash(byte[] message) throws NoSuchAlgorithmException, IOException {
		MessageDigest digest = MessageDigest.getInstance("MD5");
		digest.update(message);
	
		return digest.digest();
		
	} // hashByteArray()
	
	/**
	 * Méthode permettant d'effectuer un hash MD5 d'un fichier
	 * @param pathname : Chemin du fichier à hasher
	 * @return Le tableau d'octets hashé
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
	 * Méthode permettant de signer avec l'algorithme SHA1WITHRSA un tableau d'octets
	 * @param privateKey : Clef privée utilisée pour la signature
	 * @param message : Tableau d'octets à signer
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

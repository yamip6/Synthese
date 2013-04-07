/**
 * Classe contenant les outils utilisés par l'application
 * @author Benjamin Gastaldi
 * @version 1.0 
 */

package utils;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import java.io.File;
import java.io.IOException;

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

} // Tools

/**
 * Classe contenant les outils utilis�s par l'application
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
	 * M�thode permettant de g�n�rer une paire de clef RSA
	 * @param side : C�t� pour lequel on veut g�n�rer cette paire de clef (client ou serveur)
	 */
	public static void keyGenerator() throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, IOException, InvalidKeySpecException {
		// G�n�ration de la pair de clefs
		KeyPair keyPair = Crypto.generateKeyPair("RSA");
		PublicKey pubKey = keyPair.getPublic();
		PrivateKey privKey = keyPair.getPrivate();

		// G�n�ration d'une clef � partir d'une passphrase pour prot�ger la cl� priv�e
		byte[] secretKeySalt = new byte[16];
		Crypto.randomFillBuffer(secretKeySalt);
		SecretKey secretKey = Crypto.getKeyFromPBKDF("Entrez votre passphrase pour prot�ger votre clef priv�e : ", secretKeySalt);

		// Encapsulation de la clef priv�e
		Cipher keyWrapper = Cipher.getInstance("AES");
		keyWrapper.init(Cipher.WRAP_MODE, secretKey);
		byte[] wrappedPrivateKey = keyWrapper.wrap(privKey);

		// Sauvegarde des clefs
		Utils.saveBuffer(wrappedPrivateKey, new File("keys/private.key"));
		Utils.saveBuffer(secretKeySalt, new File("keys/private.salt.key"));
		Utils.saveBuffer(pubKey.getEncoded(), new File("keys/public.key"));
		
	} // keyGenerator()

} // Tools

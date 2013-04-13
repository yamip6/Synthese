package utils;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Console;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import client.ihm.InviteKeyGenerator;

public class Utils {
	/**
	 * 
	 * @param buffer
	 * @return
	 */
	public static String byteArrayToHexString(byte[] buffer) {
		String hex = "";
		if (buffer.length == 0)
			return hex;

		for (int i = 0; i < buffer.length; i++)
			hex += String.format("%02x", Byte.valueOf(buffer[i]));

		return hex;	
	} // byteArrayToHexString()
	
	/**
	 * M�thode permettant de convertir un int en tableau d'octets
	 * @param value : Valeur enti�re que l'on souhaite convertir
	 * @param octet : Nombre d'octets du tableau d'octets r�sultat
	 * @return Le tableau d'octets
	 */
    public static final byte[] intToByteArray(int value, int octet) {
    	switch(octet) {
    	    case 4 :
    	    	return new byte[] {(byte)(value >>> 24), (byte)(value >> 16 & 0xff), (byte)(value >> 8 & 0xff), (byte)(value & 0xff)};
    	    case 2 :
    	    	return new byte[] {(byte)(value >> 8 & 0xff), (byte)(value & 0xff)};
    	    case 1 :
    	    	return new byte[] {(byte)(value & 0xff)};
    	    default :
    	    	return null;
    	}
    	
    } // intToByteArray()
    
    /**
     * M�thode permettant de convertir un tableau d'octets en valeur enti�re
     * @param b : Tableau d'octets � convertir
     * @return Entier correspondant
     */
    public static final int byteArrayToInt(byte[] b) {
        int value = 0;   
        for(int i = 0; i < b.length; i++) {
            value = value << 8;
            value += b[i] & 0xff;
        }
        return value;
        
    } // byteArrayToInt()
    
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
	 * This function changes an arrayList of String to an array of bytes
	 * @param l
	 * @return array of bytes
	 * @throws IOException
	 */
	public static byte[] arrayListToByte(ArrayList<String> l) throws IOException{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(baos);
		for (String element : l) {
		    out.writeUTF(element);
		}
		return baos.toByteArray();
	} // arrayListToByte()
	
	public static ArrayList<String> arrayByteToList (byte[] l) throws IOException {
		ByteArrayInputStream bais = new ByteArrayInputStream(l);
		DataInputStream in = new DataInputStream(bais);
		ArrayList<String> out = new ArrayList<String>();
		while (in.available() > 0) {
		    String element = in.readUTF();
		    out.add(element);
		}
		return out;
	} // arrayByteToList ()
	
	public static void saveBuffer(byte[] buffer, File file) throws IOException {
		OutputStream os = new FileOutputStream(file);
		os.write(buffer);
		os.close();
	} // saveBuffer()
	
	public static byte[] readBuffer(File file) throws IOException {
		InputStream is = new FileInputStream(file);
		byte result[] = new byte[(int) file.length()];
		is.read(result);
		is.close();
		return result;	
	} // readBuffer()
	
	public static char[] readPassword(String message) throws IOException {

		Console con = System.console();
		if (con != null)
			return con.readPassword(message);
		else {
			System.out.println(message);
			return new BufferedReader(new InputStreamReader(System.in)).readLine().toCharArray();
		}
		
	} // readPassword()
	
} // ManipString

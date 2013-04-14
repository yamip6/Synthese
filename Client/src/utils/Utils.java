package utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.ArrayList;

import javax.swing.JOptionPane;

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
	 * Method which convert int to byte array
	 * @param value : Integer value that you want to convert
	 * @param octet : Number of bytes in the byte array result
	 * @return The byte array
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
     * Method to convert a byte array to an integer value
     * @param b : Byte array to convert
     * @return Integer corresponding
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
     * Method to concatenate 2 byte arrays
     * @param a : First byte array
     * @param b : Second byte array
     * @return The byte array composed of a followed by b
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
	public static byte[] arrayListToByteArray(ArrayList<String> l) throws IOException{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(baos);
		for (String element : l) {
		    out.writeUTF(element);
		}
		return baos.toByteArray();
		
	} // arrayListToByte()
	
	/**
	 * 
	 * @param l
	 * @return
	 * @throws IOException
	 */
	public static ArrayList<String> byteArrayToList (byte[] l) throws IOException {
		ByteArrayInputStream bais = new ByteArrayInputStream(l);
		DataInputStream in = new DataInputStream(bais);
		ArrayList<String> out = new ArrayList<String>();
		while (in.available() > 0) {
		    String element = in.readUTF();
		    out.add(element);
		}
		return out;
		
	} // byteArrayToList()
	
	/**
	 * 
	 * @param buffer
	 * @param file
	 * @throws IOException
	 */
	public static void saveBuffer(byte[] buffer, File file) throws IOException {
		OutputStream os = new FileOutputStream(file);
		os.write(buffer);
		os.close();
		
	} // saveBuffer()
	
	/**
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static byte[] readBuffer(File file) throws IOException {
		InputStream is = new FileInputStream(file);
		byte result[] = new byte[(int) file.length()];
		is.read(result);
		is.close();
		return result;

	} // readBuffer()
	
	/**
	 * 
	 * @param message
	 * @return
	 * @throws IOException
	 */
	public static char[] readPassword(String message) throws IOException {
		String pass = JOptionPane.showInputDialog(null, message, "Asking pass", JOptionPane.QUESTION_MESSAGE);
		return pass.toCharArray();
		
	} // readPassword()
	
} // Utils

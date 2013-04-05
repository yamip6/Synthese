package utils;

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
	
} // ManipString

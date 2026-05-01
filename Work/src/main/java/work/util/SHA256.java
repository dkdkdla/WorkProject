package work.util;

import java.security.MessageDigest;

public class SHA256 {
    
    
    
    public static String encrypt(String rawPassword) {
        try {
            
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            
            
            md.update(rawPassword.getBytes());
            byte[] byteData = md.digest();
            
            
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < byteData.length; i++) {
                sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
            }
            
            return sb.toString(); 
            
        } catch (Exception e) {
            e.printStackTrace();
            
            return null;
        }
    }
}
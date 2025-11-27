package com.sorf.syd.util;

import com.sorf.syd.Main;
import javafx.util.Pair;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.security.Key;
import java.security.MessageDigest;
import java.security.spec.InvalidKeySpecException;

public class HWIDUtil {

    public static final String HWID = getHWID();
    public static final SecretKey KEY;
    public static final IvParameterSpec IV;

    static {
        SecretKey KEY1;
        try {
            KEY1 = Main.KEY_MANAGER.generateKey(HWID);
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
            KEY1 = null;
        }
        KEY = KEY1;


        byte[] iv = new byte[16];
        System.arraycopy(HWID.getBytes(), 0, iv, 0, 16);
        IV = new IvParameterSpec(iv);
    }

    /**
     *
     * @return HWID in MD5;
     *
     */
    public static String getHWID() {
        try{
            String toEncrypt =  System.getenv("COMPUTERNAME") + System.getProperty("user.name") + System.getenv("PROCESSOR_IDENTIFIER") + System.getenv("PROCESSOR_LEVEL");
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(toEncrypt.getBytes());
            StringBuffer hexString = new StringBuffer();

            byte byteData[] = md.digest();

            for (byte aByteData : byteData) {
                String hex = Integer.toHexString(0xff & aByteData);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (Exception e) {
            Logger.error("Caught exception during getting HWID: %s", e.getLocalizedMessage());
        }
        return "bro its broken what can i do!";
    }
}

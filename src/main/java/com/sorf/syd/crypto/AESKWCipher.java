package com.sorf.syd.crypto;

import com.sorf.syd.Main;
import com.sorf.syd.util.Logger;
import org.jetbrains.annotations.NotNull;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.util.Base64;

public class AESKWCipher implements IKeyWrapping {
    private static AESKWCipher instanceAES = new AESKWCipher("AES");
    private static AESKWCipher instanceECC = new AESKWCipher("ECC");
    private final String algorithm;
    private Cipher cipher;

    private AESKWCipher(String algorithm) {
        try {
            this.cipher = Cipher.getInstance("AES/GCM/NoPadding", "BC");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | NoSuchProviderException e) {
            Logger.error("Can not create AESKW instance!");
            e.printStackTrace();
            Main.forceStop();
        } finally {
            this.algorithm = algorithm;
        }
    }

    //not tested
    public static AESKWCipher getInstanceAES() {
        return instanceAES;
    }

    public static AESKWCipher getInstanceECC() {
        return instanceECC;
    }

    public byte[] encrypt(@NotNull Key keyToWrap, @NotNull Key key) throws IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, InvalidKeyException {
        this.cipher.init(Cipher.WRAP_MODE, key, new GCMParameterSpec(128, new byte[12]));
        return this.cipher.wrap(keyToWrap);
    }

    public Key decrypt(byte[] bytes, Key key) throws InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException {
        this.cipher.init(Cipher.UNWRAP_MODE, key, new GCMParameterSpec(128, new byte[12]));
        if (this.algorithm.equals("AES")) {
            return this.cipher.unwrap(bytes, "AES", Cipher.SECRET_KEY);
        } else {
            return this.cipher.unwrap(bytes, "EC", Cipher.PRIVATE_KEY);
        }

    }

    public byte[] encryptWithBase64(@NotNull Key keyToWrap, @NotNull Key key) throws InvalidKeyException, IllegalBlockSizeException, InvalidAlgorithmParameterException {
        this.cipher.init(Cipher.WRAP_MODE, key, new GCMParameterSpec(128, new byte[12]));
        byte[] result = this.cipher.wrap(keyToWrap);
        return Base64.getEncoder().encode(result);
    }

    public Key decryptWithBase64(byte[] bytes, Key key) throws InvalidKeyException, NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        this.cipher.init(Cipher.UNWRAP_MODE, key, new GCMParameterSpec(128, new byte[12]));
        if (this.algorithm.equals("AES")) {
            return this.cipher.unwrap(Base64.getDecoder().decode(bytes), "AES", Cipher.SECRET_KEY);
        } else {
            return this.cipher.unwrap(Base64.getDecoder().decode(bytes), "EC", Cipher.PRIVATE_KEY);
        }
    }

}
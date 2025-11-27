package com.sorf.syd.crypto;

import com.sorf.syd.Main;
import com.sorf.syd.util.Logger;
import org.jetbrains.annotations.NotNull;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class AESCipher extends AbstractInstance{
    private static AESCipher instance = new AESCipher();
    private Cipher cipher;

    //PADDING, USE PADDING OR DIE
    //No more padding, thx BouncyCastle ;)
    private AESCipher() {
        try {
            this.cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            Logger.error("Can not create AESCipher instance!");
            e.printStackTrace();
            Main.forceStop();
        }
    }

    public static AESCipher getInstance() {
        return instance;
    }

    @Override
    public byte[] encrypt(byte[] bytes, Key key, IvParameterSpec iv) throws IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, InvalidKeyException {
        this.cipher.init(Cipher.ENCRYPT_MODE, key, iv);
        return this.cipher.doFinal(bytes);
    }

    @Override
    public byte[] decrypt(byte[] bytes, Key key, IvParameterSpec iv) throws InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        this.cipher.init(Cipher.DECRYPT_MODE, key, iv);
        return this.cipher.doFinal(bytes);
    }

    @Override
    public byte[] encryptWithBase64(byte @NotNull [] bytes, @NotNull Key key, @NotNull IvParameterSpec iv) throws InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        this.cipher.init(Cipher.ENCRYPT_MODE, key, iv);
        byte[] result = this.cipher.doFinal(bytes);
        return Base64.getEncoder().encode(result);
    }

    @Override
    public byte[] decryptWithBase64(byte[] bytes, Key key, IvParameterSpec iv) throws InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        this.cipher.init(Cipher.DECRYPT_MODE, key, iv);
        return this.cipher.doFinal(Base64.getDecoder().decode(bytes));
    }
}

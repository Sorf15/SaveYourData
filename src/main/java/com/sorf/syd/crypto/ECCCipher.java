package com.sorf.syd.crypto;

import com.sorf.syd.Main;
import com.sorf.syd.util.Logger;
import org.bouncycastle.jce.interfaces.ECPrivateKey;
import org.bouncycastle.jce.spec.ECPublicKeySpec;
import org.jetbrains.annotations.NotNull;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

public class ECCCipher extends AbstractInstance{
    private static ECCCipher instance = new ECCCipher();
    private Cipher cipher;


    //No need of padding, cuz we're using ECIES, not ECC
    private ECCCipher() {
        try {
            this.cipher = Cipher.getInstance("ECIES", "BC");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | NoSuchProviderException e) {
            Logger.error("Can not create CryptoUtil instance!");
            e.printStackTrace();
            Main.forceStop();
        }
    }

    public static ECCCipher getInstance() {
        return instance;
    }

    @Override
    public byte[] encrypt(byte @NotNull [] bytes, @NotNull Key key, IvParameterSpec iv) throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        this.cipher.init(Cipher.ENCRYPT_MODE, (PublicKey) key);
        return this.cipher.doFinal(bytes);
    }

    @Override
    public byte[] decrypt(byte @NotNull [] bytes, @NotNull Key key, IvParameterSpec iv) throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        this.cipher.init(Cipher.DECRYPT_MODE, (PrivateKey) key);
        return this.cipher.doFinal(bytes);
    }

    @Override
    public byte[] encryptWithBase64(byte @NotNull [] bytes, @NotNull Key key, IvParameterSpec iv) throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        this.cipher.init(Cipher.ENCRYPT_MODE, (PublicKey) key);
        byte[] result = this.cipher.doFinal(bytes);
        return Base64.getEncoder().encode(result);
    }

    @Override
    public byte[] decryptWithBase64(byte[] bytes, Key key, IvParameterSpec iv) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        this.cipher.init(Cipher.DECRYPT_MODE, (PrivateKey) key);
        return this.cipher.doFinal(Base64.getDecoder().decode(bytes));
    }

    public static PublicKey getPublicKeyFromPrivateKey(PrivateKey privateKey1) throws NoSuchAlgorithmException, InvalidKeySpecException {
        ECPrivateKey privateKey = (ECPrivateKey) privateKey1;
        return KeyFactory.getInstance("EC").generatePublic(new ECPublicKeySpec(privateKey.getParameters().getG().multiply(privateKey.getD()), privateKey.getParameters()));
    }
}

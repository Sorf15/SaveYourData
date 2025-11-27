package com.sorf.syd.user;

import com.sorf.syd.Main;
import com.sorf.syd.crypto.AESKWCipher;
import com.sorf.syd.crypto.ECCCipher;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

public class StorageKey {
    private final PublicKey publicKey;
    private byte[] encodedKey;
    protected StorageKey(String key) throws InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException,
            InvalidKeyException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchProviderException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("ECIES", "BC");
        keyPairGenerator.initialize(256);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        this.publicKey = keyPair.getPublic();

        SecretKey keyToEncrypt = Main.KEY_MANAGER.generateKey(key);
        this.encodedKey = AESKWCipher.getInstanceECC().encrypt(keyPair.getPrivate(), keyToEncrypt);
    }

    protected StorageKey(String key, byte[] encryptedKey) throws InvalidKeySpecException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        SecretKey keyToDecrypt = Main.KEY_MANAGER.generateKey(key);
        this.encodedKey = encryptedKey;
        PrivateKey tempKey = (PrivateKey) AESKWCipher.getInstanceECC().decrypt(this.encodedKey, keyToDecrypt);
        this.publicKey = ECCCipher.getPublicKeyFromPrivateKey(tempKey);
    }

    public byte[] getEncodedKey() {
        return encodedKey;
    }

    public PublicKey getPublicKey() {
        return this.publicKey;
    }

    public void changePrivateKey(String oldKey, String newKey) throws InvalidKeySpecException, InvalidAlgorithmParameterException,
            IllegalBlockSizeException, BadPaddingException, InvalidKeyException, NoSuchAlgorithmException {

        SecretKey keyToDecrypt = Main.KEY_MANAGER.generateKey(oldKey);
        PrivateKey privateKey = (PrivateKey) AESKWCipher.getInstanceECC().decrypt(this.encodedKey, keyToDecrypt);
        SecretKey keyToEncrypt = Main.KEY_MANAGER.generateKey(newKey);
        this.encodedKey = AESKWCipher.getInstanceECC().encrypt(privateKey, keyToEncrypt);
    }
}

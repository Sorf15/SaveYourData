package com.sorf.syd.crypto;

import org.jetbrains.annotations.NotNull;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Random;

public class KeyManager {
    private final @NotNull SecretKeyFactory keyGenerator;
    private final @NotNull KeyGenerator randomKeyGenerator;

    public KeyManager() throws NoSuchAlgorithmException {
        this.keyGenerator = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
        this.randomKeyGenerator = KeyGenerator.getInstance("AES");
    }


    //TODO: Refactor code: use appropriate Key e.g. SecretKey, PrivateKey, ECPrivateKey
    public @NotNull SecretKey generateKey(@NotNull String password) throws InvalidKeySpecException {
        KeySpec spec = new PBEKeySpec(password.toCharArray(), password.getBytes(StandardCharsets.UTF_16BE), 65536, 256);
        return new SecretKeySpec(this.keyGenerator.generateSecret(spec).getEncoded(), "AES");
    }

    /**
     * @implNote USE UTF-16BE
     */
    public @NotNull SecretKey generateKey(byte @NotNull [] password) throws InvalidKeySpecException {
        KeySpec spec = new PBEKeySpec(new String(password, StandardCharsets.UTF_16BE).toCharArray(), password, 65536, 256);
        return new SecretKeySpec(this.keyGenerator.generateSecret(spec).getEncoded(), "AES");
    }

    public @NotNull SecretKey generateKey(@NotNull String password, byte @NotNull [] salt) throws InvalidKeySpecException {
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 256);
        return new SecretKeySpec(this.keyGenerator.generateSecret(spec).getEncoded(), "AES");
    }

    /**
     * @implNote USE UTF-16BE
     */
    public @NotNull SecretKey generateKey(byte @NotNull [] password, byte @NotNull [] salt) throws InvalidKeySpecException {
        KeySpec spec = new PBEKeySpec(new String(password, StandardCharsets.UTF_16BE).toCharArray(), salt, 65536, 256);
        return new SecretKeySpec(this.keyGenerator.generateSecret(spec).getEncoded(), "AES");
    }

    public @NotNull SecretKey generateKey() {
        return this.randomKeyGenerator.generateKey();
    }

    public static IvParameterSpec generateIV(byte @NotNull [] key) {
        byte[] iv = new byte[16];
        new Random(ByteBuffer.wrap(key).getLong()).nextBytes(iv);
        return new IvParameterSpec(iv);
    }
}

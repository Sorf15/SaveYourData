package com.sorf.syd.user;

import com.sorf.syd.Main;
import com.sorf.syd.crypto.AESCipher;
import com.sorf.syd.crypto.ECCCipher;
import com.sorf.syd.crypto.KeyManager;
import com.sorf.syd.gui.ShadowPassword;
import com.sorf.syd.token.Token;
import com.sorf.syd.util.Logger;
import com.sorf.syd.util.ArraySplitter;
import com.sorf.syd.util.UUIDUtil;
import com.sorf.syd.util.event.Event;
import com.sorf.syd.util.event.PasswordEvent;
import com.sorf.syd.util.event.UpdateScreenEvent;
import org.apache.commons.lang3.ArrayUtils;
import org.bouncycastle.crypto.prng.FixedSecureRandom;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.*;

public class PasswordStorage {

    private HashMap<UUID, Password> passwords = new HashMap<>();
    private byte[] salt = new byte[16];
    private final User user;
    private PublicKey publicKey = null;
    private PrivateKey privateKey = null;

    protected PasswordStorage(User user, String key) {
        this.user = user;
        Random rand = new Random(ByteBuffer.wrap(key.getBytes()).getLong());
        rand.nextBytes(this.salt);

        try {
            this.publicKey = passwordDerivation(key).getPublic();
        } catch (Exception e) {
            Logger.error("Caught exception %s in PassStorage: %s", e.getClass().getSimpleName(), e.getMessage());
            e.printStackTrace();
        }
    }

    protected PasswordStorage(User user, byte @NotNull [] storage, String key) {
        this.user = user;
        Random rand = new Random(ByteBuffer.wrap(key.getBytes()).getLong());
        rand.nextBytes(this.salt);

        try {
            KeyPair keyPair = passwordDerivation(key);
            this.publicKey = keyPair.getPublic();
            this.privateKey = keyPair.getPrivate();

            byte[] intermediate = ECCCipher.getInstance().decrypt(storage, this.privateKey, null);
            ArraySplitter.splitArray(intermediate, (byte) 126, false).forEach(e -> {
                Password password = Password.getFromBytes(e);
                this.passwords.put(password.uuid, password);
            });
            this.privateKey = null;
        } catch (Exception e) {
            Logger.error("Caught exception %s in PassStorage: %s", e.getClass().getSimpleName(), e.getMessage());
            e.printStackTrace();
        }
    }

    protected byte @NotNull [] save() throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        byte[] result = new byte[0];
        for (Password password : this.passwords.values()) {
            result = ArrayUtils.addAll(result, password.toBytes());
            result = ArrayUtils.add(result, (byte) 126);
        }
        return ECCCipher.getInstance().encrypt(result, this.publicKey, null);
    }

    private KeyPair passwordDerivation(String key) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException,
            NoSuchProviderException, InvalidKeySpecException {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(key.toCharArray(), this.salt, 65536, 256);
        SecretKey secretKey = factory.generateSecret(spec);

        // Use the derived key to generate an ECC key pair
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC", "BC");
        ECGenParameterSpec ecSpec = new ECGenParameterSpec("secp256r1");
        keyPairGenerator.initialize(ecSpec, new FixedSecureRandom(secretKey.getEncoded()));
        return keyPairGenerator.generateKeyPair();
    }

    protected void updateKey(String key) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException, InvalidAlgorithmParameterException {
        Random rand = new Random(ByteBuffer.wrap(key.getBytes()).getLong());
        rand.nextBytes(this.salt);

        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(key.toCharArray(), this.salt, 65536, 256);
        SecretKey secretKey = factory.generateSecret(spec);

        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC", "BC");
        ECGenParameterSpec ecSpec = new ECGenParameterSpec("secp256r1");
        keyPairGenerator.initialize(ecSpec, new FixedSecureRandom(secretKey.getEncoded()));
        this.publicKey = keyPairGenerator.generateKeyPair().getPublic();
    }

    protected void addPass(@NotNull String name, @NotNull String login, @NotNull String pass, PublicKey key) throws InvalidAlgorithmParameterException, IllegalBlockSizeException, InvalidKeySpecException, BadPaddingException, InvalidKeyException {
        Password password = new Password(new Date(), name, login, pass.getBytes(StandardCharsets.UTF_16BE), key, this);
        this.passwords.put(password.uuid, password);
        ShadowPassword shadowPassword = new ShadowPassword(password.timestamp, password.name, password.login, password.uuid);
        Main.EVENT_BUS.fire(new UpdateScreenEvent.Table(shadowPassword, Event.Timing.RUNNING, false));
    }

    protected void addPass(String @NotNull [] name, String @NotNull [] login, String @NotNull [] pass, PublicKey key) throws InvalidAlgorithmParameterException, IllegalBlockSizeException, InvalidKeySpecException, BadPaddingException, InvalidKeyException {
        List<ShadowPassword> changedPass = new ArrayList<>();
        for (int i = 0; i < pass.length; i++) {
            Password password = new Password(new Date(), name[i], login[i], pass[i].getBytes(StandardCharsets.UTF_16BE), key, this);
            this.passwords.put(password.uuid, password);
            ShadowPassword shadowPassword = new ShadowPassword(password.timestamp, password.name, password.login, password.uuid);
            changedPass.add(shadowPassword);
        }
        Main.EVENT_BUS.fire(new UpdateScreenEvent.Table(changedPass, Event.Timing.RUNNING, false));
    }

    protected void removePass(@NotNull UUID uuid) {
        this.passwords.remove(uuid);
        Main.EVENT_BUS.fire(new UpdateScreenEvent.Table(ShadowPassword.getPasswordMap().remove(uuid), Event.Timing.RUNNING, true));
    }

    protected void removePass(@NotNull List<UUID> uuid) {
        List<ShadowPassword> passwordsForEvent = new ArrayList<>();
        uuid.forEach(uuid1 -> {
            this.passwords.remove(uuid1);
            passwordsForEvent.add(ShadowPassword.getPasswordMap().remove(uuid1));
        });
        Main.EVENT_BUS.fire(new UpdateScreenEvent.Table(passwordsForEvent, Event.Timing.RUNNING, true));
    }

    protected void editPass(@NotNull UUID uuid, @NotNull String newName, @NotNull String newLogin, @NotNull String newPass, @NotNull PublicKey publicKey) throws InvalidKeySpecException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        Password oldPass = this.passwords.get(uuid);
        if (oldPass != null) {
            byte[] pass = newPass.getBytes(StandardCharsets.UTF_16BE);
            Key encryptionKey = Main.KEY_MANAGER.generateKey();
            byte[] encrypted = AESCipher.getInstance().encryptWithBase64(pass, encryptionKey, KeyManager.generateIV(encryptionKey.getEncoded()));
            byte[] encodedPass = ECCCipher.getInstance().encryptWithBase64(encryptionKey.getEncoded(), publicKey, null);
            passwords.put(uuid, new Password(oldPass.timestamp, newName, newLogin, encrypted, uuid, encodedPass));
            Main.EVENT_BUS.fire(new UpdateScreenEvent.Table(ShadowPassword.getPasswordMap().remove(uuid), Event.Timing.RUNNING, true));
            Main.EVENT_BUS.fire(new UpdateScreenEvent.Table(new ShadowPassword(oldPass.timestamp, newName, newLogin, uuid), Event.Timing.RUNNING, false));
        }
    }

    protected List<ShadowPassword> getPasswords() {
        List<ShadowPassword> passwords = new ArrayList<>();
        this.passwords.values().forEach(password ->
                passwords.add(new ShadowPassword(password.timestamp, password.name, password.login, password.uuid)));
        return passwords;
    }

    protected void retrievePassword(PasswordEvent event, byte[] privateKey) {
        Password password = this.passwords.get(event.getPassword().getUuid());
        if (password != null) {
            Token token = new Token(password.uuid, password.timestamp.getTime(), password.pass, this.user.getHash512().getBytes(StandardCharsets.UTF_16BE),
                    password.encodedKey, privateKey);
            Main.getTokenThread().decryptPassword(new PasswordEvent(token));
        }
    }

    private HashMap<UUID, Password> getPasswords1() {
        return this.passwords;
    }




    public static class Password {
        private final Date timestamp;
        private final String name;
        private final String login;
        private final byte[] pass;
        private final byte[] encodedKey;
        private final UUID uuid;

        private Password(@NotNull Date timestamp, @NotNull String name, @Nullable String login,
                         byte @NotNull [] pass, @NotNull PublicKey key, PasswordStorage storage) throws InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
            this.timestamp = timestamp;
            this.name = name;
            this.login = login == null ? "" : login;
            UUID uuid = UUID.randomUUID();
            while (storage.getPasswords1().containsKey(uuid)) {
                uuid = UUID.randomUUID();
            }
            this.uuid = uuid;
            Key encryptionKey = Main.KEY_MANAGER.generateKey();
            this.pass = AESCipher.getInstance().encryptWithBase64(pass, encryptionKey, KeyManager.generateIV(encryptionKey.getEncoded()));
            this.encodedKey = ECCCipher.getInstance().encryptWithBase64(encryptionKey.getEncoded(), key, null);
        }

        private Password(Date timestamp, String name, String login, byte[] pass, UUID uuid, byte[] encodedKey) {
            this.timestamp = timestamp;
            this.name = name;
            this.login = login;
            this.pass = pass;
            this.uuid = uuid;
            this.encodedKey = encodedKey;
        }

        private static Password getFromBytes(byte @NotNull [] bytes) {
            List<byte[]> list = ArraySplitter.splitArray(bytes, (byte) 127, true);
            if (list.size() != 6) {
                throw new RuntimeException("Wrong input! Array size is not 6! It is " + list.size());
            }
            Date timestamp = new Date();
            byte[] date = Base64.getDecoder().decode(list.get(0));
            timestamp.setTime(ByteBuffer.wrap(date).getLong());
            String name = new String(Base64.getDecoder().decode(list.get(1)), StandardCharsets.UTF_16BE);
            String login = new String(Base64.getDecoder().decode(list.get(2)), StandardCharsets.UTF_16BE);

            return new Password(timestamp, name, login, list.get(5), UUIDUtil.convertBytesWithBase64ToUUID(list.get(3)), list.get(4));
        }

        private byte @NotNull [] toBytes() {
            long l = this.timestamp.getTime();
            byte[] timestamp = new byte[8];
            for (int i = 7; i >= 0; i--) {
                timestamp[i] = (byte)(l & 0xFF);
                l >>= 8;
            }
            timestamp = Base64.getEncoder().encode(timestamp);

            byte[] name = Base64.getEncoder().encode(this.name.getBytes(StandardCharsets.UTF_16BE));
            byte[] login = Base64.getEncoder().encode(this.login.getBytes(StandardCharsets.UTF_16BE));
            byte[] result = timestamp;
            result = ArrayUtils.add(result, (byte) 127);
            result = ArrayUtils.addAll(result, name);
            result = ArrayUtils.add(result, (byte) 127);
            result = ArrayUtils.addAll(result, login);
            result = ArrayUtils.add(result, (byte) 127);
            result = ArrayUtils.addAll(result, UUIDUtil.convertUUIDToBytesWithBase64(this.uuid));
            result = ArrayUtils.add(result, (byte) 127);
            result = ArrayUtils.addAll(result, this.encodedKey);
            result = ArrayUtils.add(result, (byte) 127);
            result = ArrayUtils.addAll(result, this.pass);
            return result;
        }

    }
}

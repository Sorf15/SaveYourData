package com.sorf.syd.user;

import com.sorf.syd.Main;
import com.sorf.syd.crypto.ECCCipher;
import com.sorf.syd.util.Logger;
import org.bouncycastle.crypto.prng.FixedSecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Random;

public class FileManager {
    private final File storageFile;
    private PublicKey publicKey;
    private PrivateKey privateKey;
    private boolean valid;
    private boolean writeLock = false;
    private SecureRandom random = new SecureRandom();
    private final User user;
    private byte[] salt = new byte[16];
    private byte[] passStorage = null;
    private byte[] storageKey = null;

    protected FileManager(User user, File storageFile, String pass) {
        this.user = user;
        this.storageFile = storageFile;
        KeyPair keyPair = null;
        try {
            //TODO: maybe make salt pass-independent
            Random rand = new Random(ByteBuffer.wrap(pass.getBytes()).getLong());
            rand.nextBytes(this.salt);
            // Derive a key from the password and salt
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(pass.toCharArray(), salt, 65536, 256);
            SecretKey secretKey = factory.generateSecret(spec);

            // Use the derived key to generate an ECC key pair
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC", "BC");
            ECGenParameterSpec ecSpec = new ECGenParameterSpec("secp256r1");
            keyPairGenerator.initialize(ecSpec, new FixedSecureRandom(secretKey.getEncoded()));
            keyPair = keyPairGenerator.generateKeyPair();

        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException | InvalidKeySpecException | NoSuchProviderException e) {
            Logger.error("Couldn't create keyPair in file manager! Stopping the program!");
            e.printStackTrace();
            Main.setStopped();
        }
        this.publicKey = keyPair.getPublic();
        this.privateKey = keyPair.getPrivate();
    }

    public PublicKey getPublicKey() {
        return this.publicKey;
    }

    protected synchronized void read() throws IOException, InvalidAlgorithmParameterException, UserException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        //TODO : REWRITE EVERYWHERE USING try-with-resoursces cuz when we get an error we don't close the stream
        BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(storageFile));
        /* major bytes consist of:
            3 bytes: version
            2 bytes: offset of login (there is trash in this offset)
            1 byte: length of login (it will be 2 bytes most likely, 1 byte will be taken from recovery)
            2 byte: length of history
            8 bytes: blank (later will be used in the recovery)
         */
        if (this.privateKey == null) {
            Logger.error("HOW DA FUCK DID U CALLED IT?");
            throw new UserException();
        }


        byte[] majorBytes = new byte[16];

        inputStream.skipNBytes(32);
        if (inputStream.readNBytes(majorBytes, 0, 16) != 16) {
            throw new IOException("Storage File doesn't have majorBytes");
        }
        //TODO: use version bro it's not hard
        if (!Arrays.equals(Main.storageVersion, Arrays.copyOf(majorBytes, 3))) {
            Logger.warn("Version from storage file: %s doesn't match with app: %s",
                    Arrays.toString(Arrays.copyOf(majorBytes, 3)), Arrays.toString(Main.storageVersion));
            //TODO: use Lock instead of boolean
            this.writeLock = true;
        }
        int trash = (0xff & majorBytes[3]) << 8;
        trash |= (0xffff & majorBytes[4]);
        inputStream.skipNBytes(trash);


        byte[] encryptedUsername = inputStream.readNBytes(majorBytes[5]);
        if (!validate(encryptedUsername)) {
            Logger.debug("1");
            throw new UserException("Wrong login or password!");
        }
        int i = inputStream.read();
        if (i > 0) {
            this.storageKey = inputStream.readNBytes(i);
        }


        int historyLogin = (majorBytes[7] & 0xFF) << 8;
        historyLogin |= majorBytes[6] & 0xFF;
        if (historyLogin != 0) {
            byte[] history = inputStream.readNBytes(historyLogin);
            PasswordGenerator.History.read(history, this.privateKey);
        }

        int length = ByteBuffer.wrap(inputStream.readNBytes(4)).getInt();
        if (length != 0) {
            this.passStorage = inputStream.readNBytes(length);
        }

        this.privateKey = null;
        inputStream.close();
    }

    protected synchronized void write() throws IOException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        if (!this.writeLock) {
            BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(this.storageFile));
            outputStream.write("ITS NOT SIMPLE BRO! CLOSE IT NOW".getBytes(StandardCharsets.UTF_8));

            byte[] trash = new byte[2];
            this.random.nextBytes(trash);
            byte[] encryptedUsername = ECCCipher.getInstance().encrypt(this.user.getUserName().getBytes(), this.publicKey, null);
            byte[] history = PasswordGenerator.History.save(this.publicKey);

            //major bytes
            outputStream.write(Main.storageVersion);
            outputStream.write(trash);
            outputStream.write(encryptedUsername.length);
            outputStream.write((byte) (history.length & 0xFF));
            outputStream.write((byte) ((history.length >> 8) & 0xFF));
            outputStream.write(new byte[8]);

            //trash
            int trashLength = (0xFF & trash[0]) << 8;
            trashLength |= (0xFFFF  & trash[1]);
            int cp = trashLength;
            while (cp > 0) {
                outputStream.write(this.random.nextInt(0,256));
                cp--;
            }

            //login
            //TODO: here all user info(i.e. creation date, login time, all that stuff)
            outputStream.write(encryptedUsername);
            //storageKey
            byte[] passKey = this.user.getStorageKey().getEncodedKey();
            outputStream.write(passKey.length);
            outputStream.write(passKey);

            //password history
            outputStream.write(history);

            //passwords...
            this.passStorage = this.user.getPasswordStorage().save();
            //length of bytes
            outputStream.write(ByteBuffer.allocate(4).putInt(this.passStorage.length).array());
            //passwords
            outputStream.write(this.passStorage);

            //trash at the end
            trashLength = (0xFF & trash[0]) << 8;
            trashLength |= (0xFFFF  & trash[1]);
            while (trashLength > 0) {
                outputStream.write(this.random.nextInt(0,256));
                trashLength--;
            }

            outputStream.flush();
            outputStream.close();
        } else {
            Logger.warn("Trying to write to User Storage but WriteLock is true");
        }
    }

    private boolean validate(final byte[] login) {
        String valid;
        try {
            valid = new String(ECCCipher.getInstance().decrypt(login, this.privateKey, null));
        } catch (IllegalBlockSizeException | BadPaddingException | InvalidKeyException e) {
            e.printStackTrace();
            Logger.error("2");
            this.valid = false;
            return false;
        }
        if (!valid.equals(this.user.getUserName())) {
            Logger.info("3");
            this.valid = false;
            return false;
        }
        //Logger.info("4");
        this.valid = true;
        return true;
    }

    protected boolean isValid() {
        return this.valid;
    }

    protected byte[] getPassStorage() {
        return this.passStorage;
    }

    public byte[] getWrappedKey() throws IllegalStateException {
        if (this.storageKey == null) {
            throw new IllegalStateException("Storage Key hasn't been initialized yet!");
        }
        return this.storageKey;
    }

    protected synchronized void updateKey(String pass) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException, InvalidAlgorithmParameterException {
        Random rand = new Random(ByteBuffer.wrap(pass.getBytes()).getLong());
        rand.nextBytes(this.salt);
        // Derive a key from the password and salt
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(pass.toCharArray(), salt, 65536, 256);
        SecretKey secretKey = factory.generateSecret(spec);

        // Use the derived key to generate an ECC key pair
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC", "BC");
        ECGenParameterSpec ecSpec = new ECGenParameterSpec("secp256r1");
        keyPairGenerator.initialize(ecSpec, new FixedSecureRandom(secretKey.getEncoded()));
        this.publicKey = keyPairGenerator.generateKeyPair().getPublic();

    }

}

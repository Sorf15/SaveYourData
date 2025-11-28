package com.sorf.syd.user;

import com.sorf.syd.Main;
import com.sorf.syd.gui.ShadowPassword;
import com.sorf.syd.util.HashUtil;
import com.sorf.syd.util.Logger;
import com.sorf.syd.util.UUIDUtil;
import com.sorf.syd.util.event.EventListener;
import com.sorf.syd.util.event.PasswordEvent;
import javafx.scene.control.Alert;
import org.jetbrains.annotations.NotNull;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.io.File;
import java.io.IOException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.List;
import java.util.UUID;

public class User {

    private final String userName;
    private final UUID uuid;
    private String hash512;
    private final File storageFile;
    private FileManager fileManager;
    private PasswordStorage passwordStorage;
    private StorageKey storageKey;

    public User(@NotNull String userName, @NotNull String password) throws NoSuchAlgorithmException, UserException {
        this.userName = userName;
        this.uuid = UUIDUtil.getUUIDfromString(this.userName);
        this.storageFile = new File(Main.userData + "/" + uuid + ".pass");
        if (!this.storageFile.exists()) {
            throw new UserException("Wrong login or password!");
        }
        this.fileManager = new FileManager(this, this.storageFile, HashUtil.getSHA256(password));
        try {
            this.fileManager.read();
        } catch (IOException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException | InvalidKeyException e) {
            e.printStackTrace();
            throw new UserException("Wrong login or password!");
        }

        this.hash512 = HashUtil.getSHA512(password);
        this.passwordStorage = new PasswordStorage(this, this.fileManager.getPassStorage(), HashUtil.getSHA256(password));
        try {
            this.storageKey = new StorageKey(this.hash512, this.fileManager.getWrappedKey());
        } catch (InvalidKeySpecException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException | InvalidKeyException e) {
           Logger.error("Caught Exception in StorageKey!");
            e.printStackTrace();
            throw new UserException("Error code: 5a");
        }
    }

    private User(@NotNull String userName, @NotNull String password, boolean any) throws NoSuchAlgorithmException {
        this.userName = userName;
        this.uuid = UUIDUtil.getUUIDfromString(this.userName);
        this.storageFile = new File(Main.userData + "/" + uuid + ".pass");
        this.fileManager = new FileManager(this, this.storageFile, HashUtil.getSHA256(password));
        this.hash512 = HashUtil.getSHA512(password);
        this.passwordStorage = new PasswordStorage(this, HashUtil.getSHA256(password));
        try {
            this.storageKey = new StorageKey(this.hash512);
        } catch (InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException | InvalidKeyException | InvalidKeySpecException | NoSuchProviderException e) {
            e.printStackTrace();
            Logger.error("Caught during userSignUp: %s", e.getMessage());
            Main.setStopped();
        }
    }

    public static User createNewUser(@NotNull String userName, @NotNull String password) throws UserException, NoSuchAlgorithmException {
        File storage = new File(Main.userData + "/" + UUIDUtil.getUUIDfromString(userName) + ".pass");
        if (storage.exists()) {
            throw new UserException("User with this login already exists!");
        }
        try {
            storage.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new User(userName, password, false);
    }

    public UUID getUuid() {
        return this.uuid;
    }

    public File getStorageFile() {
        return storageFile;
    }

    public String getUserName() {
        return this.userName;
    }

    public String getHash512() {
        return this.hash512;
    }

    public PublicKey getKey() {
        return this.fileManager.getPublicKey();
    }

    protected PasswordStorage getPasswordStorage() {
        return this.passwordStorage;
    }


    @Override
    public boolean equals(@NotNull Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;

        User user = (User) o;

        if (!getUuid().equals(user.getUuid())) return false;
        return getHash512().equals(user.getHash512());
    }

    @Override
    public int hashCode() {
        int result = getUuid().hashCode();
        result = 31 * result + getHash512().hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "User{" +
                "userName='" + userName + '\'' +
                ", uuid=" + uuid +
                ", hash512='" + hash512 + '\'' +
                '}';
    }

    public void logOut() {
        try {
            this.fileManager.write();
        } catch (IOException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException | InvalidAlgorithmParameterException e) {
            Logger.error("Caught error during User Log Out!");
            e.printStackTrace();
        }
        if (!PasswordGenerator.History.isEmpty()) {
            PasswordGenerator.History.clear();
        }
        if (!ShadowPassword.getPasswordMap().isEmpty()) {
            ShadowPassword.getPasswordMap().clear();
        }
    }

    public void addPassToStorage(@NotNull String name, @NotNull String login, @NotNull String pass) {
        try {
            this.passwordStorage.addPass(name, login, pass, this.storageKey.getPublicKey());
            this.fileManager.write();
        } catch (InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException | InvalidKeyException | InvalidKeySpecException | IOException e) {
            //TODO: make proper exception
            e.printStackTrace();
        }
    }

    public void addPassToStorage(String @NotNull [] name, String @NotNull [] login, String @NotNull [] pass) {
        try {
            this.passwordStorage.addPass(name, login, pass, this.storageKey.getPublicKey());
            this.fileManager.write();
        } catch (InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException | InvalidKeyException | InvalidKeySpecException | IOException e) {
            //TODO: make proper exception
            e.printStackTrace();
        }
    }

    public void removePassFromStorage(@NotNull UUID uuid) {
        this.passwordStorage.removePass(uuid);
        try {
            this.fileManager.write();
        } catch (IOException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException | InvalidKeyException e) {
            e.printStackTrace();
        }
    }

    public void removePassFromStorage(@NotNull List<UUID> uuid) {
        this.passwordStorage.removePass(uuid);
        try {
            this.fileManager.write();
        } catch (IOException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException | InvalidKeyException e) {
            e.printStackTrace();
        }
    }

    public void editPass(@NotNull UUID uuid, @NotNull String name, @NotNull String login, @NotNull String pass) {
        try {
            this.passwordStorage.editPass(uuid, name, login, pass, this.storageKey.getPublicKey());
            this.fileManager.write();
        } catch (InvalidKeySpecException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException | InvalidKeyException | IOException e) {
            //TODO: make proper exception
            e.printStackTrace();
        }
    }

    public List<ShadowPassword> getPass() {
        return this.passwordStorage.getPasswords();
    }

    @EventListener
    public void retrievePass(PasswordEvent event) {
        this.passwordStorage.retrievePassword(event, this.storageKey.getEncodedKey());
    }

    protected StorageKey getStorageKey() {
        return storageKey;
    }

    public void changePass(@NotNull String newPass) {
        try {
            String newHash512 = HashUtil.getSHA512(newPass);
            this.storageKey.changePrivateKey(this.hash512, newHash512);
            this.passwordStorage.updateKey(HashUtil.getSHA256(newPass));
            this.fileManager.updateKey(HashUtil.getSHA256(newPass));
            this.fileManager.write();

            this.hash512 = newHash512;
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException | IllegalBlockSizeException
                | InvalidKeySpecException | InvalidKeyException | BadPaddingException | NoSuchProviderException | IOException e) {
            Logger.error("Caught error during pass-change!");
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "An error has occurred while changing the password!").show();
        }
    }
}

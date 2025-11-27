package com.sorf.syd.user;

import com.sorf.syd.Main;
import com.sorf.syd.crypto.ECCCipher;
import com.sorf.syd.util.TimeUtil;
import com.sorf.syd.util.ArraySplitter;
import org.jetbrains.annotations.NotNull;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.io.File;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class PasswordGenerator {
    private static boolean lowerCase = true;
    private static boolean upperCase = false;
    private static boolean numbers = false;
    private static int passwordLength = 16;

    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPER = LOWER.toUpperCase();
    private static final String NUMBER = "0123456789";
    private static StringBuilder PASS_CHAR = new StringBuilder();
    private static final Random rand = new SecureRandom();

    public synchronized static boolean setLowerCase(boolean state) {
        PasswordGenerator.lowerCase = state;
        return checkProperties();
    }

    public synchronized static boolean setUpperCase(boolean state) {
        PasswordGenerator.upperCase = state;
        return checkProperties();
    }

    public synchronized static boolean setNumbers(boolean state) {
        PasswordGenerator.numbers = state;
        return checkProperties();
    }

    public static void setPasswordLength(int passwordLength) {
        PasswordGenerator.passwordLength = passwordLength;
    }

    public static int getPasswordLength() {
        return passwordLength;
    }

    private static boolean checkProperties() {
        if (!lowerCase && !upperCase && !numbers) {
            lowerCase = true;
            return false;
        }
        return true;
    }

    public static boolean isLowerCase() {
        return lowerCase;
    }

    public static boolean isNumbers() {
        return numbers;
    }

    public static boolean isUpperCase() {
        return upperCase;
    }

    public synchronized static String generatePass() {
        PASS_CHAR.delete(0, PASS_CHAR.length());
        if (lowerCase) {
            PASS_CHAR.append(LOWER);
        }
        if (upperCase) {
            PASS_CHAR.append(UPPER);
        }
        if (numbers) {
            PASS_CHAR.append(NUMBER);
        }
        StringBuilder sb = new StringBuilder(passwordLength);
        for (int i = 0; i < passwordLength; i++) {
            int randomIndex = rand.nextInt(PASS_CHAR.length());
            char randomChar = PASS_CHAR.charAt(randomIndex);
            sb.append(randomChar);
        }
        History.add(new History(sb.toString(), TimeUtil.getFullTime()));
        return sb.toString();
    }

    public static class History {
        private @NotNull String timestamp;
        private @NotNull String password;

        private static LinkedList<History> passwordHistories = new LinkedList<>();
        public static final File passFile = new File(Main.dataDirectory + "/pass.old");

        public History(@NotNull String password, @NotNull String timestamp) {
            this.password = password;
            this.timestamp = timestamp;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public String getPassword() {
            return password;
        }

        //used in javafx
        public void setTimestamp(@NotNull String timestamp) {
            this.timestamp = timestamp;
        }

        public void setPassword(@NotNull String password) {
            this.password = password;
        }

        public synchronized static void read(byte[] history, @NotNull PrivateKey key) throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
            //Logger.info("History: %s + %d", Arrays.toString(history), history.length);
            //Logger.error("PRIVATE KEY: %s", Arrays.toString(key.getEncoded()));
            byte[] b = ECCCipher.getInstance().decrypt(history, key, null);
            /*int index = 0;
            int prev = 0;
            ArrayList<String> rawList = new ArrayList<>();
            for (; index < b.length; index++) {
                if (b[index] == 94) {
                    byte[] a = new byte[index - prev];
                    System.arraycopy(b, prev, a, 0, index - prev);
                    rawList.add(new String(a));
                    index++;
                    prev = index;
                }
            }
            rawList.forEach(element -> {*/
            ArraySplitter.splitArray(b, (byte) 94, false).forEach(element -> {
                String s = new String(element);
                String[] raw1 = s.split("~");
                add(new History(raw1[1], raw1[0]));
            });
        }

        public synchronized static void add(@NotNull PasswordGenerator.History pass) {
            if (passwordHistories.size() > 50) {
                passwordHistories.removeLast();
            }
            passwordHistories.addFirst(pass);
        }

        public synchronized static List<History> getHistory() {
            return passwordHistories;
        }

        public synchronized static byte[] save(@NotNull PublicKey key) throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
            if (!passwordHistories.isEmpty()) {
                StringBuilder stringBuilder = new StringBuilder();
                for (History passwordHistory : passwordHistories) {
                    stringBuilder.append(passwordHistory.getTimestamp()).append("~").append(passwordHistory.getPassword()).append("^");
                }
                //Logger.info(stringBuilder.toString());
                return ECCCipher.getInstance().encrypt(stringBuilder.toString().getBytes(), key, null);
            }
            return new byte[0];
        }

        public synchronized static void clear() {
            if (!passwordHistories.isEmpty()) {
                passwordHistories.clear();
            }
        }

        @Override
        public String toString() {
            return "History{" +
                    "timestamp='" + timestamp + '\'' +
                    ", password='" + password + '\'' +
                    '}';
        }

        public static boolean isEmpty() {
            return passwordHistories.isEmpty();
        }
    }
}
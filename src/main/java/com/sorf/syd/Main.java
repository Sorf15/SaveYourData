package com.sorf.syd;

import com.google.common.util.concurrent.ListenableFutureTask;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;
import com.sorf.syd.crypto.AESCipher;
import com.sorf.syd.crypto.KeyManager;
import com.sorf.syd.gui.FXMLControllers.ContGenerator;
import com.sorf.syd.gui.FXMLControllers.ContManage;
import com.sorf.syd.gui.MainController;
import com.sorf.syd.gui.ShadowPassword;
import com.sorf.syd.token.TokenThread;
import com.sorf.syd.user.PasswordGenerator;
import com.sorf.syd.user.User;
import com.sorf.syd.user.UserException;
import com.sorf.syd.util.*;
import com.sorf.syd.util.event.EventListener;
import com.sorf.syd.util.event.*;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jetbrains.annotations.NotNull;

import javax.crypto.spec.IvParameterSpec;
import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.util.*;
import java.util.concurrent.*;


public class Main {

    //================== THE MOST IMPORTANT STUFF OF THE CENTURY =================//

    public static final File directory = new File(System.getProperty("user.dir"));
    private static final File stateLock = new File(directory + "/unknown.state");
    public static final File dataDirectory = new File(directory + "/data");
    public static final File userData = new File(dataDirectory + "/user_data");
    public static final File resources = new File(directory + "/resources");
    public static final String version = "0.2";
    public static final byte[] storageVersion = "001".getBytes();
    public static boolean debugMode = false;

    //==================== THE MOST IMPORTANT STUFF ENDS HERE ======================//


    private static final BlockingQueue<FutureTask<?>> mainQueue = new LinkedBlockingQueue<>(30);
    private static boolean isStopped = false;
    public static final EventBus EVENT_BUS = new EventBus();
    private static User user = null;
    public static final KeyManager KEY_MANAGER;
    private static Thread mainThread;
    private static TokenThread tokenThread = new TokenThread();
    public static List<ExportPass> exportPassList = new CopyOnWriteArrayList<>();
    public static volatile int exportPassValue = 0;
    public static volatile boolean exportPass = false;

    private Main(){}

    static {
        KeyManager KEY_MANAGER1 = null;
        try {
            KEY_MANAGER1 = new KeyManager();
        } catch (NoSuchAlgorithmException e) {
            Logger.error("Couldn't create KEY_MANAGER instance! Stopping the program!");
            e.printStackTrace();
            isStopped = true;
        } finally {
            KEY_MANAGER = KEY_MANAGER1;
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 0) {
            for (String s : args) {
                if (s.equals("-debugMode")) {
                    debugMode = true;
                    Logger.debug("DEBUG MODE ON!");
                    break;
                }
            }
        }

        stateLock.createNewFile();
        RandomAccessFile randomAccessFile = new RandomAccessFile(stateLock, "rw");
        FileChannel fileChannel = randomAccessFile.getChannel();
        FileLock lock = null;
        try {
            lock = fileChannel.tryLock();
            if (lock == null) {
                Logger.warn("Another instance is already running.");
                forceStop();
            }
        } catch (IOException e) {
            Logger.warn("Another instance is already running.");
            e.printStackTrace();
            forceStop();
        }

        mainThread = Thread.currentThread();
        Security.addProvider(new BouncyCastleProvider());
        if (isStopped) {
            return;
        }

        dataDirectory.mkdir();
        userData.mkdir();

        MainController mainController = new MainController();
        new Thread(mainController).start();
        new Thread(tokenThread).start();
        EVENT_BUS.register(mainController);
        EVENT_BUS.register(Main.class);

        Config.read();
        if ((Boolean) Config.getValue("working")) {
            Logger.warn("It seems that the program has been stopped incorrect! Ignoring!");
        } else {
            Config.write("working", true);
        }

        //Logger.debug(Config.keys.toString());

        if ((Boolean) Config.getValue("keep_signed_in")) {
            startupLogIn();
        }

        ArrayList<Object> list = new ArrayList<>();
        list.add(Config.getValue("genLength"));
        list.add(Config.getValue("genLower"));
        list.add(Config.getValue("genUpper"));
        list.add(Config.getValue("genNumbers"));
        EVENT_BUS.fire(new UpdateScreenEvent.Generator(list, Event.Timing.STARTUP));

        while (!isStopped) {
            try {
                FutureTask<?> task = mainQueue.poll(1, TimeUnit.SECONDS);
                if (task != null) {
                    task.run();
                }
            } catch (InterruptedException e) {
                Logger.error("Caught InterruptedException error while waiting for queue: %s", e.getMessage());
            }
            if (exportPass && exportPassList.size() >= exportPassValue) {
                exportPassList();
            }
        }
        EVENT_BUS.shutdown();
        Config.write("working", false);



        if (lock != null) {
            lock.close();
        }
        fileChannel.close();
        randomAccessFile.close();


    }

    public static void handleNewUser(@NotNull final String username, @NotNull final String password) {
        try {
            user = User.createNewUser(username, password);
            EVENT_BUS.fire(new ChangeSceneEvent(ChangeSceneEvent.Type.MAINAPP));
            EVENT_BUS.fire(new UpdateScreenEvent(UpdateScreenEvent.State.HOME, UpdateScreenEvent.Destination.MAIN_APP));

            Config.write("logged_in", true);
            if ((Boolean) Config.getValue("keep_signed_in")) {
                saveLogInState(username, password);
            }

        } catch (UserException e) {
            EVENT_BUS.fire(new UpdateScreenEvent(e.getLocalizedMessage(), UpdateScreenEvent.Destination.SIGN_UP));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void handleUserSignIn(String username, String password) {
        if (username.isBlank() && password.isBlank()) {
            return;
        }
        try {
            user = new User(username, password);

            Config.write("logged_in", true);
            if ((Boolean) Config.getValue("keep_signed_in")) {
                saveLogInState(username, password);
            }

            AsyncTask.getInstance().invoke(() -> {
                long startTime = System.currentTimeMillis();
                while (MainController.isPrimaryStageNull()) {
                    Thread.onSpinWait();
                    if (new Date().getTime() - startTime > 30*1000) {
                        Logger.error("Waited more than 30 seconds, but primaryStage is still null! Skipping current AsyncTask!");
                        return;
                    }
                }
                EVENT_BUS.fire(new ChangeSceneEvent(ChangeSceneEvent.Type.MAINAPP));
                EVENT_BUS.fire(new UpdateScreenEvent(UpdateScreenEvent.State.HOME, UpdateScreenEvent.Destination.MAIN_APP));
                EVENT_BUS.fire(new UpdateScreenEvent.Table(user.getPass(), Event.Timing.STARTUP, false));
            });


            if (PasswordGenerator.History.isEmpty()) {
                return;
            }
            AsyncTask.getInstance().invoke(() -> {
                while (!ContGenerator.isInitialized()) {
                    Thread.onSpinWait();
                }
                EVENT_BUS.fire(new UpdateScreenEvent.Generator(PasswordGenerator.History.getHistory(), Event.Timing.RUNNING));
            });
        } catch (UserException e) {
            EVENT_BUS.fire(new UpdateScreenEvent(e.getLocalizedMessage(), UpdateScreenEvent.Destination.SIGN_IN));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void saveLogInState(@NotNull final String username, @NotNull final String password) {
        File state = new File(directory + "/state.lock");
        try {
            state.createNewFile();

            Key key = HWIDUtil.KEY;
            IvParameterSpec iv = HWIDUtil.IV;

            //TODO: create smth more interesting
            byte[] encryptedUsername = AESCipher.getInstance().encrypt(username.getBytes(), key, iv);
            byte[] encryptedPassword = AESCipher.getInstance().encrypt(password.getBytes(), key, iv);

            FileOutputStream outputStream = new FileOutputStream(state);
            outputStream.write(encryptedUsername.length);
            outputStream.write(encryptedUsername);
            outputStream.write(encryptedPassword);

            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            Logger.error("Caught %s exception during savingLogInState", e.getClass().getSimpleName());
            e.printStackTrace();
        }

    }

    public static void startupLogIn() {
        try {
            File state = new File(directory + "/state.lock");
            FileInputStream inputStream = new FileInputStream(state);

            int i;
            ArrayList<Byte> bytes = new ArrayList<>();
            while ((i = inputStream.read()) != -1) {
                bytes.add((byte) i);
            }
            inputStream.close();

            int j = 0;
            byte[] usernameRaw = new byte[bytes.get(0)];
            for (; j < bytes.get(0); j++) {
                usernameRaw[j] = bytes.get(j+1);
            }

            byte[] passwordRaw = new byte[bytes.size() - j - 1];
            int k = 0;
            for (; j < bytes.size() - 1; j++) {
                passwordRaw[k] = bytes.get(j+1);
                k++;
            }

            Key key = KEY_MANAGER.generateKey(HWIDUtil.HWID);
            String username = new String(AESCipher.getInstance().decrypt(usernameRaw, key, HWIDUtil.IV));
            String password = new String(AESCipher.getInstance().decrypt(passwordRaw, key, HWIDUtil.IV));
            handleUserSignIn(username, password);

        } catch (Exception e) {
            Logger.error("Caught %s exception during simple login", e.getClass().getSimpleName());
            e.printStackTrace();
        }
    }

    public static void logOut() {
        AsyncTask.getInstance().invoke(() -> {
            try {
                Config.write("keep_signed_in", false);
                Config.write("logged_in", false);
                PrintWriter writer = new PrintWriter(directory + "/state.lock");
                writer.print("");
                writer.flush();
                writer.close();
            } catch (ConfigurationException | FileNotFoundException e) {
                Logger.error("Caught %s exception during logout", e.getClass().getSimpleName());
                e.printStackTrace();
            }
        });
        user.logOut();
        EVENT_BUS.fire(new UpdateScreenEvent.Generator(null, Event.Timing.STOP));
        EVENT_BUS.fire(new UpdateScreenEvent.Table((ShadowPassword) null, Event.Timing.STOP, true));
        user = null;
        EVENT_BUS.fire(new ChangeSceneEvent(ChangeSceneEvent.Type.SIGNIN));
        EVENT_BUS.fire(new UpdateScreenEvent(UpdateScreenEvent.State.HOME, UpdateScreenEvent.Destination.MAIN_APP));
    }

    @EventListener
    public static void convertPass(PasswordEvent event) {
        if (user != null) {
            user.retrievePass(event);
        }
    }

    public static <V> void addScheduledTask(@NotNull Callable<V> callable) {
        if (Thread.currentThread() == mainThread) {
            try {
                callable.call();
            } catch (Exception e) {
                Logger.error("Caught error during executing scheduled task in main thread: %s", e.getMessage());
            }

        } else {
            ListenableFutureTask<V> listenablefuturetask = ListenableFutureTask.create(callable);
            mainQueue.add(listenablefuturetask);
        }
    }

    public static void addScheduledTask(@NotNull Runnable runnable) {
        addScheduledTask(Executors.callable(runnable));
    }

    public static void setStopped() {
        isStopped = true;
        AsyncTask.getInstance().invoke(() -> {
            try {
                if (user != null) {
                    user.logOut();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        AsyncTask.getInstance().invoke(() -> {
            Logger.debug("WRITING GENERATOR");
            try {
                Config.write("genLength", PasswordGenerator.getPasswordLength());
                Config.write("genLower", PasswordGenerator.isLowerCase());
                Config.write("genUpper", PasswordGenerator.isUpperCase());
                Config.write("genNumbers", PasswordGenerator.isNumbers());
            } catch (ConfigurationException e) {
                Logger.error("Caught exception during config save in AsyncTask!");
                e.printStackTrace();
            }
        });
        Logger.debug("Sending stop event!");
        EVENT_BUS.fire(new StopEvent());

    }

    public static void forceStop() {
        isStopped = true;
    }

    public static void addPasswordToStorage(@NotNull String name, @NotNull String login, @NotNull String pass) {
        if (user != null) {
            user.addPassToStorage(name, login, pass);
        }
    }

    public static void addPasswordToStorage(String @NotNull [] name, String @NotNull [] login, String @NotNull [] pass) {
        if (user != null) {
            user.addPassToStorage(name, login, pass);
        }
    }

    public static void removePassFromStorage(@NotNull UUID uuid) {
        if (user != null) {
            user.removePassFromStorage(uuid);
        }
    }

    public static void removePassFromStorage(@NotNull List<UUID> uuid) {
        if (user != null) {
            user.removePassFromStorage(uuid);
        }
    }

    public static void editPasswordInStorage(@NotNull UUID uuid, @NotNull String name, @NotNull String login, @NotNull String pass) {
        if (user != null) {
            user.editPass(uuid, name, login, pass);
        }
    }

    public static String getUserSHA512() {
        return user.getHash512();
    }

    public static void changePass(@NotNull String newPass) {
        if (user != null) {
            user.changePass(newPass);
        }
    }

    public static TokenThread getTokenThread() {
        return tokenThread;
    }

    public static void importPasswords(@NotNull String[] strings, CSVReader csvReader) {
        try {
            LinkedList<String[]> lines = (LinkedList<String[]>) csvReader.readAll();

            String[] original = lines.poll();
            int name = findMatch(strings[0], original);
            int username = findMatch(strings[1], original);
            int password = findMatch(strings[2], original);
            String[] names = new String[lines.size()];
            String[] usernames = new String[lines.size()];
            String[] passwords = new String[lines.size()];
//            List<String> names = new ArrayList<>();
//            List<String> usernames = new ArrayList<>();
//            List<String> passwords = new ArrayList<>();
            for (int i = 0; i < lines.size(); i++) {
                names[i] = lines.get(i)[name];
                usernames[i] = lines.get(i)[username];
                passwords[i] = lines.get(i)[password];
            }

            //TODO: check if it works (the whole import)
            csvReader.close();
            Main.addPasswordToStorage(names, usernames, passwords);
        } catch (CsvException | IOException e) {
            e.printStackTrace();
        }
    }

    private static int findMatch(String first, String[] second) {
        for (int i = 0; i < second.length; i++) {
            if (second[i].equals(first)) return i;
        }
        return 0;
    }

    private static void exportPassList() {
        exportPass = false;
        exportPassValue = 0;
        try {
            File exportFile = ((ContManage) MainController.controllers.get("mManage")).exportFile;
            if (exportFile == null) {
                //HOW?
                Logger.error("Couldn't get exportFile for PassExport cuz it's null!");
                return;
            }

            FileWriter outputfile = new FileWriter(exportFile);
            CSVWriter writer = new CSVWriter(outputfile, ',', '\u0000', '\u0000', "\n");
            String[] header = {"name", "login", "password"};
            writer.writeNext(header);

            //TODO: include timestamp?
            List<String[]> passList = exportPassList.stream().map(pass -> new String[]{pass.getName(), pass.getLogin(), pass.getPass()}).toList();

            writer.writeAll(passList);

            writer.close();
            Logger.info("Done!");
        } catch (IOException e) {
            Logger.error("Caught exception during passExport!");
            e.printStackTrace();
        } finally {
            exportPassList.clear();
        }
    }

    // TODO: cooldown on button request, remove some unnecessary checks, timings
    // TODO: obfuscation in file names, methods that throws unnecessary exceptions, instead of a lot of if else predicate?
    // TODO: Optimisation & memory leaks, config version, magic recovery, error codes
    //

}

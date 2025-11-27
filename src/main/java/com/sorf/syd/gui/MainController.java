package com.sorf.syd.gui;

import com.sorf.syd.Main;
import com.sorf.syd.gui.FXMLControllers.FXMLController;
import com.sorf.syd.util.Logger;
import com.sorf.syd.util.event.ChangeSceneEvent;
import com.sorf.syd.util.event.EventListener;
import com.sorf.syd.util.event.EventPriority;
import com.sorf.syd.util.event.StopEvent;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventTarget;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class MainController extends Application implements Runnable {

    //TODO: remake it (maybe)
    public static Scene signInGUI;
    public static Scene signUpGUI;
    public static Scene mainScrenGUI;

    private static Stage primaryStage;
    private static SYDTrayIcon trayIcon;

    public static AnchorPane mGenerator;
    public static AnchorPane mHome;
    public static AnchorPane mManage;

    public static StackPane lorem;

    public static DialogPane dAddPass;
    public static DialogPane dEditPass;
    public static DialogPane dSelectColumn;
    public static DialogPane dEditAccPass;

    public static @NotNull HashMap<String, FXMLController> controllers = new HashMap<>();


    @Override
    public void start(Stage primaryStage) {
        MainController.primaryStage = primaryStage;
        File iconFile = new File(Main.resources +"/3yBRPoKItRX11xNvTFiA9O6kHU8RUNMz");

        //trayIcon = new TrayIcon(primaryStage, iconFile);
        trayIcon = new SYDTrayIcon(iconFile);

        primaryStage.setTitle("SYD");
        primaryStage.getIcons().add(new Image(iconFile.getAbsolutePath()));
        primaryStage.setScene(signInGUI);
        primaryStage.show();

        primaryStage.setOnCloseRequest(event -> {
            SwingUtilities.invokeLater(() -> trayIcon.stageHidden());

        });

    }

    @Override
    public void init() {
        try {
            signInGUI = loadFXML("signInGUI", "signin.fxml");
            signUpGUI = loadFXML("signUpGUI", "signup.fxml");
            mainScrenGUI = loadFXML("mainScrenGUI", "mainapp.fxml");
            mGenerator = loadFXML("mGenerator", "generator.fxml");
            mHome = loadFXML("mHome", "home.fxml");
            mManage = loadFXML("mManage", "manage.fxml");
            dAddPass = loadFXML("dAddPass", "dialogAddPass.fxml");
            dEditPass = loadFXML("dEditPass", "dialogEditPass.fxml");
            dSelectColumn = loadFXML("dSelectColumn", "dialogImportPass.fxml");
            dEditAccPass = loadFXML("dEditAccPass", "dialogEditAccPass.fxml");
            lorem = loadFXML("lorem", "lorem.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private <T extends EventTarget> T loadFXML(String name, String location) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(location));
        T copy = loader.load();
        controllers.put(name, loader.getController());
        return copy;
    }


    @Override
    public void run() {
        Thread.currentThread().setName("UIThread");
        launch();
    }

    @EventListener
    public void changeScene(ChangeSceneEvent event) {
        switch (event.type) {
            case EXIT -> {
                Main.addScheduledTask(Main::setStopped);
                Platform.runLater(() -> primaryStage.close());
            }
            case SIGNIN -> Platform.runLater(() -> {
                primaryStage.setScene(signInGUI);
                centerStage();
            });
            case SIGNUP -> Platform.runLater(() -> {
                primaryStage.setScene(signUpGUI);
                centerStage();
            });
            case MAINAPP -> Platform.runLater(() -> {
                primaryStage.setScene(mainScrenGUI);
                centerStage();

            });
            case UNKNOWN -> {
                event.setCanceled(true);
                Logger.warn("Received ChangeSceneEvent with Type.UNKNOWN!");
            }
        }
    }

    @EventListener(priority = EventPriority.HIGHEST)
    public void stopAll(StopEvent event) {
        if (trayIcon != null) {
            trayIcon.terminate();
        }
        Platform.setImplicitExit(true);
        if (primaryStage.isShowing()) {
            Platform.runLater(() -> primaryStage.close());
        }
        //Platform.exit();
        Thread.currentThread().interrupt();
    }

    /**
     * @return true if primaryStage is null
     */
    public static boolean isPrimaryStageNull() {
        return primaryStage == null;
    }

    protected static void showStage() {
        if (!isPrimaryStageNull()) {
            primaryStage.show();
        }
        SwingUtilities.invokeLater(() -> trayIcon.stageShown());
    }

    protected static void hideStage() {
        if (!isPrimaryStageNull()) {
            primaryStage.hide();
        }
        SwingUtilities.invokeLater(() -> trayIcon.stageHidden());
    }

    public static void centerStage() {
        Platform.runLater(() -> {
            if (!isPrimaryStageNull()) {
                primaryStage.centerOnScreen();
            }
        });
    }

    public static <T extends Dialog<?>> void setChildToScene(@NotNull T child) {
        if (!isPrimaryStageNull()) {
            child.initOwner(primaryStage);
        }
    }

    public static <T extends Stage> void setChildToScene(@NotNull T child) {
        if (!isPrimaryStageNull()) {
            child.initOwner(primaryStage);
        }
    }

    public static File showFileChooser(@NotNull FileChooser fileChooser, int value) {
        if (isPrimaryStageNull()) {
            return null;
        }
        return switch (value) {
            case 0 -> fileChooser.showOpenDialog(primaryStage);
            case 1 -> fileChooser.showSaveDialog(primaryStage);
            default -> null;
        };
    }

}

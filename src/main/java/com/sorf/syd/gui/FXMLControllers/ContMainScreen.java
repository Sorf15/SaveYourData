package com.sorf.syd.gui.FXMLControllers;

import com.sorf.syd.Main;
import com.sorf.syd.gui.MainController;
import com.sorf.syd.gui.dialog.DialogEditAccPass;
import com.sorf.syd.util.Logger;
import com.sorf.syd.util.event.EventListener;
import com.sorf.syd.util.event.UpdateScreenEvent;
import javafx.application.Platform;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Optional;

public class ContMainScreen extends FXMLController {
    public Scene scene;
    public SplitPane sp;
    public VBox leftVbox;
    //public AnchorPane rightAP;
    public BorderPane rightBP;

    public ImageView ivLogo;
    public Button bHome;
    public Button bManage;
    public Button bGenerator;
    public Button bSettings;
    public Button bWiFi;
    public Button bDocs;
    public BorderPane borderPane;

    public Button bAccount;
    public Button bNotification;
    public Button bPremium;

    public ImageView ivHome;
    public ImageView ivManage;
    public ImageView ivGenerator;
    public ImageView ivSettings;
    public ImageView ivWiFi;
    public ImageView ivDocs;

    public ImageView ivNotification;

    private static final PseudoClass SELECTED_CLASS = PseudoClass.getPseudoClass("selected");

    @Override
    public void initialize(){
        Main.EVENT_BUS.register(this);

        ivLogo.setEffect(new ColorAdjust());
        applyColorAdjust((ColorAdjust) ivLogo.getEffect(), Color.rgb(62, 72, 121));

        sp.getDividers().get(0).positionProperty().addListener((observable, oldValue, newValue) -> {
            if (Math.abs(newValue.floatValue() - 0.075) > 0.001) sp.setDividerPosition(0, 0.075);
        });

        ivHome.setEffect(new ColorAdjust());
        ivGenerator.setEffect(new ColorAdjust());
        ivManage.setEffect(new ColorAdjust());
        ivSettings.setEffect(new ColorAdjust());
        ivWiFi.setEffect(new ColorAdjust());
        ivDocs.setEffect(new ColorAdjust());
        disableSelected();

        ivNotification.setEffect(new ColorAdjust());
        applyColorAdjust((ColorAdjust) ivNotification.getEffect(), Color.rgb(56, 60, 84));


        //TODO add tooltip to all
        bHome.setTooltip(new Tooltip("Home"));
        bManage.setTooltip(new Tooltip("Manage"));
        bGenerator.setTooltip(new Tooltip("Generator"));
        bSettings.setTooltip(new Tooltip("Settings"));
        bWiFi.setTooltip(new Tooltip("WiFi Passwords"));
        bDocs.setTooltip(new Tooltip("Important Docs"));

        bNotification.setTooltip(new Tooltip("Not implemented"));
        bPremium.setTooltip(new Tooltip("Not implemented"));
        bAccount.setTooltip(new Tooltip("Manage Account"));

//        bMoreInfo.setTooltip(new Tooltip("TEXT"));


//        ivMoreInfo.setImage(new Image("file:resources/pBNWym3Cg3mQ33JG09mAV9WCKyrOxyVF"));

//        bMoreInfo.hoverProperty().addListener((observable, oldValue, newValue) -> {
//            if (!newValue) {
//                bMoreInfo.setStyle("-fx-background-color: rgb(0,0,0,0);");
//                bMoreInfo.setTextFill(Color.rgb(170, 173, 181));
//                applyColorAdjust((ColorAdjust) ivMoreInfo.getEffect(), Color.rgb(170, 173, 181));
//            } else {
//                bMoreInfo.setTextFill(Color.rgb(242, 243, 245));
//                applyColorAdjust((ColorAdjust) ivMoreInfo.getEffect(), Color.rgb(242, 243, 245));
//
//                bMoreInfo.setStyle("-fx-background-color: #2e3449;");
//            }
//        });

//
    }

    private void disableSelected() {
        bHome.pseudoClassStateChanged(SELECTED_CLASS, false);
        bManage.pseudoClassStateChanged(SELECTED_CLASS, false);
        bGenerator.pseudoClassStateChanged(SELECTED_CLASS, false);
        bSettings.pseudoClassStateChanged(SELECTED_CLASS, false);
        bWiFi.pseudoClassStateChanged(SELECTED_CLASS, false);
        bDocs.pseudoClassStateChanged(SELECTED_CLASS, false);
        applyColorAdjust((ColorAdjust) ivHome.getEffect(), Color.rgb(56, 60, 84));
        applyColorAdjust((ColorAdjust) ivGenerator.getEffect(), Color.rgb(56, 60, 84));
        applyColorAdjust((ColorAdjust) ivManage.getEffect(), Color.rgb(56, 60, 84));
        applyColorAdjust((ColorAdjust) ivSettings.getEffect(), Color.rgb(56, 60, 84));
        applyColorAdjust((ColorAdjust) ivWiFi.getEffect(), Color.rgb(56, 60, 84));
        applyColorAdjust((ColorAdjust) ivDocs.getEffect(), Color.rgb(56, 60, 84));
    }

    @EventListener
    public void updateScreen(UpdateScreenEvent event) {
        if (event.destination == UpdateScreenEvent.Destination.MAIN_APP) {
            if (event.type == UpdateScreenEvent.Type.CHANGEMENU) {
                Platform.runLater(() -> {
                    rightBP.getChildren().removeIf(node -> BorderPane.getAlignment(node) == Pos.CENTER);
                    disableSelected();
                });
                switch (event.state) {
                    case HOME -> Platform.runLater(() -> {
                        rightBP.setCenter(MainController.mHome);

                        bHome.pseudoClassStateChanged(SELECTED_CLASS, true);
                        applyColorAdjust((ColorAdjust) ivHome.getEffect(), Color.rgb(235, 235, 245));
                    });
                    case MANAGE -> Platform.runLater(() -> {
                        rightBP.setCenter(MainController.mManage);

                        bManage.pseudoClassStateChanged(SELECTED_CLASS, true);
                        applyColorAdjust((ColorAdjust) ivManage.getEffect(), Color.rgb(235, 235, 245));
                    });
                    case GENERATOR -> Platform.runLater(() -> {
                        rightBP.setCenter(MainController.mGenerator);

                        bGenerator.pseudoClassStateChanged(SELECTED_CLASS, true);
                        applyColorAdjust((ColorAdjust) ivGenerator.getEffect(), Color.rgb(235, 235, 245));
                    });
                    case SETTINGS -> Platform.runLater(() -> {
                        rightBP.setCenter(null);
                        bSettings.pseudoClassStateChanged(SELECTED_CLASS, true);
                        applyColorAdjust((ColorAdjust) ivSettings.getEffect(), Color.rgb(235, 235, 245));
                    });
                    case WIFI -> Platform.runLater(() -> {
                        rightBP.setCenter(null);
                        bWiFi.pseudoClassStateChanged(SELECTED_CLASS, true);
                        applyColorAdjust((ColorAdjust) ivWiFi.getEffect(), Color.rgb(235, 235, 245));
                    });
                    case DOCS -> Platform.runLater(() -> {
                        rightBP.setCenter(MainController.lorem);
                        bDocs.pseudoClassStateChanged(SELECTED_CLASS, true);
                        applyColorAdjust((ColorAdjust) ivDocs.getEffect(), Color.rgb(235, 235, 245));
                    });
                }
            }
        }
    }

    @FXML
    public void bAccount() {
        MenuItem item1 = new MenuItem("Log out");
        item1.setOnAction(event -> Main.logOut());
        MenuItem item2 = new MenuItem("Change password");
        item2.setOnAction(event -> {
            DialogEditAccPass dialog = new DialogEditAccPass(Main.getUserSHA512());
            Optional<String> result = dialog.showAndWait();
            result.ifPresent(s ->
                    Main.addScheduledTask(
                            () -> Main.changePass(s)));

        });
        ContextMenu contextMenu = new ContextMenu(item2, item1);
        contextMenu.show(bAccount, Side.BOTTOM, 0,0);
    }

    @FXML
    public void bHome() {
        Main.EVENT_BUS.fire(new UpdateScreenEvent(UpdateScreenEvent.State.HOME, UpdateScreenEvent.Destination.MAIN_APP));

    }

    @FXML
    public void bManage() {
        Main.EVENT_BUS.fire(new UpdateScreenEvent(UpdateScreenEvent.State.MANAGE, UpdateScreenEvent.Destination.MAIN_APP));

    }

    @FXML
    public void bGenerator() {
        Main.EVENT_BUS.fire(new UpdateScreenEvent(UpdateScreenEvent.State.GENERATOR, UpdateScreenEvent.Destination.MAIN_APP));

    }

    @FXML
    public void bSettings() {
        Main.EVENT_BUS.fire(new UpdateScreenEvent(UpdateScreenEvent.State.SETTINGS, UpdateScreenEvent.Destination.MAIN_APP));
    }

    @FXML
    public void bWiFi() {
        Main.EVENT_BUS.fire(new UpdateScreenEvent(UpdateScreenEvent.State.WIFI, UpdateScreenEvent.Destination.MAIN_APP));
    }

    @FXML
    public void bDocs() {
        Main.EVENT_BUS.fire(new UpdateScreenEvent(UpdateScreenEvent.State.DOCS, UpdateScreenEvent.Destination.MAIN_APP));
    }

    @FXML
    public void bPremium() {
        new Alert(Alert.AlertType.INFORMATION, "Not Implemented", ButtonType.CLOSE).show();
    }

    @FXML
    public void bNotification() {
        new Alert(Alert.AlertType.INFORMATION, "Not Implemented", ButtonType.CLOSE).show();
    }
}

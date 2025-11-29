package com.sorf.syd.gui.FXMLControllers;

import com.sorf.syd.Main;
import com.sorf.syd.gui.skins.VisiblePasswordFieldSkin;
import com.sorf.syd.util.Config;
import com.sorf.syd.util.event.ChangeSceneEvent;
import com.sorf.syd.util.event.EventListener;
import com.sorf.syd.util.event.UpdateScreenEvent;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Pair;
import org.apache.commons.configuration2.ex.ConfigurationException;

public class ContSignUp extends FXMLController {

    public Button actionButton;
    public Button switchScreen;
    public CheckBox checkbox;
    public HBox hbox;
    public ImageView ivLogin;
    public ImageView ivPass;
    public ImageView ivPass1;
    public Label lSignIn;
    public Label lWrongInput;
    public PasswordField passwordField;
    public PasswordField passwordField1;
    public Scene scene;
    public TextField tfLogIn;
    public VBox vbox;

    @Override
    public void initialize(){
        Main.EVENT_BUS.register(this);
        passwordField.setSkin(new VisiblePasswordFieldSkin(passwordField));
        passwordField1.setSkin(new VisiblePasswordFieldSkin(passwordField1));
        ivLogin.setImage(new Image("file:resources/zYatDw0QMKoOJ89NxdxiiTl7Za5VHfzh"));
        ivPass.setImage(new Image("file:resources/cyL3izewwb7UnCRAwCI47BE8rPwM5Pkz"));
        ivPass1.setImage(new Image("file:resources/cyL3izewwb7UnCRAwCI47BE8rPwM5Pkz"));
    }

    @FXML
    public void buttonSwitchScreen() {
        Main.EVENT_BUS.fire(new ChangeSceneEvent(ChangeSceneEvent.Type.SIGNIN));
        lWrongInput.setVisible(false);
        clearFields();
    }

    @FXML
    public void actionButton() throws ConfigurationException {
        try {
            validateString(tfLogIn.getText());
            validateString(passwordField.getText());
            if (!passwordField.getText().equals(passwordField1.getText())) {
                lWrongInput.setText("Passwords do not match!");
                lWrongInput.setVisible(true);
                return;
            }

            Main.addScheduledTask(() -> Main.handleNewUser(tfLogIn.getText(), passwordField.getText()));
            lWrongInput.setVisible(false);
            clearFields();
            Config.write("keep_signed_in", checkbox.isSelected());
        } catch (IllegalStateException e) {
            lWrongInput.setText(e.getMessage());
            lWrongInput.setVisible(true);
        }
    }

    @EventListener
    public void updateScreen(UpdateScreenEvent event) {
        if (event.destination == UpdateScreenEvent.Destination.SIGN_UP) {
            if (event.type == UpdateScreenEvent.Type.WRONGIO) {
                Platform.runLater(() -> {
                    lWrongInput.setText(event.message);
                    lWrongInput.setVisible(true);
                });
            }
        }
    }

    private void clearFields() {
        tfLogIn.clear();
        passwordField.clear();
        passwordField1.clear();
        checkbox.setSelected(false);
    }
}

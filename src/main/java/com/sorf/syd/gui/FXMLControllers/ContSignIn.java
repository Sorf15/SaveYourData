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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.apache.commons.configuration2.ex.ConfigurationException;

public class ContSignIn extends FXMLController {
    public Scene scene;
    public AnchorPane anchorpane;
    public VBox vbox;
    public Label lSignIn;
//    public GridPane gridpane;
//    public Label lLogin;
//    public Label lPassword;
    public TextField tfLogIn;
    public PasswordField passwordField;
    public Button buttonSignIn;
    public Button buttonSignUp;
    public HBox hbox;
    public CheckBox checkbox;
//    public RadioButton radiobutton;
    public Label lWrongInput;
    public ImageView ivLogin;
    public ImageView ivPass;

    @Override
    public void initialize() {
        Main.EVENT_BUS.register(this);
        passwordField.setSkin(new VisiblePasswordFieldSkin(passwordField));
        ivLogin.setImage(new Image("file:resources/zYatDw0QMKoOJ89NxdxiiTl7Za5VHfzh"));
        ivPass.setImage(new Image("file:resources/cyL3izewwb7UnCRAwCI47BE8rPwM5Pkz"));
//        tfLogIn.setSkin(new CustomTextFieldSkin(tfLogIn, "Login"));
    }

    @EventListener
    public void updateScreen(UpdateScreenEvent event) {
        if (event.destination == UpdateScreenEvent.Destination.SIGN_IN) {
            if (event.type == UpdateScreenEvent.Type.WRONGIO) {
                Platform.runLater(() -> {
                    lWrongInput.setText(event.message);
                    lWrongInput.setVisible(true);
                });
            }
        }
    }

    @FXML
    public void buttonSignUpClicked() {
        Main.EVENT_BUS.fire(new ChangeSceneEvent(ChangeSceneEvent.Type.SIGNUP));
        lWrongInput.setVisible(false);
        clearFields();
    }

    @FXML
    public void buttonSignInClicked() throws ConfigurationException {
        try {
            String login = tfLogIn.getText(), pass = passwordField.getText();
            boolean state = checkbox.isSelected();
            Main.addScheduledTask(() -> {
                try {
                    boolean b=Config.write("keep_signed_in", state);
                } catch (ConfigurationException e) {
                    e.printStackTrace();
                }
                Main.handleUserSignIn(login, pass);});
            lWrongInput.setVisible(false);
            clearFields();
        } catch (IllegalStateException e) {
            lWrongInput.setText(e.getMessage());
            lWrongInput.setVisible(true);
        }
    }

    private void clearFields() {
        tfLogIn.clear();
        passwordField.clear();
        checkbox.setSelected(false);
    }
}

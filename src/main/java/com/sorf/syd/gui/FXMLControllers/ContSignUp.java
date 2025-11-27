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
//    public Scene scene;
//    public AnchorPane anchorpane;
//    public VBox vbox;
//    public Label lSignUp;
//    public Label lLogin;
//    public TextField tfLogIn;
//    public Label lPassword;
//    public PasswordField passwordField;
//    public PasswordField passwordField1;
//    public Label lPassword1;
//    public GridPane gridpane;
//    public HBox hbox;
//    public HBox hbox1;
//    public Label lHasAcc;
//    public Button buttonSignIn;
//    public Button buttonSignUp;
//    public RadioButton radiobutton;
//    public Label lWrongInput;
//    public HBox hbox2;
    
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
        if (tfLogIn.getText().isBlank() || passwordField.getText().isBlank() || passwordField1.getText().isBlank()) {
            lWrongInput.setText("Login or Password fields can not be empty");
            lWrongInput.setVisible(true);
        }  else if (passwordField.getText().length() > 64 || tfLogIn.getText().length() > 64) {
            lWrongInput.setText("Login or password can not be longer than 64 symbols!");
            lWrongInput.setVisible(true);
        } else if (tfLogIn.getText().contains(" ") || passwordField.getText().contains(" ")) {//TODO: redo checks for invalid characters
            lWrongInput.setText("Login or password contains forbidden symbols!");
            lWrongInput.setVisible(true);
        } else if (!passwordField.getText().equals(passwordField1.getText())) {
            lWrongInput.setText("Passwords do not match!");
            lWrongInput.setVisible(true);
        } else {
            Config.write("keep_signed_in", checkbox.isSelected());

            Main.addScheduledTask(() -> Main.handleNewUser(tfLogIn.getText(), passwordField.getText()));
            //super.addToQueue(new Pair<String, Object>("newUser", new String[]{tfLogIn.getText(), passwordField.getText()}));
            lWrongInput.setVisible(false);
            clearFields();
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

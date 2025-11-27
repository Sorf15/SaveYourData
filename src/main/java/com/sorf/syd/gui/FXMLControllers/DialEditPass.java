package com.sorf.syd.gui.FXMLControllers;

import com.sorf.syd.gui.skins.VisiblePasswordFieldSkin;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class DialEditPass extends FXMLController {
    public DialogPane dialogPane;
    public Label lTitle;
    public VBox vBox;
    public GridPane gridPane;
    public Label lName;
    public Label lLogin;
    public Label lPass;
    public TextField tfName;
    public TextField tfLogin;
    public PasswordField passwordField;
    public Label lError;

    @Override
    public void initialize() {
        Button button = (Button) dialogPane.lookupButton(ButtonType.OK);
        button.addEventFilter(ActionEvent.ACTION, event -> {
            if (!validateDialog()) {
                event.consume();
                lError.setVisible(true);
            } else {
                lError.setVisible(false);
            }
        });

        Button button1 = (Button) dialogPane.lookupButton(ButtonType.CANCEL);
        button1.addEventFilter(ActionEvent.ACTION, actionEvent -> {
            clearFields();
            lError.setVisible(false);
        });
        this.passwordField.setSkin(new VisiblePasswordFieldSkin(this.passwordField));
    }

    private boolean validateDialog() {
        return !tfName.getText().isBlank() && !passwordField.getText().isBlank();
    }

    public void clearFields() {
        tfLogin.clear();
        tfName.clear();
        passwordField.clear();
    }
}

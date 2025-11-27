package com.sorf.syd.gui.FXMLControllers;

import com.sorf.syd.gui.skins.VisiblePasswordFieldSkin;
import com.sorf.syd.util.HashUtil;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.security.NoSuchAlgorithmException;

public class DialEditAccPass extends FXMLController {
    public DialogPane dialogPane;
    public Label lTitle;
    public VBox vBox;
    public GridPane gridPane;
    public Label lOldPass;
    public Label lNewPass;
    public Label lConfNewPass;
    public PasswordField pfOld;
    public PasswordField pfNew;
    public PasswordField pfConf;
    public Label lError;

    public String originalHash;

    @Override
    public void initialize() {
        Button button = (Button) dialogPane.lookupButton(ButtonType.OK);
        button.addEventFilter(ActionEvent.ACTION, event -> {
            if (!validateDialog(originalHash)) {
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
        this.pfOld.setSkin(new VisiblePasswordFieldSkin(this.pfOld));
        this.pfNew.setSkin(new VisiblePasswordFieldSkin(this.pfNew));
        this.pfConf.setSkin(new VisiblePasswordFieldSkin(this.pfConf));
    }

    public boolean validateDialog(String originalHash) {
        if (pfOld.getText().isBlank() || pfConf.getText().isBlank() || pfNew.getText().isBlank()) {
            lError.setText("Wrong input!");
            return false;
        }
        try {
            if (!HashUtil.getSHA512(pfOld.getText()).equals(originalHash)) {
                lError.setText("Wrong old password");
                return false;
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        if (!pfNew.getText().equals(pfConf.getText())) {
            lError.setText("Passwords does not match!");
            return false;
        }

        if (pfNew.getText().equals(pfOld.getText())){
            lError.setText("Old and New passwords are the same!");
            return false;
        }

        return true;
    }

    public void clearFields() {
        pfOld.clear();
        pfNew.clear();
        pfConf.clear();
    }
}

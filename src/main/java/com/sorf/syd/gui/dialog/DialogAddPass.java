package com.sorf.syd.gui.dialog;

import com.sorf.syd.Main;
import com.sorf.syd.gui.FXMLControllers.DialAddPass;
import com.sorf.syd.gui.MainController;
import com.sorf.syd.gui.ShadowPassword;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.stage.Modality;
import javafx.util.Callback;

public class DialogAddPass extends Dialog<ShadowPassword> {

    public DialogAddPass() {
        super();
        this.setTitle("Add Password");
        this.setDialogPane(MainController.dAddPass);
        this.setResultConverter();
        MainController.setChildToScene(this);
        this.initModality(Modality.WINDOW_MODAL);
    }

    private void setResultConverter() {
        Callback<ButtonType, ShadowPassword> callbackConverter = buttonType -> {
            if (buttonType == ButtonType.OK) {
                Main.addScheduledTask(() -> {
                    DialAddPass controller = (DialAddPass) MainController.controllers.get("dAddPass");
                    Main.addPasswordToStorage(controller.tfName.getText(), controller.tfLogin.getText(), controller.passwordField.getText());
                    ((DialAddPass) MainController.controllers.get("dAddPass")).clearFields();
                });
            }
            return null;
        };
        setResultConverter(callbackConverter);
    }

}

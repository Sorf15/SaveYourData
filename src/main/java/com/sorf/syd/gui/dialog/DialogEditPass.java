package com.sorf.syd.gui.dialog;

import com.sorf.syd.Main;
import com.sorf.syd.gui.FXMLControllers.DialEditPass;
import com.sorf.syd.gui.MainController;
import com.sorf.syd.gui.ShadowPassword;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.stage.Modality;
import javafx.util.Callback;

public class DialogEditPass extends Dialog<ShadowPassword> {

    private final ShadowPassword password;

    public DialogEditPass(ShadowPassword password) {
        super();
        this.password = password;
        this.setTitle("Edit Password");
        this.setDialogPane(MainController.dEditPass);
        this.setResultConverter();
        MainController.setChildToScene(this);
        this.initModality(Modality.WINDOW_MODAL);
    }

    private void setResultConverter() {
        Callback<ButtonType, ShadowPassword> callbackConverter = buttonType -> {
            if (buttonType == ButtonType.OK) {
                Main.addScheduledTask(() -> {
                    DialEditPass controller = (DialEditPass) MainController.controllers.get("dEditPass");
                    Main.editPasswordInStorage(password.getUuid(),controller.tfName.getText(),
                            controller.tfLogin.getText(), controller.passwordField.getText());
                    ((DialEditPass) MainController.controllers.get("dEditPass")).clearFields();
                });
            }
            return null;
        };
        setResultConverter(callbackConverter);
    }
}

package com.sorf.syd.gui.dialog;

import com.sorf.syd.gui.FXMLControllers.DialEditAccPass;
import com.sorf.syd.gui.MainController;
import com.sorf.syd.util.Logger;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.stage.Modality;
import javafx.util.Callback;

public class DialogEditAccPass extends Dialog<String>{

    public DialogEditAccPass(String originalHash) {
        super();
        this.setTitle("Edit Password");
        this.setDialogPane(MainController.dEditAccPass);
        this.setResultConverter();
        MainController.setChildToScene(this);
        this.initModality(Modality.WINDOW_MODAL);
        ((DialEditAccPass) MainController.controllers.get("dEditAccPass")).originalHash = originalHash;
    }

    private void setResultConverter() {
        Callback<ButtonType, String> callbackConverter = buttonType -> {
            if (buttonType == ButtonType.OK) {
                DialEditAccPass instance = (DialEditAccPass) MainController.controllers.get("dEditAccPass");
                instance.lError.setVisible(false);
                String pass = instance.pfNew.getText();
                instance.clearFields();
                return pass;

            }
            return null;
        };
        setResultConverter(callbackConverter);
    }
}

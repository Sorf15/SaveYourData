package com.sorf.syd.gui.dialog;

import com.sorf.syd.gui.FXMLControllers.DialConfirmation;
import com.sorf.syd.gui.MainController;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.stage.Modality;
import javafx.util.Callback;

public class DialogConfirmation extends Dialog<Boolean> {
    private String text;

    public DialogConfirmation(String text) {
        super();
        this.setTitle("Confirm Action");
        this.setDialogPane(MainController.dConfirm);
        this.setResultConverter();
        MainController.setChildToScene(this);
        this.initModality(Modality.WINDOW_MODAL);
        ((DialConfirmation) MainController.controllers.get("dConfirm")).lTitle.setText(text);

    }

    private void setResultConverter() {
        Callback<ButtonType, Boolean> callbackConverter = buttonType -> {
            return buttonType == ButtonType.YES;
        };
        setResultConverter(callbackConverter);
    }
}

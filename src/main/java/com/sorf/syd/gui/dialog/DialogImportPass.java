package com.sorf.syd.gui.dialog;

import com.sorf.syd.gui.FXMLControllers.DialSelectFromFile;
import com.sorf.syd.gui.MainController;
import com.sorf.syd.util.Logger;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.stage.Modality;
import javafx.util.Callback;

public class DialogImportPass extends Dialog<String[]> {
    private DialSelectFromFile instance;

    public DialogImportPass(String[] list) {
        super();
        this.setTitle("Select Columns");
        this.setDialogPane(MainController.dSelectColumn);
        this.instance = (DialSelectFromFile) MainController.controllers.get("dSelectColumn");
        this.instance.addFields(list);
        this.setResultConverter();
        MainController.setChildToScene(this);
        this.initModality(Modality.WINDOW_MODAL);
    }

    private void setResultConverter() {
        Callback<ButtonType, String[]> callback = buttonType -> {
            if (buttonType == ButtonType.OK) {
                Logger.info("Name: %s",this.instance.cbName.getSelectionModel().getSelectedItem());
                Logger.info("Login: %s",this.instance.cbLogin.getSelectionModel().getSelectedItem());
                Logger.info("Pass: %s",this.instance.cbPassword.getSelectionModel().getSelectedItem());
                return new String[]{
                        this.instance.cbName.getSelectionModel().getSelectedItem(),
                        this.instance.cbLogin.getSelectionModel().getSelectedItem(),
                        this.instance.cbPassword.getSelectionModel().getSelectedItem()
                };
            }
            return null;
        };
        setResultConverter(callback);
    }
}

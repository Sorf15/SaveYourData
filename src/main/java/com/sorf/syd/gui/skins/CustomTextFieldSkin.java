package com.sorf.syd.gui.skins;

import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.skin.TextFieldSkin;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

public class CustomTextFieldSkin extends TextFieldSkin {

    private Label tfLogin;
    private HBox hBox;
    private ImageView icon;
    private boolean initialized = false;

    public CustomTextFieldSkin(TextField control, String text) {

        super(control);
        tfLogin = new Label(text);

        icon = new ImageView("file:resources/zYatDw0QMKoOJ89NxdxiiTl7Za5VHfzh");
        icon.setFitHeight(30);
        icon.setFitWidth(30);

        hBox = new HBox(icon, tfLogin);
        tfLogin.setFocusTraversable(false);
        hBox.toFront();
        getChildren().add(hBox);


        initialized = true;
    }

    @Override
    protected void layoutChildren(double x, double y, double w, double h) {
        super.layoutChildren(x, y, w, h);

        layoutInArea(hBox, x,y,w,h, 0, HPos.LEFT, VPos.BOTTOM);
    }

    @Override
    protected String maskText(String txt) {
        if (initialized) {
            tfLogin.setVisible(txt.isEmpty());
        }
        return txt;
    }
}

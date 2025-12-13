package com.sorf.syd.gui.FXMLControllers;

import com.sorf.syd.Main;
import com.sorf.syd.gui.MainController;
import com.sorf.syd.gui.ShadowPassword;
import com.sorf.syd.gui.dialog.DialogAddPass;
import com.sorf.syd.gui.dialog.DialogEditPass;
import com.sorf.syd.gui.skins.VisiblePasswordFieldSkin;
import com.sorf.syd.util.event.EventListener;
import com.sorf.syd.util.event.PasswordEvent;
import com.sorf.syd.util.event.UpdateScreenEvent;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.util.Callback;

import java.nio.charset.StandardCharsets;

public class ContHome extends FXMLController{
    public AnchorPane anchorPane;
    public VBox mainVBox;
    public TableView<ShadowPassword> tvPassword;
    public TableColumn<ShadowPassword, String> tcDate;
    public TableColumn<ShadowPassword, String> tcName;
    public TableColumn<ShadowPassword, String> tcLogin;
    public TableColumn<ShadowPassword, String> tcPassword;
    public TableColumn<ShadowPassword, Void> tcAction;
    public HBox hBox;
    public Button bAdd;
    public Button bEdit;
    public Button bCopy;
    public Button bDelete;

    private TableView.TableViewSelectionModel<ShadowPassword> tableViewSelectionModel;
    private static boolean initialized = false;

    @Override
    public void initialize() {
        Main.EVENT_BUS.register(this);

        tableViewSelectionModel = tvPassword.getSelectionModel();
        tvPassword.setPlaceholder(new Label("Passwords have not been generated"));
        tcDate.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        tcName.setCellValueFactory(new PropertyValueFactory<>("name"));
        tcLogin.setCellValueFactory(new PropertyValueFactory<>("login"));
        tcPassword.setCellValueFactory(new PropertyValueFactory<>("pass"));


        Callback<TableColumn<ShadowPassword, Void>, TableCell<ShadowPassword, Void>> callback = new Callback<>() {
            @Override
            public TableCell<ShadowPassword, Void> call(TableColumn<ShadowPassword, Void> shadowPasswordVoidTableColumn) {
                return new TableCell<>() {
                    private final SVGPath actionIcon = new SVGPath();
                    private final Button btn = new Button("");

                    {

                        btn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                        btn.setFocusTraversable(false);
                        btn.setScaleX(0.7D);
                        btn.setScaleY(0.7D);
                        btn.setScaleZ(0.7D);
                        actionIcon.setContent(VisiblePasswordFieldSkin.Icons.VIEWER.getContent());
                        btn.setGraphic(actionIcon);
                        btn.setOnAction((ActionEvent event) -> {
                            ShadowPassword data = getTableView().getItems().get(getIndex());
                            if (data.isPassShown()) {
                                data.setPassShown(false);
                                data.setPass("********");
                                tvPassword.refresh();
                            } else {
                                Main.EVENT_BUS.fire(new PasswordEvent(data, bytes -> Platform.runLater(() ->
                                        ((ContHome) MainController.controllers.get("mHome")).setPass(data, new String(bytes, StandardCharsets.UTF_16BE)))));
                            }
                        });
                    }
                    @Override
                    protected void updateItem(Void unused, boolean empty) {
                        super.updateItem(unused, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(btn);
                        }
                    }
                };
            }
        };
        tcAction.setCellFactory(callback);

        initialized = true;
    }

    public static boolean isInitialized() {
        return initialized;
    }

    @EventListener
    public void updateTable(UpdateScreenEvent.Table event) {
        switch (event.timing) {
            case STARTUP:
                tvPassword.getItems().clear();
                event.password.forEach(pass -> tvPassword.getItems().add(pass));
                break;
            case RUNNING:
                if (event.toRemove) {
                    event.password.forEach(pass -> tvPassword.getItems().remove(pass));
                } else {
                    event.password.forEach(pass -> tvPassword.getItems().add(pass));
                }
                break;
            case STOP:
                tvPassword.getItems().clear();
                break;
        }
    }

    @FXML
    public void bAdd() {
        Dialog<ShadowPassword> dialog = new DialogAddPass();
        dialog.showAndWait();
    }

    @FXML
    public void bDel() {
        ShadowPassword password = tableViewSelectionModel.getSelectedItem();
        if (password != null) {
            Main.addScheduledTask(() -> Main.removePassFromStorage(password.getUuid()));
        }
    }

    @FXML
    public void bCopy() {
        ShadowPassword password = tableViewSelectionModel.getSelectedItem();
        if (password == null) {
            return;
        }
        if (password.isPassShown()) {
            copyToClip(password.getPass());
        } else {
            Main.EVENT_BUS.fire(new PasswordEvent(password, bytes -> Platform.runLater(() -> copyToClip(new String(bytes, StandardCharsets.UTF_16BE)))));
        }
    }

    @FXML
    public void bEdit() {
        ShadowPassword password = tableViewSelectionModel.getSelectedItem();
        if (password == null) {
            return;
        }
        Dialog<ShadowPassword> dialog = new DialogEditPass(password);
        DialEditPass controller = (DialEditPass) MainController.controllers.get("dEditPass");
        controller.tfName.setText(password.getName());
        controller.tfLogin.setText(password.getLogin());
        if (password.isPassShown()) {
            controller.passwordField.setText(password.getPass());
            dialog.showAndWait();
        } else {
            Main.EVENT_BUS.fire(new PasswordEvent(password, bytes -> {
                Platform.runLater(() -> {
                    setPass(password, new String(bytes, StandardCharsets.UTF_16BE));
                    controller.passwordField.setText(password.getPass());
                    dialog.showAndWait();
                });
            }));
        }
    }

    public void setPass(ShadowPassword pass, String value) {
        pass.setPassShown(true);
        pass.setPass(value);
        tvPassword.refresh();
    }

}

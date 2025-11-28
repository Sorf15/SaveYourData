package com.sorf.syd.gui.FXMLControllers;

import com.opencsv.CSVReader;
import com.sorf.syd.Main;
import com.sorf.syd.gui.MainController;
import com.sorf.syd.gui.ShadowPassword;
import com.sorf.syd.gui.dialog.DialogImportPass;
import com.sorf.syd.util.ExportPass;
import com.sorf.syd.util.Logger;
import com.sorf.syd.util.event.EventListener;
import com.sorf.syd.util.event.PasswordEvent;
import com.sorf.syd.util.event.UpdateScreenEvent;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ContManage extends FXMLController{
    public AnchorPane anchorPane;
    public VBox mainVBox;
    public TableView<ShadowPassword> tvPassword;
    public TableColumn<ShadowPassword, String> tcDate;
    public TableColumn<ShadowPassword, String> tcName;
    public TableColumn<ShadowPassword, String> tcLogin;
    public TableColumn<ShadowPassword, String> tcPassword;
    public TableColumn<ShadowPassword, Boolean> tcAction;
    public HBox hBox;
    public Button bImport;
    public Button bExport;
    public Button bExportAll;
    public Button bDelete;
    public CheckBox cbSelectAll;

    private TableView.TableViewSelectionModel<ShadowPassword> tableViewSelectionModel;
    private static boolean initialized = false;
    private List<ShadowPassword> selectedPass = new ArrayList<>();

    private FileChooser importFileChooser;
    private FileChooser exportFileChooser;
    public File exportFile = null;

    @Override
    public void initialize() {
        Main.EVENT_BUS.register(this);

        tableViewSelectionModel = tvPassword.getSelectionModel();
        tvPassword.setPlaceholder(new Label("Passwords have not been generated"));
        tcDate.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        tcName.setCellValueFactory(new PropertyValueFactory<>("name"));
        tcLogin.setCellValueFactory(new PropertyValueFactory<>("login"));
        tcPassword.setCellValueFactory(new PropertyValueFactory<>("pass"));

        tcAction.setCellValueFactory(cellData -> cellData.getValue().selectedProperty());
        tcAction.setCellFactory(column -> new EditingCell());
        tcAction.setEditable(true);

        importFileChooser = new FileChooser();
        importFileChooser.setTitle("Open Password File");
        importFileChooser.getExtensionFilters().addAll(
          new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );
        exportFileChooser = new FileChooser();
        exportFileChooser.setTitle("Save Password File");
        exportFileChooser.getExtensionFilters().addAll(
          new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );

        initialized = true;
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

    public static boolean isInitialized() {
        return initialized;
    }

    @FXML
    public void bImport() {
        File file = MainController.showFileChooser(this.importFileChooser, 0);
        if (file == null) {
            return;
        }
        try {
            CSVReader csvReader = new CSVReader(new FileReader(file));
            String[] lines = csvReader.peek();
            if (lines == null) {
                new Alert(Alert.AlertType.ERROR, "Selected file is empty!").show();
                return;
            }

            DialogImportPass dialog = new DialogImportPass(lines);
            Optional<String[]> result = dialog.showAndWait();

            if (result.isPresent()) Main.addScheduledTask(() -> Main.importPasswords(result.get(), csvReader));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @FXML
    public void cbSelectAll() {
        if (cbSelectAll.isSelected()) {
            this.selectedPass.clear();
            this.tvPassword.getItems().forEach(password -> {
                password.setSelected(true);
                this.selectedPass.add(password);
            });
        } else {
            this.selectedPass.clear();
            this.tvPassword.getItems().forEach(password -> password.setSelected(false));
        }
    }

    @FXML
    public void bExport() {
        if (this.selectedPass.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "There is no selected passwords");
            alert.showAndWait();
            return;
        }
        exportFile = MainController.showFileChooser(this.exportFileChooser, 1);
        if (exportFile == null) {
            return;
        }

        Main.exportPassValue = selectedPass.size();
        Main.exportPass = true;

        this.selectedPass.forEach(data -> Main.EVENT_BUS.fire(new PasswordEvent(data, bytes -> {
                ExportPass exportPass = new ExportPass(data.getTimestamp(), data.getName(),
                data.getLogin(), new String(bytes, StandardCharsets.UTF_16BE));
                Main.exportPassList.add(exportPass);
        })));

    }

    @FXML
    public void bExportAll() {
        exportFile = MainController.showFileChooser(this.exportFileChooser, 1);
        if (exportFile == null) {
            return;
        }

        Main.exportPassValue = this.tvPassword.getItems().size();
        Main.exportPass = true;

        this.tvPassword.getItems().forEach(data -> Main.EVENT_BUS.fire(new PasswordEvent(data, bytes -> {
            ExportPass exportPass = new ExportPass(data.getTimestamp(), data.getName(),
                    data.getLogin(), new String(bytes, StandardCharsets.UTF_16BE));
            Main.exportPassList.add(exportPass);
        })));
    }

    //TODO: Confirm Delete
    @FXML
    public void bDel() {
        List<UUID> passToDel = selectedPass.stream().map(ShadowPassword::getUuid).toList();
        if (!passToDel.isEmpty()) Main.addScheduledTask(() -> {
            Main.removePassFromStorage(passToDel);
        });
    }

    public void updateCheckBox() {
        cbSelectAll.selectedProperty().set(selectedPass.size() == tvPassword.getItems().size() && !tvPassword.getItems().isEmpty());
    }

    private static class EditingCell extends TableCell<ShadowPassword, Boolean> {
        private final CheckBox checkBox;

        public EditingCell() {
            checkBox = new CheckBox();
            checkBox.setOnAction(event -> {
                if (getTableRow() != null) {
                    TableRow<ShadowPassword> currentRow = getTableRow();
                    ShadowPassword shadowPassword = currentRow.getItem();
                    shadowPassword.setSelected(checkBox.isSelected());
                    ContManage instance = (ContManage) MainController.controllers.get("mManage");
                    if (checkBox.isSelected()) {
                        instance.selectedPass.add(shadowPassword);
                    } else {
                        instance.selectedPass.remove(shadowPassword);
                    }
                    instance.updateCheckBox();
                }
            });
            setGraphic(checkBox);
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            setEditable(true);
        }

        @Override
        public void updateItem(Boolean item, boolean empty) {
            super.updateItem(item, empty);

            if (empty) {
                setGraphic(null);
            } else {
                checkBox.setSelected(item);
                setGraphic(checkBox);
            }
        }
    }
}

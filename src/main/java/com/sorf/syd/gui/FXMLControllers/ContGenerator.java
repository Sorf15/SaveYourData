package com.sorf.syd.gui.FXMLControllers;

import com.sorf.syd.Main;
import com.sorf.syd.util.AsyncTask;
import com.sorf.syd.user.PasswordGenerator;
import com.sorf.syd.util.event.Event;
import com.sorf.syd.util.event.EventListener;
import com.sorf.syd.util.event.UpdateScreenEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;



public class ContGenerator extends FXMLController {
    public Label lSliderValue;
    public Slider slider;
    public CheckBox cbLowerCase;
    public CheckBox cbUpperCase;
    public CheckBox cbNumbers;
    public TextField tfPassword;
    public Button bGenerate;
    public Button bCopy;
    public TableView<PasswordGenerator.History> tableView;
    public TableColumn<PasswordGenerator.History, String> tbcDate;
    public TableColumn<PasswordGenerator.History, String> tbcPassword;
    private TableView.TableViewSelectionModel<PasswordGenerator.History> tableViewSelectionModel;

    private static boolean initialized = false;

    public static Boolean isInitialized() {
        return initialized;

    }

    @Override
    public void initialize() {
        Main.EVENT_BUS.register(this);
        slider.valueProperty().addListener((observable, oldValue, newValue) -> {
            PasswordGenerator.setPasswordLength(newValue.intValue());
            lSliderValue.setText(String.valueOf(PasswordGenerator.getPasswordLength()));
        });

        cbLowerCase.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (!PasswordGenerator.setLowerCase(cbLowerCase.isSelected())) {
                cbLowerCase.setSelected(true);
            }
        });

        cbUpperCase.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (!PasswordGenerator.setUpperCase(cbUpperCase.isSelected())) {
                cbLowerCase.setSelected(true);
            }
        });

        cbNumbers.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (!PasswordGenerator.setNumbers(cbNumbers.isSelected())) {
                cbLowerCase.setSelected(true);
            }
        });

        initialized = true;

        tableViewSelectionModel = tableView.getSelectionModel();
        tableView.setPlaceholder(new Label("Passwords have not been generated"));
        tbcDate.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        tbcPassword.setCellValueFactory(new PropertyValueFactory<>("password"));
    }

    public PasswordGenerator.History getSelectedItem() {
        return tableViewSelectionModel.getSelectedItem();
    }


    @EventListener
    @SuppressWarnings("unchecked")
    public void updateScreen(UpdateScreenEvent.Generator event) {
        if (event.timing == Event.Timing.STARTUP) {
         AsyncTask.getInstance().invoke(() -> {
             while (!isInitialized()) {
                 Thread.onSpinWait();
             }
             slider.setValue((Integer) event.list.get(0));
             cbLowerCase.setSelected((Boolean) event.list.get(1));
             cbUpperCase.setSelected((Boolean) event.list.get(2));
             cbNumbers.setSelected((Boolean) event.list.get(3));
         });
        } else if (event.timing == Event.Timing.RUNNING) {
            tableView.getItems().clear();
            tableView.getItems().addAll(event.list);
        } else if (event.timing == Event.Timing.STOP) {
            tableView.getItems().clear();
        }
    }

    @FXML
    public void bGenerate() {
        tfPassword.setText(PasswordGenerator.generatePass());
        Main.EVENT_BUS.fire(new UpdateScreenEvent.Generator(PasswordGenerator.History.getHistory(), Event.Timing.RUNNING));
    }

    @FXML
    public void bCopy() {
        copyToClip(tfPassword.getText());
    }

    @FXML
    public void bCopySel() {
        PasswordGenerator.History history = getSelectedItem();
        if (history != null) {
            copyToClip(history.getPassword());
        }
    }

    @FXML
    public void bClearSel() {
        PasswordGenerator.History history = getSelectedItem();
        if (history != null) {
            tableView.getItems().remove(history);
            PasswordGenerator.History.getHistory().remove(history);
        }
    }

    @FXML
    public void bClearAll() {
        tableView.getItems().clear();
        PasswordGenerator.History.clear();
    }

}

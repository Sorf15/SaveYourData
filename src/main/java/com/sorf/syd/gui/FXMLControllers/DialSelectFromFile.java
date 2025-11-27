package com.sorf.syd.gui.FXMLControllers;

import javafx.event.ActionEvent;
import javafx.scene.control.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DialSelectFromFile extends FXMLController {
    public DialogPane dialogPane;
    public ChoiceBox<String> cbName;
    public ChoiceBox<String> cbLogin;
    public ChoiceBox<String> cbPassword;
    public Label lError;
    private List<String> fields = new ArrayList<>();

    @Override
    public void initialize() {
        Button button = (Button) dialogPane.lookupButton(ButtonType.OK);
        button.addEventFilter(ActionEvent.ACTION, event -> {
            if (!validateDialog()) {
                event.consume();
                lError.setVisible(true);
            } else {
                lError.setVisible(false);
            }
        });

        Button button1 = (Button) dialogPane.lookupButton(ButtonType.CANCEL);
        button1.addEventFilter(ActionEvent.ACTION, actionEvent -> clearFields());
    }

    private boolean validateDialog() {
        List<String> copy = new ArrayList<>(fields);
        return copy.remove(cbName.getValue()) && copy.remove(cbLogin.getValue()) && copy.remove(cbPassword.getValue());
    }

    public void clearFields() {
        cbName.getItems().clear();
        cbLogin.getItems().clear();
        cbPassword.getItems().clear();
    }

    public void addFields(String[] fields) {
        clearFields();
        this.fields = Arrays.asList(fields);
        for (String s : fields) {
            cbName.getItems().add(s);
            cbLogin.getItems().add(s);
            cbPassword.getItems().add(s);
        }
    }
}

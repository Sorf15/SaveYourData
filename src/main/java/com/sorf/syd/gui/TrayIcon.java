package com.sorf.syd.gui;

import com.dustinredmond.fxtrayicon.FXTrayIcon;
import com.sorf.syd.Main;
import com.sorf.syd.util.Logger;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;

import javax.swing.*;
import java.io.File;

public class TrayIcon {
    private final static MenuItem hide;
    private final static MenuItem show;
    private final static MenuItem exit;
    private FXTrayIcon trayIcon;

    static {
        exit = new MenuItem("Exit");
        exit.setOnAction(e -> Main.addScheduledTask(Main::setStopped));

        show = new MenuItem("Show App");
        show.setOnAction(e -> MainController.showStage());

        hide = new MenuItem("Hide App");
        hide.setOnAction(e -> MainController.hideStage());


    }


    public TrayIcon(Stage stage, File icon) {
        trayIcon = new FXTrayIcon.Builder(stage, icon)
                .onAction(e -> MainController.showStage())
                .menuItem("Hide App", e -> MainController.hideStage())
                .menuItem("Exit", e -> Main.addScheduledTask(Main::setStopped))
                .build();

        trayIcon.show();

        Logger.info("TrayIcon elements: %d", trayIcon.getMenuItemCount());
    }

    protected void stageShown() {
        SwingUtilities.invokeLater(() -> trayIcon.removeMenuItem(0));
        SwingUtilities.invokeLater(() -> trayIcon.insertMenuItem(hide, 0));
    }

    protected void stageHidden() {
        SwingUtilities.invokeLater(() -> trayIcon.removeMenuItem(0));
        SwingUtilities.invokeLater(() -> trayIcon.insertMenuItem(show, 0));
    }

    protected void terminate() {
        SwingUtilities.invokeLater(trayIcon::clear);
        SwingUtilities.invokeLater(trayIcon::hide);
        Logger.debug("Terminating TrayIcon");
    }
}

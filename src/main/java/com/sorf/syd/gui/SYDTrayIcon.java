package com.sorf.syd.gui;

import com.sorf.syd.Main;
import com.sorf.syd.util.Logger;
import javafx.application.Platform;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.TrayIcon;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class SYDTrayIcon {

    private SystemTray systemTray = null;
    private static Image trayImage;// = Toolkit.getDefaultToolkit().getImage(Main.resources +"/3yBRPoKItRX11xNvTFiA9O6kHU8RUNMz");
    private PopupMenu popupMenu;
    private MenuItem hide;
    private MenuItem show;
    private MenuItem exit;

    private TrayIcon trayIcon;

    public SYDTrayIcon(File image){
        if (SystemTray.isSupported()) {
            systemTray = SystemTray.getSystemTray();
            Platform.setImplicitExit(false);
        }

        loadImage(image);
        popupMenu = new PopupMenu();

        hide = new MenuItem("Hide App");
        show = new MenuItem("Show App");
        exit = new MenuItem("Exit");

        hide.addActionListener(e -> Platform.runLater(MainController::hideStage));
        show.addActionListener(e -> Platform.runLater(MainController::showStage));
        exit.addActionListener(e -> Main.addScheduledTask(Main::setStopped)); //TODo: sometimes it just doesn't work from the first try!
        //https://community.spiceworks.com/topic/138897-java-not-running-appearing-in-system-tray - deleting cache
        //https://stackoverflow.com/questions/29995727/java-systemtray-icon-does-not-always-work - race condition in the internals where a call to the system was taking too long to complete so the java side was giving up.

        popupMenu.add(hide);
        popupMenu.add(exit);

        trayIcon = new TrayIcon(trayImage, "SYD", popupMenu);

        trayIcon.addActionListener(e -> Platform.runLater(MainController::showStage));
        trayIcon.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1)
                Platform.runLater(MainController::showStage);
            }

            @Override
            public void mousePressed(MouseEvent e) {}

            @Override
            public void mouseReleased(MouseEvent e) {}

            @Override
            public void mouseEntered(MouseEvent e) {}

            @Override
            public void mouseExited(MouseEvent e) {}
        });

        try {
            systemTray.add(trayIcon);
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

    private void loadImage(File image) {
        try {
            FileInputStream is = new FileInputStream(image);

            java.awt.Image var4;
            try {
                var4 = ImageIO.read(is).getScaledInstance(16, 16, 4);
            } catch (Throwable var7) {
                try {
                    is.close();
                } catch (Throwable var6) {
                    var7.addSuppressed(var6);
                }

                throw var7;
            }

            is.close();
            trayImage = var4;
        } catch (FileNotFoundException var8) {
            throw new IllegalStateException("Unable to load the Image at the provided path (File not found): " + image.getAbsolutePath(), var8);
        } catch (IOException var9) {
            throw new IllegalStateException("Unable to read the Image at the provided path (perhaps not an image file, or it is corrupt): " + image.getAbsolutePath(), var9);
        }
    }

    protected void stageShown() {
        popupMenu.remove(0);
        popupMenu.insert(hide, 0);
    }

    protected void stageHidden() {
        popupMenu.remove(0);
        popupMenu.insert(show, 0);
    }

    protected void terminate() {
        if (systemTray == null || trayIcon == null) {
            return;
        }

        systemTray.remove(trayIcon);
        Logger.debug("Terminating TrayIcon");
    }
}

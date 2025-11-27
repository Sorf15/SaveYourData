package com.sorf.syd.gui;

import com.sorf.syd.util.TimeUtil;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.util.*;

public class ShadowPassword {
    private final Date timestamp;
    private final String name;
    private final String login;
    private volatile String pass = "********";
    private final UUID uuid;
    private boolean passShown = false;
    private SimpleBooleanProperty selected = new SimpleBooleanProperty(false);
    private static Map<UUID, ShadowPassword> passwordMap = Collections.synchronizedMap(new HashMap<>());

    public ShadowPassword(Date timestamp, String name, String login, UUID uuid) {
        this.timestamp = timestamp;
        this.name = name;
        this.login = login;
        this.uuid = uuid;
        passwordMap.put(this.uuid, this);
    }

    public static Map<UUID, ShadowPassword> getPasswordMap() {
        return passwordMap;
    }

    //USED IN FX TABLE
    public String getTimestamp() {
        return TimeUtil.getFullTime(timestamp);
    }

    //USED IN FX TABLE
    public String getPass() {
        return this.pass;
    }

    //USED IN FX TABLE
    public String getName() {
        return name;
    }

    //USED IN FX TABLE
    public String getLogin() {
        return login;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public UUID getUuid() {
        return this.uuid;
    }

    public long salt() {
        return this.timestamp.getTime();
    }

    public boolean isPassShown() {
        return passShown;
    }

    public void setPassShown(boolean passShown) {
        this.passShown = passShown;
    }

    public SimpleBooleanProperty selectedProperty() {
        return this.selected;
    }

    //USED IN FX TABLE
    public boolean isSelected() {
        return selected.get();
    }

    //USED IN FX TABLE
    public void setSelected(boolean selected) {
        this.selected.set(selected);
    }
}

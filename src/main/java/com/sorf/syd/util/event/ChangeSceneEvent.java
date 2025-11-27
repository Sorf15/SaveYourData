package com.sorf.syd.util.event;

import org.jetbrains.annotations.NotNull;

public class ChangeSceneEvent extends Event {
    public enum Type {
        SIGNUP, SIGNIN, MAINAPP, EXIT, UNKNOWN;
    }
    public final Type type;
    private static ListenerList listeners = new ListenerList();

    public ChangeSceneEvent(){
        this.type = Type.UNKNOWN;
    }

    public ChangeSceneEvent(@NotNull Type type) {
        this.type = type;
    }

    public ListenerList getListenerList()
    {
        return listeners;
    }


}

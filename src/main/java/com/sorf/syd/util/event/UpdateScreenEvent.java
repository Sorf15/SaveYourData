package com.sorf.syd.util.event;

import com.sorf.syd.gui.ShadowPassword;

import java.util.ArrayList;
import java.util.List;

public class UpdateScreenEvent extends Event {
    public enum Type {
        WRONGIO, RECIEVEDATA, CHANGEMENU, UNKNOWN;
    }

    public enum State {
        HOME, MANAGE, GENERATOR, SETTINGS, WIFI, DOCS, UNKNOWN;
    }

    public enum Destination {
        SIGN_UP, SIGN_IN, MAIN_APP, UNKNOWN;
    }

    public final Type type;
    public final String message;
    public final Destination destination;
    public final State state;
    private static ListenerList listeners = new ListenerList();

    public UpdateScreenEvent(){
        this.type = Type.UNKNOWN;
        this.message = "";
        this.destination = Destination.UNKNOWN;
        this.state = State.UNKNOWN;
    }

    public UpdateScreenEvent(String message, Destination destination) {
        this.type = Type.WRONGIO;
        this.message = message;
        this.destination = destination;
        this.state = State.UNKNOWN;
    }

    public UpdateScreenEvent(State state, Destination destination) {
        this.type = Type.CHANGEMENU;
        this.message = "";
        this.destination = destination;
        this.state = state;
    }

    private UpdateScreenEvent(Type type, String message, Destination destination, State state) {
        this.type = type;
        this.message = message;
        this.destination = destination;
        this.state = state;
    }

    public ListenerList getListenerList()
    {
        return listeners;
    }


    public static class Generator extends UpdateScreenEvent {
        private static ListenerList listeners = new ListenerList();
        public final List list;
        public final Timing timing;

        public Generator() {
            super();
            this.list = new ArrayList();
            this.timing = Timing.DEFAULT;
        }

        public Generator(List list, Timing timing) {
            super(Type.RECIEVEDATA, "", Destination.MAIN_APP, State.UNKNOWN);
            this.list = list;
            this.timing = timing;
        }

        @Override
        public ListenerList getListenerList()
        {
            return listeners;
        }
    }

    public static class Table extends UpdateScreenEvent {
        private static ListenerList listeners = new ListenerList();
        public final Timing timing;
        public final List<ShadowPassword> password;
        public final boolean toRemove;

        public Table() {
            super();
            this.timing = Timing.DEFAULT;
            this.password = null;
            this.toRemove = false;
        }


        public Table(ShadowPassword pass, Timing timing, boolean toRemove) {
            super(Type.RECIEVEDATA, "", Destination.MAIN_APP, State.UNKNOWN);
            this.timing = timing;
            List<ShadowPassword> list = new ArrayList<>();
            list.add(pass);
            this.password = list;
            this.toRemove = toRemove;
        }

        public Table(List<ShadowPassword> passwords, Timing timing, boolean toRemove) {
            super(Type.RECIEVEDATA, "", Destination.MAIN_APP, State.UNKNOWN);
            this.timing = timing;
            this.password = passwords;
            this.toRemove = toRemove;
        }

        @Override
        public ListenerList getListenerList() {
            return listeners;
        }
    }
}

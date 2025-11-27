package com.sorf.syd.util.event;

public class StopEvent extends Event {
    private static ListenerList list = new ListenerList();

    public StopEvent() {}

    @Override
    public ListenerList getListenerList() {
        return list;
    }
}

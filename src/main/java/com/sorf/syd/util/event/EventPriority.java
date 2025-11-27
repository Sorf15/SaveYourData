package com.sorf.syd.util.event;

public enum EventPriority implements IEventListener {
    HIGHEST,
    HIGH,
    NORMAL,
    LOW,
    LOWEST;

    @Override
    public void invoke(Event event) {
        event.setPhase(this);
    }
}

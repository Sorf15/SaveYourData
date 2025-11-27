package com.sorf.syd.util.event;

import com.sorf.syd.gui.ShadowPassword;
import com.sorf.syd.token.Token;
import com.sorf.syd.util.ICall;

import java.util.concurrent.Callable;

public class PasswordEvent extends Event {
    private static ListenerList list = new ListenerList();


    private final ShadowPassword password;
    private final Token token;
    private final ICall<byte[]> resultCallable;

    public PasswordEvent() {
        this.password = null;
        this.token = null;
        this.resultCallable = null;
    }

    public PasswordEvent(ShadowPassword password, ICall<byte[]> result) {
        this.password = password;
        this.token = new Token(this.password.getUuid(), this.password.salt());
        this.resultCallable = result;
    }

    public PasswordEvent(Token token) {
        this.password = null;
        this.token = token;
        this.resultCallable = null;
    }

    public ShadowPassword getPassword() {
        return password;
    }

    public Token getToken() {
        return token;
    }

    public ICall<byte[]> getResultCallable() {
        return this.resultCallable;
    }

    @Override
    public ListenerList getListenerList() {
        return list;
    }

}

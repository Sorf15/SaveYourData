package com.sorf.syd.util;

public class ExportPass {
    private final String timestamp;
    private final String name;
    private final String login;
    private final String pass;

    public ExportPass(String timestamp, String name, String login, String pass) {
        this.timestamp = timestamp;
        this.name = name;
        this.login = login;
        this.pass = pass;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getName() {
        return name;
    }

    public String getLogin() {
        return login;
    }

    public String getPass() {
        return pass;
    }
}

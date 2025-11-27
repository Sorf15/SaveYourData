package com.sorf.syd.user;

public class UserException extends Exception {

    private final int error_code;

    public UserException() {
        super();
        this.error_code = 0;
    }

    public UserException(String message) {
        super(message);
        this.error_code = 0;
    }

    public UserException(String message, int status_code) {
        super(message);
        this.error_code = status_code;
    }

    public int getErrorCode() {
        return error_code;
    }

}

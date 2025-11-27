package com.sorf.syd.crypto;

import java.security.Key;

public interface IKeyWrapping {

    byte[] encrypt(Key keyToWrap, Key key) throws Exception;

    Key decrypt(byte[] bytes, Key key) throws Exception;

    byte[] encryptWithBase64(Key keyToWrap, Key key) throws Exception;

    Key decryptWithBase64(byte[] bytes, Key key) throws Exception;
}

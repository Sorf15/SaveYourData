package com.sorf.syd.crypto;

import javax.crypto.spec.IvParameterSpec;
import java.security.Key;

public interface ICipher {

    byte[] encrypt(byte[] bytes, Key key, IvParameterSpec iv) throws Exception;

    byte[] decrypt(byte[] bytes, Key key, IvParameterSpec iv) throws Exception;

    byte[] encryptWithBase64(byte[] bytes, Key key, IvParameterSpec iv) throws Exception;

    byte[] decryptWithBase64(byte[] bytes, Key key, IvParameterSpec iv) throws Exception;


}

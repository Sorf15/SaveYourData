package com.sorf.syd.token;

import com.sorf.syd.Main;
import com.sorf.syd.crypto.AESCipher;
import com.sorf.syd.crypto.AESKWCipher;
import com.sorf.syd.crypto.ECCCipher;
import com.sorf.syd.crypto.KeyManager;
import com.sorf.syd.gui.ShadowPassword;
import com.sorf.syd.util.ICall;
import com.sorf.syd.util.event.EventListener;
import com.sorf.syd.util.event.PasswordEvent;
import com.sorf.syd.util.event.StopEvent;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class TokenThread implements Runnable {

    private boolean isStopped = false;
    private Map<Token, ICall<byte[]>> resultMap = new ConcurrentHashMap<>();
    private List<Token> tokenList = new CopyOnWriteArrayList<>();
    private Token tokenToRemove = null;

    public TokenThread() {
        Main.EVENT_BUS.register(this);
    }

    @Override
    public void run() {
        Thread.currentThread().setName("TokenThread");
        while (!isStopped) {
            if (this.tokenToRemove != null) {
                tokenList.remove(this.tokenToRemove);
                this.tokenToRemove = null;
            }

            resultMap.keySet().forEach(token -> {
                Token passToken = findMatch(token);
                if (passToken == null) {
                    return;
                }
                try {
                     SecretKey secretKey = Main.KEY_MANAGER.generateKey(passToken.getKey());
                     PrivateKey privateKey = (PrivateKey) AESKWCipher.getInstanceECC().decrypt(passToken.getPrivateKey(), secretKey);
                     byte[] keyBytes = ECCCipher.getInstance().decryptWithBase64(passToken.getEncodedKey(), privateKey, null);
                     SecretKey keyToStorage = new SecretKeySpec(keyBytes, "AES");

                     byte[] decrypted = AESCipher.getInstance().decryptWithBase64(passToken.getPass(), keyToStorage, KeyManager.generateIV(keyToStorage.getEncoded()));
                     resultMap.get(passToken).call(decrypted);
                     //TODO: make here proper exception
                 } catch (InvalidKeySpecException | InvalidAlgorithmParameterException
                        | IllegalBlockSizeException | BadPaddingException | InvalidKeyException | NoSuchAlgorithmException e) {
                     e.printStackTrace();
                 } finally {
                     this.tokenToRemove = passToken;
                 }
            });
        }
    }

    @EventListener
    public void stop(StopEvent event) {
        this.isStopped = true;
    }

    @EventListener
    public void decryptPassword(PasswordEvent event) {
        ShadowPassword instance = event.getPassword();
        Token token = event.getToken();
        if (Instant.now().isAfter(token.getInstant().plusSeconds(60*5L))) {
            throw new IllegalStateException("Expired token!");
        }

        if (token.getVersion() == 1) {
            if (instance == null  || event.getResult() == null) {
                throw new NullPointerException("ShadowPassword is null!");
            }
            resultMap.put(token, event.getResultCallable());
        } else if (token.getVersion() == 2) {
            tokenList.add(token);
        } else {
            throw new IllegalArgumentException("Invalid Token version!");
        }
    }

    private Token findMatch(Token token) {
        for (Token token1 : tokenList) {
            if (token1.equals(token)) {
                return token1;
            }
        }
        return null;
    }


}

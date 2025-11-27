package com.sorf.syd.token;

import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.UUID;

public class Token {
    private final UUID uuid;
    private final Instant instant;
    private final long salt;
    private final int version;
    private final byte[] pass;
    private final byte[] key;
    private final byte[] encodedKey;
    private final byte[] privateKey;

    public Token(@NotNull UUID uuid, long salt) {
        this.uuid = uuid;
        this.instant = Instant.now();
        this.salt = salt;
        this.version = 1;
        this.key = null;
        this.pass = null;
        this.encodedKey = null;
        this.privateKey = null;
    }

    public Token(@NotNull UUID uuid, long salt, byte[] pass, byte[] key, byte @NotNull [] encodedKey, byte @NotNull [] privateKey) {
        this.uuid = uuid;
        this.instant = Instant.now();
        this.salt = salt;
        this.version = 2;
        this.pass = pass;
        this.key = key;
        this.encodedKey = encodedKey;
        this.privateKey = privateKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Token)) return false;

        Token token = (Token) o;

        if (salt != token.salt) return false;
        return uuid.equals(token.uuid);
    }


    @Override
    public int hashCode() {
        int result = uuid.hashCode();
        result = 31 * result + (int) (salt ^ (salt >>> 32));
        return result;
    }

    int getVersion() {
        return this.version;
    }

    UUID getUuid() {
        return this.uuid;
    }

    long getSalt() {
        return this.salt;
    }

    Instant getInstant() {
        return this.instant;
    }

    byte[] getPass() {
        return this.pass;
    }

    byte[] getKey() {
        return this.key;
    }

    byte[] getEncodedKey() {
        return encodedKey;
    }

    byte[] getPrivateKey() {
        return privateKey;
    }
}

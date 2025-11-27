package com.sorf.syd.util;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;

public class UUIDUtil {

    // Domain Name System
    public static final UUID NAMESPACE_DNS = new UUID(0x6ba7b8109dad11d1L, 0x80b400c04fd430c8L);
    // Uniform Resource Locator
    public static final UUID NAMESPACE_URL = new UUID(0x6ba7b8119dad11d1L, 0x80b400c04fd430c8L);
    // ISO Object ID
    public static final UUID NAMESPACE_ISO_OID = new UUID(0x6ba7b8129dad11d1L, 0x80b400c04fd430c8L);
    // X.500 Distinguished Name
    public static final UUID NAMESPACE_X500_DN = new UUID(0x6ba7b8149dad11d1L, 0x80b400c04fd430c8L);

    private static final int VERSION_3 = 3;
    private static final int VERSION_5 = 5;

    private static final String MESSAGE_DIGEST_MD5 = "MD5"; // UUIDv3
    private static final String MESSAGE_DIGEST_SHA1 = "SHA-1"; // UUIDv5

    private static UUID getHashUuid(UUID namespace, String name, String algorithm, int version) {

        final byte[] hash;
        final MessageDigest hasher;

        try {
            hasher = MessageDigest.getInstance(algorithm);

            if (namespace != null) {
                hasher.update(toBytes(namespace.getMostSignificantBits()));
                hasher.update(toBytes(namespace.getLeastSignificantBits()));
            }

            hash = hasher.digest(name.getBytes(StandardCharsets.UTF_8));
            long msb = toNumber(hash, 0, 8);
            long lsb = toNumber(hash, 8, 16);
            msb = (msb & 0xffffffffffff0fffL) | (version & 0x0f) << 12;
            lsb = (lsb & 0x3fffffffffffffffL) | 0x8000000000000000L;

            return new UUID(msb, lsb);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Message digest algorithm not supported.");
        }
    }

    public static UUID getMd5Uuid(String string) {
        return getHashUuid(null, string, MESSAGE_DIGEST_MD5, VERSION_3);
    }

    public static UUID getSha1Uuid(String string) {
        return getHashUuid(null, string, MESSAGE_DIGEST_SHA1, VERSION_5);
    }

    public static UUID getMd5Uuid(UUID namespace, String string) {
        return getHashUuid(namespace, string, MESSAGE_DIGEST_MD5, VERSION_3);
    }

    public static UUID getSha1Uuid(UUID namespace, String string) {
        return getHashUuid(namespace, string, MESSAGE_DIGEST_SHA1, VERSION_5);
    }

    private static byte[] toBytes(final long number) {
        return new byte[]{(byte) (number >>> 56), (byte) (number >>> 48), (byte) (number >>> 40),
                (byte) (number >>> 32), (byte) (number >>> 24), (byte) (number >>> 16), (byte) (number >>> 8),
                (byte) (number)};
    }

    private static long toNumber(final byte[] bytes, final int start, final int length) {
        long result = 0;
        for (int i = start; i < length; i++) {
            result = (result << 8) | (bytes[i] & 0xff);
        }
        return result;
    }

    public static UUID getUUIDfromString(String string) {
        return getSha1Uuid(string);
    }

    public static byte[] convertUUIDToBytes(UUID uuid) {
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return bb.array();
    }

    public static byte[] convertUUIDToBytesWithBase64(UUID uuid) {
        return Base64.getEncoder().encode(convertUUIDToBytes(uuid));
    }

    public static UUID convertBytesToUUID(byte[] bytes) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        long high = byteBuffer.getLong();
        long low = byteBuffer.getLong();
        return new UUID(high, low);
    }

    public static UUID convertBytesWithBase64ToUUID(byte[] bytes) {
        byte[] uuid = Base64.getDecoder().decode(bytes);
        return convertBytesToUUID(uuid);
    }
}
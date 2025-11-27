package com.sorf.syd.crypto;

import org.jetbrains.annotations.NotNull;

public abstract class AbstractInstance implements ICipher {

    /**
     * Adds space characters at the end of the string to avoid {@link javax.crypto.IllegalBlockSizeException IllegalBlockSizeException}.
     * String length will be multiple of 8.
     *
     * @param inputString string to be trimmed for the encryption
     * @return String that is ready for encryption
     */
    public static String trimString(@NotNull final String inputString) {
        String copy = inputString;
        while (copy.length() % 8 != 0 ) {
            copy = String.join("", copy, " ");
        }
        return copy;
    }

    /**
     * Adds space characters at the end of the string.
     *
     * @implNote If (length - inputString) < 0, then {@link AbstractInstance#trimString(String)}
     *
     * @param inputString string to be trimmed
     * @param length preferred length
     * @return String that is trimmed for the specified length
     */
    public static String trimString(@NotNull final String inputString, int length) {
        if (inputString.length() < length) {
            return trimString(inputString);
        }
        String copy = inputString;
        while (copy.length() % length != 0 ) {
            copy = String.join("", copy, " ");
        }
        return copy;
    }
}

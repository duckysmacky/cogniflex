package io.github.duckysmacky.cogniflex.hashing;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Objects;

public class PhotoHasher implements Hasher<byte[]> {

    private static final String ALGORITHM = "SHA-256";

    @Override
    public String algorithm() {
        return ALGORITHM;
    }

    @Override
    public String hash(byte[] value) {
        Objects.requireNonNull(value, "Photo bytes must not be null");

        MessageDigest digest = createDigest();
        byte[] hash = digest.digest(value);

        return HexFormat.of().formatHex(hash);
    }

    private MessageDigest createDigest() {
        try {
            return MessageDigest.getInstance(ALGORITHM);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Hash algorithm is not available: " + ALGORITHM, ex);
        }
    }
}

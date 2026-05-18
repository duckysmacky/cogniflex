package io.github.duckysmacky.cogniflex.hashing;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Objects;

public class TextHasher implements Hasher<String> {

    private static final String ALGORITHM = "SHA-256";

    @Override
    public String algorithm() {
        return ALGORITHM;
    }

    @Override
    public String hash(String value) {
        Objects.requireNonNull(value, "Text must not be null");

        MessageDigest digest = createDigest();
        byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));

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

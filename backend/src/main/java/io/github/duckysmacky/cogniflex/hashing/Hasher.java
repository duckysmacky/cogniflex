package io.github.duckysmacky.cogniflex.hashing;

public interface Hasher<T> {

    String algorithm();

    String hash(T value);
}

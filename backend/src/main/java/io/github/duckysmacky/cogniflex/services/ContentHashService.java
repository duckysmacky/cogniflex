package io.github.duckysmacky.cogniflex.services;

import io.github.duckysmacky.cogniflex.dto.ContentHash;
import io.github.duckysmacky.cogniflex.enums.MediaType;
import io.github.duckysmacky.cogniflex.hashing.Hasher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class ContentHashService {

    private final Hasher<String> textHasher;
    private final Hasher<byte[]> photoHasher;
    private final Hasher<byte[]> videoHasher;

    public ContentHashService(
            @Qualifier("textHasher") Hasher<String> textHasher,
            @Qualifier("photoHasher") Hasher<byte[]> photoHasher,
            @Qualifier("videoHasher") Hasher<byte[]> videoHasher
    ) {
        this.textHasher = textHasher;
        this.photoHasher = photoHasher;
        this.videoHasher = videoHasher;
    }

    public ContentHash hashText(String text) {
        return new ContentHash(
                textHasher.algorithm(),
                textHasher.hash(text)
        );
    }

    public ContentHash hashMedia(MediaType mediaType, byte[] content) {
        Hasher<byte[]> hasher = switch (mediaType) {
            case IMAGE -> photoHasher;
            case VIDEO -> videoHasher;
        };

        return new ContentHash(
                hasher.algorithm(),
                hasher.hash(content)
        );
    }
}
package io.github.duckysmacky.cogniflex.analysis;

import io.github.duckysmacky.cogniflex.processing.media.ParsedMedia;
import io.github.duckysmacky.cogniflex.processing.text.PreprocessedText;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@Component
public class ContentItemFactory {
    public static final String TEXT_ATTRIBUTE = "text";
    public static final String NORMALIZED_TEXT_ATTRIBUTE = "normalizedText";
    public static final String CONTENT_TYPE_ATTRIBUTE = "contentType";
    public static final String SIZE_ATTRIBUTE = "size";
    public static final String EXTENSION_ATTRIBUTE = "extension";
    public static final String HIDDEN_CHARACTERS_ATTRIBUTE = "hiddenCharacters";
    public static final String LOCALE_ATTRIBUTE = "locale";
    public static final String FORMAT_ATTRIBUTE = "format";
    public static final String WIDTH_ATTRIBUTE = "width";
    public static final String HEIGHT_ATTRIBUTE = "height";

    public ContentItem fromText(PreprocessedText text) {
        if (text == null) {
            throw new IllegalArgumentException("Preprocessed text is required");
        }

        Map<String, String> attributes = new LinkedHashMap<>();
        attributes.put(TEXT_ATTRIBUTE, text.modelInput());
        attributes.put(NORMALIZED_TEXT_ATTRIBUTE, text.normalizedText());
        attributes.put(HIDDEN_CHARACTERS_ATTRIBUTE, Integer.toString(text.stats().hiddenCharacters()));
        attributes.put(LOCALE_ATTRIBUTE, languageTag(text.locale()));

        return new ContentItem(
            ContentType.TEXT,
            null,
            null,
            null,
            attributes
        );
    }

    public ContentItem fromMedia(ParsedMedia media) {
        if (media == null) {
            throw new IllegalArgumentException("Parsed media is required");
        }

        Map<String, String> attributes = new LinkedHashMap<>();
        putIfPresent(attributes, CONTENT_TYPE_ATTRIBUTE, media.contentType());
        putIfPresent(attributes, EXTENSION_ATTRIBUTE, media.extension());
        attributes.put(SIZE_ATTRIBUTE, Long.toString(media.size()));
        if (media.format() != null) {
            attributes.put(FORMAT_ATTRIBUTE, media.format().name().toLowerCase(Locale.ROOT));
        }
        if (media.dimensions() != null) {
            attributes.put(WIDTH_ATTRIBUTE, Integer.toString(media.dimensions().width()));
            attributes.put(HEIGHT_ATTRIBUTE, Integer.toString(media.dimensions().height()));
        }

        return new ContentItem(
            ContentType.from(InputType.MEDIA, media.mediaType()),
            null,
            media.filename(),
            media.bytes(),
            attributes
        );
    }

    private void putIfPresent(Map<String, String> attributes, String key, String value) {
        if (value != null && !value.isBlank()) {
            attributes.put(key, value);
        }
    }

    private String languageTag(Locale locale) {
        Locale effectiveLocale = locale == null ? Locale.ROOT : locale;
        return effectiveLocale.toLanguageTag();
    }
}

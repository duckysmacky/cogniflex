package io.github.duckysmacky.cogniflex.converters;

import io.github.duckysmacky.cogniflex.enums.DetectionKind;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class DetectionKindConverter implements AttributeConverter<DetectionKind, Integer> {

    @Override
    public Integer convertToDatabaseColumn(DetectionKind attribute) {
        return attribute == null ? null : attribute.getCode();
    }

    @Override
    public DetectionKind convertToEntityAttribute(Integer dbData) {
        return dbData == null ? null : DetectionKind.fromCode(dbData);
    }
}

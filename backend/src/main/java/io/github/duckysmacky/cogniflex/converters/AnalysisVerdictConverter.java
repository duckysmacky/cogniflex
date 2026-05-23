package io.github.duckysmacky.cogniflex.converters;

import io.github.duckysmacky.cogniflex.analysis.AnalysisVerdict;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class AnalysisVerdictConverter implements AttributeConverter<AnalysisVerdict, String> {

    @Override
    public String convertToDatabaseColumn(AnalysisVerdict attribute) {
        return attribute == null ? null : attribute.toString();
    }

    @Override
    public AnalysisVerdict convertToEntityAttribute(String dbData) {
        return dbData == null ? null : AnalysisVerdict.fromString(dbData);
    }
}

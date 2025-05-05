package com.freelancer.portal.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Custom deserializer for LocalDateTime that can handle both date-only and 
 * full datetime formats.
 */
public class FlexibleLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @Override
    public LocalDateTime deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        String dateText = parser.getText();
        
        try {
            // First try to parse as full datetime
            return LocalDateTime.parse(dateText, DATE_TIME_FORMATTER);
        } catch (DateTimeParseException e1) {
            try {
                // If that fails, try to parse as date-only and set time to midnight
                LocalDate date = LocalDate.parse(dateText, DATE_FORMATTER);
                return date.atStartOfDay();
            } catch (DateTimeParseException e2) {
                // If all standard formats fail, try the ISO format as fallback
                try {
                    return LocalDateTime.parse(dateText);
                } catch (DateTimeParseException e3) {
                    throw new IOException("Failed to parse date value '" + dateText + 
                                         "': not in any of the supported formats", e3);
                }
            }
        }
    }
}
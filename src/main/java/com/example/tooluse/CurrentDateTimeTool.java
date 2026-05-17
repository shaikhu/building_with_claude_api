package com.example.tooluse;

import com.anthropic.core.JsonValue;
import com.anthropic.models.messages.Tool;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class CurrentDateTimeTool {
    private static final String DEFAULT_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static final Tool DEFINITION = Tool.builder()
            .name("get_current_date_time")
            .description("Returns the current local date and/or time as a formatted string. Use this tool whenever the user asks for the current date, time, or a combined datetime value. The format is specified using Java DateTimeFormatter pattern syntax (e.g., 'yyyy-MM-dd' for date only, 'HH:mm:ss' for time only, 'yyyy-MM-dd HH:mm:ss' for full datetime). If the format pattern is invalid, the tool returns an error string describing the failure rather than throwing an exception. Do not use this tool for timezone conversions or historical/future datetime calculations.")
            .inputSchema(Tool.InputSchema.builder()
                    .properties(Tool.InputSchema.Properties.builder()
                            .putAdditionalProperty("format", JsonValue.from(Map.of(
                                    "type", "string",
                                    "description", "A Java DateTimeFormatter pattern string that controls the output format. Common symbols: 'yyyy' = 4-digit year, 'MM' = 2-digit month, 'dd' = 2-digit day, 'HH' = 24-hour hour, 'mm' = minutes, 'ss' = seconds. The pattern is case-sensitive. An invalid pattern will not throw — it returns an error message string instead."
                            )))
                            .build())
                    .addRequired("format")
                    .build())
            .build();

    public static String getCurrentDateTime(String format) {
        if (format == null || format.isEmpty()) {
            format = DEFAULT_FORMAT;
        }

        try {
            return LocalDateTime.now().format(DateTimeFormatter.ofPattern(format));
        } catch (DateTimeException e) {
            return "error creating date using format %s".formatted(format);
        }
    }
}

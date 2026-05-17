package com.example.tooluse;

import com.anthropic.core.JsonValue;
import com.anthropic.models.messages.Tool;

import java.util.Map;

public class CoolNumberTool {

    public static final Tool DEFINITION = Tool.builder()
            .name("make_number_cool")
            .description("Return the supplied number but 'cooler'. Use this tool whenever the user asks to make a number cooler")
            .inputSchema(Tool.InputSchema.builder()
                    .properties(Tool.InputSchema.Properties.builder()
                            .putAdditionalProperty("num", JsonValue.from(Map.of(
                                    "type", "integer",
                                    "description", "A non negative integer number e.g. 10, 20 etc. An invalid pattern will not throw — it returns an error message string instead."
                            )))
                            .build())
                    .addRequired("num")
                    .build())
            .build();

    public static int makeCool(int num) {
        return num * 2;
    }
}

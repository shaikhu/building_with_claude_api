package com.example.evaluation;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.regex.PatternSyntaxException;
import java.util.regex.Pattern;

/**
 * Validate the code samples created by Claude.
 *
 * Simple scoring by returning 10 if the code is valid/compiles, 0 otherwise.
 */
public class SyntaxValidator {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static int validateJson(String text) {
        try {
            MAPPER.readTree(text.strip());
            return 10;
        } catch (Exception e) {
            return 0;
        }
    }

    public static int validateRegex(String text) {
        try {
            Pattern.compile(text.strip());
            return 10;
        } catch (PatternSyntaxException e) {
            return 0;
        }
    }

    public static int validatePython(String text) {
        try {
            Process process = new ProcessBuilder("python3", "-c", "import ast, sys; ast.parse(sys.stdin.read())")
                    .redirectErrorStream(true)
                    .start();
            process.getOutputStream().write(text.strip().getBytes());
            process.getOutputStream().close();
            return process.waitFor() == 0 ? 10 : 0;
        } catch (Exception e) {
            return 0;
        }
    }
}

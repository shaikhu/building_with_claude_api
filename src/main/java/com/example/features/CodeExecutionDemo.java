package com.example.features;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.core.MultipartField;
import com.anthropic.models.beta.AnthropicBeta;
import com.anthropic.models.beta.files.FileMetadata;
import com.anthropic.models.beta.files.FileUploadParams;
import com.anthropic.core.http.HttpResponse;
import com.anthropic.models.beta.messages.BetaCodeExecutionOutputBlock;
import com.anthropic.models.beta.messages.BetaCodeExecutionResultBlock;
import com.anthropic.models.beta.messages.BetaCodeExecutionTool20250522;
import com.anthropic.models.beta.messages.BetaCodeExecutionToolResultBlock;
import com.anthropic.models.beta.messages.BetaCodeExecutionToolResultBlockContent;
import com.anthropic.models.beta.messages.BetaContentBlock;
import com.anthropic.models.beta.messages.BetaContentBlockParam;
import com.anthropic.models.beta.messages.BetaFileDocumentSource;
import com.anthropic.models.beta.messages.BetaMessage;
import com.anthropic.models.beta.messages.BetaRequestDocumentBlock;
import com.anthropic.models.beta.messages.BetaTextBlockParam;
import com.anthropic.models.beta.messages.BetaToolUseBlock;
import com.anthropic.models.beta.messages.MessageCreateParams;
import com.anthropic.models.messages.Model;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class CodeExecutionDemo {
    private static final String PROMPT = """
            Run a detailed analysis to determine major drivers of churn.
            Your final output should include at least one detailed plot summarizing your findings.

            Critical note: Every time you execute code, you're starting with a completely clean slate.
            No variables or library imports from previous executions exist. You need to redeclare/reimport all variables/libraries.
            """;

    public static void main(String... args) throws Exception {
        AnthropicClient client = AnthropicOkHttpClient.fromEnv();

        // Step 1: Upload the CSV via the Files API
        Path csvPath = Paths.get(CodeExecutionDemo.class.getResource("streaming.csv").toURI());

        MultipartField<InputStream> fileField = MultipartField.<InputStream>builder()
                .value(Files.newInputStream(csvPath))
                .filename(csvPath.getFileName().toString())
                .contentType("text/plain")
                .build();

        FileMetadata uploaded = client.beta().files().upload(
                FileUploadParams.builder().file(fileField).build()
        );

        String fileId = uploaded.id();
        System.out.println("Uploaded file ID: " + fileId);

        // Step 2: Reference the CSV as a document block and ask Claude to analyse it
        BetaContentBlockParam csvDocument = BetaContentBlockParam.ofDocument(
                BetaRequestDocumentBlock.builder()
                        .source(BetaFileDocumentSource.builder().fileId(fileId).build())
                        .build()
        );

        BetaContentBlockParam question = BetaContentBlockParam.ofText(
                BetaTextBlockParam.builder().text(PROMPT).build()
        );

        // Step 3: Send the request with the code execution tool enabled
        BetaMessage message = client.beta().messages().create(
                MessageCreateParams.builder()
                        .model(Model.CLAUDE_SONNET_4_5)
                        .maxTokens(16000)
                        .addBeta(AnthropicBeta.FILES_API_2025_04_14)
                        .addBeta(AnthropicBeta.CODE_EXECUTION_2025_05_22)
                        .addTool(BetaCodeExecutionTool20250522.builder().build())
                        .addUserMessageOfBetaContentBlockParams(List.of(csvDocument, question))
                        .build()
        );

        // Step 4: Print every content block Claude produced; save any plot images to disk
        System.out.println();
        AtomicInteger plotIndex = new AtomicInteger(1);
        for (BetaContentBlock block : message.content()) {
            if (block.isText()) {
                System.out.println(block.asText().text());

            } else if (block.isToolUse()) {
                BetaToolUseBlock toolUse = block.asToolUse();
                System.out.println("\n--- Code (" + toolUse.name() + ") ---");
                System.out.println(toolUse._input());

            } else if (block.isCodeExecutionToolResult()) {
                BetaCodeExecutionToolResultBlock result = block.asCodeExecutionToolResult();
                BetaCodeExecutionToolResultBlockContent content = result.content();
                System.out.println("\n--- Execution result ---");
                if (content.isResultBlock()) {
                    BetaCodeExecutionResultBlock res = content.asResultBlock();
                    if (!res.stdout().isBlank()) {
                        System.out.println("[stdout]\n" + res.stdout());
                    }
                    if (!res.stderr().isBlank()) {
                        System.out.println("[stderr]\n" + res.stderr());
                    }
                    System.out.println("[return code: " + res.returnCode() + "]");

                    // Each output block is a plot image stored in the Files API — download and save it
                    for (BetaCodeExecutionOutputBlock output : res.content()) {
                        Path plotPath = Paths.get("plot_" + plotIndex.getAndIncrement() + ".png");
                        try (HttpResponse response = client.beta().files().download(output.fileId());
                             InputStream body = response.body()) {
                            Files.copy(body, plotPath);
                        }
                        System.out.println("[plot saved → " + plotPath.toAbsolutePath() + "]");
                    }
                } else if (content.isError()) {
                    System.out.println("[error code] " + content.asError().errorCode());
                }
            }
        }
    }
}

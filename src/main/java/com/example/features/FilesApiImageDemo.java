package com.example.features;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.core.MultipartField;
import com.anthropic.models.beta.files.FileMetadata;
import com.anthropic.models.beta.files.FileUploadParams;
import com.anthropic.models.beta.messages.BetaContentBlockParam;
import com.anthropic.models.beta.messages.BetaFileImageSource;
import com.anthropic.models.beta.messages.BetaImageBlockParam;
import com.anthropic.models.beta.messages.BetaMessage;
import com.anthropic.models.beta.messages.BetaTextBlockParam;
import com.anthropic.models.beta.AnthropicBeta;
import com.anthropic.models.beta.messages.MessageCreateParams;
import com.anthropic.models.messages.Model;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class FilesApiImageDemo {
    private static final String PROMPT = "What does this satellite image show? Briefly describe the property layout.";

    public static void main(String... args) throws Exception {
        AnthropicClient client = AnthropicOkHttpClient.fromEnv();

        // Step 1: Upload the image via the Files API to obtain a reusable file ID
        Path imagePath = Paths.get(FilesApiImageDemo.class.getResource("prop1.png").toURI());

        MultipartField<InputStream> fileField = MultipartField.<InputStream>builder()
                .value(Files.newInputStream(imagePath))
                .filename(imagePath.getFileName().toString())
                .contentType("image/png")
                .build();

        FileMetadata uploaded = client.beta().files().upload(
                FileUploadParams.builder().file(fileField).build()
        );

        String fileId = uploaded.id();
        System.out.println("Uploaded file ID: " + fileId);

        // Step 2: Reference the uploaded image by file ID — no base64 encoding needed
        BetaContentBlockParam imageBlock = BetaContentBlockParam.ofImage(
                BetaImageBlockParam.builder()
                        .source(BetaFileImageSource.builder().fileId(fileId).build())
                        .build()
        );

        BetaContentBlockParam textBlock = BetaContentBlockParam.ofText(
                BetaTextBlockParam.builder().text(PROMPT).build()
        );

        // Step 3: Send the message via the beta messages API.
        // The files-api beta header is required to use the 'file' image source type.
        BetaMessage message = client.beta().messages().create(
                MessageCreateParams.builder()
                        .model(Model.CLAUDE_HAIKU_4_5)
                        .maxTokens(256)
                        .addBeta(AnthropicBeta.FILES_API_2025_04_14)
                        .addUserMessageOfBetaContentBlockParams(List.of(imageBlock, textBlock))
                        .build()
        );

        System.out.println(message.content().getFirst().asText().text());
    }
}

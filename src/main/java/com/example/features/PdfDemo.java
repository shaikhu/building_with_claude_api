package com.example.features;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.Base64PdfSource;
import com.anthropic.models.messages.ContentBlockParam;
import com.anthropic.models.messages.DocumentBlockParam;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.Model;
import com.anthropic.models.messages.TextBlockParam;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;

public class PdfDemo {
    public static void main(String... args) throws Exception {
        AnthropicClient client = AnthropicOkHttpClient.fromEnv();

        byte[] fileBytes = Files.readAllBytes(
                Paths.get(PdfDemo.class.getResource("earth.pdf").toURI())
        );

        Base64PdfSource pdfSource = Base64PdfSource.builder()
                .data(Base64.getEncoder().encodeToString(fileBytes))
                .build();

        Message message = client.messages().create(
                MessageCreateParams.builder()
                        .model(Model.CLAUDE_HAIKU_4_5)
                        .maxTokens(1024)
                        .addUserMessageOfBlockParams(List.of(
                                ContentBlockParam.ofDocument(DocumentBlockParam.builder().source(pdfSource).build()),
                                ContentBlockParam.ofText(TextBlockParam.builder().text("Please summarise this document").build())
                        ))
                        .build()
        );

        System.out.println(message.content().getFirst().asText().text());
    }
}

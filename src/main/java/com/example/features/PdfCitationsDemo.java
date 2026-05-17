package com.example.features;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.Base64PdfSource;
import com.anthropic.models.messages.CitationPageLocation;
import com.anthropic.models.messages.CitationsConfigParam;
import com.anthropic.models.messages.ContentBlockParam;
import com.anthropic.models.messages.DocumentBlockParam;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.Model;
import com.anthropic.models.messages.TextBlock;
import com.anthropic.models.messages.TextBlockParam;
import com.anthropic.models.messages.TextCitation;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;

/**
 * Citations are enabled per-document, not at the request level
 */
public class PdfCitationsDemo {
    public static void main(String... args) throws Exception {
        AnthropicClient client = AnthropicOkHttpClient.fromEnv();

        byte[] fileBytes = Files.readAllBytes(
                Paths.get(PdfCitationsDemo.class.getResource("earth.pdf").toURI())
        );

        Base64PdfSource pdfSource = Base64PdfSource.builder()
                .data(Base64.getEncoder().encodeToString(fileBytes))
                .build();

        Message message = client.messages().create(
                MessageCreateParams.builder()
                        .model(Model.CLAUDE_HAIKU_4_5)
                        .maxTokens(1024)
                        .addUserMessageOfBlockParams(List.of(
                                ContentBlockParam.ofDocument(DocumentBlockParam.builder()
                                        .source(pdfSource)
                                        .citations(CitationsConfigParam.builder().enabled(true).build())
                                        .build()),
                                ContentBlockParam.ofText(TextBlockParam.builder()
                                        .text("How were Earth's atmosphere and oceans formed?")
                                        .build())
                        ))
                        .build()
        );

        TextBlock textBlock = message.content().getFirst().asText();
        System.out.println(textBlock.text());

        // Print each citation's source page range and the exact text that was cited
        textBlock.citations().ifPresent(citations -> {
            System.out.println("\n--- Citations ---");
            for (TextCitation citation : citations) {
                if (citation.isPageLocation()) {
                    CitationPageLocation loc = citation.asPageLocation();
                    System.out.printf("Pages %d–%d: \"%s\"%n",
                            loc.startPageNumber(), loc.endPageNumber(), loc.citedText());
                }
            }
        });
    }
}

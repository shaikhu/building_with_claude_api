package com.example.features;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.CitationCharLocation;
import com.anthropic.models.messages.CitationsConfigParam;
import com.anthropic.models.messages.ContentBlockParam;
import com.anthropic.models.messages.DocumentBlockParam;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.Model;
import com.anthropic.models.messages.PlainTextSource;
import com.anthropic.models.messages.TextBlock;
import com.anthropic.models.messages.TextBlockParam;
import com.anthropic.models.messages.TextCitation;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class TextCitationsDemo {
    public static void main(String... args) throws Exception {
        AnthropicClient client = AnthropicOkHttpClient.fromEnv();

        String article = Files.readString(
                Paths.get(TextCitationsDemo.class.getResource("earth_article.txt").toURI())
        );

        // Citations on inline text require a DocumentBlockParam with a PlainTextSource,
        // not a raw TextBlockParam — the same pattern as PdfCitationsDemo but for text
        DocumentBlockParam articleBlock = DocumentBlockParam.builder()
                .source(PlainTextSource.builder().data(article).build())
                .citations(CitationsConfigParam.builder().enabled(true).build())
                .build();

        Message message = client.messages().create(
                MessageCreateParams.builder()
                        .model(Model.CLAUDE_HAIKU_4_5)
                        .maxTokens(1024)
                        .addUserMessageOfBlockParams(List.of(
                                ContentBlockParam.ofDocument(articleBlock),
                                ContentBlockParam.ofText(TextBlockParam.builder()
                                        .text("How were Earth's atmosphere and oceans formed?")
                                        .build())
                        ))
                        .build()
        );

        TextBlock textBlock = message.content().getFirst().asText();
        System.out.println(textBlock.text());

        // Text documents cite by character offset rather than page number
        textBlock.citations().ifPresent(citations -> {
            System.out.println("\n--- Citations ---");
            for (TextCitation citation : citations) {
                if (citation.isCharLocation()) {
                    CitationCharLocation loc = citation.asCharLocation();
                    System.out.printf("Chars %d–%d: \"%s\"%n",
                            loc.startCharIndex(), loc.endCharIndex(), loc.citedText());
                }
            }
        });
    }
}

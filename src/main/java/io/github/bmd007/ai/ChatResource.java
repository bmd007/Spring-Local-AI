package io.github.bmd007.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.openai.OpenAiImageModel;
import org.springframework.ai.openai.OpenAiImageOptions;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.springframework.http.HttpHeaders.ACCEPT;

@RestController
public class ChatResource {

    private static final Logger log = LoggerFactory.getLogger(ChatResource.class);

    private final ChatClient ollamaClient;
    private final EmbeddingModel embeddingModel;
    private final OpenAiImageModel openAiImageModel;
    private final ChatClient geminiClient;
    private final RestClient restClient;

    public ChatResource(OllamaChatModel model,
                        @Qualifier("ollamaEmbeddingModel") EmbeddingModel embeddingModel,
                        OpenAiImageModel openAiImageModel,
                        VertexAiGeminiChatModel vertexAiGeminiChatModel,
                        RestClient.Builder restClientBuilder) {
        restClient = restClientBuilder.defaultHeader(ACCEPT, MediaType.APPLICATION_JSON_VALUE).build();
        this.ollamaClient = ChatClient.create(model)
            .mutate()
            .defaultSystem("You are a friendly chat bot that. You have access to tools. Use them to answer.")
            .build();
        this.geminiClient = ChatClient.create(vertexAiGeminiChatModel)
            .mutate()
            .defaultSystem("You are a friendly chat bot that. You have access to tools. Use them to answer.")
            .build();
        this.embeddingModel = embeddingModel;
        this.openAiImageModel = openAiImageModel;
    }

    @GetMapping("explanations/key-figures/market-cap/instruments/{oderBookId}")
    public String explainMarketCapNumbers(@PathVariable String oderBookId) {

        var uri = UriComponentsBuilder.fromUri(URI.create("https://api.test.nntech.io"))
            .path("company-data/v1/key-figures/MARKET_CAPITALIZATION/instruments/{oderBookId}")
            .queryParam("resolution", "YEAR")
            .build(oderBookId);

        var marketCap = restClient.get()
            .uri(uri)
            .header("x-locale", "en_SE")
            .retrieve()
//            .body(KeyFigureDTO.class)
            .body(String.class);
        var prompt = Prompt.builder()
            .content("""
                Here we have key figures for market capitalization.
                Explain the overall picture of market cap for this instrument.
                
                %s""".formatted(marketCap))
            .chatOptions(ChatOptions.builder()
                .temperature(0d)
                .build())
            .build();
        return geminiClient.prompt(prompt)
            .call()
            .content();
    }

    @GetMapping("/chat/gemini")
    public String chatGemini(@RequestParam String input) {
        var prompt = Prompt.builder()
            .content(input)
            .chatOptions(ChatOptions.builder()
                .temperature(0d)
                .build())
            .build();
        return geminiClient.prompt(prompt)
            .tools(new TooBox())
            .call()
            .content();
    }

    @GetMapping("/chat")
    public String chat(@RequestParam String input) {
        var prompt = Prompt.builder()
            .content(input)
            .chatOptions(ChatOptions.builder()
                .temperature(0d)
                .build())
            .build();
        return ollamaClient.prompt(prompt)
            .tools(new TooBox())
            .call()
            .content();
    }

    @GetMapping("/image")
    public String imageGenerator(@RequestParam String input) {
        return CompletableFuture.supplyAsync(() -> openAiImageModel.call(
                new ImagePrompt(input, OpenAiImageOptions.builder()
                    .quality("hd")
                    .N(1)
                    .height(1024)
                    .width(1024).build())
            ))
            .exceptionally(throwable -> {
                log.error("Error generating image", throwable);
                return null;
            })
            .join()
            .getResult()
            .getOutput()
            .getUrl();
    }

    @GetMapping("/embed")
    public Map embed(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
        EmbeddingResponse embeddingResponse = this.embeddingModel.embedForResponse(List.of(message));
        return Map.of("embedding", embeddingResponse);
    }


    public record KeyFigureDTO(
        String currency,
        List<PointDTO> points,
        Instant retrievedAt,
        List<SourceDTO> sources,
        BigDecimal currentValue,
        String orderBookId,
        String displayName,
        String keyFigureTypeId,
        int numberOfDecimals,
        String unit,
        String graphType,
        String contentfulKey
    ) {
        public record PointDTO(
            BigDecimal value,
            String pointType,
            String fiscalPeriod,
            String displayName,
            LocalDate date
        ) {
        }

        public record SourceDTO(
            String source,
            String displayName,
            Instant timestamp
        ) {
        }
    }

}

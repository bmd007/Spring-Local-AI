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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
public class ChatResource {

    private static final Logger log = LoggerFactory.getLogger(ChatResource.class);

    private final ChatClient ollamaClient;
    private final EmbeddingModel embeddingModel;
    private final OpenAiImageModel openAiImageModel;
    private final ChatClient geminiClient;

    public ChatResource(OllamaChatModel model,
                        @Qualifier("ollamaEmbeddingModel") EmbeddingModel embeddingModel,
                        OpenAiImageModel openAiImageModel,
                        VertexAiGeminiChatModel vertexAiGeminiChatModel) {
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
}

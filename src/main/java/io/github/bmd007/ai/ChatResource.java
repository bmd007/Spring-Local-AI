package io.github.bmd007.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class ChatResource {

    private final ChatClient chatClient;
    private final EmbeddingModel embeddingModel;

    public ChatResource(OllamaChatModel model, EmbeddingModel embeddingModel) {
        this.chatClient = ChatClient.create(model)
            .mutate()
            .defaultSystem("You are a friendly chat bot that answers question in the voice of a Pirate.")
            .build();
        this.embeddingModel = embeddingModel;
    }

    @GetMapping("/chat")
    public String chat(@RequestParam String input) {
        var prompt = Prompt.builder()
            .content(input)
            .chatOptions(ChatOptions.builder()
                .temperature(0.1)
                .build())
            .build();
        return chatClient.prompt(prompt)
            .tools(new DateTimeTools())
            .call()
            .content();
    }

    @GetMapping("/embed")
    public Map embed(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
        EmbeddingResponse embeddingResponse = this.embeddingModel.embedForResponse(List.of(message));
        return Map.of("embedding", embeddingResponse);
    }
}

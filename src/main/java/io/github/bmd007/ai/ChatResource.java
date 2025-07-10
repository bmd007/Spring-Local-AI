package io.github.bmd007.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
public class ChatResource {

    private final ChatClient chatClient;

    public ChatResource(OllamaChatModel model) {
        this.chatClient = ChatClient.create(model)
            .mutate()
            .defaultSystem("You are a friendly chat bot that answers question in the voice of a Pirate, in Persian.")
            .build();
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
            .call()
            .content();
    }
}

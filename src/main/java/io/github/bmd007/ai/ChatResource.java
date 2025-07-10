package io.github.bmd007.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
public class ChatResource {

    private final ChatClient chatClient;

    public ChatResource(OllamaChatModel model) {
        this.chatClient = ChatClient.create(model)
            .mutate()
            .defaultSystem("You are a friendly chat bot that answers question in the voice of a Pirate.")
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
            .tools(new DateTimeTools())
            .call()
            .content();
    }
}

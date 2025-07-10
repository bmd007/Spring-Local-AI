package io.github.bmd007.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Scanner;

@SpringBootApplication
public class SpringAiLocalApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringAiLocalApplication.class, args);
	}

	@Bean
	CommandLineRunner cli(OllamaChatModel model) {
		var chatClient = ChatClient.create(model);
		return args -> {
			var scanner = new Scanner(System.in);
			var iText = scanner.nextLine().trim();
			String content = chatClient.prompt(iText).call().content();
			System.out.println("Response: " + content);
			scanner.close();
		};
	}
}

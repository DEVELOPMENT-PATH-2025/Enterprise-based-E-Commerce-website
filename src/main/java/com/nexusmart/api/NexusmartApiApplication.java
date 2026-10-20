package com.nexusmart.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = {
    org.springframework.ai.autoconfigure.openai.OpenAiAutoConfiguration.class,
    org.springframework.ai.autoconfigure.vectorstore.pgvector.PgVectorStoreAutoConfiguration.class
})
public class NexusmartApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(NexusmartApiApplication.class, args);
	}

}

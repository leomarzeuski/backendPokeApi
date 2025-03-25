package com.example.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@Configuration
public class AIConfig {

        @Value("${ai.provider.url:https://api.openai.com/v1}")
        private String aiProviderUrl;

        @Value("${ai.provider.api-key}")
        private String apiKey;

        @Bean(name = "aiWebClient")
        public WebClient aiWebClient() {
                // Configure memory limits for large responses
                ExchangeStrategies strategies = ExchangeStrategies.builder()
                                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(2 * 1024 * 1024)) // 2MB
                                .build();

                // Configure timeout
                HttpClient httpClient = HttpClient.create()
                                .responseTimeout(java.time.Duration.ofSeconds(30));

                return WebClient.builder()
                                .baseUrl(aiProviderUrl)
                                .defaultHeader("Authorization", "Bearer "
                                                + apiKey)
                                .defaultHeader("Content-Type", "application/json")
                                .exchangeStrategies(strategies)
                                .clientConnector(new ReactorClientHttpConnector(httpClient))
                                .build();
        }
}
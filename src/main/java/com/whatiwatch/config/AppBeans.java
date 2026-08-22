package com.whatiwatch.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.whatiwatch.ai.AiBackend;
import com.whatiwatch.ai.GeminiBackend;
import com.whatiwatch.ai.GroqBackend;
import com.whatiwatch.ai.OllamaBackend;
import com.whatiwatch.ai.PromptBuilder;
import com.whatiwatch.recommendation.AiBackendRegistry;
import com.whatiwatch.recommendation.AiRecommendationParser;
import com.whatiwatch.recommendation.RecommendationService;
import com.whatiwatch.recommendation.TasteProfileService;
import com.whatiwatch.tmdb.TmdbClient;

/**
 * Central Spring configuration: builds the beans that need construction config
 * (API keys, URLs) which Spring can't auto-wire.
 */
@Configuration
public class AppBeans {

    @Value("${tmdb.api-key}")
    private String tmdbApiKey;

    @Value("${ai.groq.api-key}")
    private String groqApiKey;

    @Value("${ai.groq.model:openai/gpt-oss-120b}")
    private String groqModel;

    @Value("${ai.gemini.api-key:}")
    private String geminiApiKey;

    @Value("${ai.gemini.model:gemini-2.5-flash}")
    private String geminiModel;

    @Value("${ai.ollama.base-url:http://localhost:11434}")
    private String ollamaBaseUrl;

    @Value("${ai.ollama.model:llama3.1}")
    private String ollamaModel;
    
    // ------- TMDB --------

    @Bean
    public TmdbClient tmdbClient() {
        return new TmdbClient(tmdbApiKey);
    }

    //------- AI backends --------
    
    @Bean
    public OllamaBackend ollamaBackend() {
        return new OllamaBackend(ollamaBaseUrl, ollamaModel);
    }

    /**
     * Registers the available AI backends.
     * Ollama is always available (local). Groq and Gemini
     * are registered if their API keys are configured
     */

    @Bean
    public AiBackendRegistry aiBackendRegistry(OllamaBackend ollamaBackend) {
        List<AiBackend> available = new ArrayList<>();
        available.add(ollamaBackend);

        if (groqApiKey != null && !groqApiKey.isBlank()) {
            available.add(new GroqBackend(groqApiKey, groqModel));
        }
        if (geminiApiKey != null && !geminiApiKey.isBlank()) {
            available.add(new GeminiBackend(geminiApiKey, geminiModel));
        }

        return new AiBackendRegistry(available);
    }

    // ----- Stateless helpers -------

    @Bean
    public PromptBuilder promptBuilder() {
        return new PromptBuilder();
    }

    @Bean
    public AiRecommendationParser aiRecommendationParser() {
        return new AiRecommendationParser();
    }

    // ------- Services --------

    @Bean
    public TasteProfileService tasteProfileService(TmdbClient tmdbClient) {
        return new TasteProfileService(tmdbClient::getMovie);
    }

    @Bean
    public RecommendationService recommendationService(PromptBuilder promptBuilder,
                                                    AiRecommendationParser parser,
                                                    TmdbClient tmdbClient) {
        return new RecommendationService(promptBuilder, parser, tmdbClient::searchMovie);
    }

}

package com.example.backend.service;

import com.example.backend.models.pokemon.Pokemon;
import com.example.backend.models.ai.AIRequest;
import com.example.backend.models.content.ContentMetadata;
import com.example.backend.models.content.GeneratedContent;
import com.example.backend.models.content.GenerationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class ContentGenerationService {

    private final PokemonService pokemonService;
    private final AIService aiService;

    @Autowired
    public ContentGenerationService(PokemonService pokemonService, AIService aiService) {
        this.pokemonService = pokemonService;
        this.aiService = aiService;
    }

    public Mono<GeneratedContent> generateContent(GenerationRequest request) {
        switch (request.getContentType().toLowerCase()) {
            case "story":
                return generateStory(request);
            case "strategy":
                return generateStrategy(request);
            case "pokedex":
                return generatePokedexEntry(request);
            default:
                return Mono.error(new IllegalArgumentException(
                        "Invalid content type. Supported types: story, strategy, pokedex"));
        }
    }

    private Mono<GeneratedContent> generateStory(GenerationRequest request) {
        // Determine which Pokémon to feature
        Mono<Pokemon> pokemonMono;
        if (request.getPokemonId() != null) {
            pokemonMono = pokemonService.getPokemonById(request.getPokemonId());
        } else if (request.getPokemonName() != null && !request.getPokemonName().isEmpty()) {
            pokemonMono = pokemonService.getPokemonByName(request.getPokemonName().toLowerCase());
        } else {
            // Default to a random Pokémon if none specified
            return pokemonService.getAllPokemons(100, 0)
                    .collectList()
                    .flatMap(pokemonList -> {
                        if (pokemonList.isEmpty()) {
                            return Mono.error(new RuntimeException("No Pokémon available"));
                        }
                        int randomIndex = new Random().nextInt(pokemonList.size());
                        return Mono.just(pokemonList.get(randomIndex));
                    })
                    .flatMap(pokemon -> generateStoryForPokemon(pokemon, request));
        }

        return pokemonMono.flatMap(pokemon -> generateStoryForPokemon(pokemon, request));
    }

    private Mono<GeneratedContent> generateStoryForPokemon(Pokemon pokemon, GenerationRequest request) {
        // Create a detailed prompt for the AI
        String prompt = buildStoryPrompt(pokemon, request);

        AIRequest aiRequest = new AIRequest();
        aiRequest.setPrompt(prompt);
        aiRequest.setContentType("story");
        aiRequest.setSubject(pokemon.getName());
        aiRequest.setMaxLength(request.getMaxLength() != null ? request.getMaxLength() : 1500);

        return aiService.generateContent(aiRequest)
                .flatMap(storyText -> {
                    // Generate image prompt if requested
                    if (Boolean.TRUE.equals(request.getIncludeImagePrompt())) {
                        return generateImagePrompt(pokemon, "story")
                                .map(imagePrompt -> createGeneratedContent(
                                        "The Adventure of " + capitalize(pokemon.getName()),
                                        storyText, "story", pokemon, null, request, imagePrompt));
                    } else {
                        return Mono.just(createGeneratedContent(
                                "The Adventure of " + capitalize(pokemon.getName()),
                                storyText, "story", pokemon, null, request, null));
                    }
                });
    }

    private Mono<GeneratedContent> generateStrategy(GenerationRequest request) {
        // Check if we have team members specified
        if ((request.getTeamMembers() == null || request.getTeamMembers().isEmpty()) &&
                (request.getTeamIds() == null || request.getTeamIds().isEmpty())) {

            // If not, but we have a single Pokémon specified
            if (request.getPokemonName() != null || request.getPokemonId() != null) {
                Mono<Pokemon> pokemonMono = request.getPokemonId() != null
                        ? pokemonService.getPokemonById(request.getPokemonId())
                        : pokemonService.getPokemonByName(request.getPokemonName().toLowerCase());

                return pokemonMono.flatMap(pokemon -> {
                    // Generate strategy for single Pokémon
                    return generateStrategyForTeam(
                            Collections.singletonList(pokemon),
                            "Battle Strategy for " + capitalize(pokemon.getName()),
                            request);
                });
            } else {
                return Mono.error(new IllegalArgumentException(
                        "Either teamMembers, teamIds, or a single Pokémon must be specified for a strategy"));
            }
        }

        // Handle team specified by IDs
        if (request.getTeamIds() != null && !request.getTeamIds().isEmpty()) {
            return Flux.fromIterable(request.getTeamIds())
                    .flatMap(pokemonService::getPokemonById)
                    .collectList()
                    .flatMap(team -> {
                        if (team.isEmpty()) {
                            return Mono.error(new RuntimeException("No valid Pokémon found for the specified IDs"));
                        }

                        // Generate title based on team
                        String title = generateTeamTitle(team);
                        return generateStrategyForTeam(team, title, request);
                    });
        }

        // Handle team specified by names
        return Flux.fromIterable(request.getTeamMembers())
                .flatMap(name -> pokemonService.getPokemonByName(name.toLowerCase()))
                .collectList()
                .flatMap(team -> {
                    if (team.isEmpty()) {
                        return Mono.error(new RuntimeException("No valid Pokémon found for the specified names"));
                    }

                    // Generate title based on team
                    String title = generateTeamTitle(team);
                    return generateStrategyForTeam(team, title, request);
                });
    }

    private Mono<GeneratedContent> generateStrategyForTeam(List<Pokemon> team, String title,
            GenerationRequest request) {
        // Create a detailed prompt for the AI
        String prompt = buildStrategyPrompt(team, request);

        AIRequest aiRequest = new AIRequest();
        aiRequest.setPrompt(prompt);
        aiRequest.setContentType("strategy");
        aiRequest.setSubject("team_strategy");
        aiRequest.setMaxLength(request.getMaxLength() != null ? request.getMaxLength() : 2000);

        return aiService.generateContent(aiRequest)
                .flatMap(strategyText -> {
                    // Generate image prompt if requested
                    if (Boolean.TRUE.equals(request.getIncludeImagePrompt())) {
                        return generateImagePrompt(team, "strategy")
                                .map(imagePrompt -> createGeneratedContent(
                                        title, strategyText, "strategy", null, team, request, imagePrompt));
                    } else {
                        return Mono.just(createGeneratedContent(
                                title, strategyText, "strategy", null, team, request, null));
                    }
                });
    }

    private Mono<GeneratedContent> generatePokedexEntry(GenerationRequest request) {
        // Determine which Pokémon to feature
        Mono<Pokemon> pokemonMono;
        if (request.getPokemonId() != null) {
            pokemonMono = pokemonService.getPokemonById(request.getPokemonId());
        } else if (request.getPokemonName() != null && !request.getPokemonName().isEmpty()) {
            pokemonMono = pokemonService.getPokemonByName(request.getPokemonName().toLowerCase());
        } else {
            return Mono.error(new IllegalArgumentException(
                    "Either pokemonId or pokemonName must be provided for a Pokédex entry"));
        }

        return pokemonMono.flatMap(pokemon -> {
            // Create a detailed prompt for the AI
            String prompt = buildPokedexPrompt(pokemon, request);

            AIRequest aiRequest = new AIRequest();
            aiRequest.setPrompt(prompt);
            aiRequest.setContentType("pokedex");
            aiRequest.setSubject(pokemon.getName());
            aiRequest.setMaxLength(request.getMaxLength() != null ? request.getMaxLength() : 1200);

            return aiService.generateContent(aiRequest)
                    .flatMap(entryText -> {
                        // Generate image prompt if requested
                        if (Boolean.TRUE.equals(request.getIncludeImagePrompt())) {
                            return generateImagePrompt(pokemon, "pokedex")
                                    .map(imagePrompt -> createGeneratedContent(
                                            "Pokédex Entry: " + capitalize(pokemon.getName()),
                                            entryText, "pokedex", pokemon, null, request, imagePrompt));
                        } else {
                            return Mono.just(createGeneratedContent(
                                    "Pokédex Entry: " + capitalize(pokemon.getName()),
                                    entryText, "pokedex", pokemon, null, request, null));
                        }
                    });
        });
    }

    // Helper methods to build prompts

    private String buildStoryPrompt(Pokemon pokemon, GenerationRequest request) {
        StringBuilder prompt = new StringBuilder();

        // Add target audience and tone if specified
        if (request.getTargetAudience() != null || request.getTone() != null) {
            prompt.append("Write a ");
            if (request.getTone() != null) {
                prompt.append(request.getTone().toLowerCase()).append(" ");
            }
            prompt.append("Pokémon adventure story ");
            if (request.getTargetAudience() != null) {
                prompt.append("for ").append(request.getTargetAudience().toLowerCase()).append(" ");
            }
            prompt.append("featuring the Pokémon ").append(pokemon.getName()).append(".\n\n");
        } else {
            prompt.append("Write a short adventure story featuring the Pokémon ").append(pokemon.getName())
                    .append(".\n\n");
        }

        prompt.append("Details about ").append(pokemon.getName()).append(":\n");
        prompt.append("- Type(s): ");
        pokemon.getTypes().forEach(type -> prompt.append(type.getType().getName()).append(", "));
        prompt.append("\n");

        prompt.append("- Height: ").append(pokemon.getHeight() / 10.0).append(" meters\n");
        prompt.append("- Weight: ").append(pokemon.getWeight() / 10.0).append(" kg\n");

        if (pokemon.getAbilities() != null && !pokemon.getAbilities().isEmpty()) {
            prompt.append("- Abilities: ");
            pokemon.getAbilities()
                    .forEach(ability -> prompt.append(ability.getAbility().getName().replace("-", " ")).append(", "));
            prompt.append("\n");
        }

        prompt.append("\nThe story should be engaging and capture the essence of this Pokémon's characteristics. ");
        prompt.append("Include elements of the Pokémon's natural habitat, behavior, and abilities. ");

        // Add formatting instructions if specified
        if (request.getFormat() != null) {
            if ("markdown".equalsIgnoreCase(request.getFormat())) {
                prompt.append(
                        "\n\nPlease format the story with Markdown, using headings, paragraphs, and emphasis where appropriate.");
            } else if ("html".equalsIgnoreCase(request.getFormat())) {
                prompt.append("\n\nPlease format the story with basic HTML tags for structure and emphasis.");
            }
        }

        return prompt.toString();
    }

    private String buildStrategyPrompt(List<Pokemon> team, GenerationRequest request) {
        StringBuilder prompt = new StringBuilder();

        // Add target audience and tone if specified
        if (request.getTargetAudience() != null || request.getTone() != null) {
            prompt.append("Write a ");
            if (request.getTone() != null) {
                prompt.append(request.getTone().toLowerCase()).append(" ");
            }
            prompt.append("battle strategy guide ");
            if (request.getTargetAudience() != null) {
                prompt.append("for ").append(request.getTargetAudience().toLowerCase()).append(" ");
            }
            prompt.append("covering the following Pokémon team:\n\n");
        } else {
            prompt.append("Write a comprehensive battle strategy guide for a Pokémon team consisting of:\n\n");
        }

        for (Pokemon pokemon : team) {
            prompt.append("- ").append(capitalize(pokemon.getName())).append("\n");
            prompt.append("  Types: ");
            pokemon.getTypes().forEach(type -> prompt.append(capitalize(type.getType().getName())).append(", "));
            prompt.append("\n");

            if (pokemon.getAbilities() != null && !pokemon.getAbilities().isEmpty()) {
                prompt.append("  Abilities: ");
                pokemon.getAbilities().forEach(ability -> prompt
                        .append(capitalize(ability.getAbility().getName().replace("-", " "))).append(", "));
                prompt.append("\n");
            }
            prompt.append("\n");
        }

        prompt.append("Include the following in your strategy guide:\n");
        prompt.append("1. Team overview and strengths\n");
        prompt.append("2. Role of each Pokémon in the team\n");
        prompt.append("3. Suggested move sets\n");
        prompt.append("4. Battle tactics against common threats\n");
        prompt.append("5. Best lead Pokémon and switching strategies\n\n");

        prompt.append("Keep the guide informative but concise, with practical advice for trainers.");

        // Add formatting instructions if specified
        if (request.getFormat() != null) {
            if ("markdown".equalsIgnoreCase(request.getFormat())) {
                prompt.append(
                        "\n\nPlease format the strategy with Markdown, using headings, paragraphs, and emphasis where appropriate.");
            } else if ("html".equalsIgnoreCase(request.getFormat())) {
                prompt.append("\n\nPlease format the strategy with basic HTML tags for structure and emphasis.");
            }
        }

        return prompt.toString();
    }

    private String buildPokedexPrompt(Pokemon pokemon, GenerationRequest request) {
        StringBuilder prompt = new StringBuilder();

        // Add target audience and tone if specified
        if (request.getTargetAudience() != null || request.getTone() != null) {
            prompt.append("Create a ");
            if (request.getTone() != null) {
                prompt.append(request.getTone().toLowerCase()).append(" ");
            }
            prompt.append("Pokédex entry ");
            if (request.getTargetAudience() != null) {
                prompt.append("for ").append(request.getTargetAudience().toLowerCase()).append(" ");
            }
            prompt.append("about ").append(pokemon.getName()).append(".\n\n");
        } else {
            prompt.append("Create a detailed Pokédex entry for ").append(pokemon.getName()).append(".\n\n");
        }

        prompt.append("Details to incorporate:\n");
        prompt.append("- Pokémon #").append(pokemon.getId()).append("\n");

        prompt.append("- Type(s): ");
        pokemon.getTypes().forEach(type -> prompt.append(type.getType().getName()).append(", "));
        prompt.append("\n");

        prompt.append("- Height: ").append(pokemon.getHeight() / 10.0).append(" meters\n");
        prompt.append("- Weight: ").append(pokemon.getWeight() / 10.0).append(" kg\n");

        if (pokemon.getAbilities() != null && !pokemon.getAbilities().isEmpty()) {
            prompt.append("- Abilities: ");
            pokemon.getAbilities()
                    .forEach(ability -> prompt.append(ability.getAbility().getName().replace("-", " ")).append(", "));
            prompt.append("\n");
        }

        prompt.append("\nThe Pokédex entry should include:\n");
        prompt.append("1. General species description\n");
        prompt.append("2. Physical characteristics\n");
        prompt.append("3. Natural habitat and behavior\n");
        prompt.append("4. Special abilities and how they use them\n");
        prompt.append("5. Relationships with other Pokémon or humans\n\n");

        prompt.append("Write in the style of an official Pokédex entry, with scientific yet engaging language.");

        // Add formatting instructions if specified
        if (request.getFormat() != null) {
            if ("markdown".equalsIgnoreCase(request.getFormat())) {
                prompt.append(
                        "\n\nPlease format the entry with Markdown, using headings, paragraphs, and emphasis where appropriate.");
            } else if ("html".equalsIgnoreCase(request.getFormat())) {
                prompt.append("\n\nPlease format the entry with basic HTML tags for structure and emphasis.");
            }
        }

        return prompt.toString();
    }

    private Mono<String> generateImagePrompt(Pokemon pokemon, String contentType) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Generate a short, detailed image prompt (50-75 words) for an illustration of the Pokémon ")
                .append(pokemon.getName())
                .append(" based on its characteristics. The prompt should be suitable for an image generation AI.");

        AIRequest aiRequest = new AIRequest();
        aiRequest.setPrompt(prompt.toString());
        aiRequest.setContentType("image_prompt");
        aiRequest.setSubject(pokemon.getName());
        aiRequest.setMaxLength(200);

        return aiService.generateContent(aiRequest);
    }

    private Mono<String> generateImagePrompt(List<Pokemon> team, String contentType) {
        StringBuilder prompt = new StringBuilder();
        prompt.append(
                "Generate a short, detailed image prompt (50-75 words) for an illustration of a Pokémon team featuring ");

        for (int i = 0; i < team.size(); i++) {
            if (i > 0) {
                if (i == team.size() - 1) {
                    prompt.append(" and ");
                } else {
                    prompt.append(", ");
                }
            }
            prompt.append(team.get(i).getName());
        }

        prompt.append(". The prompt should be suitable for an image generation AI.");

        AIRequest aiRequest = new AIRequest();
        aiRequest.setPrompt(prompt.toString());
        aiRequest.setContentType("image_prompt");
        aiRequest.setSubject("pokemon_team");
        aiRequest.setMaxLength(200);

        return aiService.generateContent(aiRequest);
    }

    private GeneratedContent createGeneratedContent(
            String title,
            String content,
            String contentType,
            Pokemon featuredPokemon,
            List<Pokemon> featuredTeam,
            GenerationRequest request,
            String imagePrompt) {

        // Create metadata
        ContentMetadata metadata = new ContentMetadata();
        metadata.setTargetAudience(request.getTargetAudience());
        metadata.setLanguage(request.getLanguage() != null ? request.getLanguage() : "en");
        metadata.setFormat(request.getFormat() != null ? request.getFormat() : "text");
        metadata.setTone(request.getTone());

        // Estimate word count
        if (content != null) {
            int wordCount = content.split("\\s+").length;
            metadata.setWordCount(wordCount);
        }

        // Create tags
        List<String> tags = new ArrayList<>();
        tags.add(contentType);

        if (featuredPokemon != null) {
            tags.add(featuredPokemon.getName());
            featuredPokemon.getTypes().forEach(type -> tags.add(type.getType().getName()));
        }

        if (featuredTeam != null) {
            featuredTeam.forEach(pokemon -> tags.add(pokemon.getName()));
        }

        metadata.setTags(tags);

        // Create content object
        return GeneratedContent.builder()
                .id(UUID.randomUUID().toString())
                .title(title)
                .content(content)
                .contentType(contentType)
                .featuredPokemon(featuredPokemon)
                .featuredTeam(featuredTeam)
                .generatedDate(LocalDateTime.now())
                .imagePrompt(imagePrompt)
                .metadata(metadata)
                .build();
    }

    private String generateTeamTitle(List<Pokemon> team) {
        if (team.size() == 1) {
            return "Battle Strategy for " + capitalize(team.get(0).getName());
        } else if (team.size() <= 3) {
            StringBuilder title = new StringBuilder("Team Strategy: ");
            for (int i = 0; i < team.size(); i++) {
                if (i > 0) {
                    if (i == team.size() - 1) {
                        title.append(" & ");
                    } else {
                        title.append(", ");
                    }
                }
                title.append(capitalize(team.get(i).getName()));
            }
            return title.toString();
        } else {
            // Get the first Pokémon and the total count
            return "Team Strategy: " + capitalize(team.get(0).getName()) + " & " + (team.size() - 1) + " More";
        }
    }

    private String capitalize(String string) {
        if (string == null || string.isEmpty()) {
            return string;
        }
        return string.substring(0, 1).toUpperCase() + string.substring(1);
    }
}
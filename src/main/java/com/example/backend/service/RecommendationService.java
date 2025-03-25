package com.example.backend.service;

import com.example.backend.models.pokemon.Pokemon;
import com.example.backend.models.recommendation.RecommendationRequest;
import com.example.backend.models.recommendation.RecommendationResult;
import com.example.backend.models.recommendation.SimilarityScore;
import com.example.backend.models.ai.AIRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    private final PokemonService pokemonService;
    private final AIService aiService;

    @Autowired
    public RecommendationService(PokemonService pokemonService, AIService aiService) {
        this.pokemonService = pokemonService;
        this.aiService = aiService;
    }

    public Mono<RecommendationResult> findSimilarPokemon(RecommendationRequest request) {
        // Determine which Pokemon to use as the base
        Mono<Pokemon> basePokemonMono;
        if (request.getPokemonId() != null) {
            basePokemonMono = pokemonService.getPokemonById(request.getPokemonId());
        } else if (request.getPokemonName() != null && !request.getPokemonName().isEmpty()) {
            basePokemonMono = pokemonService.getPokemonByName(request.getPokemonName().toLowerCase());
        } else {
            return Mono.error(new IllegalArgumentException("Either pokemonId or pokemonName must be provided"));
        }

        // Default limit if not specified
        int limit = request.getLimit() != null ? request.getLimit() : 5;

        return basePokemonMono.flatMap(basePokemon -> {
            // Extract types from the base Pokemon
            List<String> types = basePokemon.getTypes().stream()
                    .map(type -> type.getType().getName())
                    .collect(Collectors.toList());

            // Default to primary type if no preferred types specified
            List<String> searchTypes = request.getPreferredTypes() != null && !request.getPreferredTypes().isEmpty()
                    ? request.getPreferredTypes()
                    : Collections.singletonList(types.get(0));

            // Get Pokemon with the specified types
            return Flux.fromIterable(searchTypes)
                    .flatMap(pokemonService::getPokemonsByType)
                    .distinct(Pokemon::getId)
                    .filter(p -> !p.getId().equals(basePokemon.getId())) // Exclude the base Pokemon
                    .take(50) // Limit candidates for performance
                    .collectList()
                    .flatMap(candidates -> {
                        // Calculate similarity scores
                        List<SimilarityScore> scoredCandidates = calculateSimilarityScores(
                                basePokemon, candidates, request.getSimilarityStrategy());

                        // Sort by score in descending order and take the top N
                        List<Pokemon> similarPokemon = scoredCandidates.stream()
                                .sorted(Comparator.comparing(SimilarityScore::getScore).reversed())
                                .limit(limit)
                                .map(SimilarityScore::getPokemon)
                                .collect(Collectors.toList());

                        // Calculate average similarity score
                        double avgScore = scoredCandidates.stream()
                                .limit(limit)
                                .mapToDouble(SimilarityScore::getScore)
                                .average()
                                .orElse(0.0);

                        // Generate match reasons
                        List<String> matchReasons = generateMatchReasons(scoredCandidates.stream()
                                .limit(limit)
                                .collect(Collectors.toList()));

                        // Generate explanations using AI
                        return generateExplanations(basePokemon, similarPokemon)
                                .map(explanations -> {
                                    RecommendationResult result = new RecommendationResult();
                                    result.setBasePokemon(basePokemon);
                                    result.setSimilarPokemon(similarPokemon);
                                    result.setExplanations(explanations);
                                    result.setMatchReasons(matchReasons);
                                    result.setAverageSimilarityScore(avgScore);
                                    return result;
                                });
                    });
        });
    }

    private List<SimilarityScore> calculateSimilarityScores(
            Pokemon basePokemon,
            List<Pokemon> candidates,
            String strategy) {

        List<SimilarityScore> scores = new ArrayList<>();

        for (Pokemon candidate : candidates) {
            double score = 0.0;
            Map<String, Double> scoreBreakdown = new HashMap<>();

            // Type similarity (using Jaccard similarity)
            Set<String> baseTypes = basePokemon.getTypes().stream()
                    .map(type -> type.getType().getName())
                    .collect(Collectors.toSet());

            Set<String> candidateTypes = candidate.getTypes().stream()
                    .map(type -> type.getType().getName())
                    .collect(Collectors.toSet());

            Set<String> intersection = new HashSet<>(baseTypes);
            intersection.retainAll(candidateTypes);

            Set<String> union = new HashSet<>(baseTypes);
            union.addAll(candidateTypes);

            double typeSimilarity = union.isEmpty() ? 0 : (double) intersection.size() / union.size();
            scoreBreakdown.put("typeSimilarity", typeSimilarity);

            // Height similarity (normalized)
            double heightDiff = Math.abs(basePokemon.getHeight() - candidate.getHeight()) /
                    (double) Math.max(basePokemon.getHeight(), candidate.getHeight());
            double heightSimilarity = 1.0 - heightDiff;
            scoreBreakdown.put("heightSimilarity", heightSimilarity);

            // Weight similarity (normalized)
            double weightDiff = Math.abs(basePokemon.getWeight() - candidate.getWeight()) /
                    (double) Math.max(basePokemon.getWeight(), candidate.getWeight());
            double weightSimilarity = 1.0 - weightDiff;
            scoreBreakdown.put("weightSimilarity", weightSimilarity);

            // Base experience similarity (normalized)
            double expDiff = Math.abs(basePokemon.getBaseExperience() - candidate.getBaseExperience()) /
                    (double) Math.max(basePokemon.getBaseExperience(), candidate.getBaseExperience());
            double expSimilarity = 1.0 - expDiff;
            scoreBreakdown.put("baseExpSimilarity", expSimilarity);

            // Apply weights based on strategy
            if ("type".equals(strategy)) {
                // Type-focused strategy
                score = typeSimilarity * 0.7 + heightSimilarity * 0.1 + weightSimilarity * 0.1 + expSimilarity * 0.1;
            } else if ("stats".equals(strategy)) {
                // Stats-focused strategy
                score = typeSimilarity * 0.2 + heightSimilarity * 0.2 + weightSimilarity * 0.2 + expSimilarity * 0.4;
            } else {
                // Balanced strategy (default)
                score = typeSimilarity * 0.4 + heightSimilarity * 0.2 + weightSimilarity * 0.2 + expSimilarity * 0.2;
            }

            SimilarityScore similarityScore = new SimilarityScore();
            similarityScore.setPokemon(candidate);
            similarityScore.setScore(score);
            similarityScore.setScoreBreakdown(scoreBreakdown);

            scores.add(similarityScore);
        }

        return scores;
    }

    private List<String> generateMatchReasons(List<SimilarityScore> topMatches) {
        List<String> reasons = new ArrayList<>();

        for (SimilarityScore match : topMatches) {
            Map<String, Double> breakdown = match.getScoreBreakdown();

            // Find the top 2 similarity factors
            List<Map.Entry<String, Double>> sortedFactors = breakdown.entrySet().stream()
                    .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                    .limit(2)
                    .collect(Collectors.toList());

            StringBuilder reason = new StringBuilder();
            reason.append(match.getPokemon().getName())
                    .append(" matches because of ");

            for (int i = 0; i < sortedFactors.size(); i++) {
                Map.Entry<String, Double> factor = sortedFactors.get(i);

                switch (factor.getKey()) {
                    case "typeSimilarity":
                        reason.append("similar types");
                        break;
                    case "heightSimilarity":
                        reason.append("similar height");
                        break;
                    case "weightSimilarity":
                        reason.append("similar weight");
                        break;
                    case "baseExpSimilarity":
                        reason.append("similar strength");
                        break;
                }

                if (i < sortedFactors.size() - 1) {
                    reason.append(" and ");
                }
            }

            reasons.add(reason.toString());
        }

        return reasons;
    }

    private Mono<Map<Long, String>> generateExplanations(Pokemon basePokemon, List<Pokemon> similarPokemon) {
        // Build a prompt for the AI to generate explanations
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append(
                "Generate brief explanations (1-2 sentences each) for why the following Pokémon are similar to ")
                .append(basePokemon.getName())
                .append(" (ID: ").append(basePokemon.getId()).append("):\n\n");

        for (Pokemon pokemon : similarPokemon) {
            promptBuilder.append("- ").append(pokemon.getName())
                    .append(" (ID: ").append(pokemon.getId()).append("):\n");

            // Add some context about the Pokémon
            promptBuilder.append("  Types: ");
            pokemon.getTypes().forEach(type -> promptBuilder.append(type.getType().getName()).append(", "));
            promptBuilder.append("\n");

            promptBuilder.append("  Height: ").append(pokemon.getHeight() / 10.0).append("m")
                    .append(", Weight: ").append(pokemon.getWeight() / 10.0).append("kg")
                    .append(", Base Experience: ").append(pokemon.getBaseExperience())
                    .append("\n\n");
        }

        promptBuilder.append("Format your response as:\n");
        promptBuilder.append("ID1: Explanation for first Pokémon\n");
        promptBuilder.append("ID2: Explanation for second Pokémon\n");
        promptBuilder.append("etc.\n");

        // Call AI service to generate explanations
        AIRequest aiRequest = new AIRequest();
        aiRequest.setPrompt(promptBuilder.toString());
        aiRequest.setContentType("explanation");
        aiRequest.setSubject("pokemon_similarity");
        aiRequest.setMaxLength(1000);

        return aiService.generateContent(aiRequest)
                .map(response -> {
                    // Parse the response into a map of ID -> explanation
                    Map<Long, String> explanations = new HashMap<>();
                    String[] lines = response.split("\n");

                    for (String line : lines) {
                        line = line.trim();
                        if (line.isEmpty())
                            continue;

                        // Parse ID: Explanation format
                        int colonIndex = line.indexOf(':');
                        if (colonIndex > 0) {
                            try {
                                Long id = Long.parseLong(line.substring(0, colonIndex).trim());
                                String explanation = line.substring(colonIndex + 1).trim();
                                explanations.put(id, explanation);
                            } catch (NumberFormatException e) {
                                // Skip lines that don't match expected format
                                System.err.println("Error parsing explanation line: " + line);
                            }
                        }
                    }

                    return explanations;
                });
    }
}
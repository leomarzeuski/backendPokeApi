package com.example.backend.service;

import com.example.backend.models.comparison.PokemonComparison;
import com.example.backend.models.comparison.StatComparison;
import com.example.backend.models.pokemon.Pokemon;
import com.example.backend.models.pokemon.PokemonType;
import com.example.backend.models.type.TypeEffectiveness;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ComparisonService {

    private final PokemonService pokemonService;
    private final Map<String, Map<String, Double>> typeEffectivenessChart;

    @Autowired
    public ComparisonService(PokemonService pokemonService) {
        this.pokemonService = pokemonService;
        this.typeEffectivenessChart = initializeTypeChart();
    }

    public Flux<Pokemon> comparePokemons(List<Long> pokemonIds) {
        return Flux.fromIterable(pokemonIds)
                .flatMap(pokemonService::getPokemonById)
                .collectList()
                .flatMapMany(pokemonList -> {
                    Map<Long, Integer> idToIndex = new HashMap<>();
                    for (int i = 0; i < pokemonIds.size(); i++) {
                        idToIndex.put(pokemonIds.get(i), i);
                    }

                    pokemonList
                            .sort(Comparator.comparingInt(p -> idToIndex.getOrDefault(p.getId(), Integer.MAX_VALUE)));
                    return Flux.fromIterable(pokemonList);
                });
    }

    public Mono<PokemonComparison> generateDetailedComparison(List<Long> pokemonIds) {
        return Flux.fromIterable(pokemonIds)
                .flatMap(pokemonService::getPokemonById)
                .collectList()
                .map(pokemonList -> {
                    PokemonComparison comparison = new PokemonComparison();
                    comparison.setPokemons(pokemonList);

                    Map<Long, TypeEffectiveness> typeEffectivenessMap = new HashMap<>();
                    Map<Long, List<String>> abilitiesMap = new HashMap<>();
                    Map<Long, String> spriteUrlsMap = new HashMap<>();

                    for (Pokemon pokemon : pokemonList) {
                        Long id = pokemon.getId();

                        // Calculate type effectiveness
                        TypeEffectiveness effectiveness = calculateTypeEffectiveness(pokemon);
                        typeEffectivenessMap.put(id, effectiveness);

                        // Extract abilities
                        List<String> abilities = pokemon.getAbilities().stream()
                                .map(ability -> ability.getAbility().getName())
                                .collect(Collectors.toList());
                        abilitiesMap.put(id, abilities);

                        // Extract sprite URLs
                        if (pokemon.getSprites() != null && pokemon.getSprites().getFrontDefault() != null) {
                            spriteUrlsMap.put(id, pokemon.getSprites().getFrontDefault());
                        }
                    }

                    comparison.setTypeEffectiveness(typeEffectivenessMap);
                    comparison.setAbilities(abilitiesMap);
                    comparison.setSpriteUrls(spriteUrlsMap);

                    // Generate stat comparison
                    StatComparison statComparison = generateStatComparison(pokemonList);
                    comparison.setStatComparison(statComparison);

                    return comparison;
                });
    }

    private StatComparison generateStatComparison(List<Pokemon> pokemonList) {
        Map<Long, Integer> height = new HashMap<>();
        Map<Long, Integer> weight = new HashMap<>();
        Map<Long, Integer> baseExp = new HashMap<>();
        Map<String, Long> highest = new HashMap<>();
        Map<String, Long> lowest = new HashMap<>();
        Map<String, Map<Long, Double>> normalizedValues = new HashMap<>();

        // Initialize normalized value maps
        Map<Long, Double> normalizedHeight = new HashMap<>();
        Map<Long, Double> normalizedWeight = new HashMap<>();
        Map<Long, Double> normalizedBaseExp = new HashMap<>();
        normalizedValues.put("height", normalizedHeight);
        normalizedValues.put("weight", normalizedWeight);
        normalizedValues.put("baseExperience", normalizedBaseExp);

        // Find min and max values for normalization
        int minHeight = Integer.MAX_VALUE;
        int maxHeight = Integer.MIN_VALUE;
        int minWeight = Integer.MAX_VALUE;
        int maxWeight = Integer.MIN_VALUE;
        int minBaseExp = Integer.MAX_VALUE;
        int maxBaseExp = Integer.MIN_VALUE;

        // First pass: collect data and find min/max
        for (Pokemon pokemon : pokemonList) {
            Long id = pokemon.getId();
            Integer pokemonHeight = pokemon.getHeight();
            Integer pokemonWeight = pokemon.getWeight();
            Integer pokemonBaseExp = pokemon.getBaseExperience();

            height.put(id, pokemonHeight);
            weight.put(id, pokemonWeight);
            baseExp.put(id, pokemonBaseExp);

            // Update min/max values
            minHeight = Math.min(minHeight, pokemonHeight);
            maxHeight = Math.max(maxHeight, pokemonHeight);
            minWeight = Math.min(minWeight, pokemonWeight);
            maxWeight = Math.max(maxWeight, pokemonWeight);
            minBaseExp = Math.min(minBaseExp, pokemonBaseExp);
            maxBaseExp = Math.max(maxBaseExp, pokemonBaseExp);
        }

        // Second pass: normalize values and find highest/lowest
        Long highestHeightId = null;
        Long lowestHeightId = null;
        Long highestWeightId = null;
        Long lowestWeightId = null;
        Long highestBaseExpId = null;
        Long lowestBaseExpId = null;

        for (Pokemon pokemon : pokemonList) {
            Long id = pokemon.getId();

            // Normalize height (avoid division by zero)
            double heightRange = maxHeight - minHeight;
            if (heightRange > 0) {
                normalizedHeight.put(id, (height.get(id) - minHeight) / heightRange);
            } else {
                normalizedHeight.put(id, 1.0); // All values are equal
            }

            // Normalize weight
            double weightRange = maxWeight - minWeight;
            if (weightRange > 0) {
                normalizedWeight.put(id, (weight.get(id) - minWeight) / weightRange);
            } else {
                normalizedWeight.put(id, 1.0);
            }

            // Normalize base experience
            double baseExpRange = maxBaseExp - minBaseExp;
            if (baseExpRange > 0) {
                normalizedBaseExp.put(id, (baseExp.get(id) - minBaseExp) / baseExpRange);
            } else {
                normalizedBaseExp.put(id, 1.0);
            }

            // Update highest/lowest IDs
            if (highestHeightId == null || height.get(id) > height.get(highestHeightId)) {
                highestHeightId = id;
            }
            if (lowestHeightId == null || height.get(id) < height.get(lowestHeightId)) {
                lowestHeightId = id;
            }

            if (highestWeightId == null || weight.get(id) > weight.get(highestWeightId)) {
                highestWeightId = id;
            }
            if (lowestWeightId == null || weight.get(id) < weight.get(lowestWeightId)) {
                lowestWeightId = id;
            }

            if (highestBaseExpId == null || baseExp.get(id) > baseExp.get(highestBaseExpId)) {
                highestBaseExpId = id;
            }
            if (lowestBaseExpId == null || baseExp.get(id) < baseExp.get(lowestBaseExpId)) {
                lowestBaseExpId = id;
            }
        }

        // Set highest and lowest
        highest.put("height", highestHeightId);
        highest.put("weight", highestWeightId);
        highest.put("baseExperience", highestBaseExpId);

        lowest.put("height", lowestHeightId);
        lowest.put("weight", lowestWeightId);
        lowest.put("baseExperience", lowestBaseExpId);

        return StatComparison.builder()
                .height(height)
                .weight(weight)
                .baseExperience(baseExp)
                .highest(highest)
                .lowest(lowest)
                .normalizedValues(normalizedValues)
                .build();
    }

    private TypeEffectiveness calculateTypeEffectiveness(Pokemon pokemon) {
        List<PokemonType> types = pokemon.getTypes();
        List<String> pokemonTypes = types.stream()
                .map(type -> type.getType().getName())
                .collect(Collectors.toList());

        // Get primary and secondary types
        String primaryType = pokemonTypes.isEmpty() ? null : pokemonTypes.get(0);
        String secondaryType = pokemonTypes.size() > 1 ? pokemonTypes.get(1) : null;

        // Lists to store effectiveness data
        List<String> strongAgainst = new ArrayList<>();
        List<String> weakAgainst = new ArrayList<>();
        List<String> resistantTo = new ArrayList<>();
        List<String> immuneTo = new ArrayList<>();

        // All possible types
        String[] allTypes = {
                "normal", "fire", "water", "electric", "grass", "ice",
                "fighting", "poison", "ground", "flying", "psychic", "bug",
                "rock", "ghost", "dragon", "dark", "steel", "fairy"
        };

        // Calculate offensive effectiveness (what this Pokémon is strong against)
        for (String defenderType : allTypes) {
            double maxEffectiveness = 1.0;

            // Check effectiveness of each of this Pokémon's types against the defender type
            for (String attackerType : pokemonTypes) {
                double effectiveness = getTypeEffectiveness(attackerType, defenderType);
                maxEffectiveness = Math.max(maxEffectiveness, effectiveness);
            }

            if (maxEffectiveness > 1.0) {
                strongAgainst.add(defenderType);
            }
        }

        // Calculate defensive effectiveness (what this Pokémon is weak against,
        // resistant to, or immune to)
        for (String attackerType : allTypes) {
            double effectivenessProduct = 1.0;

            // Calculate combined effectiveness against all of this Pokémon's types
            for (String defenderType : pokemonTypes) {
                effectivenessProduct *= getTypeEffectiveness(attackerType, defenderType);
            }

            if (effectivenessProduct == 0.0) {
                immuneTo.add(attackerType);
            } else if (effectivenessProduct < 1.0) {
                resistantTo.add(attackerType);
            } else if (effectivenessProduct > 1.0) {
                weakAgainst.add(attackerType);
            }
        }

        return TypeEffectiveness.builder()
                .strongAgainst(strongAgainst)
                .weakAgainst(weakAgainst)
                .resistantTo(resistantTo)
                .immuneTo(immuneTo)
                .primaryType(primaryType)
                .secondaryType(secondaryType)
                .build();
    }

    private double getTypeEffectiveness(String attackerType, String defenderType) {
        if (!typeEffectivenessChart.containsKey(attackerType) ||
                !typeEffectivenessChart.get(attackerType).containsKey(defenderType)) {
            return 1.0;
        }

        return typeEffectivenessChart.get(attackerType).get(defenderType);
    }

    private Map<String, Map<String, Double>> initializeTypeChart() {
        Map<String, Map<String, Double>> chart = new HashMap<>();

        String[] types = {
                "normal", "fire", "water", "electric", "grass", "ice",
                "fighting", "poison", "ground", "flying", "psychic", "bug",
                "rock", "ghost", "dragon", "dark", "steel", "fairy"
        };

        // Set default effectiveness (1.0) for all type combinations
        for (String attackType : types) {
            Map<String, Double> defenseMap = new HashMap<>();
            for (String defendType : types) {
                defenseMap.put(defendType, 1.0);
            }
            chart.put(attackType, defenseMap);
        }

        // Normal type
        chart.get("normal").put("rock", 0.5);
        chart.get("normal").put("ghost", 0.0);
        chart.get("normal").put("steel", 0.5);

        // Fire type
        chart.get("fire").put("fire", 0.5);
        chart.get("fire").put("water", 0.5);
        chart.get("fire").put("grass", 2.0);
        chart.get("fire").put("ice", 2.0);
        chart.get("fire").put("bug", 2.0);
        chart.get("fire").put("rock", 0.5);
        chart.get("fire").put("dragon", 0.5);
        chart.get("fire").put("steel", 2.0);

        // Water type
        chart.get("water").put("fire", 2.0);
        chart.get("water").put("water", 0.5);
        chart.get("water").put("grass", 0.5);
        chart.get("water").put("ground", 2.0);
        chart.get("water").put("rock", 2.0);
        chart.get("water").put("dragon", 0.5);

        // Electric type
        chart.get("electric").put("water", 2.0);
        chart.get("electric").put("electric", 0.5);
        chart.get("electric").put("grass", 0.5);
        chart.get("electric").put("ground", 0.0);
        chart.get("electric").put("flying", 2.0);
        chart.get("electric").put("dragon", 0.5);

        // Grass type
        chart.get("grass").put("fire", 0.5);
        chart.get("grass").put("water", 2.0);
        chart.get("grass").put("grass", 0.5);
        chart.get("grass").put("poison", 0.5);
        chart.get("grass").put("ground", 2.0);
        chart.get("grass").put("flying", 0.5);
        chart.get("grass").put("bug", 0.5);
        chart.get("grass").put("rock", 2.0);
        chart.get("grass").put("dragon", 0.5);
        chart.get("grass").put("steel", 0.5);

        // Ice type
        chart.get("ice").put("fire", 0.5);
        chart.get("ice").put("water", 0.5);
        chart.get("ice").put("grass", 2.0);
        chart.get("ice").put("ice", 0.5);
        chart.get("ice").put("ground", 2.0);
        chart.get("ice").put("flying", 2.0);
        chart.get("ice").put("dragon", 2.0);
        chart.get("ice").put("steel", 0.5);

        // Fighting type
        chart.get("fighting").put("normal", 2.0);
        chart.get("fighting").put("ice", 2.0);
        chart.get("fighting").put("poison", 0.5);
        chart.get("fighting").put("flying", 0.5);
        chart.get("fighting").put("psychic", 0.5);
        chart.get("fighting").put("bug", 0.5);
        chart.get("fighting").put("rock", 2.0);
        chart.get("fighting").put("ghost", 0.0);
        chart.get("fighting").put("dark", 2.0);
        chart.get("fighting").put("steel", 2.0);
        chart.get("fighting").put("fairy", 0.5);

        // Poison type
        chart.get("poison").put("grass", 2.0);
        chart.get("poison").put("poison", 0.5);
        chart.get("poison").put("ground", 0.5);
        chart.get("poison").put("rock", 0.5);
        chart.get("poison").put("ghost", 0.5);
        chart.get("poison").put("steel", 0.0);
        chart.get("poison").put("fairy", 2.0);

        // Add more type matchups as needed

        return chart;
    }
}
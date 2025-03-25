package com.example.backend.controller;

import com.example.backend.models.Move;
import com.example.backend.models.PokemonResponse;
import com.example.backend.service.MoveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/moves")
public class MoveController {

    private final MoveService moveService;

    @Autowired
    public MoveController(MoveService moveService) {
        this.moveService = moveService;
    }

    @GetMapping("/{name}")
    public Mono<Move> getMoveByName(@PathVariable String name) {
        return moveService.getMoveByName(name);
    }

    @GetMapping("/id/{id}")
    public Mono<Move> getMoveById(@PathVariable Long id) {
        return moveService.getMoveById(id);
    }

    @GetMapping
    public Mono<PokemonResponse> getMoveList(
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") int offset) {
        return moveService.getMoveList(limit, offset);
    }
}
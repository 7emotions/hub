package com.hub.controller;

import org.springframework.web.bind.annotation.*;

import com.hub.dto.*;
import com.hub.service.GameService;

@RestController
@RequestMapping("/api/game")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping("/state")
    public GameStateDTO getGameState() {
        return gameService.getGameState();
    }

    @PostMapping("/move")
    public MoveResponseDTO makeMove(@RequestBody MoveRequestDTO request) {
        return gameService.makeMove(request);
    }

    @PostMapping("/new")
    public GameStateDTO newGame() {
        return gameService.newGame();
    }

    @PostMapping("/undo")
    public GameStateDTO undoMove() {
        return gameService.undoMove();
    }

    @PostMapping("/redo")
    public GameStateDTO redoMove() {
        return gameService.redoMove();
    }

    @PostMapping("/position")
    public GameStateDTO loadPosition(@RequestBody String fen) {
        return gameService.loadPosition(fen);
    }

    @GetMapping("/moves")
    public java.util.List<LegalMoveDTO> getLegalMoves() {
        return gameService.getLegalMoves();
    }
}

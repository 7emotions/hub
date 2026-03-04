package com.hub.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.hub.config.GameConfig;
import com.hub.dto.*;

class GameServiceTest {

    private GameService gameService;

    @BeforeEach
    void setUp() {
        GameConfig config = new GameConfig();
        config.setVariant("normal");
        config.setMoves(0);
        config.setTime(5.0);
        config.setInc(0.0);

        gameService = new GameService(config);
        gameService.init();
    }

    @Test
    void testNewGame() {
        GameStateDTO state = gameService.newGame();

        assertNotNull(state);
        assertNotNull(state.getBoard());
        assertEquals(0, state.getBoard().getTurn()); // white to move
        assertFalse(state.getBoard().isEnd());
        assertEquals(1, state.getBoard().getMoveNumber());
        assertNotNull(state.getLegalMoves());
        assertFalse(state.getLegalMoves().isEmpty());

        // Check initial position has 20 white pieces and 20 black pieces
        int[][] board = state.getBoard().getBoard();
        int whitePieces = 0, blackPieces = 0;
        for (int rk = 0; rk < 10; rk++) {
            for (int fl = 0; fl < 10; fl++) {
                if (board[rk][fl] == 1) whitePieces++;
                if (board[rk][fl] == 2) blackPieces++;
            }
        }
        assertEquals(20, whitePieces);
        assertEquals(20, blackPieces);
    }

    @Test
    void testMakeMove() {
        gameService.newGame();

        // Get a legal move
        java.util.List<LegalMoveDTO> moves = gameService.getLegalMoves();
        assertFalse(moves.isEmpty());

        LegalMoveDTO legalMove = moves.get(0);
        MoveRequestDTO request = new MoveRequestDTO();
        request.setFrom(legalMove.getFrom());
        request.setTo(legalMove.getTo());

        MoveResponseDTO response = gameService.makeMove(request);

        assertTrue(response.isSuccess());
        assertEquals("Move played", response.getMessage());
        assertEquals(1, response.getBoard().getTurn()); // black to move after white's move
    }

    @Test
    void testIllegalMove() {
        gameService.newGame();

        // Try to move a piece to an invalid square
        MoveRequestDTO request = new MoveRequestDTO();
        request.setFrom(1);
        request.setTo(2);

        MoveResponseDTO response = gameService.makeMove(request);

        assertFalse(response.isSuccess());
        assertEquals("Illegal move", response.getMessage());
    }

    @Test
    void testUndoRedo() {
        gameService.newGame();

        // Get initial FEN
        GameStateDTO initialState = gameService.getGameState();
        String initialFen = initialState.getBoard().getFen();

        // Make a move
        java.util.List<LegalMoveDTO> moves = gameService.getLegalMoves();
        LegalMoveDTO legalMove = moves.get(0);
        MoveRequestDTO request = new MoveRequestDTO();
        request.setFrom(legalMove.getFrom());
        request.setTo(legalMove.getTo());
        gameService.makeMove(request);

        // Verify state changed
        GameStateDTO afterMove = gameService.getGameState();
        assertNotEquals(initialFen, afterMove.getBoard().getFen());

        // Undo
        GameStateDTO afterUndo = gameService.undoMove();
        assertEquals(initialFen, afterUndo.getBoard().getFen());

        // Redo
        GameStateDTO afterRedo = gameService.redoMove();
        assertEquals(afterMove.getBoard().getFen(), afterRedo.getBoard().getFen());
    }

    @Test
    void testLoadFEN() {
        gameService.newGame();

        // Load a specific position: white king on 1, black man on 50
        String fen = "W:WK1:B50";
        GameStateDTO state = gameService.loadPosition(fen);

        assertNotNull(state);
        assertEquals(0, state.getBoard().getTurn()); // white to move
        assertEquals(fen, state.getBoard().getFen());
    }
}

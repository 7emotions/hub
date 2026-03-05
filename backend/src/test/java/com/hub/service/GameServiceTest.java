package com.hub.service;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.hub.config.GameConfig;
import com.hub.draughts.*;
import com.hub.dto.*;

import static org.junit.jupiter.api.Assertions.*;

class GameServiceTest {

    private GameService service;

    @BeforeAll
    static void initDraughts() {
        Draughts.init("normal");
    }

    @BeforeEach
    void setUp() {
        GameConfig config = new GameConfig();
        config.setVariant("normal");
        config.setMoves(0);
        config.setTime(5.0);
        config.setInc(0.0);
        service = new GameService(config);
        service.init();
    }

    // ------------------------------------------------------------------
    // Bug 1: buildBoardDTO highlighted squares after a move
    // ------------------------------------------------------------------

    @Test
    void highlightedSquaresCorrectAfterQuietMove() {
        // Starting position: White to move.
        // White man at square 31 moves to square 26 (common opening).
        MoveRequestDTO req = new MoveRequestDTO();
        req.setFrom(31);
        req.setTo(26);

        MoveResponseDTO resp = service.makeMove(req);
        assertTrue(resp.isSuccess(), "move 31-26 should succeed");

        long[] hl = resp.getBoard().getHighlightedSquares();
        assertNotNull(hl);
        assertEquals(2, hl.length, "should highlight from and to");

        java.util.Set<Long> hlSet = new java.util.HashSet<>();
        for (long v : hl) hlSet.add(v);
        assertTrue(hlSet.contains(31L), "from square 31 should be highlighted");
        assertTrue(hlSet.contains(26L), "to square 26 should be highlighted");
    }

    @Test
    void highlightedSquaresCorrectAfterCapture() {
        // White man at 33, Black man at 28, empty at 22
        service.loadPosition("W:W33:B28");

        MoveRequestDTO req = new MoveRequestDTO();
        req.setFrom(33);
        req.setTo(22);

        MoveResponseDTO resp = service.makeMove(req);
        assertTrue(resp.isSuccess(), "capture 33x22 should succeed");

        long[] hl = resp.getBoard().getHighlightedSquares();
        assertNotNull(hl);
        assertEquals(2, hl.length, "should highlight from and to after capture");

        java.util.Set<Long> hlSet = new java.util.HashSet<>();
        for (long v : hl) hlSet.add(v);
        assertTrue(hlSet.contains(33L), "from square 33 should be highlighted");
        assertTrue(hlSet.contains(22L), "to square 22 should be highlighted");
    }

    @Test
    void highlightedSquaresCorrectAfterMultiCapture() {
        // White man at 34, Black men at 29 and 18. Multi-capture: 34x23x12.
        service.loadPosition("W:W34:B29,18");

        MoveRequestDTO req = new MoveRequestDTO();
        req.setFrom(34);
        req.setTo(12);

        MoveResponseDTO resp = service.makeMove(req);
        assertTrue(resp.isSuccess(), "multi-capture 34x12 should succeed");

        long[] hl = resp.getBoard().getHighlightedSquares();
        assertNotNull(hl);
        assertEquals(2, hl.length, "should highlight from and to after multi-capture");

        java.util.Set<Long> hlSet = new java.util.HashSet<>();
        for (long v : hl) hlSet.add(v);
        assertTrue(hlSet.contains(34L), "from square 34 should be highlighted");
        assertTrue(hlSet.contains(12L), "to square 12 should be highlighted");
    }

    // ------------------------------------------------------------------
    // Bug 2: All legal capture moves can be played via makeMove
    // ------------------------------------------------------------------

    @Test
    void allLegalMovesCanBePlayed() {
        // For several capture positions, verify that every legal move
        // reported by buildLegalMoves can be played via makeMove.
        String[] positions = {
            "W:W33:B28",
            "W:W34:B29,18",
            "W:WK28:B22,33,23,32",
            "W:WK23:B18,14,29,34",
        };

        for (String fen : positions) {
            service.loadPosition(fen);
            List<LegalMoveDTO> moves = service.getLegalMoves();
            assertFalse(moves.isEmpty(), "position " + fen + " should have moves");

            for (LegalMoveDTO move : moves) {
                // Reload position before each move attempt
                service.loadPosition(fen);
                MoveRequestDTO req = new MoveRequestDTO();
                req.setFrom(move.getFrom());
                req.setTo(move.getTo());
                MoveResponseDTO resp = service.makeMove(req);
                assertTrue(resp.isSuccess(),
                        "legal move " + move.getNotation() + " (from="
                                + move.getFrom() + " to=" + move.getTo()
                                + ") should succeed for position " + fen);
            }
        }
    }

    // ------------------------------------------------------------------
    // buildLegalMoves: verify correct from/to/captured extraction
    // ------------------------------------------------------------------

    @Test
    void legalMovesFromStartPosition() {
        GameStateDTO state = service.getGameState();
        List<LegalMoveDTO> moves = state.getLegalMoves();
        // In the starting position, White has 9 possible quiet moves
        assertEquals(9, moves.size(), "starting position should have 9 legal moves");

        for (LegalMoveDTO m : moves) {
            assertTrue(m.getFrom() >= 31 && m.getFrom() <= 35,
                    "from should be a White piece on row 7 (squares 31-35)");
            assertTrue(m.getTo() >= 26 && m.getTo() <= 30,
                    "to should be an empty square on row 6 (squares 26-30)");
            assertEquals(0, m.getCaptured().length,
                    "no captures in starting position");
        }
    }

    @Test
    void legalMovesCaptureExtraction() {
        // White man at 33, Black man at 28, empty at 22
        service.loadPosition("W:W33:B28");
        List<LegalMoveDTO> moves = service.getLegalMoves();

        assertEquals(1, moves.size());
        LegalMoveDTO m = moves.get(0);
        assertEquals(33, m.getFrom());
        assertEquals(22, m.getTo());
        assertArrayEquals(new int[]{28}, m.getCaptured(),
                "captured piece at 28 should be reported");
    }

    @Test
    void legalMovesMultiCapture() {
        // White man at 34, Black men at 29 and 18
        // White can capture: 34x23x12 (captures 29 then 18)
        service.loadPosition("W:W34:B29,18");
        List<LegalMoveDTO> moves = service.getLegalMoves();

        assertEquals(1, moves.size());
        LegalMoveDTO m = moves.get(0);
        assertEquals(34, m.getFrom());
        assertEquals(12, m.getTo());

        assertEquals(2, m.getCaptured().length);
        java.util.Set<Integer> caps = new java.util.HashSet<>();
        for (int c : m.getCaptured()) caps.add(c);
        assertTrue(caps.contains(29), "should capture piece at 29");
        assertTrue(caps.contains(18), "should capture piece at 18");
    }

    @Test
    void legalMovesAfterCapture() {
        // After a capture, the opponent should have correct legal moves.
        // White man at 33, Black men at 28 and 16.
        // White captures 33x22. Then it's Black's turn with man at 16.
        service.loadPosition("W:W33:B28,16");

        MoveRequestDTO req = new MoveRequestDTO();
        req.setFrom(33);
        req.setTo(22);
        MoveResponseDTO resp = service.makeMove(req);
        assertTrue(resp.isSuccess(), "capture should succeed");

        // After capture, it's Black's turn
        assertEquals(1, resp.getBoard().getTurn(), "should be Black's turn");

        List<LegalMoveDTO> blackMoves = resp.getLegalMoves();
        assertFalse(blackMoves.isEmpty(), "Black should have legal moves");

        // Black piece at 16 should be able to move forward
        for (LegalMoveDTO m : blackMoves) {
            assertEquals(16, m.getFrom(),
                    "all Black moves should be from piece at 16");
            assertTrue(m.getTo() == 21,
                    "Black man at 16 should move to 21");
        }
    }

    @Test
    void legalMovesKingCapture() {
        // White King at 28 with multiple Black pieces to capture individually
        service.loadPosition("W:WK28:B22,33,23,32");
        List<LegalMoveDTO> moves = service.getLegalMoves();

        // All moves should be captures (single captures since pieces block each other)
        assertFalse(moves.isEmpty(), "should have capture moves");
        for (LegalMoveDTO m : moves) {
            assertEquals(28, m.getFrom(), "all moves should be from 28");
            assertTrue(m.getCaptured().length > 0, "all moves should be captures");
        }
    }
}

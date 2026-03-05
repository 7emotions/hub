package com.hub.service;

import java.util.ArrayList;

import org.springframework.stereotype.Service;

import com.hub.config.GameConfig;
import com.hub.draughts.*;
import com.hub.dto.*;

import jakarta.annotation.PostConstruct;

@Service
public class GameService {

    private Game game;
    private final GameConfig config;

    public GameService(GameConfig config) {
        this.config = config;
    }

    @PostConstruct
    public void init() {
        Draughts.init(config.getVariant());
        game = new Game();
        game.set_time_control(config.getMoves(), config.getTime() * 60.0, config.getInc());
    }

    public synchronized GameStateDTO getGameState() {
        GameStateDTO state = new GameStateDTO();
        state.setBoard(buildBoardDTO());
        state.setLegalMoves(buildLegalMoves());
        state.setWhiteTime(game.time(Side.White));
        state.setBlackTime(game.time(Side.Black));
        return state;
    }

    public synchronized MoveResponseDTO makeMove(MoveRequestDTO request) {
        MoveResponseDTO response = new MoveResponseDTO();

        try {
            int from = Square.from_std(request.getFrom());
            int to = Square.from_std(request.getTo());

            Pos pos = game.pos();
            com.hub.draughts.List list = Gen.gen_moves(pos);
            long mv = list.find(Bit.bit(from) | Bit.bit(to));

            if (mv == Move.None || mv == Move.Amb) {
                response.setSuccess(false);
                response.setMessage("Illegal move");
                response.setBoard(buildBoardDTO());
                response.setLegalMoves(buildLegalMoves());
                return response;
            }

            game.add_move(mv);
            response.setSuccess(true);
            response.setMessage("Move played");
            response.setBoard(buildBoardDTO());
            response.setLegalMoves(buildLegalMoves());
        } catch (Bad_Input e) {
            response.setSuccess(false);
            response.setMessage("Invalid square number");
            response.setBoard(buildBoardDTO());
            response.setLegalMoves(buildLegalMoves());
        }

        return response;
    }

    public synchronized GameStateDTO newGame() {
        game = new Game();
        game.set_time_control(config.getMoves(), config.getTime() * 60.0, config.getInc());
        return getGameState();
    }

    public synchronized GameStateDTO undoMove() {
        int i = game.i();
        if (i > 0) {
            game.go_to(i - 1);
        }
        return getGameState();
    }

    public synchronized GameStateDTO redoMove() {
        int i = game.i();
        if (i < game.size()) {
            game.go_to(i + 1);
        }
        return getGameState();
    }

    public synchronized GameStateDTO loadPosition(String fen) {
        try {
            Pos pos = FEN.from_fen(fen.trim());
            game = new Game(pos);
            game.set_time_control(config.getMoves(), config.getTime() * 60.0, config.getInc());
        } catch (Bad_Input e) {
            // keep current game on invalid FEN
        }
        return getGameState();
    }

    public synchronized java.util.List<LegalMoveDTO> getLegalMoves() {
        return buildLegalMoves();
    }

    private BoardDTO buildBoardDTO() {
        Pos pos = game.pos();
        int[][] board = new int[10][10];

        for (int rk = 0; rk < Square.Rank_Size; rk++) {
            for (int fl = 0; fl < Square.File_Size; fl++) {
                if (Square.is_light(fl, rk)) {
                    board[rk][fl] = -1;
                } else {
                    int sq = Square.make(fl, rk);
                    switch (pos.piece_side(sq)) {
                        case White_Man:  board[rk][fl] = 1; break;
                        case Black_Man:  board[rk][fl] = 2; break;
                        case White_King: board[rk][fl] = 3; break;
                        case Black_King: board[rk][fl] = 4; break;
                        case Empty:      board[rk][fl] = 0; break;
                    }
                }
            }
        }

        BoardDTO dto = new BoardDTO();
        dto.setBoard(board);
        dto.setTurn(pos.turn());
        dto.setEnd(game.is_end());
        dto.setMoveNumber(game.move_number());
        dto.setFen(FEN.to_fen(pos));

        long lastMove = game.last_move();
        if (lastMove != Move.None) {
            java.util.List<Long> highlighted = new ArrayList<>();
            pos = game.pos();
            long froms = lastMove & pos.side(pos.turn());
            long tos = lastMove & pos.empty();
            if (tos == 0) tos = froms;

            int from = Bit.first(froms);
            int to = Bit.first(tos);

            if (Square.is_valid(from)) {
                highlighted.add((long) Square.to_std(from));
            }
            if (Square.is_valid(to)) {
                highlighted.add((long) Square.to_std(to));
            }
            dto.setHighlightedSquares(highlighted.stream().mapToLong(Long::longValue).toArray());
        } else {
            dto.setHighlightedSquares(new long[0]);
        }

        return dto;
    }

    private java.util.List<LegalMoveDTO> buildLegalMoves() {
        java.util.List<LegalMoveDTO> moves = new ArrayList<>();
        Pos pos = game.pos();
        com.hub.draughts.List list = Gen.gen_moves(pos);

        for (int i = 0; i < list.size(); i++) {
            long mv = list.move(i);

            long froms = mv & pos.side(pos.turn());
            long tos = mv & pos.empty();
            if (tos == 0) tos = froms;

            int from = Bit.first(froms);
            int to = Bit.first(tos);

            LegalMoveDTO dto = new LegalMoveDTO();
            dto.setFrom(Square.to_std(from));
            dto.setTo(Square.to_std(to));
            dto.setNotation(Move.to_string(mv, pos));

            // 提取被吃掉的棋子位置
            long caps = mv & pos.side(Side.opp(pos.turn()));
            if (caps != 0) {
                java.util.List<Integer> capturedList = new ArrayList<>();
                for (long b = caps; b != 0; b = Bit.rest(b)) {
                    int sq = Bit.first(b);
                    capturedList.add(Square.to_std(sq));
                }
                dto.setCaptured(capturedList.stream().mapToInt(Integer::intValue).toArray());
            } else {
                dto.setCaptured(new int[0]);
            }

            moves.add(dto);
        }

        return moves;
    }
}

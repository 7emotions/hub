package com.hub.dto;

public class GameStateDTO {

    private BoardDTO board;
    private java.util.List<LegalMoveDTO> legalMoves;
    private double whiteTime;
    private double blackTime;

    public BoardDTO getBoard() {
        return board;
    }

    public void setBoard(BoardDTO board) {
        this.board = board;
    }

    public java.util.List<LegalMoveDTO> getLegalMoves() {
        return legalMoves;
    }

    public void setLegalMoves(java.util.List<LegalMoveDTO> legalMoves) {
        this.legalMoves = legalMoves;
    }

    public double getWhiteTime() {
        return whiteTime;
    }

    public void setWhiteTime(double whiteTime) {
        this.whiteTime = whiteTime;
    }

    public double getBlackTime() {
        return blackTime;
    }

    public void setBlackTime(double blackTime) {
        this.blackTime = blackTime;
    }
}

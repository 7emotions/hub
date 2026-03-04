package com.hub.dto;

public class BoardDTO {

    private int[][] board;
    private int turn;
    private boolean isEnd;
    private int moveNumber;
    private long[] highlightedSquares;
    private String fen;

    public int[][] getBoard() {
        return board;
    }

    public void setBoard(int[][] board) {
        this.board = board;
    }

    public int getTurn() {
        return turn;
    }

    public void setTurn(int turn) {
        this.turn = turn;
    }

    public boolean isEnd() {
        return isEnd;
    }

    public void setEnd(boolean end) {
        isEnd = end;
    }

    public int getMoveNumber() {
        return moveNumber;
    }

    public void setMoveNumber(int moveNumber) {
        this.moveNumber = moveNumber;
    }

    public long[] getHighlightedSquares() {
        return highlightedSquares;
    }

    public void setHighlightedSquares(long[] highlightedSquares) {
        this.highlightedSquares = highlightedSquares;
    }

    public String getFen() {
        return fen;
    }

    public void setFen(String fen) {
        this.fen = fen;
    }
}

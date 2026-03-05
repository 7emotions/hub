package com.hub.dto;

public class MoveResponseDTO {

    private boolean success;
    private String message;
    private BoardDTO board;
    private java.util.List<LegalMoveDTO> legalMoves;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

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
}

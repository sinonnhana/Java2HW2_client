package org.example.demo;

import java.io.Serializable;

public class GameState implements Serializable {
    private static final long serialVersionUID = 1L;

    private int[][] board;
    private int player1Score;
    private int player2Score;

    public GameState(int[][] board, int player1Score, int player2Score) {
        this.board = board;
        this.player1Score = player1Score;
        this.player2Score = player2Score;
    }

    public int[][] getBoard() {
        return board;
    }

    public int getPlayer1Score() {
        return player1Score;
    }

    public int getPlayer2Score() {
        return player2Score;
    }

    public void setBoard(int[][] board) {
        this.board = board;
    }

    public void setPlayer1Score(int score) {
        this.player1Score = score;
    }

    public void setPlayer2Score(int score) {
        this.player2Score = score;
    }
}

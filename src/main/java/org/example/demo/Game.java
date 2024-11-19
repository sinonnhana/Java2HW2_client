package org.example.demo;

import java.util.Random;

public class Game {

    // row length
    int row;

    // col length
    int col;

    // board content
    int[][] board;


    public Game(int[][] board){
        this.board = board;
        this.row = board.length;
        this.col = board[0].length;
    }


    // randomly initialize the game board
    public static int[][] SetupBoard(int row, int col) {
        int[][] board = new int[row][col];
        int totalCells = row * col;
        int zeroCount = totalCells / 3; // 确保0的数量占1/3以上
        int nonZeroCells = totalCells - zeroCount;
        int pairsCount = nonZeroCells / 2; // 成对的非零元素数量

        Random random = new Random();

        // 首先填充0
        for (int i = 0; i < zeroCount; i++) {
            int r, c;
            do {
                r = random.nextInt(row);
                c = random.nextInt(col);
            } while (board[r][c] != 0); // 找到一个未被填充的位置
            board[r][c] = 0;
        }

        // 填充成对的非零元素
        for (int i = 0; i < pairsCount; i++) {
            int pairValue = 1 + random.nextInt(11); // 随机生成1到11之间的数值
            for (int j = 0; j < 2; j++) { // 每个元素成对添加两次
                int r, c;
                do {
                    r = random.nextInt(row);
                    c = random.nextInt(col);
                } while (board[r][c] != 0); // 找到一个未被填充的位置
                board[r][c] = pairValue;
            }
        }

        // 填充剩余空位为0（如果有）
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                if (board[i][j] == 0) {
                    board[i][j] = 0;
                }
            }
        }

        return board;
    }

    // judge the validity of an operation
    public int[] judge(int row1, int col1, int row2, int col2){
        int[] record = new int[9];
        if ((board[row1][col1] != board[row2][col2]) || (row1 == row2 && col1 == col2)) {
            record[0] = 0;
            return record;
        }

        // one line
        if (isDirectlyConnected(row1, col1, row2, col2, board)) {
            record[0] = 1;
            record[1] = row1;
            record[2] = col1;
            record[3] = row2;
            record[4] = col2;
            return record;
        }

        // two lines
        if((row1 != row2) && (col1 != col2)){
            if(board[row1][col2] == 0 && isDirectlyConnected(row1, col1, row1, col2, board)
            && isDirectlyConnected(row1, col2, row2, col2, board)){
                record[0] = 2;
                record[1] = row1;
                record[2] = col1;
                record[3] = row1;
                record[4] = col2;
                record[5] = row2;
                record[6] = col2;
                return record;
            }

            if(board[row2][col1] == 0 && isDirectlyConnected(row2, col2, row2, col1, board)
            && isDirectlyConnected(row2, col1, row1, col1, board)){
                record[0] = 2;
                record[1] = row1;
                record[2] = col1;
                record[3] = row2;
                record[4] = col1;
                record[5] = row2;
                record[6] = col2;
                return record;
            }

        }

        // three lines
        if(row1 != row2)
            for (int i = 0; i < board[0].length; i++) {
                if (board[row1][i] == 0 && board[row2][i] == 0 &&
                        isDirectlyConnected(row1, col1, row1, i, board) && isDirectlyConnected(row1, i, row2, i, board)
                        && isDirectlyConnected(row2, col2, row2, i, board)){
                    record[0] = 3;
                    record[1] = row1;
                    record[2] = col1;
                    record[3] = row1;
                    record[4] = i;
                    record[5] = row2;
                    record[6] = i;
                    record[7] = row2;
                    record[8] = col2;
                    return record;
                }
            }
        if(col1 != col2)
            for (int j = 0; j < board.length; j++){
                if (board[j][col1] == 0 && board[j][col2] == 0 &&
                        isDirectlyConnected(row1, col1, j, col1, board) && isDirectlyConnected(j, col1, j, col2, board)
                        && isDirectlyConnected(row2, col2, j, col2, board)){
                    record[0] = 3;
                    record[1] = row1;
                    record[2] = col1;
                    record[3] = j;
                    record[4] = col1;
                    record[5] = j;
                    record[6] = col2;
                    record[7] = row2;
                    record[8] = col2;
                    return record;
                }
            }

        record[0] = 0;
        return record;
    }

    // judge whether
    private boolean isDirectlyConnected(int row1, int col1, int row2, int col2, int[][] board) {
        if (row1 == row2) {
            int minCol = Math.min(col1, col2);
            int maxCol = Math.max(col1, col2);
            for (int col = minCol + 1; col < maxCol; col++) {
                if (board[row1][col] != 0) {
                    return false;
                }
            }
            return true;
        } else if (col1 == col2) {
            int minRow = Math.min(row1, row2);
            int maxRow = Math.max(row1, row2);
            for (int row = minRow + 1; row < maxRow; row++) {
                if (board[row][col1] != 0) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

}

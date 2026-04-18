package dk.ek.chess_bot.engine.pieces;

import dk.ek.chess_bot.engine.Board;
import dk.ek.chess_bot.engine.IntegerEncoder;

public class Piece {

    private static final int[] ROOK_DIRECTIONS = {16, 1, -16, -1};
    private static final int[] BISHOP_DIRECTIONS = {15, 17, -15, -17};
    private static final int[] QUEEN_DIRECTIONS = {16, 1, -16, -1, 15, 17, -15, -17};

    // Moves for rook, bishop and queen. int[] directions is the relevant list of directions from this class
    public static int getSlidingMoves(boolean isWhite, int pos, int[] board, int[] directions, int[] buffer, int counter) {
        // Set what piece we are
        int piece = board[pos];
        // Check each direction
        for(int dir : directions){
            // Set target square to avoid doing the math again and again
            int target = pos+dir;
            // Check if target square is on the board
            if(!isOffBoard(target)){
                // If square is empty, encode as a valid move
                if(target == 0){
                    buffer[counter++] = IntegerEncoder.encodeMove(
                            pos, target, piece, false, 0
                    );
                    // If square is an enemy, encode it
                } else if (isEnemy(isWhite,board[target])){
                    buffer[counter++] = IntegerEncoder.encodeMove(
                            pos, target, piece, true, board[target]
                    );
                }
            }
        }
        return counter;
    }

    public static boolean isEnemy(boolean isWhite, int pieceToCapture) {
        if(isWhite) {
            return pieceToCapture > 7 && pieceToCapture < 15;
        }
        else {
            return pieceToCapture > 0 && pieceToCapture < 7;
        }
    }

    static boolean isOffBoard(int squareIndex){
        return (squareIndex & 0x88) != 0;
    }

    public static void main(String[] args) {
        int [] board = new Board().board;

        int[] buffer = new int[100];

        int amount = getSlidingMoves(false, 4, board, BISHOP_DIRECTIONS, buffer, 0);
        //System.out.println(amount);

        for(int i: buffer){
            System.out.println(IntegerEncoder.decodeFromSquare(i) + " -> " + IntegerEncoder.decodeToSquare(i));
        }
        System.out.println(IntegerEncoder.decodeToSquare(buffer[1]));
    }
}

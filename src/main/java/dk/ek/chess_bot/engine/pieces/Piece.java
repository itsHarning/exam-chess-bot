package dk.ek.chess_bot.engine.pieces;

import dk.ek.chess_bot.engine.Board;
import dk.ek.chess_bot.engine.IntegerEncoder;

public class Piece {

    private static final int[] ROOK_DIRECTIONS = {16, 1, -16, -1};
    private static final int[] BISHOP_DIRECTIONS = {15, 17, -15, -17};
    private static final int[] KING_QUEEN_DIRECTIONS = {16, 1, -16, -1, 15, 17, -15, -17};
    private static final int[] KNIGHT_MOVES = {33, 31, 18, -14, -33, -31, -18, 14};

    public static int getMoves(boolean isWhite, int pos, int[] board, int[] buffer, int counter){
        return switch (board[pos]) {
            case 2, 10 -> getNonSlidingMoves(isWhite, pos, board, KNIGHT_MOVES, buffer, counter);
            case 3, 11 -> getAllSlidingMoves(isWhite, pos, board, BISHOP_DIRECTIONS, buffer, counter);
            case 4, 12 -> getAllSlidingMoves(isWhite, pos, board, ROOK_DIRECTIONS, buffer, counter);
            case 5, 13 -> getAllSlidingMoves(isWhite, pos, board, KING_QUEEN_DIRECTIONS, buffer, counter);
            case 6, 14 -> getNonSlidingMoves(isWhite, pos, board, KING_QUEEN_DIRECTIONS, buffer, counter);
            default -> pos;
        };
    }

    public static int getAllSlidingMoves(boolean isWhite, int pos, int[] board, int[] directions, int[] buffer, int counter) {
        // Check each direction
        for(int direction : directions)
            // Get sliding moves
            counter = getSlidingMoves(isWhite, pos, board, direction, buffer, counter);
        return counter;
    }

    public static int getSlidingMoves(boolean isWhite, int pos, int[] board, int direction, int[] buffer, int counter){
        int piece = board[pos];
        int target = pos+direction;
        // Check if target square is on the board
        if(!isOffBoard(target)){
            // If square is empty, encode as a valid move
            if(board[target] == 0){
                buffer[counter++] = IntegerEncoder.encodeMove(
                        pos, target, piece, false, 0
                );
                // Call this method with target as new position
                counter = getSlidingMoves(isWhite, target, board, direction, buffer, counter);
                // If square is an enemy, encode it
            } else if (isEnemy(isWhite,board[target])){
                buffer[counter++] = IntegerEncoder.encodeMove(
                        pos, target, piece, true, board[target]
                );
            }
        }

        return counter;
    }

    public static int getNonSlidingMoves(boolean isWhite, int pos, int[] board, int[] directions, int[] buffer, int counter){
        // Set what piece we are
        int piece = board[pos];
        // Check each direction
        for(int direction : directions) {
            int target = pos+direction;
            // Check if target square is on the board
            if(!isOffBoard(target)){
                // If square is empty, encode as a valid move
                if(board[target] == 0){
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

        //int amount = getAllSlidingMoves(false, 1, board, BISHOP_DIRECTIONS, buffer, 0);
        int nonSliding = getNonSlidingMoves(false, 1, board, KING_QUEEN_DIRECTIONS, buffer, 0);

        for(int i: buffer){
            System.out.println(IntegerEncoder.decodeFromSquare(i) + " -> " + IntegerEncoder.decodeToSquare(i));
        }
        System.out.println(IntegerEncoder.decodeToSquare(buffer[1]));
    }
}

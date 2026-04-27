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
            case 1, 9 -> getPawnMoves(isWhite, pos, board, buffer, counter);
            case 2, 10 -> getNonSlidingMoves(isWhite, pos, board, KNIGHT_MOVES, buffer, counter);
            case 3, 11 -> getAllSlidingMoves(isWhite, pos, board, BISHOP_DIRECTIONS, buffer, counter);
            case 4, 12 -> getAllSlidingMoves(isWhite, pos, board, ROOK_DIRECTIONS, buffer, counter);
            case 5, 13 -> getAllSlidingMoves(isWhite, pos, board, KING_QUEEN_DIRECTIONS, buffer, counter);
            case 6, 14 -> getNonSlidingMoves(isWhite, pos, board, KING_QUEEN_DIRECTIONS, buffer, counter);
            default -> counter;
        };
    }

    public static int getPawnMoves(boolean isWhite, int pos, int[] board, int[] buffer, int counter) {
        int piece = board[pos];
        if(isWhite && piece != 1) return counter;
        if(!isWhite && piece != 9) return counter;

        int forward;
        // Decide forward
        if (isWhite) {forward = 16;}
        else {forward = -16;}

        // Check forward
        if(!isOffBoard(pos+forward) && board[pos+forward] == 0){
            buffer[counter++] = IntegerEncoder.encodeMove(
                    pos, pos+forward, piece, false, 0
            , false, false);
            // If pawn has not moved, check the square further forward
            if(!isOffBoard(pos+forward+forward) && board[pos+forward+forward] == 0 && pawnAtStart(isWhite, pos)) {
                buffer[counter++] = IntegerEncoder.encodeMove(
                        pos, pos+forward+forward, piece, false, 0, false, false
                );
            }
        }
        // Check if attack is possible
        if(!isOffBoard(pos+forward+1) && (isEnemy(isWhite, board[pos+forward+1]))) {
            buffer[counter++] = IntegerEncoder.encodeMove(
                    pos, pos+forward+1, piece, true, board[pos+forward+1], false, false
            );
        }
        // Check both possible attacks
        if(!isOffBoard(pos+forward-1) && (isEnemy(isWhite, board[pos+forward-1]))) {
            buffer[counter++] = IntegerEncoder.encodeMove(
                    pos, pos+forward-1, piece, true, board[pos+forward-1], false, false
            );
        }

        return counter;
    }

    public static boolean pawnAtStart(boolean isWhite, int pos) {
        if(isWhite) {return (16 <= pos && pos < 24);}
        else {return (96 <= pos && pos < 104);}
    }

    public static int getAllSlidingMoves(boolean isWhite, int pos, int[] board, int[] directions, int[] buffer, int counter) {
        int piece = board[pos];
        if(isWhite && !(piece >= 1 && piece <=6)) return counter;
        if(!isWhite && !(piece >= 9 && piece <=14)) return counter;
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
        while (true) {
            if(isOffBoard(target)) {
                break;
            }
            if(isFriend(isWhite, board[target])) {
                break;
            };
            if (isEnemy(isWhite,board[target])) {
                buffer[counter++] = IntegerEncoder.encodeMove(
                        pos, target, piece, true, board[target], false, false
                );
                break;
            }
            if(board[target] == 0){
                buffer[counter++] = IntegerEncoder.encodeMove(
                        pos, target, piece, false, 0, false, false
                );
                target += direction;
            }
        }
        /* if(!isOffBoard(target)){
            // If square is empty, encode as a valid move
            if(board[target] == 0){
                buffer[counter++] = IntegerEncoder.encodeMove(
                        pos, target, piece, false, 0, false, false
                );
                // Call this method with target as new position
                counter = getSlidingMoves(isWhite, pos, board, direction+direction, buffer, counter);
                // If square is an enemy, encode it
            } else if (isEnemy(isWhite,board[target])){
                buffer[counter++] = IntegerEncoder.encodeMove(
                        pos, target, piece, true, board[target], false, false
                );
            }
        } */

        return counter;
    }

    public static int getNonSlidingMoves(boolean isWhite, int pos, int[] board, int[] directions, int[] buffer, int counter){
        // Set what piece we are
        int piece = board[pos];

        if(isWhite && !(piece >= 1 && piece <=6)) return counter;
        if(!isWhite && !(piece >= 9 && piece <=14)) return counter;

        // Check each direction
        for(int direction : directions) {
            int target = pos+direction;
            // Check if target square is on the board
            if(!isOffBoard(target)){
                // If square is empty, encode as a valid move
                if(board[target] == 0){
                    buffer[counter++] = IntegerEncoder.encodeMove(
                            pos, target, piece, false, 0, false, false
                    );
                    // If square is an enemy, encode it
                } else if (isEnemy(isWhite,board[target])){
                    buffer[counter++] = IntegerEncoder.encodeMove(
                            pos, target, piece, true, board[target], false, false
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
    public static boolean isFriend(boolean isWhite, int pieceToCapture) {
        if(isWhite) {
            return pieceToCapture > 0 && pieceToCapture < 7;
        }
        else {
            return pieceToCapture > 8 && pieceToCapture < 15;
        }
    }

    static boolean isOffBoard(int squareIndex){
        return (squareIndex & 0x88) != 0;
    }

    public static void main(String[] args) {
        int [] board = new Board().board;

        int[] buffer = new int[100];
        board[81] = 2;
        board[83] = 2;
        //int amount = getAllSlidingMoves(false, 1, board, BISHOP_DIRECTIONS, buffer, 0);
        int nonSliding = getPawnMoves(false, 98, board, buffer, 0);

        for(int i: buffer){
            System.out.println(IntegerEncoder.decodeFromSquare(i) + " -> " + IntegerEncoder.decodeToSquare(i));
        }
        System.out.println(IntegerEncoder.decodeToSquare(buffer[1]));
    }
}

package dk.ek.chess_bot.engine;

import dk.ek.chess_bot.engine.Board;
import dk.ek.chess_bot.engine.IntegerEncoder;
import dk.ek.chess_bot.engine.ThreatDetector;

import static dk.ek.chess_bot.engine.Pieces.*;

public class MoveController {

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
            }
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

    public static int getCastling(boolean isWhite, int[] board, int[] moveList, boolean wk, boolean wq, boolean bk, boolean bq, int counter) {
        // If wk, wq, bk, bq are falses, then we return the counter with nothing changes
        if (!wk && !wq && !bk && !bq) {
            return counter;
        }
        // WK
        if (isWhite && wk) {
            if (board[4] == WKING && board[7] == WROOK) {
                if (board[5] == EMPTY && board[6] == EMPTY) {
                    if (!ThreatDetector.isSquareThreatened(board, 4, false)
                            && !ThreatDetector.isSquareThreatened(board, 5, false)
                            && !ThreatDetector.isSquareThreatened(board, 6, false)) {

                        moveList[counter++] = IntegerEncoder.encodeMove(
                                4, 6, WKING, false, 0, false, true
                        );
                    }
                }
            }
        }
        // WQ
        if (isWhite && wq) {
            if (board[4] == WKING && board[0] == WROOK) {
                if (board[1] == EMPTY && board[2] == EMPTY && board[3] ==  EMPTY) {
                    if (!ThreatDetector.isSquareThreatened(board, 4, false)
                            && !ThreatDetector.isSquareThreatened(board, 2, false)
                            && !ThreatDetector.isSquareThreatened(board, 3, false)) {

                        moveList[counter++] = IntegerEncoder.encodeMove(
                                4, 2, WKING, false, 0, false, true
                        );
                    }
                }
            }
        }
        //BK
        if (!isWhite && bk) {
            if (board[116] == BKING && board[119] == BROOK) {
                if (board[117] == EMPTY && board[118] ==  EMPTY) {
                    if (!ThreatDetector.isSquareThreatened(board, 116, true)
                            && !ThreatDetector.isSquareThreatened(board, 117, true)
                            && !ThreatDetector.isSquareThreatened(board, 118, true)) {

                        moveList[counter++] = IntegerEncoder.encodeMove(
                                116, 118, BKING, false, 0, false, true
                        );
                    }
                }
            }
        }
        //BQ
        if (!isWhite && bq) {
            if (board[116] == BKING && board[112] == BROOK) {
                if (board[113] == EMPTY && board[114] ==  EMPTY && board[115] ==  EMPTY) {
                    if (!ThreatDetector.isSquareThreatened(board, 116, true)
                            && !ThreatDetector.isSquareThreatened(board, 115, true)
                            && !ThreatDetector.isSquareThreatened(board, 114, true)) {

                        moveList[counter++] = IntegerEncoder.encodeMove(
                                116, 114, BKING, false, 0, false, true
                        );
                    }
                }
            }
        }
        //HEY Harning

        return counter;
    }

    public static void main(String[] args) {
        /* [TESTING]
        // TEST 1: White king side
        int[] board1 = new int[128];
        board1[4] = WKING;
        board1[7] = WROOK;
        board1[36] = BBISHOP;

        int[] buffer1 = new int[100];

        int result1 = getCastling(true, board1, buffer1, true, false, false, false, 0);

        System.out.println("White king side counter: " + result1);
        for (int i = 0; i < result1; i++) {
            System.out.println(IntegerEncoder.decodeFromSquare(buffer1[i]) + " -> " +
                    IntegerEncoder.decodeToSquare(buffer1[i]));
        }


        // TEST 2: White queen side
        int[] board2 = new int[128];
        board2[4] = WKING;
        board2[0] = WROOK;
        board2[36] = BBISHOP;

        int[] buffer2 = new int[100];

        int result2 = getCastling(true, board2, buffer2, false, true, false, false, 0);

        System.out.println("White queen side counter: " + result2);
        for (int i = 0; i < result2; i++) {
            System.out.println(IntegerEncoder.decodeFromSquare(buffer2[i]) + " -> " +
                    IntegerEncoder.decodeToSquare(buffer2[i]));
        }


        // TEST 3: Black king side
        int[] board3 = new int[128];
        board3[116] = BKING;
        board3[119] = BROOK;
        board3[84] = WBISHOP;

        int[] buffer3 = new int[100];

        int result3 = getCastling(false, board3, buffer3, false, false, true, false, 0);

        System.out.println("Black king side counter: " + result3);
        for (int i = 0; i < result3; i++) {
            System.out.println(IntegerEncoder.decodeFromSquare(buffer3[i]) + " -> " +
                    IntegerEncoder.decodeToSquare(buffer3[i]));
        }


        // TEST 4: Black queen side
        int[] board4 = new int[128];
        board4[116] = BKING;
        board4[112] = BROOK;


        int[] buffer4 = new int[100];

        int result4 = getCastling(false, board4, buffer4, false, false, false, true, 0);

        System.out.println("Black queen side counter: " + result4);
        for (int i = 0; i < result4; i++) {
            System.out.println(IntegerEncoder.decodeFromSquare(buffer4[i]) + " -> " +
                    IntegerEncoder.decodeToSquare(buffer4[i]));
        }
        */

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

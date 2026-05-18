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

    private static final int[] WHITE_PROMOS = {WROOK, WKNIGHT, WBISHOP, WQUEEN};
    private static final int[] BLACK_PROMOS = {BROOK, BKNIGHT, BBISHOP, BQUEEN};

    //public static int getMoves(boolean isWhite, int pos, int[] board, int[] buffer, int counter){
    public static int getMoves(boolean isWhite, int pos, int[] board, int[] buffer, int enPassantIndex, int counter){
        return switch (board[pos]) {
            case 1, 9 -> getPawnMoves(isWhite, pos, board, buffer, enPassantIndex, counter);
            case 2, 10 -> getNonSlidingMoves(isWhite, pos, board, KNIGHT_MOVES, buffer, counter);
            case 3, 11 -> getAllSlidingMoves(isWhite, pos, board, BISHOP_DIRECTIONS, buffer, counter);
            case 4, 12 -> getAllSlidingMoves(isWhite, pos, board, ROOK_DIRECTIONS, buffer, counter);
            case 5, 13 -> getAllSlidingMoves(isWhite, pos, board, KING_QUEEN_DIRECTIONS, buffer, counter);
            case 6, 14 -> getNonSlidingMoves(isWhite, pos, board, KING_QUEEN_DIRECTIONS, buffer, counter);
            default -> counter;
        };
    }

    public static int getPawnMoves(boolean isWhite, int pos, int[] board, int[] buffer, int enPassantIndex, int counter) {
        int piece = board[pos];
        if(isWhite && piece != 1) return counter;
        if(!isWhite && piece != 9) return counter;

        int forward;
        // Decide forward
        if (isWhite) {forward = 16;}
        else {forward = -16;}

        int target = pos + forward;
        boolean isPromo = isPromo(target);

        // Check forward
        if(!isOffBoard(target) && board[target] == 0){
            // If promo, encode for all promotion types
            if(isPromo) {
                for (int promoPiece : pawnPromotions(isWhite)) {
                    buffer[counter++] = IntegerEncoder.encodeMove(
                            pos, target, promoPiece, false, 0, true, false);
                }
            } else {
                // Else encode forward
                buffer[counter++] = IntegerEncoder.encodeMove(
                        pos, target, piece, false, 0, false, false);
                // And check if pawn has moved, then encode double forward
                if(!isOffBoard(target+forward) && board[target+forward] == 0 && pawnAtStart(isWhite, pos)) {
                    buffer[counter++] = IntegerEncoder.encodeMove(
                            pos, target+forward, piece, false, 0, false, false

                    );
                }
            }
        }

        // Check if attack is possible
        int attackIndex1 = target+1;
        int attackIndex2 = target-1;

        // If promotion, encode move for each promotion piece, where we set piece moving to the promotion piece
        if(isPromo) {
            for (int promoPiece : pawnPromotions(isWhite)) {
                counter = checkPawnAttacks(attackIndex1, isWhite, board, buffer, counter, pos, promoPiece, true, enPassantIndex);
                counter = checkPawnAttacks(attackIndex2, isWhite, board, buffer, counter, pos, promoPiece, true, enPassantIndex);
            }
            // Else just check regular attacks
        } else {
            counter = checkPawnAttacks(attackIndex1, isWhite, board, buffer, counter, pos, piece, false, enPassantIndex);
            counter = checkPawnAttacks(attackIndex2, isWhite, board, buffer, counter, pos, piece, false, enPassantIndex);
        }


        return counter;
    }

    // Used for pawn movement when encoding move
    public static boolean isPromo(int target){
            return (target >= 112 && target < 120) || (target >= 0 && target < 8);
    }

    // Encode pawn attack - for promos, the piece transformed into is encoded as piece moving
    public static int checkPawnAttacks(int attackIndex, boolean isWhite, int[] board, int [] buffer, int counter, int pos, int piece, boolean isPromo, int enPassantIndex) {
        if(!isOffBoard(attackIndex) && (isEnemy(isWhite, board[attackIndex]))
                || (attackIndex == enPassantIndex && enPassantIndex != -1)) {
            buffer[counter++] = IntegerEncoder.encodeMove(
                    pos, attackIndex, piece, true, board[attackIndex], isPromo, false
            );
        } return counter;
    }



    public static boolean pawnAtStart(boolean isWhite, int pos) {
        if(isWhite) {return (16 <= pos && pos < 24);}
        else {return (96 <= pos && pos < 104);}
    }

    public static int[] pawnPromotions(boolean isWhite) {
        return isWhite ? WHITE_PROMOS : BLACK_PROMOS;
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

    void testIsPromo(int pawnPos){
        int[] board = Board.getFreshBoard();
        board[pawnPos] = BPAWN;

        int[] buffer = new int [100];


        int result = getPawnMoves(false, pawnPos, board,buffer, -1, 0);

//        System.out.println("Position: " + pawnPos + ", Pawn moves: " + result);
        for (int i = 0; i < result; i++) {
            int move = buffer[i];
//            System.out.println(IntegerEncoder.decodeFromSquare(move) + " -> "
//                    + IntegerEncoder.decodeToSquare(move)
//                    + ", PROMO:" + IntegerEncoder.decodeIsPromo(move)
//                    + ", NEW PIECE: " + IntegerEncoder.decodeOwnPieceType(move)
//                    + ", MOVE SCORE: " + IntegerEncoder.decodeScore(move));
        }
//        System.out.println();
    }
}

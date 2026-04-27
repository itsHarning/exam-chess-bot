package dk.ek.chess_bot.engine;

import static dk.ek.chess_bot.engine.GameStateManager.board;
import static dk.ek.chess_bot.engine.Pieces.*;

public class ThreatDetector {

    private static final int[] KNIGHT_MOVES = {33, 31, 18, -14, -33, -31, -18, 14};
    private static final int[] ROOK_DIRECTIONS = {16, 1, -16, -1};
    private static final int[] BISHOP_DIRECTIONS = {15, 17, -15, -17};
    private static final int[] KING_DIRECTIONS = {16, 1, -16, -1, 15, 17, -15, -17};

    public static boolean isSquareThreatened(int[] board, int square, boolean byWhite) {
        if (isOffBoard(square)) {
            return false;
        }

        return pawnThreatens(board, square, byWhite)
                || knightThreatens(board, square, byWhite)
                || slidingThreatens(board, square, byWhite, ROOK_DIRECTIONS, WROOK, BROOK, WQUEEN, BQUEEN)
                || slidingThreatens(board, square, byWhite, BISHOP_DIRECTIONS, WBISHOP, BBISHOP, WQUEEN, BQUEEN)
                || kingThreatens(board, square, byWhite);

    }

    public static boolean isKingInCheck(int[] board, boolean whiteKing) {
        int king = whiteKing ? WKING : BKING;
        int kingSquare = findPiece(board, king);

        if (kingSquare == -1) {
            return false;
        }

        return isSquareThreatened(board, kingSquare, !whiteKing);
    }

    private static int findPiece(int[] board, int piece) {
        for (int i = 0; i < 128; i++) {
            if (!isOffBoard(i) && board[i] == piece) {
                return i;
            }
        }
        return -1;
    }

    private static boolean pawnThreatens(int[] board, int square, boolean byWhite) {
        int pawn = byWhite ? WPAWN : BPAWN;

        int attack1 = byWhite ? square - 15 : square + 15;
        int attack2 =  byWhite ? square - 17 : square + 17;

        return (!isOffBoard(attack1) && board[attack1] == pawn)
                || (!isOffBoard(attack2) && board[attack2] == pawn);
    }

    private static boolean knightThreatens(int[] board, int square, boolean byWhite) {
        int knight = byWhite ? WKNIGHT : BKNIGHT;

        for (int move : KNIGHT_MOVES) {
            int target = square + move;

            if (!isOffBoard(target) && board[target] == knight) {
                return true;
            }
        }

        return false;
    }

    private static boolean slidingThreatens(
            int[] board,
            int square,
            boolean byWhite,
            int[] directions,
            int whitePiece,
            int blackPiece,
            int whiteQueen,
            int blackQueen
    ) {
        int piece = byWhite ? whitePiece : blackPiece;
        int queen = byWhite ? whiteQueen : blackQueen;

        for (int direction : directions) {
            int target = square + direction;

            while (!isOffBoard(target)) {
                if (board[target] == EMPTY) {
                    target += direction;
                    continue;
                }

                if (board[target] == piece || board[target] == queen) {
                    return true;
                }
                break;
            }
        }
        return false;
    }

    private static boolean kingThreatens(int[] board, int square, boolean byWhite) {
        int king = byWhite ? WKING : BKING;

        for (int direction : KING_DIRECTIONS) {
            int target = square + direction;

            if (!isOffBoard(target) && board[target] == king) {
                return true;
            }
        }
        return false;
    }

    private static boolean isOffBoard(int square) {
        return (square & 0x88) != 0;
    }
    // TESTING
    public static void main(String[] args) {
        /*/ TEST 1 Noncheck m. fuld fresh board.
        int[] board = Board.getFreshBoard();

        System.out.println("Start position:");
        Board.printBoard(board);

        boolean whiteInCheck = isKingInCheck(board, true);
        boolean blackInCheck = isKingInCheck(board, false);

        System.out.println("White king in check: " + whiteInCheck);
        System.out.println("Black king in check: " + blackInCheck);
        /*/
        // TEST 2 CHECK!
        int[] board = new int[128];

        board[4] = WKING; // White king on e1
        board[116] = BKING; // black king on e8
        board[36] = BROOK; // Black rook on e3
        board[20] = WPAWN; // White Pawn on e2 // Blocking the BRook.

        Board.printBoard(board);

        System.out.println("White king in check: " + isKingInCheck(board, true)); //True = white king
        System.out.println("Black king in check: " + isKingInCheck(board, false)); //False = black king

        board[20] = EMPTY;
        Board.printBoard(board);
        System.out.println("White king in check: " + isKingInCheck(board, true));
        //
    }


}


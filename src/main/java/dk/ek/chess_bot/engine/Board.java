package dk.ek.chess_bot.engine;

import static dk.ek.chess_bot.engine.Pieces.*;

public class Board {

    // A 0x88 integer array representing pieces on a chess board.
    public int[] board;

    // The constructor for class Board.
    // Initialises the board to the chess starting position and sets the current player colour to white.
    public Board() {
        this.board = new int[] {
                WROOK, WKNIGHT, WBISHOP, WQUEEN, WKING, WBISHOP, WKNIGHT, WROOK, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY,
                WPAWN, WPAWN, WPAWN, WPAWN, WPAWN, WPAWN, WPAWN, WPAWN, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY,
                EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY,
                EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY,
                EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY,
                EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY,
                BPAWN, BPAWN, BPAWN, BPAWN, BPAWN, BPAWN, BPAWN, BPAWN, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY,
                BROOK, BKNIGHT, BBISHOP, BQUEEN, BKING, BBISHOP, BKNIGHT, BROOK, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY };
    }

    // Another constructor for class Board.
    // Initialises the board to the positions represented by the FEN string.
    public Board(String fen) {
        GameState gameState = Translator.gameStateFromFEN(fen);
        this.board = gameState.getCurrentBoard();
    }
    //The left segment is the board, the right is off the board.
    //IMPORTANT: Remember the arrays are flipped from what we usually expect in chess, so for white we fetch in reverse order
    //Just to say we are writing these to be human readable at a glance, but for WHITE we need to read them reverse
    final static int[] examplePieceSquareTable = new int[]{
            0   ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,     0,0,0,0,0,0,0,0,
            0   ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,     0,0,0,0,0,0,0,0,
            0   ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,     0,0,0,0,0,0,0,0,
            0   ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,     0,0,0,0,0,0,0,0,
            0   ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,     0,0,0,0,0,0,0,0,
            0   ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,     0,0,0,0,0,0,0,0,
            0   ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,     0,0,0,0,0,0,0,0,
            0   ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,     0,0,0,0,0,0,0,0,
    };

    final static int[] pawnScores = new int[]{
            0   ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,     0,0,0,0,0,0,0,0,
            50  ,50 ,50 ,50 ,50 ,50 ,50 ,50 ,     0,0,0,0,0,0,0,0,
            10  ,10 ,20 ,30 ,30 ,20 ,10 ,10 ,     0,0,0,0,0,0,0,0,
            5   ,5  ,10 ,27 ,27 ,10 ,5  ,5  ,     0,0,0,0,0,0,0,0,
            0   ,0  ,0  ,25 ,25 ,0  ,0  ,0  ,     0,0,0,0,0,0,0,0,
            -5  ,-5 ,10 ,0  ,0  ,-10,-5 ,-5 ,     0,0,0,0,0,0,0,0,
            5   ,10 ,10 ,-25,-25,10 ,10 ,5  ,     0,0,0,0,0,0,0,0,
            0   ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,     0,0,0,0,0,0,0,0
    };

    final static int[] knightScores = new int[]{
            -50 ,-40,-30,-30,-30,-30,-40,-50,     0,0,0,0,0,0,0,0,
            -40 ,-20,0  ,0  ,0  ,0  ,-20,-40,     0,0,0,0,0,0,0,0,
            -30 ,0  ,10 ,15 ,15 ,10 ,0  ,-30,     0,0,0,0,0,0,0,0,
            -30 ,5  ,15 ,20 ,20 ,15 ,5  ,-30,     0,0,0,0,0,0,0,0,
            -30 ,0  ,15 ,20 ,20 ,15 ,0  ,-30,     0,0,0,0,0,0,0,0,
            -30 ,5  ,10 ,15 ,15 ,10 ,5  ,-30,     0,0,0,0,0,0,0,0,
            -40 ,-20,0  ,5  ,5  ,0  ,-20,-40,     0,0,0,0,0,0,0,0,
            -50 ,-40,-20,-30,-30,-20,-40,-50,     0,0,0,0,0,0,0,0
    };
    final static int[] bishopScores = new int[]{
            -20,-10,-10,-10,-10,-10,-10,-20,      0,0,0,0,0,0,0,0,
            -10,  0,  0,  0,  0,  0,  0,-10,      0,0,0,0,0,0,0,0,
            -10,  0,  5, 10, 10,  5,  0,-10,      0,0,0,0,0,0,0,0,
            -10,  5,  5, 10, 10,  5,  5,-10,      0,0,0,0,0,0,0,0,
            -10,  0, 10, 10, 10, 10,  0,-10,      0,0,0,0,0,0,0,0,
            -10, 10, 10, 10, 10, 10, 10,-10,      0,0,0,0,0,0,0,0,
            -10,  5,  0,  0,  0,  0,  5,-10,      0,0,0,0,0,0,0,0,
            -20,-10,-40,-10,-10,-40,-10,-20,      0,0,0,0,0,0,0,0
    };
    final static int[] rookScores = new int[]{
            0   ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,     0,0,0,0,0,0,0,0,
            5   ,10 ,10 ,10 ,10 ,10 ,10 ,5  ,     0,0,0,0,0,0,0,0,
            -5  ,0  ,0  ,0  ,0  ,0  ,0  ,-5 ,     0,0,0,0,0,0,0,0,
            -5  ,0  ,0  ,0  ,0  ,0  ,0  ,-5 ,     0,0,0,0,0,0,0,0,
            -5  ,0  ,0  ,0  ,0  ,0  ,0  ,-5 ,     0,0,0,0,0,0,0,0,
            -5  ,0  ,0  ,0  ,0  ,0  ,0  ,-5 ,     0,0,0,0,0,0,0,0,
            -5  ,0  ,0  ,0  ,0  ,0  ,0  ,-5 ,     0,0,0,0,0,0,0,0,
            0   ,0  ,0  ,5  ,5  ,0  ,0  ,0  ,     0,0,0,0,0,0,0,0
    };
    final static int[] kingEarlyScores = new int[]{
            -30, -40, -40, -50, -50, -40, -40, -30,     0,0,0,0,0,0,0,0,
            -30, -40, -40, -50, -50, -40, -40, -30,     0,0,0,0,0,0,0,0,
            -30, -40, -40, -50, -50, -40, -40, -30,     0,0,0,0,0,0,0,0,
            -30, -40, -40, -50, -50, -40, -40, -30,     0,0,0,0,0,0,0,0,
            -20, -30, -30, -40, -40, -30, -30, -20,     0,0,0,0,0,0,0,0,
            -10, -20, -20, -20, -20, -20, -20, -10,     0,0,0,0,0,0,0,0,
             20,  20,   0,   0,   0,   0,  20,  20,     0,0,0,0,0,0,0,0,
             20,  30,  10,   0,   0,  10,  30,  20,     0,0,0,0,0,0,0,0
    };
    final static int[] kingLateScores = new int[]{ //In the late game, the king becomes a valuable attacking piece! Let's use it!
            -50,-40,-30,-20,-20,-30,-40,-50,        0,0,0,0,0,0,0,0,
            -30,-20,-10,  0,  0,-10,-20,-30,        0,0,0,0,0,0,0,0,
            -30,-10, 20, 30, 30, 20,-10,-30,        0,0,0,0,0,0,0,0,
            -30,-10, 30, 40, 40, 30,-10,-30,        0,0,0,0,0,0,0,0,
            -30,-10, 30, 40, 40, 30,-10,-30,        0,0,0,0,0,0,0,0,
            -30,-10, 20, 30, 30, 20,-10,-30,        0,0,0,0,0,0,0,0,
            -30,-30,  0,  0,  0,  0,-30,-30,        0,0,0,0,0,0,0,0,
            -50,-30,-30,-30,-30,-30,-30,-50,         0,0,0,0,0,0,0,0
    };

    public static int getScore(int[] board, boolean isWhite){
        int score = 0;
        int phaseScore = 0; //Here we add the value of non pawn/king pieces to interpolate between early/mid/late game.
        for (int i = 0; i < 128; i++) {
            switch(board[i]){
                case 1:
                    score += 100; //We give a flat boost for having a pawn
                    score += getPieceScore(pawnScores, i, true);
                    break;
                case 9:
                    score -= 100; //We give a flat boost for having a pawn
                    score -= getPieceScore(pawnScores, i, false);
                    break;
                case 2:
                    score += 300;
                    phaseScore += 1;
                    score += getPieceScore(knightScores, i, true);
                    break;
                case 10:
                    score -= 300;
                    phaseScore += 1;
                    score -= getPieceScore(knightScores, i, false);
                    break;
                case 3:
                    score += 300;
                    phaseScore += 1;
                    score += getPieceScore(bishopScores, i, true);
                    break;
                case 11:
                    score -= 300;
                    phaseScore += 1;
                    score -= getPieceScore(bishopScores, i, false);
                    break;
                case 4:
                    score += 500;
                    phaseScore += 2;
                    score += getPieceScore(rookScores, i, true);
                    break;
                case 12:
                    score -= 500;
                    phaseScore += 2;
                    score -= getPieceScore(rookScores, i, false);
                    break;
                case 5:
                    score += 900;
                    phaseScore += 4;
                    //We don't need a queen square table. She is so strong she is good everywhere! and her high base value makes boosting for position less relevant.
                    break;
                case 13:
                    score -= 900;
                    phaseScore += 4;
                    //We don't need a queen square table. She is so strong she is good everywhere! and her high base value makes boosting for position less relevant.
                    break;
                case 6:
                    score += 20000; //REMEMBER HIGH VALUE FOR THE KING!
                    if(phaseScore > 24) phaseScore = 24;
                    score += (int) lerp(
                            getPieceScore(kingEarlyScores, i, true),
                            getPieceScore(kingLateScores, i, true),
                            phaseValues[phaseScore]);
                    break;
                case 14:
                    score -= 20000;
                    if(phaseScore > 24) phaseScore = 24;
                    score -= (int) lerp(
                            getPieceScore(kingEarlyScores, i, false),
                            getPieceScore(kingLateScores, i, false),
                            phaseValues[phaseScore]);
                    break;

            }
        }

        if (isWhite){return score;} //If we are white we want the score as is calculated
        else{return -score;} //If we are black we want the inverse
    }

    static final int[] phaseValues = new int[] {0, 4, 8, 12, 17, 21, 25, 29, 33, 37, 42, 46, 50, 54, 58, 62, 67, 71, 75, 79, 83, 88, 92, 96, 100};

    static double lerp(int a, int b, int slider){
        if (slider<1) slider = 1;
        if (slider>100) slider = 100;
        double t = (double) (slider-1)/99;

        return (double) a+t*(b-a);
    }

    static int getPieceScore(int[] pieceSquareTable, int pos, boolean isWhite){
        if(isWhite){pos = pos^0b01110111;} //We flip the rank and file of the position, "flipping it" (Simply counting from the back won't work)
        return pieceSquareTable[pos];
    }

    static int[] getFreshBoard(){
        return new int[] {
                WROOK, WKNIGHT, WBISHOP, WQUEEN, WKING, WBISHOP, WKNIGHT, WROOK,    EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY,
                WPAWN, WPAWN, WPAWN, WPAWN, WPAWN, WPAWN, WPAWN, WPAWN,             EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY,
                EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY,             EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY,
                EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY,             EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY,
                EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY,             EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY,
                EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY,             EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY,
                BPAWN, BPAWN, BPAWN, BPAWN, BPAWN, BPAWN, BPAWN, BPAWN,             EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY,
                BROOK, BKNIGHT, BBISHOP, BQUEEN, BKING, BBISHOP, BKNIGHT, BROOK,    EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY };

    }

    static int[] getSetup(){
        //Build a function here that contacts the GUI to either get a clean board or a FEN board


        return getFreshBoard();
    }


    static void printBoard(int[] board){
        boolean whitespace = true;
        for (int i = 7; i > -1; i--) {
            switch (i){
                case 0:
                    System.out.print("1");
                    break;
                case 1:
                    System.out.print("2");
                    break;
                case 2:
                    System.out.print("3");
                    break;
                case 3:
                    System.out.print("4");
                    break;
                case 4:
                    System.out.print("5");
                    break;
                case 5:
                    System.out.print("6");
                    break;
                case 6:
                    System.out.print("7");
                    break;
                case 7:
                    System.out.print("8");
                    break;
            }

            for (int j = 0; j < 8; j++) {
                System.out.print("\t");
                int space = board[(j+(i*16))];

                switch (space){
                    case 1:
                        System.out.print("♙");
                        break;
                    case 2:
                        System.out.print("♘");
                        break;
                    case 3:
                        System.out.print("♗");
                        break;
                    case 4:
                        System.out.print("♖");
                        break;
                    case 5:
                        System.out.print("♕");
                        break;
                    case 6:
                        System.out.print("♔");
                        break;
                    case 9:
                        System.out.print("♟");
                        break;
                    case 10:
                        System.out.print("♞");
                        break;
                    case 11:
                        System.out.print("♝");
                        break;
                    case 12:
                        System.out.print("♜");
                        break;
                    case 13:
                        System.out.print("♛");
                        break;
                    case 14:
                        System.out.print("♚");
                        break;
                    default:
                        if(whitespace){
                            System.out.print("⬜");
                        }else{
                            System.out.print("⬛");
                        }
                }
                whitespace = !whitespace;
            }
            System.out.println();
            whitespace = !whitespace;
        }
        // System.out.println("###############################");
    }
}

package dk.harning.chess_demo.engine;

import dk.harning.chess_demo.engine.pieces.Pawn;

public class Bot {

    static int[] currentBoard;
    static boolean blackCastleKingSide;
    static boolean blackCastleQueenSide;
    static boolean whiteCastleKingSide;
    static boolean whiteCastleQueenSide;

    static boolean isWhiteToMove;
    static int enPassantIndex;
    static int totalMoves;
    static int halfMoveClock;

    static String getNextMove(GameState gameState){
        currentBoard = gameState.getCurrentBoard();
        blackCastleQueenSide = gameState.isBlackCastleQueenSide();
        blackCastleKingSide = gameState.isBlackCastleKingSide();
        whiteCastleKingSide = gameState.isBlackCastleKingSide();
        whiteCastleQueenSide = gameState.isWhiteCastleQueenSide();
        isWhiteToMove = gameState.isWhiteToMove();
        enPassantIndex = gameState.getEnPassantIndex();
        totalMoves = gameState.getTotalMoves();
        halfMoveClock = gameState.getHalfMoveClock();

        int[] possibleMoves = getAllMoves();

        return "Not finished yet";
    }

    static int[] getAllMoves(){
        int[] possibleMoves = new int[218];

        int movesFound = 0; //Counting variable let's us access the index on the array we have reached

        for(int move: Pawn.getPawnMoves()){
            if (move != 0){
                possibleMoves[movesFound] = move;
                movesFound ++;
            }
        }


        return new int[]{1};
    }

}

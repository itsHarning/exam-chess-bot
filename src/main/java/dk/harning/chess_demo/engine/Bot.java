package dk.harning.chess_demo.engine;

import dk.harning.chess_demo.engine.pieces.Pawn;
import dk.harning.chess_demo.engine.pieces.Rook;

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

        int[][] possibleMoves = new int[64][256];
        getAllMoves(possibleMoves, 0);

        return "Not finished yet";
    }

    static void getAllMoves(int[][] buffer, int depth){

        int counter = 0; //Counting variable lets us access the index on the array we have reached
        counter = Pawn.getMoves(currentBoard, buffer[depth], counter, isWhiteToMove);



    }

}

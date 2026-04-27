package dk.ek.chess_bot.engine;

import dk.ek.chess_bot.engine.pieces.Pawn;
import dk.ek.chess_bot.engine.pieces.Piece;

import java.sql.Time;

import static dk.ek.chess_bot.engine.Pieces.*;
import static dk.ek.chess_bot.engine.Pieces.BBISHOP;
import static dk.ek.chess_bot.engine.Pieces.BKING;
import static dk.ek.chess_bot.engine.Pieces.BKNIGHT;
import static dk.ek.chess_bot.engine.Pieces.BPAWN;
import static dk.ek.chess_bot.engine.Pieces.BQUEEN;
import static dk.ek.chess_bot.engine.Pieces.BROOK;
import static dk.ek.chess_bot.engine.Pieces.EMPTY;
import static dk.ek.chess_bot.engine.Pieces.WBISHOP;
import static dk.ek.chess_bot.engine.Pieces.WKING;
import static dk.ek.chess_bot.engine.Pieces.WKNIGHT;
import static dk.ek.chess_bot.engine.Pieces.WPAWN;
import static dk.ek.chess_bot.engine.Pieces.WQUEEN;
import static dk.ek.chess_bot.engine.Pieces.WROOK;

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

    static int bestMoveSoFar;
    static int identity;

    static int nodesSearched = 0;

    static GameState getNextMove(GameState gameState){
        currentBoard = gameState.getCurrentBoard();
        blackCastleQueenSide = gameState.isBlackCastleQueenSide();
        blackCastleKingSide = gameState.isBlackCastleKingSide();
        whiteCastleKingSide = gameState.isWhiteCastleKingSide();
        whiteCastleQueenSide = gameState.isWhiteCastleQueenSide();
        isWhiteToMove = gameState.isWhiteToMove();
        enPassantIndex = gameState.getEnPassantIndex();
        totalMoves = gameState.getTotalMoves();
        halfMoveClock = gameState.getHalfMoveClock();
        if(isWhiteToMove){identity = 0;}else{identity = 8;}


        //We reset the best move, to purge old info
        bestMoveSoFar = 0;
        nodesSearched = 0; //Amount of nodes searched too

        int[][] possibleMoves = new int[64][256];
        int counter = 0;
        for (int i = 0; i < 128; i++) {
            counter = Piece.getMoves(isWhiteToMove, i, currentBoard, possibleMoves[0], counter);
        }

        int bestMoveFound = 0;
        int alpha = -100000;
        int beta = 100000;


        for (int i = 0; i < counter; i++) {
            makeMove(possibleMoves[0][i]);
            int score = alphaBeta(possibleMoves, 0, 6, false, alpha, beta);
            unMakeMove(possibleMoves[0][i]);
            if(score > alpha){
                alpha = score;
                bestMoveFound = possibleMoves[0][i];
            }
        }
        System.out.println("score before: " + Board.getScore(currentBoard, isWhiteToMove));
        makeMove(bestMoveFound);
        System.out.println("Found this as the best move, with a score of: " + Board.getScore(currentBoard, !isWhiteToMove) + " having searched: " + nodesSearched + " nodes");
        Board.printBoard(currentBoard);

        GameState newGameState = new GameState();
        newGameState.setWhiteToMove(isWhiteToMove);
        newGameState.setCurrentBoard(currentBoard);

        return newGameState;
    }

    static void makeMove(int move){
        //Many cases are appropriate to consider here. First we get the basic info
        int pieceType = IntegerEncoder.decodeOwnPieceType(move);
        if(!isWhiteToMove) pieceType = 0b1000|pieceType;
        int fromSquare = IntegerEncoder.decodeFromSquare(move);
        int toSquare = IntegerEncoder.decodeToSquare(move);

        //First - Normal move from one square to the other.
        currentBoard[fromSquare] = 0;
        currentBoard[toSquare] = pieceType;

        //Now, if this was a capture, the capture is implicit. If there was an enemy in the "toSquare", it is now overwritten. This is not so for the unmake function.
        //No need for specific capture logic here

        //What if it is a promotion?
        if(IntegerEncoder.decodeIsPromo(move)){
            currentBoard[toSquare] = IntegerEncoder.decodeCapturedPieceType(move); //If it is a promotion, we use the captured piecetype bits to indicate what the promotion should be

            if(IntegerEncoder.decodeIsCapture(move)){ //Technically a move can be both a capture and a promotion, in that case, we just make a queen
                if(isWhiteToMove){
                    currentBoard[toSquare] = 5; //If it is both a capture and a promo, we just make a queen
                }
                else{
                    currentBoard[toSquare] = 13; //Vice versa if we are black
                }
            }
        }
        //What if it is En Passant? we need to remove the pawn that is taken
        if(toSquare == enPassantIndex){
            if(isWhiteToMove){ //When it is white, we know we have to look down
                currentBoard[toSquare-16] = 0; //We erase the pawn below
            }
            else{
                currentBoard[toSquare+16] = 0; //We erase the pawn above
            }
        }

        //what if we are castling?
        if(IntegerEncoder.decodeIsCastle(move)){
            //Then we make the logic cool and good, for example we need to restore castling rights!
        }

        //Finally we change the side to act
        isWhiteToMove = !isWhiteToMove;
    }

    static void unMakeMove(int move){ //Unmaking a move is also quite complicated
        //1: We change back to be able to know who "We" are.
        isWhiteToMove = !isWhiteToMove;

        //2: Get basic info on the move
        int pieceType = IntegerEncoder.decodeOwnPieceType(move);
        if(!isWhiteToMove) pieceType = 0b1000 |pieceType;
        int fromSquare = IntegerEncoder.decodeFromSquare(move);
        int toSquare = IntegerEncoder.decodeToSquare(move);

        //3: restore the fromsquare to the piece we moved
        currentBoard[fromSquare] = pieceType;

        //4: Handle if it was a capture
        if(IntegerEncoder.decodeIsCapture(move)){ //If the move was a capture, we need to put the captured piece back
            int capPieceType = IntegerEncoder.decodeCapturedPieceType(move);
            if(isWhiteToMove){ //Flip to the black version if we are white (because white takes black pieces)
                capPieceType = 0b1000 | capPieceType; //We do bit math to change the number!
            }
            currentBoard[toSquare] = capPieceType; //We put the piece back
        }else{
            currentBoard[toSquare] = 0; //If it was a simple move we can leave it as empty
        }

        if(IntegerEncoder.decodeIsPromo(move)){
            if(isWhiteToMove){
                currentBoard[fromSquare] = 1;
            }
            else{
                currentBoard[fromSquare] = 9;
            }
            //We just make sure we don't put a promoted queen into where there was a pawn
        }

        //If the move was enPassant we put things back
        if(toSquare == enPassantIndex){
            if (isWhiteToMove){
                currentBoard[toSquare-16] = 9;
            }
            else{
                currentBoard[toSquare+16] = 1;
            }
        }

        //If it is castling
        if(IntegerEncoder.decodeIsCastle(move)){
            //Make the logic solid and sturdy, and make the gyal them flirty and dirty
        }

    }

    static String convertIndexToCoordinates(int index){
        int rank = 1; //3
        int file; //2

        while(index>=16){
            rank++;
            index -= 16;
        }

        file = index +1;

        String fileLetter = "U";

        switch(file){
            case 1:
                fileLetter = "A";
                break;
            case 2:
                fileLetter = "B";
                break;
            case 3:
                fileLetter = "C";
                break;
            case 4:
                fileLetter = "D";
                break;
            case 5:
                fileLetter = "E";
                break;
            case 6:
                fileLetter = "F";
                break;
            case 7:
                fileLetter = "G";
                break;
            case 8:
                fileLetter = "H";
                break;
        }

        return fileLetter + rank;
    }

    static int alphaBeta(int[][] moveList, int depth, int targetDepth, boolean isMax, int alpha, int beta){
        depth = depth+1; //We start by incrementing the depth

        nodesSearched++;

        /*
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {

        }

         */


        if(depth == targetDepth){
            return Board.getScore(currentBoard, isWhiteToMove);
        }



        int counter = 0;

        if(isMax){
            for (int i = 0; i < 128; i++) {
                counter = Piece.getMoves(isWhiteToMove, i, currentBoard, moveList[depth], counter);
            }
            for (int i = 0; i < counter; i++) {
                //TODO: implement simple selection sort

                int move = moveList[depth][i];
                if (move != 0) {
                    makeMove(move);
                    int score = alphaBeta(moveList, depth, targetDepth, !isMax, alpha, beta);
                    unMakeMove(move);

                    alpha = Math.max(alpha, score);
                    if (beta <= alpha) {
                        return alpha;
                    }
                }
            }
            return alpha;
        }
        else{
            for (int i = 0; i < 128; i++) {
                counter = Piece.getMoves(isWhiteToMove, i, currentBoard, moveList[depth], counter);
            }
            for (int i = 0; i < counter; i++) {
                //TODO: implement simple selection sort

                int move = moveList[depth][i];
                if (move != 0) {
                    makeMove(move);
                    int score = alphaBeta(moveList, depth, targetDepth, !isMax, alpha, beta);
                    unMakeMove(move);

                    beta = Math.min(beta, score);
                    if (beta <= alpha) {
                        return beta;
                    }
                }
            }
            return beta;
        }
    }

    static int getScore(int[] board){
        return 1; //Implement board heuristic here
    }

    static void getAllMoves(int[][] buffer, int depth){

        int counter = 0; //Counting variable lets us access the index on the array we have reached
        counter = Pawn.getMoves(currentBoard, buffer[depth], counter, isWhiteToMove);


    }

    public static void main(String[] args) {
        System.out.println(convertIndexToCoordinates(16));

        GameState gameState = new GameState();

        int[] board = new int[] {
                WROOK,  WKNIGHT, WBISHOP, WQUEEN, WKING, WBISHOP, WKNIGHT, WROOK,        EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY,
                WPAWN,  WPAWN,   EMPTY,   EMPTY,  EMPTY, WPAWN,   WPAWN,   WPAWN,        EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY,
                EMPTY,  EMPTY,   WPAWN,   WPAWN,  EMPTY, EMPTY,   EMPTY,   EMPTY,        EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY,
                EMPTY,  BBISHOP, EMPTY,   EMPTY,  WPAWN, EMPTY,   EMPTY,   BQUEEN,       EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY,
                EMPTY,  EMPTY,   EMPTY,   EMPTY,  BPAWN, EMPTY,   EMPTY,   EMPTY,        EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY,
                EMPTY,  EMPTY,   EMPTY,   EMPTY,  EMPTY, EMPTY,   EMPTY,   EMPTY,        EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY,
                BPAWN,  BPAWN,   BPAWN,   BPAWN,  EMPTY, BPAWN,   BPAWN,   BPAWN,        EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY,
                BROOK,  BKNIGHT, BBISHOP, EMPTY,  BKING, EMPTY,   BKNIGHT, BROOK,        EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY };
        Board.printBoard(board);
        gameState.setWhiteToMove(true);
        gameState.setCurrentBoard(board);


        getNextMove(gameState);


    }
}

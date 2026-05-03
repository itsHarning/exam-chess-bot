package dk.ek.chess_bot.engine;

import dk.ek.chess_bot.engine.pieces.Pawn;
import dk.ek.chess_bot.engine.pieces.Piece;
import javafx.concurrent.Task;

import java.sql.Time;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.concurrent.*;
import java.util.stream.IntStream;

import static dk.ek.chess_bot.engine.Pieces.*;

public class Bot {
    private static int[] currentBoard;
    private static boolean blackCastleKingSide;
    private static boolean blackCastleQueenSide;
    private static boolean whiteCastleKingSide;
    private static boolean whiteCastleQueenSide;

    private static boolean isWhiteToMove;
    private static int enPassantIndex;
    private static int totalMoves;
    private static int halfMoveClock;
    private static boolean botIsWhite;

    private static int bestMoveSoFar;

    private static int nodesSearched = 0;

    private static Instant endTime;
    static boolean ordering=false;

    static int[] pv = new int[64];
    static int[] currentPath = new int[64];

    static GameState getNextMove(GameState gameState, int givenDuration) {
        Instant start = Instant.now();
        // Target duration
        Duration duration = Duration.ofMillis(givenDuration);
        endTime = start.plus(duration);

        currentBoard = gameState.getCurrentBoard();
        blackCastleQueenSide = gameState.isBlackCastleQueenSide();
        blackCastleKingSide = gameState.isBlackCastleKingSide();
        whiteCastleKingSide = gameState.isWhiteCastleKingSide();
        whiteCastleQueenSide = gameState.isWhiteCastleQueenSide();
        isWhiteToMove = gameState.isWhiteToMove();
        enPassantIndex = gameState.getEnPassantIndex();
        totalMoves = gameState.getTotalMoves();
        halfMoveClock = gameState.getHalfMoveClock();

        botIsWhite = gameState.isWhiteToMove();

        int max_depth = 8; // Max depth, if program somehow reaches that before timer runs out
        GameState newGameState = new GameState();
        int bestMoveFoundInPrevious = 0;
        int bestMoveFound = 0;

        for (int depth = 1; depth <= max_depth; depth++) {
            System.out.println("Starting depth: " + depth);
            if (!Instant.now().isBefore(endTime)) {
                System.out.println("Out of time!");
                break;
            }

            //We reset the best move, to purge old info. I, Peter, removed it fully
            // bestMoveSoFar = 0;
            nodesSearched = 0; //Amount of nodes searched too

            // Get possible moves, pack into possibleMoves[0], meaning first array of arrays
            int[][] possibleMoves = new int[64][256];

            int counter = 0;
            for (int i = 0; i < 128; i++) {
                counter = Piece.getMoves(isWhiteToMove, i, currentBoard, possibleMoves[0], counter);
            }

            // Set initial alphaBeta values
            int alpha = -100000;
            int beta = 100_000;

            int nullReturn = -99_999;

            //Check PV first
            for (int i = 0; i < counter; i++) {
                if (possibleMoves[0][i] == pv[0]) {
                    int temp = possibleMoves[0][0];
                    possibleMoves[0][0] = possibleMoves[0][i];
                    possibleMoves[0][i] = temp;
                    break;
                }
            }

            // For every move, make the move, get score, unmake the move
            for (int i = 0; i < counter; i++) {
                makeMove(possibleMoves[0][i]);
                //
                int score = alphaBeta(possibleMoves, 0, depth, false, alpha, beta);
                unMakeMove(possibleMoves[0][i]);
                if (score == nullReturn) break; // ASK if better way to do

                if (score > alpha) {
                    alpha = score;
                    bestMoveFound = possibleMoves[0][i];
                    currentPath[0] = possibleMoves[0][i];
                    System.arraycopy(currentPath, 0, pv, 0, depth);
                }
            }

            if (Instant.now().isAfter(endTime)) {
                System.out.println("Out of time!");
                break;
            }
            bestMoveFoundInPrevious = bestMoveFound;

            System.out.println("Finished depth: " + depth + ", it took " + ChronoUnit.MILLIS.between(start, Instant.now()) + "ms");
            System.out.print("PV: ");
            for (int i = 0; i < depth; i++) {
                if (pv[i] == 0) break;
                System.out.print(convertIndexToCoordinates(IntegerEncoder.decodeFromSquare(pv[i]))
                        + "->" + convertIndexToCoordinates(IntegerEncoder.decodeToSquare(pv[i])) + " ");
            }
        }

        System.out.println("score before: " + Board.getScore(currentBoard, botIsWhite));
        makeMove(bestMoveFoundInPrevious);

        DecimalFormat numberFormatter = new DecimalFormat("#,###");
        String formattedNodesSearched = numberFormatter.format(nodesSearched).replace(",", ".");

        System.out.println("Found this as the best move, with a score of: " + Board.getScore(currentBoard, !botIsWhite) + " having searched: " + formattedNodesSearched + " nodes");
        Board.printBoard(currentBoard);

        newGameState.setWhiteToMove(isWhiteToMove);
        newGameState.setCurrentBoard(currentBoard);

        System.out.println(Translator.gameStateToFEN(newGameState));

        System.out.println("Time taken: " + ChronoUnit.MILLIS.between(start, Instant.now()) + "ms");

        return newGameState;
    }

    static void makeMove(int move) {
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

    static void unMakeMove(int move){
        //Unmaking a move is also quite complicated
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
        if (Instant.now().isAfter(endTime)) return -99_999; // ASK if better way to do

        // ASK check if checkmate
        // TODO temp simple solution
        boolean whiteKingContains = IntStream.of(currentBoard).anyMatch(piece -> piece == 6);
        boolean blackKingContains = IntStream.of(currentBoard).anyMatch(piece -> piece == 14);

        if(!blackKingContains){ //White has won
            if (botIsWhite) return 100000-depth; //If the bot is white (The maximizer) Return a HIGH value
            else return -100000 + depth; //If the bot is playing as black, and white is the minimizer, return a LOW value
        }
        if(!whiteKingContains){ //Black has won
            if (botIsWhite) return -100000 + depth; //If the bot is white (The maximizer) Return a LOW value
            else return 100000 - depth; //If the bot is playing as black, and white is the minimizer, return a HIGH value
        }

        depth = depth+1; //We start by incrementing the depth

        nodesSearched++;

        if(depth == targetDepth){
            return Board.getScore(currentBoard, botIsWhite);
        }


        int counter = 0;

        if(isMax){
            //Find all new moves on this depth
            for (int i = 0; i < 128; i++) {
                counter = Piece.getMoves(isWhiteToMove, i, currentBoard, moveList[depth], counter);
            }

            // Check PV first
            for (int i = 0; i < counter; i++) {
                if (moveList[depth][i] == pv[depth]) {
                    int temp = moveList[depth][0];
                    moveList[depth][0] = moveList[depth][i];
                    moveList[depth][i] = temp;
                    break;
                }
            }

            //For each move on this depth, find best move and check it
            for (int i = 0; i < counter; i++) {
                //TODO: implement simple selection sort
                int currentBestMove = moveList[depth][i];
                int currentMax = IntegerEncoder.decodeScore(moveList[depth][i]);

                //Make move, recursive alphaBeta lives here
                int move = moveList[depth][i];
                if (move != 0) {
                    makeMove(move);
                    int score = alphaBeta(moveList, depth, targetDepth, !isMax, alpha, beta);
                    unMakeMove(move);
                    if (score == -99_999) break; // ASK if better way to do

                    if (score > alpha) {
                        alpha = score;
                        currentPath[depth] = move;
                        System.arraycopy(currentPath, 0, pv, 0, targetDepth);
                    }
                    if (beta <= alpha) {
                        return alpha;
                    }

                    if (ordering){
                        for(int j = i+1; j < counter; j++){
                            int currentMove = moveList[depth][j];
                            int currentMoveScore = IntegerEncoder.decodeScore(currentMove);
                            if (currentMoveScore > currentMax) {
                            // If current move is new max, switch with old fake max
                            moveList[depth][i] = currentMove;
                            moveList[depth][j] = currentBestMove;
                            currentBestMove = currentMove;
                            // Also set new REAL max
                            currentMax = currentMoveScore;
                            }
                        }
                    }
                }
            }
            return alpha;
        }
        else{
            for (int i = 0; i < 128; i++) {
                counter = Piece.getMoves(isWhiteToMove, i, currentBoard, moveList[depth], counter);
            }

            // Check PV
            for (int i = 0; i < counter; i++) {
                if (moveList[depth][i] == pv[depth]) {
                    int temp = moveList[depth][0];
                    moveList[depth][0] = moveList[depth][i];
                    moveList[depth][i] = temp;
                    break;
                }
            }

            for (int i = 0; i < counter; i++) {
                //TODO: implement simple selection sort
                int currentBestMove = moveList[depth][i];
                int currentMax = IntegerEncoder.decodeScore(moveList[depth][i]);

                //Make move, recursive alphaBeta lives here
                int move = moveList[depth][i];
                if (move != 0) {
                    makeMove(move);
                    int score = alphaBeta(moveList, depth, targetDepth, !isMax, alpha, beta);
                    unMakeMove(move);
                    if (score == -99_999) break; // ASK if better way to do

                    if (score < beta) {
                        beta = score;
                        currentPath[depth] = move;
                        System.arraycopy(currentPath, 0, pv, 0, targetDepth);
                    }
                    if (beta <= alpha) return beta;
                }

                //Sort
                if (ordering){
                    for(int j = i+1; j < counter; j++){
                        int currentMove = moveList[depth][j];
                        int currentMoveScore = IntegerEncoder.decodeScore(currentMove);
                        if (currentMoveScore > currentMax) {
                            // If current move is new max, switch with old fake max
                            moveList[depth][i] = currentMove;
                            moveList[depth][j] = currentBestMove;
                            currentBestMove = currentMove;
                            // Also set new REAL max
                            currentMax = currentMoveScore;
                        }
                    }
                }
            }
            return beta;
        }
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


        getNextMove(gameState, 1000);


    }
}
package dk.ek.chess_bot.engine;

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

    private static boolean isWhiteToMove;
    private static int totalMoves;
    private static int halfMoveClock;
    private static boolean botIsWhite;

    private static int historyIndex; //To track things such as en passant indexes through time and castling rights through time, we need to maintain a history
    private static int[] enPassantHistory = new int[32];//32 would be the maximum depth
    private static boolean[][] castlingHistory = new boolean[32][4];//32 is the depth, and the four are the booleans we are tracking at each depth
    //WK, WQ, BK, BQ

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
        isWhiteToMove = gameState.isWhiteToMove();

        historyIndex = 0; //The history index tracks the state of En Passant and castling, to restore rights correctly
        enPassantHistory[0] = gameState.getEnPassantIndex(); //We need to track the history of en passant possibilities
        castlingHistory[0][0] = gameState.isWhiteCastleKingSide(); //We set the history at index 0 to be the state from the gamestate
        castlingHistory[0][1] = gameState.isWhiteCastleQueenSide();
        castlingHistory[0][2] = gameState.isBlackCastleKingSide();
        castlingHistory[0][3] = gameState.isBlackCastleKingSide();

        totalMoves = gameState.getTotalMoves();
        halfMoveClock = gameState.getHalfMoveClock();

        botIsWhite = gameState.isWhiteToMove();

        int max_depth = 25; // Max depth, if program somehow reaches that before timer runs out
        GameState newGameState = new GameState();
        int bestMoveFoundInPrevious = 0;
        int bestMoveFound = 0;

        //Step 1: Iterative depth. We start at depth 1, then we go deeper every time we complete a full search.
        for (int depth = 1; depth <= max_depth; depth++) {

            System.out.println("Starting depth: " + depth);
            if (!Instant.now().isBefore(endTime)) { //We must see if we are out of time
                System.out.println("Out of time!");
                break;
            }

            //Step 2: Purge info from previous searches
            historyIndex = 0;
            bestMoveSoFar = 0;
            nodesSearched = 0;

            // Get possible moves, pack into possibleMoves[0], meaning first array of arrays
            //Step 3: Instantiate array to store moves for the search
            int[][] possibleMoves = new int[64][256];
            int counter = 0; //Keep track of how many moves we find

            //Step 4: Find the possible moves from the initial position. ALPHA BETA ROOT
            for (int i = 0; i < 128; i++) {
                counter = Piece.getMoves(isWhiteToMove, i, currentBoard, possibleMoves[0],enPassantHistory[historyIndex], counter);
            } //Counter increases with each move we find

            // Set initial alphaBeta values
            int alpha = -100_000;
            int beta = 100_000;

            int nullReturn = -99_999;

            //Step 5: Make each available move TODO - implement game end check here as well
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
                int score = alphaBeta(possibleMoves, 0, depth, false, alpha, beta);
                unMakeMove(possibleMoves[0][i]);
                if (score == nullReturn) break; //If we run out of time in AlphaBeta, break the loop

                if (score > alpha) {
                    alpha = score;
                    bestMoveFound = possibleMoves[0][i];
                    currentPath[0] = possibleMoves[0][i];
                    System.arraycopy(currentPath, 0, pv, 0, depth);
                }
            }

            //Step 6: Make sure we still have time
            if (Instant.now().isAfter(endTime)) {
                System.out.println("Out of time!");
                break; //If we had run out of time previously, we want to break the loop before setting bad moves
            }

            //Step 7: If we completed the full search, update the previous best move.
            bestMoveFoundInPrevious = bestMoveFound; //If we reach this, we fully searched at the depth, meaning we update the previously best move
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
        newGameState.setEnPassantIndex(enPassantHistory[historyIndex]);

        System.out.println(Translator.gameStateToFEN(newGameState));

        System.out.println("Time taken: " + ChronoUnit.MILLIS.between(start, Instant.now()) + "ms");

        //Step 8: Return the updated gamestate
        return newGameState;
    }

    //Make move and unmake move need to be mirror images of each other, to ensure synchronicity.
    static void makeMove(int move) {
        //STEP 1: Get basic info from the move
        int pieceType = IntegerEncoder.decodeOwnPieceType(move);
        if(!isWhiteToMove) pieceType = 0b1000|pieceType;
        int fromSquare = IntegerEncoder.decodeFromSquare(move);
        int toSquare = IntegerEncoder.decodeToSquare(move);

        //STEP 2: We track our history, so we can know how to undo our moves correctly
        historyIndex++;

        //STEP 3: Normal move from one square to the other. Often this is all
        currentBoard[fromSquare] = 0;
        currentBoard[toSquare] = pieceType;

        //STEP 4: Capture
        //Now, if this was a capture, the capture is implicit. If there was an enemy in the "toSquare", it is now overwritten. This is not so for the unmake function.
        //No need for specific capture logic here

        //STEP 5 - SPECIFIC PAWN LOGIC
        //STEP 5.1: What if it is a promotion?
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

        //STEP 5.2: If we are moving a pawn two spaces, we need to set en passant square
        enPassantHistory[historyIndex] = -1; //By default we set it to -1, meaning no en passant
        if(pieceType == 1 || pieceType == 9){
            if (toSquare-fromSquare == 32){ //A white pawn moved two squares forward
                enPassantHistory[historyIndex]=toSquare-16;
            }
            if (toSquare-fromSquare == -32){
                enPassantHistory[historyIndex]=toSquare+16; //A black pawn moved two squares forward
            }
        }

        //STEP 5.3: What if we used enPassant?
        if(toSquare == enPassantHistory[historyIndex-1]){ //We check if our to location, matches the previous en passant index
            if(isWhiteToMove){ //When it is white, we know we have to look down
                currentBoard[toSquare-16] = 0; //We erase the pawn below
            }
            else{
                currentBoard[toSquare+16] = 0; //We erase the pawn above
            }
        }

        //STEP 6: SPECIFIC ROOK/KING LOGIC
        //STEP 6.1: If we moved the king or a rook, we need to note that we lost castling rights
        castlingHistory[historyIndex] = castlingHistory[historyIndex-1]; //First we assume that we carry our the previous state
        if (pieceType == 6){ //White moved their king, so castling is now impossible for white
            castlingHistory[historyIndex][0] = false;
            castlingHistory[historyIndex][1] = false;
        }
        if (pieceType == 4){
            if (fromSquare == 7){
                castlingHistory[historyIndex][0] = false; //White moved their tower from the original square.
            }
            if (fromSquare == 0){
                castlingHistory[historyIndex][1] = false; //White moved their tower from the original square.
            }
        }
        if (pieceType == 14){ //Black moved their king, so castling is now impossible for white
            castlingHistory[historyIndex][2] = false;
            castlingHistory[historyIndex][3] = false;
        }
        if (pieceType == 12){
            if (fromSquare == 119){
                castlingHistory[historyIndex][2] = false; //Black moved their tower from the original square.
            }
            if (fromSquare == 112){
                castlingHistory[historyIndex][3] = false; //Black moved their tower from the original square.
            }
        }//We can never regain castling rights after they are lost


        //STEP 6.2: what if we are making a castling move?
        if(IntegerEncoder.decodeIsCastle(move)){
            //Then we make the logic cool and good, for example we need to restore castling rights!
            //if(toSquare == X){
                //Also move tårn
        }

        //Finally we change the side to act
        isWhiteToMove = !isWhiteToMove;
    }

    static void unMakeMove(int move){ //Unmaking a move is essentially making a move in reverse
        //STEP 1: We change back to be able to know who "We" are.
        isWhiteToMove = !isWhiteToMove;

        //STEP 2: Get basic info on the move
        int pieceType = IntegerEncoder.decodeOwnPieceType(move);
        if(!isWhiteToMove) pieceType = 0b1000 |pieceType;
        int fromSquare = IntegerEncoder.decodeFromSquare(move);
        int toSquare = IntegerEncoder.decodeToSquare(move);

        //STEP 3: restore the from-square to the piece we moved
        currentBoard[fromSquare] = pieceType;

        //STEP 4: Handle if it was a capture - EXCEPT if the move was enpassant
        boolean isEnPassant = toSquare == enPassantHistory[historyIndex-1]; //If the sqaure we moved TO with this move was the previous enPassant index, that means this move was en passant

        if(IntegerEncoder.decodeIsCapture(move) && !isEnPassant){ //If the move was a capture, we need to put the captured piece back //EXCEPT IF IT WAS ENPASSANT
            int capPieceType = IntegerEncoder.decodeCapturedPieceType(move);
            if(isWhiteToMove){ //Flip to the black version if we are white (because white takes black pieces)
                capPieceType = 0b1000 | capPieceType; //We do bit math to change the number!
            }
            currentBoard[toSquare] = capPieceType; //We put the piece back
        }else{
            currentBoard[toSquare] = 0; //If it is enpassant or a simple move, the tosquare should be made empty
        }

        //If the move was enPassant we put things back
        if(isEnPassant){
            if (isWhiteToMove){
                currentBoard[toSquare-16] = 9;
            }
            else{
                currentBoard[toSquare+16] = 1;
            }
        }

        //STEP 5: If the move was a promotion, we make the pieces into pawns again
        if(IntegerEncoder.decodeIsPromo(move)){
            if(isWhiteToMove){
                currentBoard[fromSquare] = 1;
            }
            else{
                currentBoard[fromSquare] = 9;
            }
            //We just make sure we don't put a promoted queen into where there was a pawn
        }

        //If we moved a pawn two spaces, do we need to reset en passant square to -1? No, we need to set it to the previous value, which could have been something other than -1
        enPassantHistory[historyIndex] = -1; //We make sure to say there is no en passant at this history point, because we are moving back
        //Then we roll back the history
        historyIndex--;

        //If it is castling
        if(IntegerEncoder.decodeIsCastle(move)){

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
                counter = MoveController.getMoves(isWhiteToMove, i, currentBoard, moveList[depth], enPassantHistory[historyIndex], counter);
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
            //if isLost = true return appropriate value
            return alpha;
        }
        else{
            for (int i = 0; i < 128; i++) {
                counter = MoveController.getMoves(isWhiteToMove, i, currentBoard, moveList[depth],enPassantHistory[historyIndex], counter);
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

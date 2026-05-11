package dk.ek.chess_bot.engine;

import java.text.DecimalFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashMap;
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
    private static int[] enPassantHistory = new int[64];//32 would be the maximum depth
    private static boolean[][] castlingHistory = new boolean[64][4];//32 is the depth, and the four are the booleans we are tracking at each depth
    //WK, WQ, BK, BQ

    private static int bestMoveSoFar;

    private static int nodesSearched = 0;
    private static int timesCutoff = 0;
    private static long estimatedNotesCutoff = 0;

    private static Instant endTime;
    static boolean ordering = true;

    static int[] pv = new int[64];
    static int[] currentPath = new int[64];

    static GameState getNextMove(GameState gameState, int givenDuration) {
        Instant start = Instant.now();
        // Target duration
        Duration duration = Duration.ofMillis(givenDuration - 10);
        endTime = start.plus(duration);

        currentBoard = gameState.getCurrentBoard();
        isWhiteToMove = gameState.isWhiteToMove();

        historyIndex = 0; //The history index tracks the state of En Passant and castling, to restore rights correctly
        enPassantHistory[0] = gameState.getEnPassantIndex(); //We need to track the history of en passant possibilities
        castlingHistory[0][0] = gameState.isWhiteCastleKingSide(); //We set the history at index 0 to be the state from the gamestate
        castlingHistory[0][1] = gameState.isWhiteCastleQueenSide();
        castlingHistory[0][2] = gameState.isBlackCastleKingSide();
        castlingHistory[0][3] = gameState.isBlackCastleQueenSide();

        totalMoves = gameState.getTotalMoves();
        halfMoveClock = gameState.getHalfMoveClock();

        botIsWhite = gameState.isWhiteToMove();

        int max_depth = 20; // Max depth, if program somehow reaches that before timer runs out
        GameState newGameState = new GameState();
        int bestMoveFoundInPrevious = 0;
        int bestMoveFound = 0;
        int finalDepth = 0;
        long depthTime = 0;

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
            estimatedNotesCutoff = 0;
            timesCutoff = 0;

            //Step 3: Instantiate array to store moves for the search
            // Get possible moves, pack into possibleMoves[0], meaning first array of arrays
            int[][] possibleMoves = new int[64][256];
            int counter = 0; //Keep track of how many moves we find

            //Step 4: Find the possible moves from the initial position. ALPHA BETA ROOT
            counter = MoveController.getCastling(isWhiteToMove, currentBoard, possibleMoves[0],castlingHistory[0][0],castlingHistory[0][1],castlingHistory[0][2],castlingHistory[0][3],counter);
            for (int i = 0; i < 128; i++) {
                counter = MoveController.getMoves(isWhiteToMove, i, currentBoard, possibleMoves[0],enPassantHistory[historyIndex], counter);
            }


            int alpha = -100_000;
            int beta = 100_000;

            int nullReturn = -99_999;

            //Step 5: Make each available move
            //Check PV first
            for (int i = 0; i < counter; i++) {
                if (possibleMoves[0][i] == pv[0]) {
                    int temp = possibleMoves[0][0];
                    possibleMoves[0][0] = possibleMoves[0][i];
                    possibleMoves[0][i] = temp;
                    break;
                }
            }
            boolean validMoves = false;

            // For every move, make the move, get score, unmake the move -see alphaBeta for in depth comments
            for (int i = 0; i < counter; i++) {
                makeMove(possibleMoves[0][i]);
                int score = -1_000_000;
                boolean kingChecked = ThreatDetector.isKingInCheck(currentBoard, !isWhiteToMove);
                if (!kingChecked){
                    validMoves = true;
                    score = alphaBeta(possibleMoves, 0, depth, false, alpha, beta);
                    unMakeMove(possibleMoves[0][i]);
                    if (score == nullReturn) break; //If we run out of time in AlphaBeta, break the loop

                    if (score > alpha) {
                        alpha = score;
                        bestMoveFound = possibleMoves[0][i];
                        currentPath[0] = possibleMoves[0][i];
                        System.arraycopy(currentPath, 0, pv, 0, depth);
                    }
                }else{
                    unMakeMove(possibleMoves[0][i]);
                }

            }

            if(!validMoves){
                System.out.println("There were no valid moves for this position");
                gameState.setLoss(true); //If we had no moves, the game is lost for us
            }

            //Step 6: Make sure we still have time
            if (Instant.now().isAfter(endTime)) {
                System.out.println("Out of time!");
                break; //If we had run out of time previously, we want to break the loop before setting bad moves
            }

            //Step 7: If we completed the full search, update the previous best move.
            bestMoveFoundInPrevious = bestMoveFound; //If we reach this, we fully searched at the depth, meaning we update the previously best move
            finalDepth = depth;
            depthTime = ChronoUnit.MILLIS.between(start, Instant.now());
            System.out.println("Finished depth: " + depth + ", it took " + depthTime + "ms");
            System.out.print("PV: ");
            for (int i = 0; i < depth; i++) {
                if (pv[i] == 0) break;
                System.out.print(convertIndexToCoordinates(IntegerEncoder.decodeFromSquare(pv[i]))
                        + "->" + convertIndexToCoordinates(IntegerEncoder.decodeToSquare(pv[i])) + " ");
            }
        }

        System.out.println();
        System.out.println("--------------------MOVE REPORT-----------------------");
        System.out.println("Final depth reached: " + finalDepth);
        System.out.println("score before: " + Board.getScore(currentBoard));

        if (!gameState.isLoss()) makeMove(bestMoveFoundInPrevious);
        newGameState.setMoveFrom(IntegerEncoder.decodeFromSquare(bestMoveFoundInPrevious));
        newGameState.setMoveTo(IntegerEncoder.decodeToSquare(bestMoveFoundInPrevious));

        System.out.println(" Score after: " + Board.getScore(currentBoard));
        DecimalFormat numberFormatter = new DecimalFormat("#,###");
        String formattedNodesSearched = numberFormatter.format(nodesSearched).replace(",", ".");

        System.out.println("searched " + formattedNodesSearched + " nodes");

        long timeTaken = ChronoUnit.MILLIS.between(start, Instant.now());
        String formattedEstimatedNodesSearched = numberFormatter.format(estimatedNotesCutoff).replace(",", ".");
        System.out.println("Cut off: " + timesCutoff + " times. Having pruned an estimated: " + formattedEstimatedNodesSearched + " nodes");
        double ebf = Math.pow(nodesSearched, 1.0 / finalDepth);
        System.out.println("Effective branching factor: " + ebf);
        double nps = nodesSearched / (depthTime / 1000.0);
        String formattedNps = numberFormatter.format(nps).replace(",", ".");
        System.out.println("Nodes per second: " + formattedNps);

        newGameState.setWhiteToMove(isWhiteToMove);
        newGameState.setCurrentBoard(currentBoard);
        newGameState.setEnPassantIndex(enPassantHistory[historyIndex]);
        newGameState.setWhiteCastleKingSide(castlingHistory[historyIndex][0]);
        newGameState.setWhiteCastleQueenSide(castlingHistory[historyIndex][1]);
        newGameState.setBlackCastleKingSide(castlingHistory[historyIndex][2]);
        newGameState.setBlackCastleKingSide(castlingHistory[historyIndex][3]);
        if (isCheckMate()) newGameState.setWon(true);
        if (gameState.isLoss()) newGameState.setLoss(true);
        System.out.println("FEN STRING: " + Translator.gameStateToFEN(newGameState));

        System.out.println("Time taken: " + timeTaken + "ms");
        System.out.println("--------------------END REPORT-----------------------");

        //Step 8: Return the updated gamestate
        return newGameState;
    }

    static boolean isCheckMate(){
        int[] buffer = new int[256];
        int counter = 0;
        counter = MoveController.getCastling(isWhiteToMove, currentBoard, buffer, castlingHistory[historyIndex][0],castlingHistory[historyIndex][1],castlingHistory[historyIndex][2],castlingHistory[historyIndex][3],counter);
        for (int i = 0; i <128; i++) {
            counter = MoveController.getMoves(isWhiteToMove, i, currentBoard, buffer, enPassantHistory[historyIndex], counter);
        }
        boolean validMoves = false;
        int validMovesAvailable = 0;
        for (int i = 0; i < counter; i++) {
            makeMove(buffer[i]);
            if (!ThreatDetector.isKingInCheck(currentBoard,!isWhiteToMove)){
                validMovesAvailable++;
                validMoves = true;
            }
            unMakeMove(buffer[i]);
        }
        System.out.println("The opposition has " + validMovesAvailable + " valid moves available");
        return !validMoves;
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
        if (IntegerEncoder.decodeIsPromo(move)){
            System.out.println("This move was a promotion making: " + pieceType);
        }
        currentBoard[toSquare] = pieceType;

        //STEP 4: Capture
        //Now, if this was a capture, the capture is implicit. If there was an enemy in the "toSquare", it is now overwritten. This is not so for the unmake function.
        //No need for specific capture logic here


        //STEP 5.2: If we are moving a pawn two spaces, we need to set en passant square
        enPassantHistory[historyIndex] = -1; //By default, we set it to -1, meaning no en passant
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
        System.arraycopy(castlingHistory[historyIndex-1], 0, castlingHistory[historyIndex],0,4); //First we assume that we carry our the previous state
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
            if (IntegerEncoder.decodeToSquare(move) == 6){ //White is castling king side
                currentBoard[6] = 6;
                currentBoard[5] = 4;
                currentBoard[4] = 0;
                currentBoard[7] = 0;
            }
            if (IntegerEncoder.decodeToSquare(move) == 2){ //White is castling queen side
                currentBoard[2] = 6;
                currentBoard[3] = 4;
                currentBoard[4] = 0;
                currentBoard[0] = 0;
            }
            if (IntegerEncoder.decodeToSquare(move) == 118){ //black is castling king side
                currentBoard[118] = 14;
                currentBoard[117] = 12;
                currentBoard[116] = 0;
                currentBoard[119] = 0;
            }
            if (IntegerEncoder.decodeToSquare(move) == 114){ //black is castling queen side
                currentBoard[114] = 14;
                currentBoard[115] = 12;
                currentBoard[116] = 0;
                currentBoard[112] = 0;
            }
        }

        //Finally we change the side to act
        isWhiteToMove = !isWhiteToMove;
    }

    static void unMakeMove(int move){
        //Unmaking a move is essentially making a move in reverse
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
        if(IntegerEncoder.decodeIsCastle(move)) {
            if (IntegerEncoder.decodeToSquare(move) == 6) { //White is castling king side
                currentBoard[6] = 0;
                currentBoard[5] = 0;
                currentBoard[4] = 6;
                currentBoard[7] = 4;
            }
            if (IntegerEncoder.decodeToSquare(move) == 2) { //White is castling queen side
                currentBoard[2] = 0;
                currentBoard[3] = 0;
                currentBoard[4] = 6;
                currentBoard[0] = 4;
            }
            if (IntegerEncoder.decodeToSquare(move) == 118) { //black is castling king side
                currentBoard[118] = 0;
                currentBoard[117] = 0;
                currentBoard[116] = 14;
                currentBoard[119] = 12;
            }
            if (IntegerEncoder.decodeToSquare(move) == 114) { //black is castling queen side
                currentBoard[114] = 0;
                currentBoard[115] = 0;
                currentBoard[116] = 14;
                currentBoard[112] = 12;
            }
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

        depth = depth+1; //We start by incrementing the depth

        nodesSearched++;

        if(depth == targetDepth){
            int score;
            if (botIsWhite) score =  Board.getScore(currentBoard);
            else score = -Board.getScore(currentBoard);
            return score;
        }


        int counter = 0;

        //Get all the moves
        //Find all new moves on this depth
        counter = MoveController.getCastling(isWhiteToMove, currentBoard, moveList[depth],castlingHistory[depth][0],castlingHistory[depth][1],castlingHistory[depth][2],castlingHistory[depth][3],counter);
        for (int i = 0; i < 128; i++) {
            counter = MoveController.getMoves(isWhiteToMove, i, currentBoard, moveList[depth],enPassantHistory[historyIndex], counter);
        }
        //Check PV first
        //We place the PV move at the front of the list, to check that first. PV move usually gives the biggest cutoff!
        for (int i = 0; i < counter; i++) {
            if (moveList[depth][i] == pv[depth]) {
                int temp = moveList[depth][0];
                moveList[depth][0] = moveList[depth][i];
                moveList[depth][i] = temp;
                break;
            }
        }
        boolean validMoves = false; //We assume we don't have any legal moves
        if(isMax){
            //For each move on this depth, find best move and check it
            for (int i = 0; i < counter; i++) {
                if (ordering && i != 0) { //When it is not the first move in the list(The PV move), we want to go through the list and find the one with highest score, which hopefully creates cutoff!

                    int bestIndex = i;
                    for (int j = i + 1; j < counter; j++) { //A selection sort algorithm loops through the list once, and finds the highest score
                        if (IntegerEncoder.decodeScore(moveList[depth][j]) > IntegerEncoder.decodeScore(moveList[depth][i])) {
                            bestIndex = j;
                        }
                    }
                    //we swap the current move with the one we found
                    int temp = moveList[depth][i];
                    moveList[depth][i] = moveList[depth][bestIndex];
                    moveList[depth][bestIndex] = temp;

                }

                //Make move, recursive alphaBeta lives here
                int move = moveList[depth][i];
                makeMove(move);
                //We find out if the move we just made left our king in check. makeMove(move) flipped the side to move, so we check the opposite (our) side
                boolean kingChecked = ThreatDetector.isKingInCheck(currentBoard, !isWhiteToMove);
                int score = -10000000;
                if(!kingChecked){ //If the king is not in check, we can make this move!
                    validMoves = true; //This means we found a valid move, and the game is not over
                    score = alphaBeta(moveList, depth, targetDepth, !isMax, alpha, beta); //We keep going down the game tree
                    unMakeMove(move); //We reset the changes
                    if (score == -99_999) break; //If we got -99_999 back that means we ran out of time further down
                        if (score > alpha) { //We found a better move than previously
                            alpha = score;
                            currentPath[depth] = move; //We update our path
                            System.arraycopy(currentPath, 0, pv, 0, targetDepth); //NEEDED??
                        }
                        if (beta <= alpha) { //This move is so good, minimizer won't let us do it. THAT MEANS CUTOFF!
                            timesCutoff++;
                            long nodes = 0;
                            int pow = targetDepth-depth;
                            nodes = (long)Math.pow(counter,pow);
                            estimatedNotesCutoff += nodes; //General stuff to estimate how much work we "save" compared to regular minimax
                            return alpha;
                        }
                    }else{
                        unMakeMove(move);
                }
            }
            if(!validMoves){ //If we don't have any valid moves, the game is over!
                if(ThreatDetector.isKingInCheck(currentBoard, isWhiteToMove)){ //If our king is in check, that means checkmate!
                    return -100000+depth;
                }else{ //If our king is not in check, but we have no valid moves, that means a stalemate. That is a 0
                    return 0;
                }
            }
            return alpha; //Finally we return alpha, if we did not return anything before
        }

        else{ //See isMax for general approach
            for (int i = 0; i < counter; i++) {
                if (ordering && i != 0) {
                    int bestIndex = i;

                    for (int j = i + 1; j < counter; j++) {
                        if (IntegerEncoder.decodeScore(moveList[depth][j]) > IntegerEncoder.decodeScore(moveList[depth][i])) {
                            bestIndex = j;
                        }
                    }
                    int temp = moveList[depth][i];
                    moveList[depth][i] = moveList[depth][bestIndex];
                    moveList[depth][bestIndex] = temp;

                }
                //Make move, recursive alphaBeta lives here
                int move = moveList[depth][i];

                makeMove(move);
                boolean kingChecked = ThreatDetector.isKingInCheck(currentBoard, !isWhiteToMove);
                int score = 1000000;
                if(!kingChecked){
                    validMoves = true;
                    score = alphaBeta(moveList, depth, targetDepth, !isMax, alpha, beta);
                    unMakeMove(move);
                    if (score == -99_999) break; // ASK if better way to do
                        if (score < beta) {
                            beta = score;
                            currentPath[depth] = move;
                            System.arraycopy(currentPath, 0, pv, 0, targetDepth);
                        }
                        if (beta <= alpha) {
                            timesCutoff++;
                            int nodes = 0;
                            int pow = targetDepth-depth;
                            nodes = (int)Math.pow(counter,pow);
                            estimatedNotesCutoff += nodes;
                            return beta;
                        }
                    }else{
                        unMakeMove(move);
                    }
            }
            if (!validMoves){
                if (ThreatDetector.isKingInCheck(currentBoard, isWhiteToMove)){
                    return 100000-depth;
                }else{
                    return 0;
                }
            }
            return beta;
        }
    }

    static long perftRecurse(long movesFound, int depth, int desiredDepth, int[][] moveList){
        depth++;

        if(depth == desiredDepth){
            movesFound++;
            return movesFound;
        }

        int counter = 0;
        counter = MoveController.getCastling(isWhiteToMove, currentBoard, moveList[depth], castlingHistory[historyIndex][0], castlingHistory[historyIndex][1], castlingHistory[historyIndex][2], castlingHistory[historyIndex][3], counter);
        for (int i = 0; i < 128; i++) {
            counter = MoveController.getMoves(isWhiteToMove, i, currentBoard, moveList[depth], enPassantHistory[historyIndex], counter);
        }
        for (int i = 0; i < counter; i++) {
            makeMove(moveList[depth][i]);
            boolean kingChecked = ThreatDetector.isKingInCheck(currentBoard, !isWhiteToMove);
            if (!kingChecked){
                movesFound = perftRecurse(movesFound, depth, desiredDepth, moveList);
                unMakeMove(moveList[depth][i]);
            }else{
                unMakeMove(moveList[depth][i]);
            }
        }
        return movesFound;
    }

    static long perftTest(GameState gameState, int desiredDepth) {
        currentBoard = gameState.getCurrentBoard();
        isWhiteToMove = gameState.isWhiteToMove();

        historyIndex = 0; //The history index tracks the state of En Passant and castling, to restore rights correctly
        enPassantHistory[0] = gameState.getEnPassantIndex(); //We need to track the history of en passant possibilities
        castlingHistory[0][0] = gameState.isWhiteCastleKingSide(); //We set the history at index 0 to be the state from the gamestate
        castlingHistory[0][1] = gameState.isWhiteCastleQueenSide();
        castlingHistory[0][2] = gameState.isBlackCastleKingSide();
        castlingHistory[0][3] = gameState.isBlackCastleQueenSide();

        totalMoves = gameState.getTotalMoves();
        halfMoveClock = gameState.getHalfMoveClock();

        botIsWhite = gameState.isWhiteToMove();

        int max_depth = desiredDepth; // Max depth, if program somehow reaches that before timer runs out
        long movesFound = 0;
        long[] movesFoundAtEach = new long[32];
        HashMap<Integer, Long> perftResults = new HashMap<>();
        perftResults.put(1, 20L);
        perftResults.put(2, 400L);
        perftResults.put(3, 8902L);
        perftResults.put(4, 197_281L);
        perftResults.put(5, 4_865_609L);
        perftResults.put(6, 119_060_324L);
        perftResults.put(7, 3_195_901_860L);
        perftResults.put(8, 84_998_978_956L);
        perftResults.put(9, 2_439_530_234_167L);
        perftResults.put(10, 69_352_859_712_417L);

        //Step 1: Iterative depth. We start at depth 1, then we go deeper every time we complete a full search.
        for (int depth = 1; depth <= max_depth; depth++) {
            System.out.println("Starting depth: " + depth);

            //Step 2: Purge info from previous searches
            historyIndex = 0;
            bestMoveSoFar = 0;
            nodesSearched = 0;
            estimatedNotesCutoff = 0;
            timesCutoff = 0;
            movesFound = 0;


            //Step 3: Instantiate array to store moves for the search
            // Get possible moves, pack into possibleMoves[0], meaning first array of arrays
            int[][] possibleMoves = new int[64][256];
            int counter = 0; //Keep track of how many moves we find

            //Step 4: Find the possible moves from the initial position. ALPHA BETA ROOT
            counter = MoveController.getCastling(isWhiteToMove, currentBoard, possibleMoves[0],castlingHistory[0][0],castlingHistory[0][1],castlingHistory[0][2],castlingHistory[0][3],counter);
            for (int i = 0; i < 128; i++) {
                counter = MoveController.getMoves(isWhiteToMove, i, currentBoard, possibleMoves[0],enPassantHistory[historyIndex], counter);
            }

            // For every move, make the move, get score, unmake the move -see alphaBeta for in depth comments
            for (int i = 0; i < counter; i++) {
                makeMove(possibleMoves[0][i]);
                boolean kingChecked = ThreatDetector.isKingInCheck(currentBoard, !isWhiteToMove);
                if (!kingChecked){

                    movesFound = perftRecurse(movesFound, 0, depth, possibleMoves);

                    unMakeMove(possibleMoves[0][i]);

                }else{
                    unMakeMove(possibleMoves[0][i]);
                }
            }

            movesFoundAtEach[depth] = movesFound;
            System.out.println(movesFound);
        }

        System.out.println();
        System.out.println("--------------------PERFT REPORT-----------------------");
        System.out.println("Final depth reached: " + desiredDepth);
        DecimalFormat numberFormatter = new DecimalFormat("#,###");
        for (int i = 1; i < desiredDepth; i++) {
            Long movesFoundAtDepth = movesFoundAtEach[i];
            Long resultAtDepth = perftResults.get(i);
            Long missingMoves = resultAtDepth-movesFoundAtDepth;

            String formattedPerft = numberFormatter.format(movesFoundAtDepth).replace(",", ".");
            String formattedResult = numberFormatter.format(resultAtDepth).replace(",", ".");
            System.out.println("found " + formattedPerft + "/" + formattedResult + " moves at depth: " + i + " MISSING: " + missingMoves);
        }
        System.out.println("--------------------END REPORT-----------------------");

        return movesFound;
    }

    public static void main(String[] args) {

        GameState gameState = new GameState();

        perftTest(gameState, 7);

    }
}
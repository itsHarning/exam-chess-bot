package dk.ek.chess_bot.engine;

import dk.ek.chess_bot.engine.pieces.Pawn;

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
        if(isWhiteToMove){identity = 0;}else{identity = 8;}

        //We reset the best move, to purge old info
        bestMoveSoFar = 0;

        int[][] possibleMoves = new int[64][256];
        getAllMoves(possibleMoves, 0);

        Board.printBoard(currentBoard);
        makeMove(possibleMoves[0][5]);
        Board.printBoard(currentBoard);
        unMakeMove(possibleMoves[0][5]);
        Board.printBoard(currentBoard);

        return convertIndexToCoordinates(IntegerEncoder.decodeFromSquare(possibleMoves[0][0]))
                + convertIndexToCoordinates(IntegerEncoder.decodeToSquare(possibleMoves[0][0]));
    }

    static void makeMove(int move){
        //Many cases are appropriate to consider here. First we get the basic info
        int pieceType = IntegerEncoder.decodeOwnPieceType(move);
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
        //First get basic info on the move
        int pieceType = IntegerEncoder.decodeOwnPieceType(move);
        int fromSquare = IntegerEncoder.decodeFromSquare(move);
        int toSquare = IntegerEncoder.decodeToSquare(move);

        currentBoard[fromSquare] = pieceType;
        currentBoard[toSquare] = 0; //If it was a simple move we can leave it at that
        if(IntegerEncoder.decodeIsCapture(move)){ //If the move was a capture however, we need to put the capture piece back
            int capPieceType = IntegerEncoder.decodeCapturedPieceType(move);
            if(!isWhiteToMove){
                capPieceType = 0b1000 | pieceType; //We do bit math to change the number!
            }
            currentBoard[toSquare] = capPieceType; //We put the piece back
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

        //Change who is moving
        isWhiteToMove = !isWhiteToMove;
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
        if(depth == targetDepth){
            return getScore(currentBoard);
        }

        int counter = 0;

        if(isMax){
            for (int i = 0; i < 128; i++) {
                if(currentBoard[i] != 0){
                    if(isWhiteToMove && (currentBoard[i]>0 && currentBoard[i]<7)){
                        //Max is white, and we have found one of our pieces
                        //Get possible moves from piece class and add to list
                        //TODO: pull new piece logic into main and pull it into branch
                    }
                    else{
                        //It is not white to move, and the piece we have found is not theirs, which means it is ours
                        //Get possible moves from piece class and add to list
                    }
                }
            }
            for (int i = 0; i < moveList[depth].length; i++) {
                //TODO: implement simple selection sort

                int move = moveList[depth][i];
                if (move != 0) {
                    makeMove(move);
                    int score = alphaBeta(moveList, depth + 1, targetDepth, !isMax, alpha, beta);
                    unMakeMove(move);

                    alpha = Math.max(alpha, score);
                    if (beta <= alpha) {
                        return alpha;
                    }
                }
            }
            return alpha;
        }
        if(!isMax){
            for (int i = 0; i < 128; i++) {
                //getMovesFromPieceClass
            }
        }
        return 1;
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

        getNextMove(gameState);

    }
}

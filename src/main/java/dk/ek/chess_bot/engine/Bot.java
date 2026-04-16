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

        System.out.println(IntegerEncoder.decodeFromSquare(possibleMoves[0][0]));
        System.out.println(IntegerEncoder.decodeToSquare(possibleMoves[0][0]));

        return convertIndexToCoordinates(IntegerEncoder.decodeFromSquare(possibleMoves[0][0]))
                + convertIndexToCoordinates(IntegerEncoder.decodeToSquare(possibleMoves[0][0]));
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

    static void getAllMoves(int[][] buffer, int depth){

        int counter = 0; //Counting variable lets us access the index on the array we have reached
        counter = Pawn.getMoves(currentBoard, buffer[depth], counter, isWhiteToMove);


    }

    public static void main(String[] args) {
        System.out.println(convertIndexToCoordinates(16));

        GameState gameState = new GameState();

        String move = getNextMove(gameState);
        System.out.println(move);
    }
}

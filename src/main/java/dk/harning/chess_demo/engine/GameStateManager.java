package dk.harning.chess_demo.engine;

public class GameStateManager {
    static int[] board;
    static boolean aiIsActive;
    static int moveCount;
    static int enPassantSquareIndex;


    public static void main(String[] args) {
        //setup
        //Get the player to either select a clean board or input a FEN string
        board = Board.getSetup(); //or something like that

        //figure out if the AI or the player is going first
        //GUI.getStartPlayer(); Or something like that

        //while(game is not over){
            //if(aiIsActive){
            //  makeMove(ChessBot.getMove(board, enPassantSquare, castlingOptions, moveCount))
            //}
            //else{
            //  makeMove(GUI.getPlayerMove());
            //}
            //aiIsActive = !aiIsActive;
            //GUI.updateBoard(board);
        //}
    }

    static void makeMove(int move){
        //int fromSquareIndex = IntEncoder.decodeFromSquare(move);
        //int toSquareIndex = IntEncoder.decodeToSquareIndex(move);

        //int piece = board[fromSquareIndex];
        //board[fromSquareIndex] = 0;
        //board[toSquareIndex] = piece;
    }

}

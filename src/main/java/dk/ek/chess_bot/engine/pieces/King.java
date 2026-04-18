package dk.ek.chess_bot.engine.pieces;

import dk.ek.chess_bot.engine.Board;
import dk.ek.chess_bot.engine.IntegerEncoder;

public class King {

    private static final int[] KING_MOVES = {
            16, -16, 1, -1, 15, 17, -15, -17
    };

    public static int getMoves(int placement, int[] board, int[] buffer, int counter, boolean isWhite){
        int pieceType = 6;
        if (!isWhite) {pieceType = 14;}

        //Check for every move if target square is either empty or enemy piece
            for(int move : KING_MOVES) {
                //Set target to avoid doing i+move repeatedly
                int target = placement+move;
                //Off board?
                if(!isOffBoard(target))
                //(board[target] == 0 || board[target] > 8)){
                        //Encode the move
                        buffer[counter] = IntegerEncoder.encodeMove(placement, target,pieceType,false,0);
                        counter++; //Counting up to target the next empty index
                    }
            return counter;
    }



    static boolean isOffBoard(int squareIndex){
        return (squareIndex & 0x88) != 0;
    }

    public static void main(String[] args) {
        int [] board = new Board().board;

        int[] buffer = new int[100];

        int amount = getMoves(6, board, buffer, 0, true);
        //System.out.println(amount);

        for(int i: buffer){
            System.out.println(IntegerEncoder.decodeFromSquare(i) + " -> " + IntegerEncoder.decodeToSquare(i));
        }
        System.out.println(IntegerEncoder.decodeToSquare(buffer[1]));
    }
}

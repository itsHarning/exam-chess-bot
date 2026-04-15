package dk.harning.chess_demo.engine.pieces;

import dk.harning.chess_demo.engine.Board;
import dk.harning.chess_demo.engine.IntegerEncoder;

public class King {

    private static final int[] KING_MOVES = {
            16, -16, 1, -1, 15, 17, -15, -17
    };

    public static int getMoves(int[] board, int[] buffer, int counter, boolean isWhite){

        int piecNr = 6;
        if(!isWhite) piecNr = 14;

        for (int i = 0; i < 128; i++) {
            if(board[i] == piecNr){ //If the current square contains a WHITE king
                //Check for every move if target square is either empty or enemy piece
                for(int move : KING_MOVES) {
                    //Set target to avoid doing i+move repeatedly
                    int target = i+move;
                    //Still only works for white
                    if(!isOffBoard(target) && (board[target] == 0 || board[target] < 8)){
                        //Encode the move
                        buffer[counter] = IntegerEncoder.encodeMove(i, target,1,false,0);
                        counter++; //Counting up to target the next empty index
                    }
                }
            }
        }
        return counter;
    }

    static boolean isOffBoard(int squareIndex){
        return (squareIndex & 0x88) != 0;
    }

    public static void main(String[] args) {
        int [] board = new Board().board;

        int[] buffer = new int[100];

        int amount = getMoves(board, buffer, 0, true);
        //System.out.println(amount);

        for(int i: buffer){
            System.out.println(IntegerEncoder.decodeFromSquare(i) + " -> " + IntegerEncoder.decodeToSquare(i));
        }
        System.out.println(IntegerEncoder.decodeToSquare(buffer[1]));
    }
}

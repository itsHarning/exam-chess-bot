package dk.ek.chess_bot.engine.pieces;

import dk.ek.chess_bot.engine.Board;
import dk.ek.chess_bot.engine.IntegerEncoder;

public class   Pawn {
    public static int getMoves(int[] board, int[] buffer, int counter, boolean isWhite){

        int piecNr = 1;
        if(!isWhite) piecNr = 9;

        int forward = 16;
        if(!isWhite) forward = -16;

        for (int i = 0; i < 128; i++) {
            if(board[i] == piecNr){ //If the current square contains a WHITE pawn
                //First we check if there is something in front of them and if the space is on the board
                if(!isOffBoard(i+forward) && board[i+forward] == 0){
                    //If we reach this point, the square in front is empty, and is on the board. Good!
                    //Encode the move
                    buffer[counter] = IntegerEncoder.encodeMove(i, i+forward,1,false,0);
                    counter++; //Counting up to target the next empty index

                    if ((isWhite && i < 25) || (!isWhite && i>95)){ //If they are in their starting position, we can do more!
                        if(!isOffBoard(i+forward*2) && board[i+(forward*2)] == 0){
                            //We can move to this space, because there is no one in front, and no one two spaces ahead
                            //Encode the move
                            buffer[counter] = IntegerEncoder.encodeMove(i, i+forward*2,1, false, 0);
                            counter++;
                        }
                    }
                }

                //Now let us see if we can take some pieces! We need to make it check for white or black - currently only checks for black
                int attackIndex1 = i+forward-1;
                if(!isOffBoard(attackIndex1)){
                    if((isWhite && board[attackIndex1] > 8)||(!isWhite && board[attackIndex1] < 8 && board[attackIndex1] != 0)){
                        buffer[counter] = IntegerEncoder.encodeMove(i, attackIndex1,1, true, board[attackIndex1]&0x7);
                        counter++;
                    }
                }

                int attackIndex2 = i+forward+1;
                if(!isOffBoard(attackIndex2)){
                    if((isWhite && board[attackIndex2] > 8)||(!isWhite && board[attackIndex2] < 8 && board[attackIndex2] != 0)){
                        buffer[counter] = IntegerEncoder.encodeMove(i, attackIndex2,1, true, board[attackIndex1]&0x7);
                        counter++;
                    }
                }

            }
        }
        return counter;
    }

    static boolean isOffBoard(int squareIndex){
        return (squareIndex & 0x88) != 0;
    }
    //TODO For testing, delete later
    public static void main(String[] args) {
        int [] board = new Board().board;

        int[] buffer = new int[100];

        board[35] = 9;

        int amount = getMoves(board, buffer, 0, true);
        //System.out.println(amount);


        for(int i: buffer){
            if(i != 0){
                System.out.println(IntegerEncoder.decodeFromSquare(i) + " -> " + IntegerEncoder.decodeToSquare(i));
            }

        }
        System.out.println(IntegerEncoder.decodeToSquare(buffer[1]));
    }
}

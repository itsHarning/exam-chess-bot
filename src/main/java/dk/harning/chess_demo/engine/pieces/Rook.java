package dk.harning.chess_demo.engine.pieces;

import dk.harning.chess_demo.engine.IntegerEncoder;

public class Rook {

    private static final int[] ROOK_DIRECTION = {
            16, 1, -16, -1
    };

    public static int getMoves(int[] board, int[] buffer, int counter, boolean isWhite){

        int piecNr = 3;
        if(!isWhite) piecNr = 11;

        for (int i = 0; i < 128; i++) {
            if(board[i] == piecNr){ //If the current square contains a WHITE bishop
                //Check for every move if target square is either empty or enemy piece
                for(int direction : ROOK_DIRECTION) {
                    counter = checkDirection(board,i,direction,buffer, counter);
                }
            }
        }
        return counter;
    }

    //This currently only checks out for white
    public static int checkDirection(int[] board, int placement, int direction, int[] buffer, int counter){
        int target = placement + direction;
        if (isOffBoard(target)) {
            return counter;
        }

        return switch (board[target]) {
            case 0 -> {
                counter = encodeMove(placement, target, buffer, counter);
                yield checkDirection(board, target, direction, buffer, counter);
            }
            case 9, 10, 11, 12, 13, 14 -> encodeMove(placement, target, buffer, counter);
            default -> counter;
        };
    }

    public static int encodeMove(int moveFrom, int moveTo, int[] buffer, int counter){
        buffer[counter] = IntegerEncoder.encodeMove(moveFrom, moveTo,1,false,0);
        return counter+1; //Counting up to target the next empty index
    }

    static boolean isOffBoard(int squareIndex){
        return (squareIndex & 0x88) != 0;
    }
}

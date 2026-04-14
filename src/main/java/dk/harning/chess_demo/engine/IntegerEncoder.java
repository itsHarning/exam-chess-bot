package dk.harning.chess_demo.engine;

public class IntegerEncoder {

    public static int encodeMove(int fromSquare, int toSquare, int pieceType, boolean capture, int capturePieceType){
        int encoded = 0; //7: fromSquare - 7: toSquare - 3: pieceType - 1: isCapture - 3: capturedType
        encoded = (encoded | fromSquare);
        encoded = (toSquare<<7) | encoded;
        encoded = (pieceType<<14) | encoded;

        int flags = 0;
        if (capture){
            flags = flags | 0b1000;
        }
        encoded = (flags<<17) |encoded;

        encoded = (capturePieceType<<21)|encoded;

        //Here we need to add the score for the move to the end. Implementation pending


        return encoded;
    }

    public static int decodeToSquare(int encodedInt) {
        return (encodedInt>>7)&0x7F;
    }
    public static int decodeFromSquare(int encodedInt) {
        return encodedInt&0x7F;
    }
    public static boolean decodeIsCapture(int encodedInt){
        if(((encodedInt>>21)&0x1) != 0){
            return true;
        }
        else{return false;}
    }
    public static int decodeOwnPieceType(int encodedInt){
        return (encodedInt>>14)&0x7;
    }
    public static int decodeCapturedPieceType(int encodedInt){
        return (encodedInt>>22)&0x7;
    }

    public static void main(String[] args) {
        int a = encodeMove(7, 23, 1, true, 5);

        System.out.println(a);
        System.out.println(Integer.toBinaryString(a));

        System.out.println(decodeToSquare(a));
        System.out.println(decodeFromSquare(a));
        System.out.println(decodeIsCapture(a));

        System.out.println(decodeOwnPieceType(a));
        System.out.println(decodeCapturedPieceType(a));
    }
}

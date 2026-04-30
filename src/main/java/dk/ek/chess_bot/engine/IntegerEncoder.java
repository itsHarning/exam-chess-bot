package dk.ek.chess_bot.engine;

public class IntegerEncoder {

    public static int encodeMove(int fromSquare, int toSquare, int pieceType, boolean capture, int capturePieceType, boolean isPromo, boolean isCastle){
        int encoded = 0; //7: fromSquare - 7: toSquare - 3: pieceType - 1: isCapture - 1: isPromo - 1: isCastle - 1: FLAG - 3: capturedType - 8: score
        encoded = (encoded | fromSquare);
        encoded = (toSquare<<7) | encoded;
        encoded = (pieceType<<14) | encoded;

        int flags = 0;
        if (capture){
            flags = flags | 0b0001;
        }
        if (isPromo){
            flags = flags | 0b0010;
        }
        if (isCastle){
            flags = flags | 0b0100;
        }
        encoded = (flags<<17) |encoded;

        encoded = (capturePieceType<<21)|encoded;

        //Here we need to add the score for the move to the end. Implementation pending. 10 is placeholder
        int score = getScore(10);
        encoded = (score << 24)|encoded;

        return encoded;
    }


    static int getScore(int move){
        /*
        // centerDist should not live here - need to hear group
        final int[] centerDist = new int[] {
                15,15, 15,15, 15,15, 15,15, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000,
                15,10, 10,10, 10,10, 10,15, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000,
                15,10,  5, 5,  5, 5, 10,15, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000,
                15,10,  5, 0,  0, 5, 10,15, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000,
                15,10,  5, 0,  0, 5, 10,15, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000,
                15,10,  5, 5,  5, 5, 10,15, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000,
                15,10, 10,10, 10,10, 10,15, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000,
                15,15, 15,15, 15,15, 15,15, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000,
        };
        int score = 0;

        // Center control: fromSquare - toSquare is added to score
        score += centerDist[decodeFromSquare(move)] - centerDist[decodeToSquare(move)];

        // Oh boy we like castling
        if(decodeIsCastle(move)) score += 50;

        // Oh boy we love promotions
        if(decodeIsPromo(move)) score += 200;

        // Attempt at LVA - HVT
        if(decodeIsCapture(move)) {
            // Get value of own piece
            int ownValue = pieceValue(decodeOwnPieceType(move));
            // Get value of enemy piece
            int enemyValue = pieceValue(decodeCapturedPieceType(move));

            // Flat 100 for capture, extra based on enemy value and own value
            score += 100 + (10 * enemyValue - ownValue);
        } */

        return 10;
    }

    public static int decodeFromSquare(int encodedInt) {
        return encodedInt&0x7F;
    }
    public static int decodeToSquare(int encodedInt) {
        return (encodedInt>>7)&0x7F;
    }
    public static int decodeOwnPieceType(int encodedInt){
        return (encodedInt>>14)&0x7;
    }
    public static boolean decodeIsCapture(int encodedInt){
        if(((encodedInt>>17)&0x1) != 0){
            return true;
        }
        else{return false;}
    }
    public static boolean decodeIsPromo(int encodedInt){
        if(((encodedInt>>18)&0x1) != 0){
            return true;
        }
        else{return false;}
    }
    public static boolean decodeIsCastle(int encodedInt){
        if(((encodedInt>>19)&0x1) != 0){
            return true;
        }
        else{return false;}
    }

    public static int decodeCapturedPieceType(int encodedInt){
        return (encodedInt>>21)&0x7;
    }

    public static int decodeScore(int encodedInt){
        return (encodedInt>>24)&0xFF;
    }





    public static void main(String[] args) {
        int a = encodeMove(7, 23, 1, true, 5, false, false);

        System.out.println(a);
        System.out.println(Integer.toBinaryString(a));


        System.out.println(decodeFromSquare(a));
        System.out.println(decodeToSquare(a));
        System.out.println(decodeOwnPieceType(a));
        System.out.println(decodeIsCapture(a));
        System.out.println(decodeIsPromo(a));
        System.out.println(decodeIsCastle(a));
        System.out.println(decodeCapturedPieceType(a));
        System.out.println(decodeScore(a));
    }
}

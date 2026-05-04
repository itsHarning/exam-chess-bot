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
        encodeScore(encoded);

        return encoded;
    }


    static int encodeScore(int move) {
        int score = calcScoreOfMove(move);
        return (score << 24)|move;
    }

    static final int[] centerDist = new int[] {
            3,3, 3,3, 3,3, 3,3,     0, 0, 0, 0, 0, 0, 0, 0,
            3,2, 2,2, 2,2, 2,3,     0, 0, 0, 0, 0, 0, 0, 0,
            3,2, 1,1, 1,1, 2,3,     0, 0, 0, 0, 0, 0, 0, 0,
            3,2, 1,0, 0,1, 2,3,     0, 0, 0, 0, 0, 0, 0, 0,
            3,2, 1,0, 0,1, 2,3,     0, 0, 0, 0, 0, 0, 0, 0,
            3,2, 1,1, 1,1, 2,3,     0, 0, 0, 0, 0, 0, 0, 0,
            3,2, 2,2, 2,2, 2,3,     0, 0, 0, 0, 0, 0, 0, 0,
            3,3, 3,3, 3,3, 3,3,     0, 0, 0, 0, 0, 0, 0, 0,
    };

    static int calcScoreOfMove(int move){
        int score = 0;

        // Center control: fromSquare - toSquare is added to score
        if(centerDist[decodeFromSquare(move)] > centerDist[decodeToSquare(move)]) {
            score += centerDist[decodeFromSquare(move)] - centerDist[decodeToSquare(move)];
        } // 3

        // Oh boy we like castling
        if(decodeIsCastle(move)) score += 10;

        // Oh boy we love promotions
        if(decodeIsPromo(move)) score += 50; //53

        // Attempt at LVA - HVT
        if(decodeIsCapture(move)) {
            // Get value of own piece
            int ownValue = pieceValue(decodeOwnPieceType(move));
            // Get value of enemy piece
            int enemyValue = pieceValue(decodeCapturedPieceType(move));

            score += 30 + (10 * enemyValue - ownValue); // Pawn cap king = 179; MAX 179 + 53 = 232; King cap Pawn =
        }
        return score;
    }

    static int pieceValue(int piece){
        return switch (piece) {
            case 1, 9 -> 1;
            case 2, 10 -> 3;
            case 3, 11 -> 3;
            case 4, 12 -> 5;
            case 5, 13 -> 9;
            case 6, 14 -> 15;
            default -> 0;
        };
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

package dk.ek.chess_bot.engine;

import static dk.ek.chess_bot.engine.IntegerEncoder.*;

public class MoveOrdering {

//    static final int[] centerDist = new int[] {
//            3,3, 3,3, 3,3, 3,3,     0, 0, 0, 0, 0, 0, 0, 0,
//            3,2, 2,2, 2,2, 2,3,     0, 0, 0, 0, 0, 0, 0, 0,
//            3,2, 1,1, 1,1, 2,3,     0, 0, 0, 0, 0, 0, 0, 0,
//            3,2, 1,0, 0,1, 2,3,     0, 0, 0, 0, 0, 0, 0, 0,
//            3,2, 1,0, 0,1, 2,3,     0, 0, 0, 0, 0, 0, 0, 0,
//            3,2, 1,1, 1,1, 2,3,     0, 0, 0, 0, 0, 0, 0, 0,
//            3,2, 2,2, 2,2, 2,3,     0, 0, 0, 0, 0, 0, 0, 0,
//            3,3, 3,3, 3,3, 3,3,     0, 0, 0, 0, 0, 0, 0, 0,
//    };
//
//    static int encodeScore(int move) {
//        int score = calcScoreOfMove(move);
//        return (score << 24)|move;
//    }
//
//
//    static int calcScoreOfMove(int move){
//        int score = 0;
//
//        // Center control: fromSquare - toSquare is added to score
//        if(centerDist[decodeFromSquare(move)] > centerDist[decodeToSquare(move)]) {
//            score += centerDist[decodeFromSquare(move)] - centerDist[decodeToSquare(move)];
//        } // 3
//
//        // Oh boy we like castling
//        if(decodeIsCastle(move)) score += 10;
//
//        // Oh boy we love promotions
//        if(decodeIsPromo(move)) score += 50; //53
//
//        // Attempt at LVA - HVT
//        if(decodeIsCapture(move)) {
//            // Get value of own piece
//            int ownValue = pieceValue(decodeOwnPieceType(move));
//            // Get value of enemy piece
//            int enemyValue = pieceValue(decodeCapturedPieceType(move));
//
//            score += 30 + (10 * enemyValue - ownValue); // Pawn cap king = 179; MAX 179 + 53 = 232; King cap Pawn =
//        }
//        return score;
//    }
//
//    static int pieceValue(int piece){
//        return switch (piece) {
//            case 1, 9 -> 1;
//            case 2, 10 -> 3;
//            case 3, 11 -> 3;
//            case 4, 12 -> 5;
//            case 5, 13 -> 9;
//            case 6, 14 -> 15;
//            default -> 0;
//        };
//    }
}

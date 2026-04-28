package dk.ek.chess_bot.engine;

import static dk.ek.chess_bot.engine.IntegerEncoder.*;

public class MoveOrdering {

    static final int[] centerDist = new int[] {
            15,15, 15,15, 15,15, 15,15, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000,
            15,10, 10,10, 10,10, 10,15, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000,
            15,10,  5, 5,  5, 5, 10,15, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000,
            15,10,  5, 0,  0, 5, 10,15, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000,
            15,10,  5, 0,  0, 5, 10,15, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000,
            15,10,  5, 5,  5, 5, 10,15, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000,
            15,10, 10,10, 10,10, 10,15, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000,
            15,15, 15,15, 15,15, 15,15, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000,
    };

    static int encodeScore(int move) {
        int score = calcScoreOfMove(move);
        return (score << 24)|move;
    }

    static int calcScoreOfMove(int move){
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
        }
        return score;
    }

    static int pieceValue(int piece){
        return switch (piece) {
            case 1 -> 1;
            case 2 -> 3;
            case 3 -> 3;
            case 4 -> 5;
            case 5 -> 9;
            case 6 -> 20;
            default -> 0;
        };
    }
}

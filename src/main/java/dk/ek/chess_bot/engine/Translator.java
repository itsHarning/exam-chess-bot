package dk.ek.chess_bot.engine;

import static dk.ek.chess_bot.engine.Pieces.*;

public class Translator {

    static String gameStateToFEN(GameState gameState){
        StringBuilder fen = new StringBuilder();
        int[] board = gameState.getCurrentBoard();

        // 1. Board part (FEN Goes from rank 8 to rank 1)
        for (int rank = 7; rank >= 0; rank--) {
            int emptyCount = 0;

            for (int file = 0; file < 8; file++) {
                int index = rank * 16 + file;
                int piece = board[index];

                if (piece == EMPTY) {
                    emptyCount++;
                } else {
                    if (emptyCount > 0) {
                        fen.append(emptyCount);
                        emptyCount = 0;
                    }
                    fen.append(pieceToFenChar(piece));
                }
            }

            if (emptyCount > 0) {
                fen.append(emptyCount);
            }

            if (rank > 0) {
                fen.append("/");
            }
        }

        // 2. Side to move
        fen.append(" ");
        fen.append(gameState.isWhiteToMove() ? "w" : "b");

        // 3. Castling
        fen.append(" ");
        String castling = buildCastlingString(gameState);
        fen.append(castling.isEmpty() ? "-" : castling);

        // 4. En Passant
        fen.append(" ");
        fen.append(enPassantToFEN(gameState.getEnPassantIndex()));

        // 5. Halfmove clock (moves since capture or pawn move)
        fen.append(" ");
        fen.append(gameState.getHalfMoveClock());

        // 6. Fullmove
        fen.append(" ");
        fen.append(gameState.getTotalMoves());

        return fen.toString();
    }

    static GameState gameStateFromFEN(String fen){
        String[] parts = fen.trim().split("\\s+");

        if (parts.length != 6) {
            throw new IllegalArgumentException("Invalid FEN: must contain 6 parts");
        }

        GameState gameState = new GameState();

        // 1. Board
        int[] board = parseBoardPart(parts[0]);
        gameState.setCurrentBoard(board);

        // 2. Side to move
        gameState.setWhiteToMove(parts[1].equals("w"));

        // 3. Castling rights
        String castling = parts[2];
        gameState.setWhiteCastleKingSide(castling.contains("K"));
        gameState.setWhiteCastleQueenSide(castling.contains("Q"));
        gameState.setBlackCastleKingSide(castling.contains("k"));
        gameState.setBlackCastleQueenSide(castling.contains("q"));

        // 4. En Passant
        gameState.setEnPassantIndex(fenToEnPassantIndex(parts[3]));

        // 5. Halfmove clock
        gameState.setHalfMoveClock(Integer.parseInt(parts[4]));

        // 6. fullmove
        gameState.setTotalMoves(Integer.parseInt(parts[5]));

        return gameState;
    }

    // Bruges til at return board fra FEN til GameState.
    private static int[] parseBoardPart(String boardPart) {
        int[] board = new int[128];

        // fill whole board with EMPTY first
        for (int i = 0; i < 128; i++) {
            board[i] = EMPTY;
        }

        String[] ranks = boardPart.split("/");

        if (ranks.length != 8) {
            throw new IllegalArgumentException("Invalid FEN board: must contain 8 ranks");
        }

        // FEN rank[0] = rank 8, men vores array har rank 7 = rank 8.
        for (int fenRank = 0; fenRank < 8; fenRank++) {
            int boardRank = 7 - fenRank;
            int file = 0;

            for (char c : ranks[fenRank].toCharArray()) {
                if (Character.isDigit(c)) {
                    int emptySquares = c - '0';
                    file += emptySquares;
                } else {
                    int index = boardRank * 16 + file;
                    board[index] = fenCharToPiece(c);
                    file++;
                }
            }

            if (file != 8) {
                throw new IllegalArgumentException("Invalid FEN rank: " + ranks[fenRank]);
            }
        }
        return board;
    }

    private static String buildCastlingString(GameState gameState) {
        StringBuilder castling = new StringBuilder();

        if (gameState.isWhiteCastleKingSide()) {
            castling.append("K");
        }
        if (gameState.isWhiteCastleQueenSide()) {
            castling.append("Q");
        }
        if (gameState.isBlackCastleKingSide()) {
            castling.append("k");
        }
        if (gameState.isBlackCastleQueenSide()) {
            castling.append("q");
        }

        return castling.toString();
    }

    //EnPassant metodes
    private static String enPassantToFEN(int index) {
        if(index < 0) {
            return "-";
        }
        int file = index % 16;
        int rank = index / 16;

        char fileChar = (char) ('a' + file); //ASCII
        char rankChar = (char) ('1' + rank); //ASCII

        return "" + fileChar + rankChar;
    }

    private static int fenToEnPassantIndex(String enPassant) {
        if (enPassant.equals("-")) {
            return -1;
        }

        char fileChar = enPassant.charAt(0);
        char rankChar = enPassant.charAt(1);

        int file = fileChar - 'a';
        int rank = rankChar - '1';

        return rank * 16 + file;
    }

    // GameState Pieces to FEN Pieces
    private static char pieceToFenChar(int piece){
        return switch (piece) {
            case WPAWN -> 'P';
            case WKNIGHT -> 'N';
            case WBISHOP -> 'B';
            case WROOK -> 'R';
            case WQUEEN -> 'Q';
            case WKING -> 'K';
            case BPAWN -> 'p';
            case BKNIGHT -> 'n';
            case BBISHOP -> 'b';
            case BROOK -> 'r';
            case BQUEEN -> 'q';
            case BKING -> 'k';
            default -> throw new IllegalArgumentException("Unknown Piece value: " + piece);
        };
    }

    private static char fenCharToPiece(char c) {
        return switch (c) {
            case 'P' -> WPAWN;
            case 'N' -> WKNIGHT;
            case 'B' -> WBISHOP;
            case 'R' -> WROOK;
            case 'Q' -> WQUEEN;
            case 'K' -> WKING;
            case 'p' -> BPAWN;
            case 'n' -> BKNIGHT;
            case 'b' -> BBISHOP;
            case 'r' -> BROOK;
            case 'q' -> BQUEEN;
            case 'k' -> BKING;
            default -> throw new IllegalArgumentException("Unknown FEN piece value: " + c);
        };
    }

    private static void printBoard(int[] board) {
        for (int rank = 7; rank >= 0; rank--) {
            for (int file = 0; file < 8; file++) {
                int index = rank * 16 + file;
                int piece = board[index];

                if (piece == EMPTY) {
                    System.out.print(". ");
                } else {
                    System.out.print(pieceToFenChar(piece) + " ");
                }
            }
            System.out.println();
        }
    }

    // Testing
    public static void main(String[] args) {

        // 1. test (FAILED)
        String result = enPassantToFEN(0);
        //Problemet er at hvis GameState har passant sat til 0 som ingen, men plads 0 er jo A1 på brættet.
        System.out.println(result);

        // 2. test (FAILED) begge a1 og - giver 0
        /*int result2 = fenToEnPassantIndex("a1");
        System.out.println(result2);*/

        /*
        String startFen = "rnbqkbnr/ppp1pppp/8/3p4/8/8/PPPPPPPP/RNBQKBNR w KQkq a1 0 1";

        System.out.println("Original FEN:");
        System.out.println(startFen);

        GameState gameState = gameStateFromFEN(startFen);

        System.out.println("\nBoard from FEN:");
        printBoard(gameState.getCurrentBoard());


        String convertedFen = gameStateToFEN(gameState);

        System.out.println("\nConverted back to FEN:");
        System.out.println(convertedFen);
        */
    }

    public static void main(String[] args) {

    }
}

package dk.ek.chess_bot.engine;

import static dk.ek.chess_bot.engine.Pieces.*;

public class Fen {

    public int[] board;

    public int[] parseFenString(String fenString) {
        this.board = boardEmpty;
        String[] split = fenString.split("\\s+");
        String fen = split[0];

        int count = 0;
        for (int rank = 7; rank >= 0; rank--) {
            for (int file = 0; file <16; file++) {
                int square = rank * 16 + file;
                if ((square & 0x88) == 0) {
                    char fenChar = fen.charAt(count);

                    if (Character.isLetter(fenChar)) {
                        this.board[square] = pieces.indexOf(fenChar);
                        count++;
                    }

                    if (Character.isDigit(fenChar)) {
                        int offset = fenChar - '0';

                        if ((square & 0x88) == 0) {
                            file--;
                        }

                        file += offset;
                        count++;
                    }

                    if (fenChar== '/') {
                        file--;
                        count++;
                    }
                }
            }
        }

        return this.board;
    }
}
